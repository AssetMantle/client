package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.blockchain.ACLAccounts
import models.{blockchain, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import queries.GetAccount
import transactions.ChangePassword
import utilities.{Email, PushNotification}
import views.companion.master.SignUp

import scala.concurrent.ExecutionContext

@Singleton
class AccountController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionAddKey: transactions.AddKey, transactionForgotPassword: transactions.ForgotPassword, email: Email, masterTransactionEmailOTP: masterTransaction.EmailOTPs, transactionsChangePassword: ChangePassword, withLoginAction: WithLoginAction, masterAccounts: master.Accounts, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: master.Organizations, masterZones: master.Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken, pushNotification: PushNotification)(implicit exec: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLER_ACCOUNT

  private implicit val logger: Logger = Logger(this.getClass)

  def checkUsernameAvailable(username: String): Action[AnyContent] = Action { implicit request =>
    if (masterAccounts.Service.checkUsernameAvailable(username)) Ok else NoContent
  }

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
          logger.info(addKeyResponse.toString)
          masterAccounts.Service.addLogin(signUpData.username, signUpData.password, blockchainAccounts.Service.create(address = addKeyResponse.address, pubkey = addKeyResponse.pubkey), request.lang.toString.stripPrefix("Lang(").stripSuffix(")").trim.split("_")(0))
          Ok(views.html.index(successes = Seq(constants.Response.SIGNED_UP)))
        } catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
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
              transactionsChangePassword.Service.post(username = loginState.username, transactionsChangePassword.Request(oldPassword = changePasswordData.oldPassword, newPassword = changePasswordData.newPassword, confirmNewPassword = changePasswordData.confirmNewPassword))
              masterAccounts.Service.updatePassword(username = loginState.username, newPassword = changePasswordData.newPassword)
              Ok(views.html.index(successes = Seq(constants.Response.PASSWORD_UPDATED)))
            } else {
              BadRequest(views.html.index(failures = Seq(constants.Response.INVALID_PASSWORD)))
            }
          }
          catch {
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
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
          email.sendEmail(subject = Messages(constants.Email.FORGOT_PASSWORD_EMAIL_OTP), toAccountID = emailOTPForgotPasswordData.username, bodyHtml = views.html.mail.forgotPasswordEmailOTP(otp) )
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
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
