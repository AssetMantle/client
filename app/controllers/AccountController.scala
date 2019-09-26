package controllers

import actors.ShutdownActor
import controllers.actions.{LoginState, WithLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.companion.master.{Login, Logout, NoteNewKeyDetails, SignUp}

import scala.concurrent.ExecutionContext

@Singleton
class AccountController @Inject()(
                                   utilitiesNotification: utilities.Notification,
                                   shutdownActor: ShutdownActor,
                                   withLoginAction: WithLoginAction,
                                   withUsernameToken: WithUsernameToken,
                                   queryGetAccount: queries.GetAccount,
                                   blockchainFiats: blockchain.Fiats,
                                   blockchainZones: blockchain.Zones,
                                   blockchainOrders: blockchain.Orders,
                                   blockchainAssets: blockchain.Assets,
                                   blockchainAccounts: blockchain.Accounts,
                                   blockchainAclHashes: blockchain.ACLHashes,
                                   blockchainAclAccounts: blockchain.ACLAccounts,
                                   blockchainNegotiations: blockchain.Negotiations,
                                   blockchainOrganizations: blockchain.Organizations,
                                   masterOrganizations: master.Organizations,
                                   masterZones: master.Zones,
                                   masterAccounts: master.Accounts,
                                   masterTransactionEmailOTP: masterTransaction.EmailOTPs,
                                   masterTransactionAccountTokens: masterTransaction.AccountTokens,
                                   transactionAddKey: transactions.AddKey,
                                   transactionForgotPassword: transactions.ForgotPassword,
                                   transactionChangePassword: transactions.ChangePassword,
                                   messagesControllerComponents: MessagesControllerComponents,
                                 )
                                 (implicit
                                  executionContext: ExecutionContext,
                                  configuration: Configuration,
                                  wsClient: WSClient,
                                 ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLER_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)


  def signUpForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.signUp(SignUp.form))
  }

  def signUp: Action[AnyContent] = Action { implicit request =>
    SignUp.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.signUp(formWithErrors))
      },
      signUpData => {
        try {
          val addKeyResponse = transactionAddKey.Service.post(transactionAddKey.Request(signUpData.username, signUpData.password))
          masterAccounts.Service.addLogin(signUpData.username, signUpData.password, blockchainAccounts.Service.create(address = addKeyResponse.address, pubkey = addKeyResponse.pubkey), request.lang.toString.stripPrefix("Lang(").stripSuffix(")").trim.split("_")(0))
          PartialContent(views.html.component.master.noteNewKeyDetails(NoteNewKeyDetails.form, addKeyResponse.name, addKeyResponse.address, addKeyResponse.pubkey, addKeyResponse.mnemonic))
        } catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }


  def loginForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.login(Login.form))
  }

  def login: Action[AnyContent] = Action { implicit request =>
    Login.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.login(formWithErrors))
      },
      loginData => {
        try {
          val userType = masterAccounts.Service.getUserType(loginData.username)
          val address = masterAccounts.Service.getAddress(loginData.username)
          implicit val loginState: LoginState = LoginState(loginData.username, userType, address, if (userType == constants.User.TRADER) Option(blockchainAclHashes.Service.getACL(blockchainAclAccounts.Service.getACLHash(address))) else None)
          val contactWarnings: Seq[constants.Response.Warning] = utilities.Contact.getWarnings(masterAccounts.Service.validateLoginAndGetStatus(loginData.username, loginData.password))
          utilitiesNotification.registerNotificationToken(loginData.username, loginData.notificationToken)
          utilitiesNotification.send(loginData.username, constants.Notification.LOGIN, loginData.username)
          loginState.userType match {
            case constants.User.GENESIS =>
              withUsernameToken.Ok(views.html.genesisIndex(warnings = contactWarnings))
            case constants.User.ZONE =>
              withUsernameToken.Ok(views.html.zoneIndex(zone = masterZones.Service.get(blockchainZones.Service.getID(loginState.address)), warnings = contactWarnings))
            case constants.User.ORGANIZATION =>
              withUsernameToken.Ok(views.html.organizationIndex(organization = masterOrganizations.Service.get(blockchainOrganizations.Service.getID(loginState.address)), warnings = contactWarnings))
            case constants.User.TRADER =>
              val aclAccount = blockchainAclAccounts.Service.get(loginState.address)
              withUsernameToken.Ok(views.html.traderIndex(totalFiat = blockchainFiats.Service.getFiatPegWallet(loginState.address).map(_.transactionAmount.toInt).sum, zone = masterZones.Service.get(aclAccount.zoneID), organization = masterOrganizations.Service.get(aclAccount.organizationID), warnings = contactWarnings))
            case constants.User.USER =>
              withUsernameToken.Ok(views.html.userIndex(warnings = contactWarnings))
            case constants.User.UNKNOWN =>
              withUsernameToken.Ok(views.html.anonymousIndex(warnings = contactWarnings))
            case constants.User.WITHOUT_LOGIN =>
              masterAccounts.Service.updateUserType(loginData.username, constants.User.UNKNOWN)
              withUsernameToken.Ok(views.html.anonymousIndex(warnings = contactWarnings))
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }


  def logoutForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.logout(Logout.form))
  }

  def logout: Action[AnyContent] = withLoginAction.authenticated { loginState =>
    implicit request =>
      Logout.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.logout(formWithErrors))
        },
        loginData => {
          try {
            if (!loginData.receiveNotifications) {
              masterTransactionAccountTokens.Service.deleteToken(loginState.username)
            } else {
              masterTransactionAccountTokens.Service.resetSessionTokenTime(loginState.username)
            }
            shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_ACCOUNT, loginState.username)
            if (masterAccounts.Service.getUserType(loginState.username) == constants.User.TRADER) {
              shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_ASSET, loginState.username)
              shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_FIAT, loginState.username)
              shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_NEGOTIATION, loginState.username)
              shutdownActor.onLogOut(constants.Module.ACTOR_MAIN_ORDER, loginState.username)
            }
            Ok(views.html.index(successes = Seq(constants.Response.LOGGED_OUT))).withNewSession
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }


  def changePasswordForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.changePassword(views.companion.master.ChangePassword.form))
  }

  def changePassword: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ChangePassword.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.changePassword(formWithErrors))
        },
        changePasswordData => {
          try {
            if (masterAccounts.Service.validateLogin(loginState.username, changePasswordData.oldPassword)) {
              transactionChangePassword.Service.post(username = loginState.username, transactionChangePassword.Request(oldPassword = changePasswordData.oldPassword, newPassword = changePasswordData.newPassword, confirmNewPassword = changePasswordData.confirmNewPassword))
              masterAccounts.Service.updatePassword(username = loginState.username, newPassword = changePasswordData.newPassword)
              Ok(views.html.index(successes = Seq(constants.Response.PASSWORD_UPDATED)))
            } else {
              BadRequest(views.html.index(failures = Seq(constants.Response.INVALID_PASSWORD)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }


  def emailOTPForgotPasswordForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.emailOTPForgotPassword(views.companion.master.EmailOTPForgotPassword.form))
  }

  def emailOTPForgotPassword: Action[AnyContent] = Action { implicit request =>
    views.companion.master.EmailOTPForgotPassword.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.emailOTPForgotPassword(formWithErrors))
      },
      emailOTPForgotPasswordData => {
        try {
          val otp = masterTransactionEmailOTP.Service.sendOTP(emailOTPForgotPasswordData.username)
          utilitiesNotification.send(accountID = emailOTPForgotPasswordData.username, notification = constants.Notification.FORGOT_PASSWORD_OTP, otp)
          PartialContent(views.html.component.master.forgotPassword(views.companion.master.ForgotPassword.form, emailOTPForgotPasswordData.username))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }


  def forgotPasswordForm(username: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.forgotPassword(views.companion.master.ForgotPassword.form, username))
  }

  def forgotPassword(username: String): Action[AnyContent] = Action { implicit request =>
    views.companion.master.ForgotPassword.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.forgotPassword(formWithErrors, username))
      },
      forgotPasswordData => {
        try {
          if (masterTransactionEmailOTP.Service.verifyOTP(username, forgotPasswordData.otp)) {
            transactionForgotPassword.Service.post(username = username, transactionForgotPassword.Request(seed = forgotPasswordData.mnemonic, newPassword = forgotPasswordData.newPassword, confirmNewPassword = forgotPasswordData.confirmNewPassword))
            masterAccounts.Service.updatePassword(username = username, newPassword = forgotPasswordData.newPassword)
            Ok(views.html.index(successes = Seq(constants.Response.PASSWORD_UPDATED)))
          } else {
            BadRequest(views.html.index(failures = Seq(constants.Response.INVALID_PASSWORD)))
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }


  def checkUsernameAvailable(username: String): Action[AnyContent] = Action { implicit request =>
    if (masterAccounts.Service.checkUsernameAvailable(username)) Ok else NoContent
  }

  def noteNewKeyDetails(name: String, blockchainAddress: String, publicKey: String, seed: String): Action[AnyContent] = Action { implicit request =>
    views.companion.master.NoteNewKeyDetails.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.noteNewKeyDetails(formWithErrors, name, blockchainAddress, publicKey, seed))
      },
      noteNewKeyDetailsData => {
        if (noteNewKeyDetailsData.confirmNoteNewKeyDetails) {
          Ok(views.html.index(successes = Seq(constants.Response.SIGNED_UP)))
        }
        else {
          BadRequest(views.html.component.master.noteNewKeyDetails(NoteNewKeyDetails.form, name, blockchainAddress, publicKey, seed))
        }
      }
    )
  }
}
