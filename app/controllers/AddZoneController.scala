package controllers

import controllers.actions.{WithGenesisLoginAction, WithUserLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import models.common.Serializable._
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.PushNotification

import scala.concurrent.ExecutionContext

@Singleton
class AddZoneController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, pushNotification: PushNotification, blockchainAccounts: blockchain.Accounts, masterZoneKYCs: master.ZoneKYCs, masterOrganizations: master.Organizations, transactionsAddZone: transactions.AddZone, blockchainZones: models.blockchain.Zones, blockchainTransactionAddZones: blockchainTransaction.AddZones, masterAccounts: master.Accounts, masterZones: master.Zones, withUserLoginAction: WithUserLoginAction, withGenesisLoginAction: WithGenesisLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ADD_ZONE

  def addZoneForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val zone = masterZones.Service.getByAccountID(loginState.username)
        Ok(views.html.component.master.addZone(views.companion.master.AddZone.form.fill(views.companion.master.AddZone.Data(name = zone.name, currency = zone.currency, address = views.companion.master.AddZone.AddressData(addressLine1 = zone.address.addressLine1, addressLine2 = zone.address.addressLine2, landmark = zone.address.landmark, city = zone.address.city, country = zone.address.country, zipCode = zone.address.zipCode, phone = zone.address.phone)))))
      } catch {
        case _: BaseException => Ok(views.html.component.master.addZone(views.companion.master.AddZone.form))
      }
  }

  def addZone(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddZone.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.addZone(formWithErrors))
        },
        addZoneData => {
          try {
            val id = masterZones.Service.insertOrUpdate(accountID = loginState.username, name = addZoneData.name, currency = addZoneData.currency, address = Address(addressLine1 = addZoneData.address.addressLine1, addressLine2 = addZoneData.address.addressLine2, landmark = addZoneData.address.landmark, city = addZoneData.address.city, country = addZoneData.address.country, zipCode = addZoneData.address.zipCode, phone = addZoneData.address.phone))
            PartialContent(views.html.component.master.userUploadOrUpdateZoneKYC(masterZoneKYCs.Service.getAllDocuments(id)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def userUploadOrUpdateZoneKYCView(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.userUploadOrUpdateZoneKYC(masterZoneKYCs.Service.getAllDocuments(masterZones.Service.getID(loginState.username))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def reviewAddZoneCompletionForm(): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val zone = masterZones.Service.getByAccountID(loginState.username)
        Ok(views.html.component.master.reviewTraderCompletion(views.companion.master.TraderCompletion.form, trader = trader, organization = masterOrganizations.Service.get(trader.organizationID), zone = masterZones.Service.get(trader.zoneID), traderKYCs = masterTraderKYCs.Service.getAllDocuments(trader.id)))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
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
              request = transactionsAddZone.Request(transactionsAddZone.BaseReq(from = loginState.address, gas = verifyZoneData.gas.toString), to = zoneAccountAddress, zoneID = verifyZoneData.zoneID, password = verifyZoneData.password, mode = transactionMode),
              action = transactionsAddZone.Service.post,
              onSuccess = blockchainTransactionAddZones.Utility.onSuccess,
              onFailure = blockchainTransactionAddZones.Utility.onFailure,
              updateTransactionHash = blockchainTransactionAddZones.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ZONE_VERIFIED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
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
        pushNotification.send(accountID, constants.Notification.PUSH_NOTIFICATION_SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        withUsernameToken.Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => InternalServerError(Messages(baseException.failure.message))
      }
  }

  def rejectKycDocument(accountID: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterZoneKYCs.Service.reject(id = accountID, documentType = documentType)
        pushNotification.send(accountID, constants.Notification.PUSH_NOTIFICATION_FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
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
            masterZones.Service.rejectZone(rejectVerifyZoneRequestData.zoneID)
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
        withUsernameToken.Ok(views.html.component.master.viewZonesInGenesis(masterZones.Service.getAllVerified))
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
          transactionsAddZone.Service.post(transactionsAddZone.Request(transactionsAddZone.BaseReq(from = addZoneData.from, gas = addZoneData.gas.toString), to = addZoneData.to, zoneID = addZoneData.zoneID, password = addZoneData.password, mode = transactionMode))
          Ok(views.html.index(successes = Seq(constants.Response.ZONE_ADDED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
