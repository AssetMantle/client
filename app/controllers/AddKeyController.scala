package controllers

import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.Inject
import models.master.Accounts
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.AddKey

import scala.concurrent.ExecutionContext

class AddKeyController @Inject()(messagesControllerComponents: MessagesControllerComponents, accounts: Accounts, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def addKeyForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.addKey(AddKey.form))
  }

  def addKey: Action[AnyContent] = Action { implicit request =>
    AddKey.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.addKey(formWithErrors))
      },
      addKeyData => {
        try {
          Ok("") //   if (accounts.Service.validateLogin(addKeyData.username, addKeyData.password)) withUsernameToken.Ok(views.html.index(success = "Logged In!"), addKeyData.username) else Ok(views.html.index(failure = "Invalid Login!"))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      })
  }
}
