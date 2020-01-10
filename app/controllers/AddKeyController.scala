package controllers

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddKeyController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionsAddKey: transactions.AddKey)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def blockchainAddKeyForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addKey())
  }

  def blockchainAddKey: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.AddKey.form.bindFromRequest().fold(
      formWithErrors => {
        Future (BadRequest(views.html.component.blockchain.addKey(formWithErrors)))
      },
      addKeyData => {
        val postRequest = transactionsAddKey.Service.post(transactionsAddKey.Request(addKeyData.name, addKeyData.password))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.KEY_ADDED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
