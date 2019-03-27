package controllers

import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.AddOrganizations
import models.master.{Accounts, Organizations}
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.AddOrganization
import views.companion.{blockchain, master}

import scala.concurrent.ExecutionContext
import scala.util.Random

class AddOrganizationController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionAddOrganization: AddOrganization, addOrganizations: AddOrganizations, organizations: Organizations, accounts: Accounts, withLoginAction: WithLoginAction)(implicit exec: ExecutionContext, configuration:Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val module: String = constants.Module.CONTROLLERS_ADD_ORGANIZATION

  def addOrganizationForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.addOrganization(master.AddOrganization.form))
  }

  def addOrganization: Action[AnyContent] = withLoginAction { implicit request =>
    master.AddOrganization.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.addOrganization(formWithErrors))
      },
      addOrganizationData => {
        try {
          Ok(views.html.index(success = organizations.Service.addOrganization(request.session.get(constants.Security.USERNAME).get, addOrganizationData.name, addOrganizationData.address, addOrganizationData.phone, addOrganizationData.email)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
        }
      }
    )
  }

  def verifyOrganizationForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.verifyOrganization(master.VerifyOrganization.form))
  }

  def verifyOrganization: Action[AnyContent] = withLoginAction { implicit request =>
    master.VerifyOrganization.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.verifyOrganization(formWithErrors))
      },
      verifyOrganizationData => {
        try {
          organizations.Service.verifyOrganization(verifyOrganizationData.organizationID, true)
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionAddOrganization.Service.kafkaPost(transactionAddOrganization.Request(from = request.session.get(constants.Security.USERNAME).get, to = accounts.Service.getAccount(organizations.Service.getOrganization(verifyOrganizationData.organizationID).accountID).accountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, password =  verifyOrganizationData.password))
            Ok(views.html.index(success = Messages(module + "." + constants.Success.VERIFY_ORGANIZATION) + verifyOrganizationData.organizationID + response.ticketID))
          } else {
            val response = transactionAddOrganization.Service.post(transactionAddOrganization.Request(from = request.session.get(constants.Security.USERNAME).get, to = accounts.Service.getAccount(organizations.Service.getOrganization(verifyOrganizationData.organizationID).accountID).accountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, password =  verifyOrganizationData.password))
            Ok(views.html.index(success = Messages(module + "." + constants.Success.VERIFY_ORGANIZATION) + verifyOrganizationData.organizationID + response.TxHash))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException =>  Ok(views.html.index(failure = Messages(blockChainException.message)))
        }
      }
    )
  }


  def blockchainAddOrganizationForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.addOrganization(blockchain.AddOrganization.form))
  }

  def blockchainAddOrganization: Action[AnyContent] = Action { implicit request =>
    blockchain.AddOrganization.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.addOrganization(formWithErrors))
      },
      addOrganizationData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionAddOrganization.Service.kafkaPost(transactionAddOrganization.Request(from = addOrganizationData.from, to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, password = addOrganizationData.password))
            addOrganizations.Service.addOrganizationKafka(from = addOrganizationData.from, to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionAddOrganization.Service.post(transactionAddOrganization.Request(from = addOrganizationData.from, to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, password = addOrganizationData.password))
            addOrganizations.Service.addOrganization(from = addOrganizationData.from, to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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