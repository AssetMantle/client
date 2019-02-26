package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.AddKey
import views.companion.blockchain.AddKey

import scala.concurrent.ExecutionContext

class AddKeyController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionAddKey: AddKey)(implicit exec: ExecutionContext,configuration: play.api.Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def addKeyForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addKey(AddKey.form))
  }

  def addKey: Action[AnyContent] = Action { implicit request =>
    AddKey.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.addKey(formWithErrors))
      },
      addKeyData => {
        try {
          Ok(views.html.index(success = Messages(constants.Success.ADD_KEY) + transactionAddKey.Service.post(new transactionAddKey.Request(addKeyData.name, addKeyData.password, addKeyData.seed)).accountAddress))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}
