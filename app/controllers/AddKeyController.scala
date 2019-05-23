package controllers

import constants.Response.Success
import exceptions.{BaseException, BlockChainException}
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
          Ok(views.html.index(successes = Seq(new Success(constants.Response.ADD_KEY.message + transactionsAddKey.Service.post(transactionsAddKey.Request(addKeyData.name, addKeyData.password, addKeyData.seed)).address))))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
