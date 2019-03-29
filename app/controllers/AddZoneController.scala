package controllers

import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.AddZones
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.AddZone
import views.companion.master
import models.master.{Accounts, Zones}
import scala.concurrent.ExecutionContext
import scala.util.Random

class AddZoneController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionAddZone: transactions.AddZone, blockchainZones: models.blockchain.Zones, blockchainTransactionAddZones: AddZones, masterAccounts: Accounts, masterZones: Zones)(implicit exec: ExecutionContext,configuration: Configuration, withLoginAction: WithLoginAction) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val module: String = constants.Module.CONTROLLERS_ADD_ZONE

  def addZoneForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.addZone(master.AddZone.form))
  }

  def addZone: Action[AnyContent] = withLoginAction { implicit request =>
    master.AddZone.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.addZone(formWithErrors))
      },
      addZoneData => {
        try {
          Ok(views.html.index(success = masterZones.Service.addZone(secretHash = util.hashing.MurmurHash3.stringHash(addZoneData.password).toString, name = addZoneData.name, currency = addZoneData.currency)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      }
    )
  }

  def verifyZoneForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.verifyZone(master.VerifyZone.form))
  }

  def verifyZone: Action[AnyContent] = withLoginAction { implicit request =>
    master.VerifyZone.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.verifyZone(formWithErrors))
      },
      verifyZoneData => {
        try {
          masterZones.Service.verifyZone(verifyZoneData.id, true)
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionAddZone.Service.kafkaPost(transactionAddZone.Request(from = request.session.get(constants.Security.USERNAME).get, to = masterAccounts.Service.getAccount(masterZones.Service.getZone(verifyZoneData.id).name).accountAddress, zoneID = verifyZoneData.id, password =  verifyZoneData.password))
            Ok(views.html.index(success = Messages(module + "." + constants.Success.VERIFY_ZONE) + verifyZoneData.id + response.ticketID))
          } else {
            val accountAddress = masterAccounts.Service.getAccount(masterZones.Service.getZone(verifyZoneData.id).name).accountAddress
            val response = transactionAddZone.Service.post(transactionAddZone.Request(from = request.session.get(constants.Security.USERNAME).get, to = accountAddress, zoneID = verifyZoneData.id, password =  verifyZoneData.password))
            blockchainZones.Service.addZone(request.session.get(constants.Security.USERNAME).get, accountAddress)
            masterAccounts.Service.updateUserType(request.session.get(constants.Security.USERNAME).get, constants.User.ZONE)
            Ok(views.html.index(success = Messages(module + "." + constants.Success.VERIFY_ZONE) + verifyZoneData.id + response.TxHash))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException =>  Ok(views.html.index(failure = Messages(blockChainException.message)))
        }
      }
    )
  }

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
            blockchainTransactionAddZones.Service.addZoneKafka(addZoneData.from, addZoneData.to, addZoneData.zoneID, null, null, response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionAddZone.Service.post( transactionAddZone.Request(from = addZoneData.from, to = addZoneData.to, zoneID = addZoneData.zoneID, password = addZoneData.password))
            blockchainTransactionAddZones.Service.addZone(addZoneData.from, addZoneData.to, addZoneData.zoneID, null, Option(response.TxHash), (Random.nextInt(899999999) + 100000000).toString, null)
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
