package controllers

import constants.AppConfig._
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.master.Profile
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import transactions.blockchain.{AddKey, ChangePassword, ForgotPassword}
import utilities.{KeyStore, Wallet}
import views.companion.master.account._

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
          if (signUpData.password == signUpData.confirmPassword) {
            val addAccount = masterAccounts.Service.create(username = signUpData.username, password = signUpData.password, language = request.lang, userType = constants.User.USER)

            def addBCAccount(wallet: Wallet) = blockchainAccounts.Service.updateOrInsertOnSignUp(wallet, signUpData.username, constants.User.USER)

            (for {
              wallet <- addAccount
              _ <- addBCAccount(wallet)
            } yield PartialContent(views.html.component.master.account.showWallet(username = signUpData.username, address = wallet.address, showMnemonics = wallet.mnemonics.takeRight(constants.Blockchain.MnemonicShown)))).recover {
              case baseException: BaseException => BadGateway(views.html.index(failures = Seq(baseException.failure)))
            }
          } else Future(InternalServerError(views.html.index(failures = Seq(constants.Response.PASSWORDS_DO_NOT_MATCH))))
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
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePasswordAndGetAccount(username = signInData.username, password = signInData.password)
          val bcAccount = blockchainAccounts.Service.tryGetByUsername(signInData.username)

          def sendNotification(validUsernamePassword: Boolean, loginState: LoginState) = if (validUsernamePassword) {
            val pushNotificationTokenUpdate = masterTransactionPushNotificationTokens.Service.update(id = loginState.username, token = signInData.pushNotificationToken)
            for {
              _ <- pushNotificationTokenUpdate
              _ <- utilitiesNotification.send(loginState.username, constants.Notification.LOGIN, loginState.username)()
            } yield loginState
          } else Future(throw new BaseException(constants.Response.INVALID_USERNAME_OR_PASSWORD))

          def getResult(profile: Option[Profile])(implicit loginState: LoginState): Future[Result] =
            withUsernameToken.Ok(views.html.assetMantle.profile(profile = profile))

          (for {
            (validUsernamePassword, masterAccount) <- validateUsernamePassword
            bcAccount <- bcAccount
            loginState <- sendNotification(validUsernamePassword, LoginState(username = masterAccount.id, userType = masterAccount.userType, address = bcAccount.address))
            profile <- masterProfiles.Service.get(loginState.username)
            result <- getResult(profile)(loginState)
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

  def changePasswordForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.account.changePassword())
  }

  def changePassword: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.account.ChangePassword.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.account.changePassword(formWithErrors)))
        },
        changePasswordData => {
          val updatePassword = masterAccounts.Service.validateAndUpdatePassword(username = loginState.username, oldPassword = changePasswordData.oldPassword, newPassword = changePasswordData.newPassword)
          val profile = masterProfiles.Service.get(loginState.username)

          (for {
            profile <- profile
            _ <- updatePassword
            result <- withUsernameToken.Ok(views.html.assetMantle.profile(profile = profile, successes = Seq(constants.Response.PASSWORD_UPDATED)))
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def forgotPasswordForm(username: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.account.forgotPassword(username = username))
  }

  def forgotPassword: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.account.ForgotPassword.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.account.forgotPassword(formWithErrors, formWithErrors(constants.FormField.USERNAME.name).value.getOrElse(""))))
        },
        forgotPasswordData => {
          val account = masterAccounts.Service.tryGet(forgotPasswordData.username)
          val bcAccount = blockchainAccounts.Service.tryGetByUsername(forgotPasswordData.username)

          def verifyAndUpdate(account: master.Account, bcAccount: blockchain.Account) = {
            val wallet = utilities.WalletGenerator.getWallet(account.partialMnemonic ++ forgotPasswordData.mnemonics.split(" "))
            if (wallet.address != bcAccount.address) {
              masterAccounts.Service.updateOnForgotPassword(account = account, newPassword = forgotPasswordData.newPassword, wallet = wallet)
            } else Future(InternalServerError(views.html.index(failures = Seq(constants.Response.INVALID_MNEMONICS))))
          }

          (for {
            account <- account
            bcAccount <- bcAccount
            _ <- verifyAndUpdate(account, bcAccount)
          } yield Ok(views.html.index())).recover {
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
            masterProfiles.Service.insertOrUpdate(Profile(accountID = loginState.username, name = updateProfileData.name, description = updateProfileData.description, socialProfiles = Seq()))
          } { profile =>
            masterProfiles.Service.insertOrUpdate(profile.copy(name = updateProfileData.name, description = updateProfileData.description))
          }

          def updatedProfile = masterProfiles.Service.get(loginState.username)

          (for {
            optionalProfile <- optionalProfile
            _ <- updateProfileNameDescription(optionalProfile)
            updatedProfile <- updatedProfile
          } yield Ok(views.html.assetMantle.profile(profile = updatedProfile, successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
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
            masterProfiles.Service.insertOrUpdate(Profile(accountID = loginState.username, name = "", description = "", socialProfiles = Seq(updateSocialProfileData.toSocialProfile)))
          } { profile =>
            masterProfiles.Service.insertOrUpdate(profile.copy(socialProfiles = profile.socialProfiles :+ updateSocialProfileData.toSocialProfile))
          }

          def profile = masterProfiles.Service.get(loginState.username)

          (for {
            optionalProfile <- optionalProfile
            _ <- updateProfileNameDescription(optionalProfile)
            profile <- profile
          } yield Ok(views.html.assetMantle.profile(profile = profile, successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
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
