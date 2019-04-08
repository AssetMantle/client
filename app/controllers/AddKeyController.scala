package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.AddKey

import scala.concurrent.ExecutionContext

class AddKeyController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionsAddKey: transactions.AddKey)(implicit exec: ExecutionContext,configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
          Ok(views.html.index(success = Messages(constants.Success.ADD_KEY) + transactionsAddKey.Service.post(transactionsAddKey.Request(addKeyData.name, addKeyData.password, addKeyData.seed)).address))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      }
    )
  }
}
