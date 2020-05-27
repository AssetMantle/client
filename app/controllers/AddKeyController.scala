package controllers

import controllers.actions.{WithoutLoginAction, WithoutLoginActionAsync}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddKeyController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionsAddKey: transactions.AddKey, withoutLoginAction: WithoutLoginAction, withoutLoginActionAsync: WithoutLoginActionAsync)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  def blockchainAddKeyForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.addKey())
  }

  def blockchainAddKey: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.AddKey.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.addKey(formWithErrors)))
      },
      addKeyData => {
        val postRequest = transactionsAddKey.Service.post(transactionsAddKey.Request(addKeyData.name, addKeyData.password, addKeyData.mnemonics))
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
