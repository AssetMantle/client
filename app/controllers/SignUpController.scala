package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.master.Accounts
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.master.SignUp

import scala.concurrent.ExecutionContext

class SignUpController @Inject()(messagesControllerComponents: MessagesControllerComponents, accounts: Accounts, blockchainAccounts: models.blockchain.Accounts)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val module: String = constants.Module.CONTROLLER_SIGN_UP

  def signUpForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.signUp(SignUp.form))
  }

  def checkUsernameAvailable(username: String): Action[AnyContent] = Action { implicit request =>
    if (accounts.Service.checkUsernameAvailable(username)) Ok else NoContent
  }

  def signUp: Action[AnyContent] = Action { implicit request =>
    SignUp.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.signUp(formWithErrors))
      },
      signUpData => {
        try {
          val x = accounts.Service.addLogin(signUpData.username, signUpData.password, blockchainAccounts.Service.addAccount(signUpData.username, signUpData.password))
          Ok(views.html.index(success = Messages(module + "." + constants.Success.SIGN_UP) + x))
        } catch {
          case baseException: BaseException =>
            Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException =>
            Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}