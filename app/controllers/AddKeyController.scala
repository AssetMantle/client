package controllers

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.AddKey

import scala.concurrent.ExecutionContext

@Singleton
class AddKeyController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionsAddKey: transactions.AddKey)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def blockchainAddKeyForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addKey(AddKey.form))
  }

  def blockchainAddKey: Action[AnyContent] = Action { implicit request =>
    AddKey.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.addKey(formWithErrors))
      },
      addKeyData => {
        try {
          transactionsAddKey.Service.post(transactionsAddKey.Request(addKeyData.name, addKeyData.password))
          Ok(views.html.index(successes = Seq(constants.Response.KEY_ADDED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
