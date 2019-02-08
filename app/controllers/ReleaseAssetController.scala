package controllers

import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.Inject
import models.master.Accounts
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.ReleaseAsset

import scala.concurrent.ExecutionContext

class ReleaseAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, accounts: Accounts, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def releaseAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.releaseAsset(ReleaseAsset.form))
  }

  def releaseAsset: Action[AnyContent] = Action { implicit request =>
    ReleaseAsset.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.releaseAsset(formWithErrors))
      },
      releaseAssetData => {
        try {
          Ok("") //if (accounts.Service.validateLogin(loginData.username, loginData.password)) withUsernameToken.Ok(views.html.index(success = "Logged In!"), loginData.username) else Ok(views.html.index(failure = "Invalid Login!"))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      })
  }
}
