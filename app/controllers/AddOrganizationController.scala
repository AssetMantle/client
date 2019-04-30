package controllers

import controllers.actions.{WithUserLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.util.Random

class AddOrganizationController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainAccounts: blockchain.Accounts, transactionsAddOrganization: transactions.AddOrganization, blockchainOrganizations: blockchain.Organizations, masterZones: master.Zones, blockchainTransactionAddOrganizations: blockchainTransaction.AddOrganizations, masterOrganizations: master.Organizations, masterAccounts: master.Accounts, withUserLoginAction: WithUserLoginAction, withZoneLoginAction: WithZoneLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private implicit val logger: Logger = Logger(this.getClass)

  def addOrganizationForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form))
  }

  def addOrganization: Action[AnyContent] = withUserLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.AddOrganization.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.addOrganization(formWithErrors))
        },
        addOrganizationData => {
          try {
            if (masterZones.Service.getStatus(addOrganizationData.zoneID) == Option(true)) {
              Ok(views.html.index(success = constants.Success.ADD_ORGANIZATION + ":" + masterOrganizations.Service.addOrganization(zoneID = addOrganizationData.zoneID, accountID = username, name = addOrganizationData.name, address = addOrganizationData.address, phone = addOrganizationData.phone, email = addOrganizationData.email)))
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

  def verifyOrganization: Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.VerifyOrganization.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.verifyOrganization(formWithErrors, formWithErrors.data(constants.Form.ORGANIZATION_ID), formWithErrors.data(constants.Form.ZONE_ID)))
        },
        verifyOrganizationData => {
          try {
            if (kafkaEnabled) {
              val organizationAccountAddress = masterAccounts.Service.getAddress(masterOrganizations.Service.getAccountId(verifyOrganizationData.organizationID))
              val response = transactionsAddOrganization.Service.kafkaPost(transactionsAddOrganization.Request(from = username, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, password = verifyOrganizationData.password))
              blockchainTransactionAddOrganizations.Service.addOrganizationKafka(from = username, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, null, null, ticketID = response.ticketID, null)
              Ok(views.html.index(success = Messages(constants.Success.VERIFY_ORGANIZATION) + verifyOrganizationData.organizationID + response.ticketID))
            } else {
              val organizationAccountID = masterOrganizations.Service.getAccountId(verifyOrganizationData.organizationID)
              val organizationAccountAddress = masterAccounts.Service.getAddress(organizationAccountID)
              val response = transactionsAddOrganization.Service.post(transactionsAddOrganization.Request(from = username, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, password = verifyOrganizationData.password))
              val fromAddress = masterAccounts.Service.getAddress(username)
              blockchainTransactionAddOrganizations.Service.addOrganization(from = username, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
              blockchainOrganizations.Service.addOrganization(verifyOrganizationData.organizationID, organizationAccountAddress)
              masterOrganizations.Service.updateStatus(verifyOrganizationData.organizationID, true)
              masterAccounts.Service.updateUserType(organizationAccountID, constants.User.ORGANIZATION)
              blockchainAccounts.Service.updateSequence(fromAddress, blockchainAccounts.Service.getSequence(fromAddress) + 1)
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

  def rejectVerifyOrganizationRequest: Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.RejectVerifyOrganizationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.rejectVerifyOrganizationRequest(formWithErrors, formWithErrors.data(constants.Form.ORGANIZATION_ID)))
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

  def viewPendingVerifyOrganizationRequests: Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewPendingVerifyOrgnizationRequests(masterOrganizations.Service.getVerifyOrganizationRequests(masterZones.Service.getZoneId(username))))
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