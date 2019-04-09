package controllers

import controllers.actions.{WithLoginAction, WithUserLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.{blockchain, blockchainTransaction, master}
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.ExecutionContext
import scala.util.Random

class AddOrganizationController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionsAddOrganization: transactions.AddOrganization, blockchainOrganizations: blockchain.Organizations, blockchainTransactionAddOrganizations: blockchainTransaction.AddOrganizations, masterOrganizations: master.Organizations, masterAccounts: master.Accounts, withUserLoginAction: WithUserLoginAction, withZoneLoginAction: WithZoneLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
          Ok(views.html.index(success = constants.Success.ADD_ORGANIZATION + ":" + masterOrganizations.Service.addOrganization(accountID = request.session.get(constants.Security.USERNAME).get, name = addOrganizationData.name, address = addOrganizationData.address, phone = addOrganizationData.phone, email = addOrganizationData.email)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      }
    )
  }

  def verifyOrganizationForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.verifyOrganization(views.companion.master.VerifyOrganization.form))
  }

  def verifyOrganization: Action[AnyContent] = withZoneLoginAction { implicit request =>
    views.companion.master.VerifyOrganization.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.verifyOrganization(formWithErrors))
      },
      verifyOrganizationData => {
        try {
          masterOrganizations.Service.verifyOrganization(verifyOrganizationData.organizationID, true)
          if (kafkaEnabled) {
            val response = transactionsAddOrganization.Service.kafkaPost(transactionsAddOrganization.Request(from = request.session.get(constants.Security.USERNAME).get, to = masterAccounts.Service.getAccount(masterOrganizations.Service.getOrganization(verifyOrganizationData.organizationID).accountID).accountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, password = verifyOrganizationData.password))
            blockchainTransactionAddOrganizations.Service.addOrganizationKafka(from = request.session.get(constants.Security.USERNAME).get, to = masterAccounts.Service.getAddress(masterOrganizations.Service.getAccountId(verifyOrganizationData.organizationID)), organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = Messages(constants.Success.VERIFY_ORGANIZATION) + verifyOrganizationData.organizationID + response.ticketID))
          } else {
            val organizationAccountID = masterOrganizations.Service.getAccountId(verifyOrganizationData.organizationID)
            val organizationAccountAddress = masterAccounts.Service.getAddress(organizationAccountID)
            val response = transactionsAddOrganization.Service.post(transactionsAddOrganization.Request(from = request.session.get(constants.Security.USERNAME).get, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, password = verifyOrganizationData.password))
            blockchainOrganizations.Service.addOrganization(verifyOrganizationData.organizationID, organizationAccountAddress)
            blockchainTransactionAddOrganizations.Service.addOrganization(from = request.session.get(constants.Security.USERNAME).get, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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