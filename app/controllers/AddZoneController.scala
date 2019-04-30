package controllers

import controllers.actions.{WithGenesisLoginAction, WithUserLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.util.Random

@Singleton
class AddZoneController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainAccounts: blockchain.Accounts, transactionsAddZone: transactions.AddZone, blockchainZones: models.blockchain.Zones, blockchainTransactionAddZones: blockchainTransaction.AddZones, masterAccounts: master.Accounts, masterZones: master.Zones, withUserLoginAction: WithUserLoginAction, withGenesisLoginAction: WithGenesisLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private implicit val logger: Logger = Logger(this.getClass)

  def addZoneForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.addZone(views.companion.master.AddZone.form))
  }

  def addZone: Action[AnyContent] = withUserLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.AddZone.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.addZone(formWithErrors))
        },
        addZoneData => {
          try {
            Ok(views.html.index(success = constants.Success.ADD_ZONE + ":" + masterZones.Service.addZone(accountID = username, name = addZoneData.name, currency = addZoneData.currency)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          }
        }
      )
  }

  def verifyZoneForm(zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.verifyZone(views.companion.master.VerifyZone.form, zoneID))
  }

  def verifyZone: Action[AnyContent] = withGenesisLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.VerifyZone.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.verifyZone(formWithErrors, formWithErrors.data(constants.Form.ZONE_ID)))
        },
        verifyZoneData => {
          try {
            if (kafkaEnabled) {
              val zoneAccountAddress = masterAccounts.Service.getAddress(masterZones.Service.getAccountId(verifyZoneData.zoneID))
              val response = transactionsAddZone.Service.kafkaPost(transactionsAddZone.Request(from = username, to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, password = verifyZoneData.password))
              blockchainTransactionAddZones.Service.addZoneKafka(username, zoneAccountAddress, verifyZoneData.zoneID, null, null, response.ticketID, null)
              Ok(views.html.index(success = Messages(constants.Success.VERIFY_ZONE) + verifyZoneData.zoneID + response.ticketID))
            } else {
              val zoneAccountID = masterZones.Service.getAccountId(verifyZoneData.zoneID)
              val zoneAccountAddress = masterAccounts.Service.getAddress(zoneAccountID)
              val response = transactionsAddZone.Service.post(transactionsAddZone.Request(from = username, to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, password = verifyZoneData.password))
              val fromAddress = masterAccounts.Service.getAddress(username)
              blockchainTransactionAddZones.Service.addZone(username, zoneAccountAddress, verifyZoneData.zoneID, null, Option(response.TxHash), Random.nextString(32), null)
              blockchainZones.Service.addZone(verifyZoneData.zoneID, zoneAccountAddress)
              masterZones.Service.updateStatus(verifyZoneData.zoneID, true)
              masterAccounts.Service.updateUserType(zoneAccountID, constants.User.ZONE)
              blockchainAccounts.Service.updateSequence(fromAddress, blockchainAccounts.Service.getSequence(fromAddress) + 1)
              Ok(views.html.index(success = Messages(constants.Success.VERIFY_ZONE) + verifyZoneData.zoneID + response.TxHash))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
            case blockChainException: BlockChainException => Ok(views.html.index(failure = Messages(blockChainException.message)))
          }
        }
      )
  }

  def viewPendingVerifyZoneRequests: Action[AnyContent] = withGenesisLoginAction.authenticated { username =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewPendingVerifyZoneRequests(masterZones.Service.getVerifyZoneRequests()))
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
      }
  }

  def rejectVerifyZoneRequestForm(zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectVerifyZoneRequest(views.companion.master.RejectVerifyZoneRequest.form, zoneID))
  }

  def rejectVerifyZoneRequest: Action[AnyContent] = withGenesisLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.RejectVerifyZoneRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.rejectVerifyZoneRequest(formWithErrors, formWithErrors.data(constants.Form.ZONE_ID)))
        },
        rejectVerifyZoneRequestData => {
          try {
            masterZones.Service.updateStatus(rejectVerifyZoneRequestData.zoneID, false)
            Ok(views.html.index(success = Messages(constants.Success.VERIFY_ZONE_REJECTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          }
        }
      )
  }

  def blockchainAddZoneForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addZone(views.companion.blockchain.AddZone.form))
  }

  def blockchainAddZone: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.AddZone.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.addZone(formWithErrors))
      },
      addZoneData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsAddZone.Service.kafkaPost(transactionsAddZone.Request(from = addZoneData.from, to = addZoneData.to, zoneID = addZoneData.zoneID, password = addZoneData.password))
            blockchainTransactionAddZones.Service.addZoneKafka(addZoneData.from, addZoneData.to, addZoneData.zoneID, null, null, response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsAddZone.Service.post(transactionsAddZone.Request(from = addZoneData.from, to = addZoneData.to, zoneID = addZoneData.zoneID, password = addZoneData.password))
            blockchainTransactionAddZones.Service.addZone(addZoneData.from, addZoneData.to, addZoneData.zoneID, null, Option(response.TxHash), Random.nextString(32), null)
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
