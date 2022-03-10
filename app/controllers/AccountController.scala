package controllers

import constants.AppConfig._
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.master.{Email, Mobile, Profile}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.{I18nSupport, MessagesProvider}
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

  private def sendNotificationsAndGetResult(loginState: LoginState, pushNotificationToken: String)(implicit requestHeader: RequestHeader, messagesProvider: MessagesProvider, flash: Flash): Future[Result] = {
    val profile = masterProfiles.Service.get(loginState.username)
    val email = masterEmails.Service.get(loginState.username)
    val mobile = masterMobiles.Service.get(loginState.username)

    def sendNotification = {
      val pushNotificationTokenUpdate = masterTransactionPushNotificationTokens.Service.update(id = loginState.username, token = pushNotificationToken)
      for {
        _ <- pushNotificationTokenUpdate
        _ <- utilitiesNotification.send(loginState.username, constants.Notification.LOGIN, loginState.username)()
      } yield ()
    }

    def getResult(warnings: Seq[constants.Response.Warning], profile: Option[Profile], email: Option[Email], mobile: Option[Mobile])(implicit loginState: LoginState, requestHeader: RequestHeader, messagesProvider: MessagesProvider, flash: Flash): Future[Result] =
      withUsernameToken.Ok(views.html.assetMantle.profile(profile = profile, email = email.fold("")(_.emailAddress), mobile = mobile.fold("")(_.mobileNumber), warnings = warnings)(requestHeader, messagesProvider, flash, otherApps, loginState))

    for {
      profile <- profile
      email <- email
      mobile <- mobile
      _ <- sendNotification
      result <- getResult(warnings = utilities.Contact.getWarnings(mobile, email), profile = profile, email = email, mobile = mobile)(loginState, requestHeader, messagesProvider, flash)
    } yield result
  }

  def signUpForm(): Action[AnyContent] = withoutLoginAction { implicit x: Option[LoginState] =>
    implicit request =>
      Ok(views.html.component.master.account.signUp())
  }

  def signUp: Action[AnyContent] = withoutLoginActionAsync { implicit x: Option[LoginState] =>
    implicit request =>
      SignUp.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.account.signUp(formWithErrors)))
        },
        signUpData => {
          val address = Future(utilities.Bech32.convertAccountPublicKeyToAccountAddress(pubkey = signUpData.publicKey))
          val identityID = utilities.Blockchain.getNubIdentity(signUpData.username).id.asString

          def verifyIdentity(address: String) = blockchainIdentityProvisions.Service.checkExists(id = identityID, address = address)

          def validateSignature(address: String) = Future(utilities.Blockchain.verifySecp256k1Signature(publicKey = signUpData.publicKey, data = utilities.Keplr.newArbitraryData(data = signUpData.username, signer = address).getSHA256, signature = signUpData.signature))

          def updateAccountsAndGetResult(validSignature: Boolean, address: String, identityVerified: Boolean) = if (validSignature && identityVerified) {
            val userType = masterAccounts.Service.addOnKeplrSignUp(username = signUpData.username, language = request.lang)

            (for {
              userType <- userType
              result <- sendNotificationsAndGetResult(loginState = LoginState(username = signUpData.username, userType = userType, address = address, identityID = identityID), pushNotificationToken = signUpData.pushNotificationToken)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          } else Future(InternalServerError(views.html.index(failures = Seq(constants.Response.INVALID_SIGNATURE_OR_USERNAME))))

          (for {
            address <- address
            identityVerified <- verifyIdentity(address)
            validSignature <- validateSignature(address)
            result <- updateAccountsAndGetResult(validSignature, address, identityVerified)
          } yield result).recover {
            case baseException: BaseException => BadGateway(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def signInForm(): Action[AnyContent] = withoutLoginAction { implicit x: Option[LoginState] =>
    implicit request =>
      Ok(views.html.component.master.account.signIn())
  }

  def signIn: Action[AnyContent] = withoutLoginActionAsync { implicit x: Option[LoginState] =>
    implicit request =>
      SignIn.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.account.signIn(formWithErrors)))
        },
        signInData => {
          val address = Future(utilities.Bech32.convertAccountPublicKeyToAccountAddress(pubkey = signInData.publicKey))
          val masterAccount = masterAccounts.Service.tryGet(signInData.username)
          val identityID = utilities.Blockchain.getNubIdentity(signInData.username).id.asString

          def validateSignature(address: String) = Future(utilities.Blockchain.verifySecp256k1Signature(publicKey = signInData.publicKey, data = utilities.Keplr.newArbitraryData(data = signInData.username, signer = address).getSHA256, signature = signInData.signature))

          def verifyIdentity(address: String) = blockchainIdentityProvisions.Service.checkExists(id = identityID, address = address)

          def getResult(validSignature: Boolean, address: String, masterAccount: master.Account, identityVerified: Boolean) = if (validSignature && identityVerified) {
            (for {
              result <- sendNotificationsAndGetResult(loginState = LoginState(username = masterAccount.id, userType = masterAccount.userType, address = address, identityID = identityID), pushNotificationToken = signInData.pushNotificationToken)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          } else Future(InternalServerError(views.html.index(failures = Seq(constants.Response.INVALID_SIGNATURE_OR_USERNAME))))

          (for {
            address <- address
            masterAccount <- masterAccount
            validSignature <- validateSignature(address)
            identityVerified <- verifyIdentity(address)
            result <- getResult(validSignature, address, masterAccount, identityVerified)
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
          } yield Created(views.html.index(successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
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
