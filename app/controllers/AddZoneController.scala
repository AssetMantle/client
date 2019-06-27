package controllers

import controllers.actions.{WithGenesisLoginAction, WithUserLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.PushNotification
import utilities.LoginState

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class AddZoneController @Inject()(messagesControllerComponents: MessagesControllerComponents, pushNotification: PushNotification, blockchainAccounts: blockchain.Accounts, masterZoneKYCs: master.ZoneKYCs, transactionsAddZone: transactions.AddZone, blockchainZones: models.blockchain.Zones, blockchainTransactionAddZones: blockchainTransaction.AddZones, masterAccounts: master.Accounts, masterZones: master.Zones, withUserLoginAction: WithUserLoginAction, withGenesisLoginAction: WithGenesisLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
          implicit val loginStateL:LoginState = LoginState(username)
          try {
            masterZones.Service.create(accountID = username, name = addZoneData.name, currency = addZoneData.currency)
            Ok(views.html.index(successes = Seq(constants.Response.ZONE_REQUEST_SENT)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
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
          implicit val loginStateL:LoginState = LoginState(username)
          try {
            val zoneAccountAddress = masterAccounts.Service.getAddress(masterZones.Service.getAccountId(verifyZoneData.zoneID))
            val ticketID: String = if (kafkaEnabled) transactionsAddZone.Service.kafkaPost(transactionsAddZone.Request(from = username, to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, password = verifyZoneData.password)).ticketID else Random.nextString(32)
            blockchainTransactionAddZones.Service.create(username, zoneAccountAddress, verifyZoneData.zoneID, null, null, ticketID, null)

            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionAddZones.Utility.onSuccess(ticketID, transactionsAddZone.Service.post(transactionsAddZone.Request(from = username, to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, password = verifyZoneData.password)))
                } catch {
                  case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionAddZones.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
            }
            Ok(views.html.index(successes = Seq(constants.Response.ZONE_VERIFIED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def viewPendingVerifyZoneRequests: Action[AnyContent] = withGenesisLoginAction.authenticated { username =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewPendingVerifyZoneRequests(masterZones.Service.getVerifyZoneRequests))
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewKycDocuments(accountID: String): Action[AnyContent] = withGenesisLoginAction.authenticated { username =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewVerificationZoneKycDouments(masterZoneKYCs.Service.getAllDocuments(accountID)))
      } catch {
        case baseException: BaseException => BadRequest(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def verifyKycDocument(accountID: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { username =>
    implicit request =>
      try {
        masterZoneKYCs.Service.verify(id = accountID, documentType = documentType)
        pushNotification.sendNotification(accountID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => BadRequest(Messages(baseException.failure.message))
      }
  }

  def rejectKycDocument(accountID: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { username =>
    implicit request =>
      try {
        masterZoneKYCs.Service.reject(id = accountID, documentType = documentType)
        pushNotification.sendNotification(accountID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
        Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => BadRequest(Messages(baseException.failure.message))
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
          implicit val loginStateL:LoginState = LoginState(username)
          try {
            masterZones.Service.updateStatus(rejectVerifyZoneRequestData.zoneID, status = false)
            masterZoneKYCs.Service.rejectAll(masterZones.Service.getAccountId(rejectVerifyZoneRequestData.zoneID))
            Ok(views.html.index(successes = Seq(constants.Response.VERIFY_ZONE_REJECTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
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
            transactionsAddZone.Service.kafkaPost(transactionsAddZone.Request(from = addZoneData.from, to = addZoneData.to, zoneID = addZoneData.zoneID, password = addZoneData.password)).ticketID
          } else {
            transactionsAddZone.Service.post(transactionsAddZone.Request(from = addZoneData.from, to = addZoneData.to, zoneID = addZoneData.zoneID, password = addZoneData.password))
          }
          Ok(views.html.index(successes = Seq(constants.Response.ZONE_ADDED)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
