package controllers

import javax.inject.Inject
import models.{blockchain, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.forms._

import scala.concurrent.ExecutionContext

class SignUpController @Inject()(messagesControllerComponents: MessagesControllerComponents, accounts: master.Accounts, accounts_bc: blockchain.Accounts)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def signUpForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.signUp(SignUp.form))
  }

  def checkUsernameAvailable(username:String): Action[AnyContent] = Action{ implicit request =>
   if (accounts.Service.checkUsernameAvailable(username)) Ok else NoContent
  }

  def signUp: Action[AnyContent] = Action { implicit request =>
    SignUp.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.signUp(formWithErrors))
      },
      signUpData => {
        val x = accounts.Service.addLogin(signUpData.username, signUpData.password, accounts_bc.Service.addAccount(signUpData.username, signUpData.password))
        Ok(views.html.index(success = s"Signed Up! Address: $x"))
      })
  }
}