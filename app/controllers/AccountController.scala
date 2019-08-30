package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.blockchain.ACLAccounts
import models.{blockchain, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import queries.GetAccount
import transactions.ChangePassword
import utilities.{Email, PushNotification}

import scala.concurrent.ExecutionContext

@Singleton
class AccountController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionForgotPassword: transactions.ForgotPassword, email: Email, masterTransactionEmailOTP: masterTransaction.EmailOTPs, transactionsChangePassword: ChangePassword, withLoginAction: WithLoginAction, masterAccounts: master.Accounts, blockchainAclAccounts: ACLAccounts, blockchainZones: blockchain.Zones, blockchainOrganizations: blockchain.Organizations, blockchainAssets: blockchain.Assets, blockchainFiats: blockchain.Fiats, blockchainNegotiations: blockchain.Negotiations, masterOrganizations: master.Organizations, masterZones: master.Zones, blockchainAclHashes: blockchain.ACLHashes, blockchainOrders: blockchain.Orders, getAccount: GetAccount, blockchainAccounts: blockchain.Accounts, withUsernameToken: WithUsernameToken, pushNotification: PushNotification)(implicit exec: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val module: String = constants.Module.ACCOUNT_CONTROLLER

  private implicit val logger: Logger = Logger(this.getClass)

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
              transactionsChangePassword.Service.post(username =  loginState.username, transactionsChangePassword.Request(oldPassword = changePasswordData.oldPassword, newPassword = changePasswordData.newPassword, confirmNewPassword = changePasswordData.confirmNewPassword))
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
          email.sendEmail(emailOTPForgotPasswordData.username, constants.Email.OTP, Seq(otp))
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
            if (masterTransactionEmailOTP.Service.verifyOTP(username, forgotPasswordData.otp)){
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
