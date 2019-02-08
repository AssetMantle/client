package controllers

import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.Inject
import models.master.Accounts
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.SendAsset

import scala.concurrent.ExecutionContext

class SendAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, accounts: Accounts, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def sendAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.sendAsset(SendAsset.form))
  }

  def sendAsset: Action[AnyContent] = Action { implicit request =>
    SendAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.sendAsset(formWithErrors))
      },
      sendAssetData => {
        try {
          Ok("") //if (accounts.Service.validateLogin(loginData.username, loginData.password)) withUsernameToken.Ok(views.html.index(success = "Logged In!"), loginData.username) else Ok(views.html.index(failure = "Invalid Login!"))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      })
  }
}
