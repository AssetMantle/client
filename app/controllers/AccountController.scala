package controllers

import constants.AppConfig._
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.master.{Email, Mobile, Profile}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import transactions.blockchain.{AddKey, ChangePassword, ForgotPassword}
import utilities.KeyStore
import views.companion.master._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountController @Inject()(
                                   utilitiesNotification: utilities.Notification,
                                   withLoginActionAsync: WithLoginActionAsync,
                                   withUserLoginAction: WithUserLoginAction,
                                   withUsernameToken: WithUsernameToken,
                                   blockchainAccounts: blockchain.Accounts,
                                   blockchainIdentityProvisions: blockchain.IdentityProvisions,
                                   masterAccounts: master.Accounts,
                                   masterProfiles: master.Profiles,
                                   masterTransactionEmailOTP: masterTransaction.EmailOTPs,
                                   masterTransactionSessionTokens: masterTransaction.SessionTokens,
                                   masterTransactionPushNotificationTokens: masterTransaction.PushNotificationTokens,
                                   transactionAddKey: AddKey,
                                   transactionForgotPassword: ForgotPassword,
                                   transactionChangePassword: ChangePassword,
                                   messagesControllerComponents: MessagesControllerComponents,
                                   masterEmails: master.Emails,
                                   masterMobiles: master.Mobiles,
                                   masterIdentifications: master.Identifications,
                                   masterAccountKYCs: master.AccountKYCs,
                                   withoutLoginAction: WithoutLoginAction,
                                   withoutLoginActionAsync: WithoutLoginActionAsync,
                                   keyStore: KeyStore
                                 )
                                 (implicit
                                  executionContext: ExecutionContext,
                                  configuration: Configuration,
                                  wsClient: WSClient,
                                 ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  def signUpForm(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.account.signUp())
  }

  def signUp: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      SignUp.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.account.signUp(formWithErrors)))
        },
        signUpData => {
          val address = Future(utilities.Bech32.convertAccountPublicKeyToAccountAddress(pubkey = signUpData.publicKey))

          def getBCAccount(address: String) = blockchainAccounts.Service.get(address)

          def validateSignature(address: String) = Future(utilities.Blockchain.verifySecp256k1Signature(publicKey = signUpData.publicKey, data = utilities.Keplr.newArbitraryData(data = signUpData.username, signer = address).getSHA256, signature = signUpData.signature))

          def updateAccountsAndGetResult(validSignature: Boolean, optionalBCAccount: Option[blockchain.Account], address: String) = if (validSignature) {
            def getIdentityIDList(address: String) = blockchainIdentityProvisions.Service.getAllIDsByProvisioned(address)

            val logInState = optionalBCAccount.fold {
              val upsertBCAccount = blockchainAccounts.Utility.onKeplrSignUp(address = address, username = signUpData.username, publicKey = signUpData.publicKey)

              def addToMaster() = masterAccounts.Service.upsertOnKeplrSignUp(username = signUpData.username, language = request.lang)

              for {
                _ <- upsertBCAccount
                userType <- addToMaster()
                identityIDs <- getIdentityIDList(address)
              } yield LoginState(username = signUpData.username, userType = userType, address = address, identityID = identityIDs.headOption.getOrElse(""))
            } { bcAccount =>
              if (bcAccount.username == signUpData.username) {
                val masterAccount = masterAccounts.Service.tryGet(signUpData.username)
                for {
                  masterAccount <- masterAccount
                  identityIDs <- getIdentityIDList(address)
                } yield LoginState(username = signUpData.username, userType = masterAccount.userType, address = address, identityID = identityIDs.headOption.getOrElse(""))
              } else throw new BaseException(constants.Response.INCORRECT_USERNAME_OR_WALLET_ADDRESS)
            }

            val profile = masterProfiles.Service.get(signUpData.username)
            val email = masterEmails.Service.get(signUpData.username)
            val mobile = masterMobiles.Service.get(signUpData.username)

            def sendNotification = {
              val pushNotificationTokenUpdate = masterTransactionPushNotificationTokens.Service.update(id = signUpData.username, token = signUpData.pushNotificationToken)
              for {
                _ <- pushNotificationTokenUpdate
                _ <- utilitiesNotification.send(signUpData.username, constants.Notification.LOGIN, signUpData.username)()
              } yield ()
            }

            def getResult(warnings: Seq[constants.Response.Warning], profile: Option[Profile], email: Option[Email], mobile: Option[Mobile])(implicit loginState: LoginState): Future[Result] = withUsernameToken.Ok(views.html.assetMantle.profile(profile = profile, email = email.fold("")(_.emailAddress), mobile = mobile.fold("")(_.mobileNumber), warnings = warnings))

            (for {
              logInState <- logInState
              profile <- profile
              email <- email
              mobile <- mobile
              _ <- sendNotification
              result <- getResult(utilities.Contact.getWarnings(mobile, email), profile, email, mobile)(logInState)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          } else Future(InternalServerError(views.html.index(failures = Seq(constants.Response.INVALID_SIGNATURE))))

          (for {
            address <- address
            bcAccount <- getBCAccount(address)
            validSignature <- validateSignature(address)
            result <- updateAccountsAndGetResult(validSignature, bcAccount, address)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def signInForm(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.account.signIn())
  }

  def signIn: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      SignIn.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.account.signIn(formWithErrors)))
        },
        signInData => {
          val address = Future(utilities.Bech32.convertAccountPublicKeyToAccountAddress(pubkey = signInData.publicKey))
          val bcAccount = blockchainAccounts.Service.tryGetByUsername(signInData.username)
          val masterAccount = masterAccounts.Service.tryGet(signInData.username)

          def validateSignature(address: String) = Future(utilities.Blockchain.verifySecp256k1Signature(publicKey = signInData.publicKey, data = utilities.Keplr.newArbitraryData(data = signInData.username, signer = address).getSHA256, signature = signInData.signature))

          def getIdentityIDList(address: String) = blockchainIdentityProvisions.Service.getAllIDsByProvisioned(address)

          def result(validSignature: Boolean, address: String, masterAccount: master.Account, identityIDs: Seq[String]) = if (validSignature) {
            val logInState = LoginState(username = masterAccount.id, userType = masterAccount.userType, address = address, identityID = identityIDs.headOption.getOrElse(""))
            val profile = masterProfiles.Service.get(signInData.username)
            val email = masterEmails.Service.get(masterAccount.id)
            val mobile = masterMobiles.Service.get(masterAccount.id)

            def sendNotification = {
              val pushNotificationTokenUpdate = masterTransactionPushNotificationTokens.Service.update(id = signInData.username, token = signInData.pushNotificationToken)
              for {
                _ <- pushNotificationTokenUpdate
                _ <- utilitiesNotification.send(signInData.username, constants.Notification.LOGIN, signInData.username)()
              } yield ()
            }

            def getResult(warnings: Seq[constants.Response.Warning], profile: Option[Profile], email: Option[Email], mobile: Option[Mobile])(implicit loginState: LoginState): Future[Result] = withUsernameToken.Ok(views.html.assetMantle.profile(profile = profile, email = email.fold("")(_.emailAddress), mobile = mobile.fold("")(_.mobileNumber), warnings = warnings))

            (for {
              profile <- profile
              email <- email
              mobile <- mobile
              _ <- sendNotification
              result <- getResult(utilities.Contact.getWarnings(mobile, email), profile, email, mobile)(logInState)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          } else Future(InternalServerError(views.html.index(failures = Seq(constants.Response.INVALID_SIGNATURE))))

          (for {
            address <- address
            _ <- bcAccount
            masterAccount <- masterAccount
            validSignature <- validateSignature(address)
            identityIDs <- getIdentityIDList(address)
            result <- result(validSignature, address, masterAccount, identityIDs)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def signOutForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.account.signOut())
  }

  def signOut: Action[AnyContent] = withLoginActionAsync { loginState =>
    implicit request =>
      SignOut.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.account.signOut(formWithErrors)))
        },
        signOutData => {
          val pushNotificationTokenDelete = if (!signOutData.receiveNotifications) masterTransactionPushNotificationTokens.Service.delete(loginState.username) else Future(0)
          val deleteSessionToken = masterTransactionSessionTokens.Service.delete(loginState.username)

          (for {
            _ <- pushNotificationTokenDelete
            _ <- deleteSessionToken
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.LOG_OUT, loginState.username)()
          } yield Ok(views.html.index(successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def updateProfileForm(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.account.updateProfile())
  }

  def updateProfile(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      UpdateProfile.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.account.updateProfile(formWithErrors)))
        },
        updateProfileData => {
          if (loginState.identityID != "") {
            val optionalProfile = masterProfiles.Service.get(loginState.username)

            def updateProfileNameDescription(optionalProfile: Option[Profile]) = optionalProfile.fold {
              masterProfiles.Service.insertOrUpdate(Profile(identityID = loginState.identityID, name = updateProfileData.name, description = updateProfileData.description, socialProfiles = Seq()))
            } { profile =>
              masterProfiles.Service.insertOrUpdate(profile.copy(name = updateProfileData.name, description = updateProfileData.description))
            }

            def updatedProfile = masterProfiles.Service.get(loginState.username)

            val email = masterEmails.Service.get(loginState.username)
            val mobile = masterMobiles.Service.get(loginState.username)

            (for {
              optionalProfile <- optionalProfile
              _ <- updateProfileNameDescription(optionalProfile)
              updatedProfile <- updatedProfile
              email <- email
              mobile <- mobile
            } yield Ok(views.html.assetMantle.profile(profile = updatedProfile, email = email.fold("")(_.emailAddress), mobile = mobile.fold("")(_.mobileNumber), successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          } else Future(InternalServerError(views.html.assetMantle.account(failures = Seq(new BaseException(constants.Response.SESSION_IDENTITY_ID_NOT_FOUND).failure))))
        }
      )
  }

  def updateSocialProfileForm(platform: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.account.updateSocialProfile(platform = platform))
  }

  def updateSocialProfile(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      UpdateSocialProfile.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.account.updateSocialProfile(formWithErrors, platform = formWithErrors.get.platform)))
        },
        updateSocialProfileData => {
          val optionalProfile = masterProfiles.Service.get(loginState.username)

          def updateProfileNameDescription(optionalProfile: Option[Profile]) = optionalProfile.fold {
            masterProfiles.Service.insertOrUpdate(Profile(identityID = loginState.identityID, name = "", description = "", socialProfiles = Seq(updateSocialProfileData.toSocialProfile)))
          } { profile =>
            masterProfiles.Service.insertOrUpdate(profile.copy(socialProfiles = profile.socialProfiles :+ updateSocialProfileData.toSocialProfile))
          }

          def profile = masterProfiles.Service.get(loginState.username)

          val email = masterEmails.Service.get(loginState.username)
          val mobile = masterMobiles.Service.get(loginState.username)

          (for {
            optionalProfile <- optionalProfile
            _ <- updateProfileNameDescription(optionalProfile)
            profile <- profile
            email <- email
            mobile <- mobile
          } yield Ok(views.html.assetMantle.profile(profile = profile, email = email.fold("")(_.emailAddress), mobile = mobile.fold("")(_.mobileNumber), successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def checkUsernameAvailable(username: String): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val checkUsernameAvailable = masterAccounts.Service.checkUsernameAvailable(username)
      for {
        checkUsernameAvailable <- checkUsernameAvailable
      } yield if (checkUsernameAvailable) Ok else NoContent
  }
}
