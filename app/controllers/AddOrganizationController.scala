package controllers

import controllers.actions.{WithUserLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.PushNotification

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class AddOrganizationController @Inject()(messagesControllerComponents: MessagesControllerComponents, pushNotification: PushNotification, blockchainAccounts: blockchain.Accounts, masterOrganizationKYCs: master.OrganizationKYCs, transactionsAddOrganization: transactions.AddOrganization, blockchainOrganizations: blockchain.Organizations, masterZones: master.Zones, blockchainTransactionAddOrganizations: blockchainTransaction.AddOrganizations, masterOrganizations: master.Organizations, masterAccounts: master.Accounts, withUserLoginAction: WithUserLoginAction, withZoneLoginAction: WithZoneLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private implicit val logger: Logger = Logger(this.getClass)

  def addOrganizationForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.addOrganization(views.companion.master.AddOrganization.form))
  }

  def addOrganization: Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddOrganization.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.addOrganization(formWithErrors))
        },
        addOrganizationData => {

          try {
            if (masterZones.Service.getStatus(addOrganizationData.zoneID) == Option(true)) {
              masterOrganizations.Service.create(zoneID = addOrganizationData.zoneID, accountID = loginState.username, name = addOrganizationData.name, address = addOrganizationData.address, phone = addOrganizationData.phone, email = addOrganizationData.email)
              Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_ADDED)))
            } else {
              Ok(views.html.index(failures = Seq(constants.Response.UNVERIFIED_ZONE)))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def verifyOrganizationForm(organizationID: String, zoneID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.verifyOrganization(views.companion.master.VerifyOrganization.form, organizationID, zoneID))
  }

  def verifyOrganization: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyOrganization.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.verifyOrganization(formWithErrors, formWithErrors.data(constants.Form.ORGANIZATION_ID), formWithErrors.data(constants.Form.ZONE_ID)))
        },
        verifyOrganizationData => {

          try {
            val organizationAccountAddress = masterAccounts.Service.getAddress(masterOrganizations.Service.getAccountId(verifyOrganizationData.organizationID))
            val ticketID: String = if (kafkaEnabled) transactionsAddOrganization.Service.kafkaPost(transactionsAddOrganization.Request(from = loginState.username, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, password = verifyOrganizationData.password)).ticketID else Random.nextString(32)
            blockchainTransactionAddOrganizations.Service.create(from = loginState.username, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, null, null, ticketID = ticketID, null)
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionAddOrganizations.Utility.onSuccess(ticketID, transactionsAddOrganization.Service.post(transactionsAddOrganization.Request(from = loginState.username, to = organizationAccountAddress, organizationID = verifyOrganizationData.organizationID, zoneID = verifyOrganizationData.zoneID, password = verifyOrganizationData.password)))
                } catch {
                  case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionAddOrganizations.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
            }
            Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_VERIFIED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def viewKycDocuments(accountID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewVerificationOrganizationKycDouments(masterOrganizationKYCs.Service.getAllDocuments(accountID)))
      } catch {
        case baseException: BaseException => BadRequest(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def verifyKycDocument(accountID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterOrganizationKYCs.Service.verify(id = accountID, documentType = documentType)
        pushNotification.sendNotification(accountID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
        Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => BadRequest(Messages(baseException.failure.message))
      }
  }

  def rejectKycDocument(accountID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterOrganizationKYCs.Service.reject(id = accountID, documentType = documentType)
        pushNotification.sendNotification(accountID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
        Ok(Messages(constants.Response.SUCCESS.message))
      } catch {
        case baseException: BaseException => BadRequest(Messages(baseException.failure.message))
      }
  }

  def rejectVerifyOrganizationRequestForm(organizationID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectVerifyOrganizationRequest(views.companion.master.RejectVerifyOrganizationRequest.form, organizationID))
  }

  def rejectVerifyOrganizationRequest: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectVerifyOrganizationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.rejectVerifyOrganizationRequest(formWithErrors, formWithErrors.data(constants.Form.ORGANIZATION_ID)))
        },
        rejectVerifyOrganizationRequestData => {

          try {
            masterOrganizations.Service.updateStatus(rejectVerifyOrganizationRequestData.organizationID, false)
            masterOrganizationKYCs.Service.rejectAll(masterOrganizations.Service.getAccountId(rejectVerifyOrganizationRequestData.organizationID))
            Ok(views.html.index(successes = Seq(constants.Response.VERIFY_ORGANIZATION_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewPendingVerifyOrganizationRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewPendingVerifyOrgnizationRequests(masterOrganizations.Service.getVerifyOrganizationRequests(masterZones.Service.getZoneId(loginState.username))))
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
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
            transactionsAddOrganization.Service.kafkaPost(transactionsAddOrganization.Request(from = addOrganizationData.from, to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, password = addOrganizationData.password))
          } else {
            transactionsAddOrganization.Service.post(transactionsAddOrganization.Request(from = addOrganizationData.from, to = addOrganizationData.to, organizationID = addOrganizationData.organizationID, zoneID = addOrganizationData.zoneID, password = addOrganizationData.password))
          }
          Ok(views.html.index(successes = Seq(constants.Response.ORGANIZATION_ADDED)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}