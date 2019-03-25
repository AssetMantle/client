package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.AddZones
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.AddZone

import scala.concurrent.ExecutionContext
import scala.util.Random

class AddZoneController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionAddZone: transactions.AddZone, addZones: AddZones)(implicit exec: ExecutionContext,configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def blockchainAddZoneForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addZone(AddZone.form))
  }

  def blockchainAddZone: Action[AnyContent] = Action { implicit request =>
    AddZone.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.addZone(formWithErrors))
      },
      addZoneData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionAddZone.Service.kafkaPost( transactionAddZone.Request(from = addZoneData.from, to = addZoneData.to, zoneID = addZoneData.zoneID, password = addZoneData.password))
            addZones .Service.addZoneKafka(addZoneData.from, addZoneData.to, addZoneData.zoneID, null, null, response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionAddZone.Service.post( transactionAddZone.Request(from = addZoneData.from, to = addZoneData.to, zoneID = addZoneData.zoneID, password = addZoneData.password))
            addZones.Service.addZone(addZoneData.from, addZoneData.to, addZoneData.zoneID, null, Option(response.TxHash), (Random.nextInt(899999999) + 100000000).toString, null)
            Ok(views.html.index(success = response.TxHash))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      }
    )
  }
}
