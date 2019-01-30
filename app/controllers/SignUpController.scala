package controllers

import exceptions.BaseException
import javax.inject.Inject
import models.{blockchain, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.forms._

import scala.concurrent.ExecutionContext

class SignUpController @Inject()(messagesControllerComponents: MessagesControllerComponents, accounts: master.Accounts, accounts_bc: blockchain.Accounts)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val module: String = constants.Module.BLOCKCHAIN

  def signUpForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.signUp(SignUp.form))
  }

  def checkUsernameAvailable(username: String): Action[AnyContent] = Action { implicit request =>
    if (accounts.Service.checkUsernameAvailable(username)) Ok else NoContent
  }

  def signUp: Action[AnyContent] = Action { implicit request =>
    SignUp.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.signUp(formWithErrors))
      },
      signUpData => {
        try {
          val x = accounts.Service.addLogin(signUpData.username, signUpData.password, accounts_bc.Service.addAccount(signUpData.username, signUpData.password))
          Ok(views.html.index(success = Messages(module + "." + constants.Success.SIGN_UP) + x))
        } catch {
          case baseException: BaseException =>
            Ok(views.html.index(failure = Messages(baseException.message)))
        }
      })
  }
}