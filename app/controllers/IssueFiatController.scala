package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.util.Random

class IssueFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, withZoneLoginAction: WithZoneLoginAction, masterTransactionIssueFiatRequests: masterTransaction.IssueFiatRequests, blockchainAclAccounts: blockchain.ACLAccounts, masterZones: master.Zones, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, masterAccounts: master.Accounts, blockchainFiats: models.blockchain.Fiats, blockchainOwners: models.blockchain.Owners, transactionsIssueFiat: transactions.IssueFiat, blockchainTransactionIssueFiats: blockchainTransaction.IssueFiats)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private implicit val logger: Logger = Logger(this.getClass)

  def issueFiatRequestForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueFiatRequest(views.companion.master.IssueFiatRequest.form))
  }

  def issueFiatRequest: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.IssueFiatRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.issueFiatRequest(formWithErrors))
        },
        issueFiatRequestData => {
          try {
            masterTransactionIssueFiatRequests.Service.addIssueFiatRequest(accountID = username, transactionID = issueFiatRequestData.transactionID, transactionAmount = issueFiatRequestData.transactionAmount)
            Ok(views.html.index(success = constants.Success.ISSUE_FIAT_REQUEST))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          }
        }
      )
  }

  def viewPendingIssueFiatRequests: Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      try {
        Ok(views.html.component.master.viewPendingIssueFiatRequests(masterTransactionIssueFiatRequests.Service.getPendingIssueFiatRequests(masterAccounts.Service.getIDsForAddresses(blockchainAclAccounts.Service.getAddressesUnderZone(masterZones.Service.getZoneId(username))))))
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
      }
  }

  def rejectIssueFiatRequestForm(requestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectIssueFiatRequest(views.companion.master.RejectIssueFiatRequest.form, requestID))
  }

  def rejectIssueFiatRequest: Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.RejectIssueFiatRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.rejectIssueFiatRequest(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID)))
        },
        rejectIssueFiatRequestData => {
          try {
            masterTransactionIssueFiatRequests.Service.updateStatus(rejectIssueFiatRequestData.requestID, false)
            Ok(views.html.index(success = Messages(constants.Success.ISSUE_FIAT_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          }
        }
      )
  }

  def issueFiatForm(requestID: String, accountID: String, transactionID: String, transactionAmount: Int): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueFiat(views.companion.master.IssueFiat.form, requestID, accountID, transactionID, transactionAmount))
  }

  def issueFiat: Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.IssueFiat.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.issueFiat(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID), formWithErrors.data(constants.Form.ACCOUNT_ID), formWithErrors.data(constants.Form.TRANSACTION_ID), formWithErrors.data(constants.Form.TRANSACTION_AMOUNT).toInt))
        },
        issueFiatData => {
          try {
            if (masterTransactionIssueFiatRequests.Service.getStatus(issueFiatData.requestID).isEmpty) {
              if (kafkaEnabled) {
                val toAddress = masterAccounts.Service.getAddress(issueFiatData.accountID)
                val response = transactionsIssueFiat.Service.kafkaPost(transactionsIssueFiat.Request(from = username, to = toAddress, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas))
                blockchainTransactionIssueFiats.Service.addIssueFiatKafka(from = username, to = toAddress, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas, null, null, ticketID = response.ticketID, null)
                Ok(views.html.index(success = response.ticketID))
              } else {
                val toAddress = masterAccounts.Service.getAddress(issueFiatData.accountID)
                val zoneAddress = masterAccounts.Service.getAddress(username)
                val response = transactionsIssueFiat.Service.post(transactionsIssueFiat.Request(from = username, to = toAddress, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas))
                blockchainTransactionIssueFiats.Service.addIssueFiat(from = username, to = toAddress, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
                masterTransactionIssueFiatRequests.Service.updateStatusAndGas(issueFiatData.requestID, true, issueFiatData.gas)
                blockchainAccounts.Service.updateSequence(toAddress, blockchainAccounts.Service.getSequence(toAddress) + 1)
                blockchainAccounts.Service.updateSequence(zoneAddress, blockchainAccounts.Service.getSequence(zoneAddress) + 1)
                for (tag <- response.Tags) {
                  if (tag.Key == constants.Response.KEY_FIAT) {
                    blockchainFiats.Service.addFiat(pegHash = tag.Value, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, redeemedAmount = 0)
                    blockchainOwners.Service.addOwner(pegHash = tag.Value, ownerAddress = toAddress, amount = issueFiatData.transactionAmount)
                  }
                }
                Ok(views.html.index(success = response.TxHash))
              }
            } else {
              Ok(views.html.index(failure = Messages(constants.Error.REQUEST_ALREADY_APPROVED_OR_REJECTED)))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
            case blockChainException: BlockChainException =>
              masterTransactionIssueFiatRequests.Service.updateComment(issueFiatData.requestID, blockChainException.message)
              Ok(views.html.index(failure = blockChainException.message))
          }
        }
      )
  }

  def blockchainIssueFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.issueFiat(views.companion.blockchain.IssueFiat.form))
  }

  def blockchainIssueFiat: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.IssueFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.issueFiat(formWithErrors))
      },
      issueFiatData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsIssueFiat.Service.kafkaPost(transactionsIssueFiat.Request(from = issueFiatData.from, to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas))
            blockchainTransactionIssueFiats.Service.addIssueFiatKafka(from = issueFiatData.from, to = issueFiatData.to, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsIssueFiat.Service.post(transactionsIssueFiat.Request(from = issueFiatData.from, to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas))
            blockchainTransactionIssueFiats.Service.addIssueFiat(from = issueFiatData.from, to = issueFiatData.to, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
