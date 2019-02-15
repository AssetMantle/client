package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.AddZone
import views.companion.blockchain.AddZone

import scala.concurrent.ExecutionContext

class AddZoneController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionAddZone: AddZone)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def addZoneForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.addZone(AddZone.form))
  }

  def addZone: Action[AnyContent] = Action { implicit request =>
    AddZone.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.addZone(formWithErrors))
      },
      addZoneData => {
        try {
          Ok(views.html.index(transactionAddZone.Service.post(new transactionAddZone.Request(addZoneData.from, addZoneData.to, addZoneData.zoneID, addZoneData.chainID, addZoneData.password)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}
