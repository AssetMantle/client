package controllers

import controllers.actions.{WithGenesisLoginAction, WithUserLoginAction}
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.PushNotification

import scala.concurrent.ExecutionContext

@Singleton
class AddZoneController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, pushNotification: PushNotification, blockchainAccounts: blockchain.Accounts, masterZoneKYCs: master.ZoneKYCs, masterOrganizations: master.Organizations, transactionsAddZone: transactions.AddZone, blockchainZones: models.blockchain.Zones, blockchainTransactionAddZones: blockchainTransaction.AddZones, masterAccounts: master.Accounts, masterZones: master.Zones, withUserLoginAction: WithUserLoginAction, withGenesisLoginAction: WithGenesisLoginAction, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module:String= constants.Module.CONTROLLERS_ADD_ZONE

  def addZoneForm(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.addZone(views.companion.master.AddZone.form))
  }

  def addZone(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddZone.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.addZone(formWithErrors))
        },
        addZoneData => {
          try {
            masterZones.Service.create(accountID = loginState.username, name = addZoneData.name, currency = addZoneData.currency)
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ZONE_REQUEST_SENT)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def verifyZoneForm(zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.verifyZone(views.companion.master.VerifyZone.form, zoneID))
  }

  def verifyZone: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyZone.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.verifyZone(formWithErrors, formWithErrors.data(constants.Form.ZONE_ID)))
        },
        verifyZoneData => {
          try {
            val zoneAccountAddress = masterAccounts.Service.getAddress(masterZones.Service.getAccountId(verifyZoneData.zoneID))
            transaction.process[blockchainTransaction.AddZone, transactionsAddZone.Request](
              entity = blockchainTransaction.AddZone(from = loginState.address, to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, gas = verifyZoneData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionAddZones.Service.create,
              request = transactionsAddZone.Request(transactionsAddZone.BaseRequest(from = loginState.address, gas = verifyZoneData.gas.toString), to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, password = verifyZoneData.password, mode = transactionMode),
              action = transactionsAddZone.Service.post,
              onSuccess = blockchainTransactionAddZones.Utility.onSuccess,
              onFailure = blockchainTransactionAddZones.Utility.onFailure,
              updateTransactionHash = blockchainTransactionAddZones.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ZONE_VERIFIED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def viewPendingVerifyZoneRequests: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewPendingVerifyZoneRequests(masterZones.Service.getVerifyZoneRequests))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def viewKycDocuments(accountID: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewVerificationZoneKycDouments(masterZoneKYCs.Service.getAllDocuments(accountID)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def verifyKycDocument(accountID: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterZoneKYCs.Service.verify(id = accountID, documentType = documentType)
        pushNotification.sendNotification(accountID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def rejectKycDocument(accountID: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterZoneKYCs.Service.reject(id = accountID, documentType = documentType)
        pushNotification.sendNotification(accountID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def rejectVerifyZoneRequestForm(zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectVerifyZoneRequest(views.companion.master.RejectVerifyZoneRequest.form, zoneID))
  }

  def rejectVerifyZoneRequest: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectVerifyZoneRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.rejectVerifyZoneRequest(formWithErrors, formWithErrors.data(constants.Form.ZONE_ID)))
        },
        rejectVerifyZoneRequestData => {
          try {
            masterZones.Service.updateStatus(rejectVerifyZoneRequestData.zoneID, status = false)
            masterZoneKYCs.Service.rejectAll(masterZones.Service.getAccountId(rejectVerifyZoneRequestData.zoneID))
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.VERIFY_ZONE_REJECTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewZonesInGenesis: Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewZonesInGenesis(masterZones.Service.getAll))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
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
          transactionsAddZone.Service.post(transactionsAddZone.Request(transactionsAddZone.BaseRequest(from = addZoneData.from, gas = addZoneData.gas.toString), to = addZoneData.to, zoneID = addZoneData.zoneID, password = addZoneData.password, mode = transactionMode))
          Ok(views.html.index(successes = Seq(constants.Response.ZONE_ADDED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
