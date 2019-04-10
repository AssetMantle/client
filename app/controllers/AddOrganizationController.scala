package controllers

import controllers.actions.{WithUserLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.{blockchain, blockchainTransaction, master}
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.ExecutionContext
import scala.util.Random

class AddOrganizationController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionsAddOrganization: transactions.AddOrganization, blockchainOrganizations: blockchain.Organizations, masterZones: master.Zones, blockchainTransactionAddOrganizations: blockchainTransaction.AddOrganizations, masterOrganizations: master.Organizations, masterAccounts: master.Accounts, withUserLoginAction: WithUserLoginAction, withZoneLoginAction: WithZoneLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def addOrganizationForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form))
  }

  def addOrganization: Action[AnyContent] = withUserLoginAction { implicit request =>
    views.companion.master.AddOrganization.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.addOrganization(formWithErrors))
      },
      addOrganizationData => {
        try {
          if(masterZones.Service.getStatus(addOrganizationData.zoneID) == Option(true)){
            Ok(views.html.index(success = constants.Success.ADD_ORGANIZATION + ":" + masterOrganizations.Service.addOrganization(zoneID = addOrganizationData.zoneID, accountID = request.session.get(constants.Security.USERNAME).get, name = addOrganizationData.name, address = addOrganizationData.address, phone = addOrganizationData.phone, email = addOrganizationData.email)))
          } else {
            Ok(views.html.index(failure = Messages(constants.Error.UNVERIFIED_ZONE)))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      }
    )
  }

  def verifyOrganizationForm(organizationID: String, zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.verifyOrganization(views.companion.master.VerifyOrganization.form, organizationID, zoneID))
  }

  def verifyOrganization: Action[AnyContent] = withZoneLoginAction { implicit request =>
    views.companion.master.VerifyOrganization.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.verifyOrganization(formWithErrors, formWithErrors.data(constants.Forms.ORGANIZATION_ID), formWithErrors.data(constants.Forms.ZONE_ID)))
      },
      verifyOrganizationData => {
        try {
          //TODO verify zoneID status
            if (kafkaEnabled) {
              val toAddress = masterAccounts.Service.getAddress(masterOrganizations.Service.getAccountId(verifyOrganizationData.organizationID))
              val response = transactionsAddOrganization.Service.kafkaPost(transactionsAddOrganization.Request(from = request.session.get(constants.Security.USERNAME).get, to = toAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, password = verifyOrganizationData.password))
              blockchainTransactionAddOrganizations.Service.addOrganizationKafka(from = request.session.get(constants.Security.USERNAME).get, to = toAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, null, null, ticketID = response.ticketID, null)
              Ok(views.html.index(success = Messages(constants.Success.VERIFY_ORGANIZATION) + verifyOrganizationData.organizationID + response.ticketID))
            } else {
              val organizationAccountID = masterOrganizations.Service.getAccountId(verifyOrganizationData.organizationID)
              val organizationAccountAddress = masterAccounts.Service.getAddress(organizationAccountID)
              val response = transactionsAddOrganization.Service.post(transactionsAddOrganization.Request(from = request.session.get(constants.Security.USERNAME).get, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, password = verifyOrganizationData.password))
              blockchainTransactionAddOrganizations.Service.addOrganization(from = request.session.get(constants.Security.USERNAME).get, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
              blockchainOrganizations.Service.addOrganization(verifyOrganizationData.organizationID, organizationAccountAddress)
              masterOrganizations.Service.updateStatus(verifyOrganizationData.organizationID, true)
              masterAccounts.Service.updateUserType(organizationAccountID, constants.User.ORGANIZATION)
              Ok(views.html.index(success = Messages(constants.Success.VERIFY_ORGANIZATION) + verifyOrganizationData.organizationID + response.TxHash))
            }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = Messages(blockChainException.message)))
        }
      }
    )
  }

  def rejectVerifyOrganizationRequestForm(organizationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectVerifyOrganizationRequest(views.companion.master.RejectVerifyOrganizationRequest.form, organizationID))
  }

  def rejectVerifyOrganizationRequest: Action[AnyContent] = withZoneLoginAction { implicit request =>
    views.companion.master.RejectVerifyOrganizationRequest.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.rejectVerifyOrganizationRequest(formWithErrors, formWithErrors.data(constants.Forms.ORGANIZATION_ID)))
      },
      rejectVerifyOrganizationRequestData => {
        try {
          masterOrganizations.Service.updateStatus(rejectVerifyOrganizationRequestData.organizationID, false)
          Ok(views.html.index(success = Messages(constants.Success.VERIFY_ORGANIZATION_REQUEST_REJECTED)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      }
    )
  }

  def viewPendingVerifyOrganizationRequests: Action[AnyContent] = withZoneLoginAction { implicit request =>
    try {
      Ok(views.html.component.master.viewPendingVerifyOrgnizationRequests(masterOrganizations.Service.getVerifyOrganizationRequests(masterZones.Service.getZoneId(request.session.get(constants.Security.USERNAME).get))))
    }
    catch {
      case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
    }
  }

  def blockchainAddOrganizationForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addOrganization(views.companion.blockchain.AddOrganization.form))
  }

  def blockchainAddOrganization: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.AddOrganization.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.addOrganization(formWithErrors))
      },
      addOrganizationData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsAddOrganization.Service.kafkaPost(transactionsAddOrganization.Request(from = addOrganizationData.from, to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, password = addOrganizationData.password))
            blockchainTransactionAddOrganizations.Service.addOrganizationKafka(from = addOrganizationData.from, to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsAddOrganization.Service.post(transactionsAddOrganization.Request(from = addOrganizationData.from, to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, password = addOrganizationData.password))
            blockchainTransactionAddOrganizations.Service.addOrganization(from = addOrganizationData.from, to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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