package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class IssueFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, withZoneLoginAction: WithZoneLoginAction, masterTransactionIssueFiatRequests: masterTransaction.IssueFiatRequests, blockchainAclAccounts: blockchain.ACLAccounts, masterZones: master.Zones, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, masterAccounts: master.Accounts, blockchainFiats: models.blockchain.Fiats, transactionsIssueFiat: transactions.IssueFiat, blockchainTransactionIssueFiats: blockchainTransaction.IssueFiats)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
            masterTransactionIssueFiatRequests.Service.create(accountID = username, transactionID = issueFiatRequestData.transactionID, transactionAmount = issueFiatRequestData.transactionAmount)
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
            masterTransactionIssueFiatRequests.Service.reject(id = rejectIssueFiatRequestData.requestID, comment = rejectIssueFiatRequestData.comment)
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
              val toAddress = masterAccounts.Service.getAddress(issueFiatData.accountID)
              val ticketID: String = if (kafkaEnabled) transactionsIssueFiat.Service.kafkaPost(transactionsIssueFiat.Request(from = username, to = toAddress, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas)).ticketID else Random.nextString(32)
              blockchainTransactionIssueFiats.Service.create(from = username, to = toAddress, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas, null, null, ticketID = ticketID, null)
              masterTransactionIssueFiatRequests.Service.accept(issueFiatData.requestID, ticketID, issueFiatData.gas)
              if (!kafkaEnabled) {
                Future {
                  try {
                    blockchainTransactionIssueFiats.Utility.onSuccess(ticketID, transactionsIssueFiat.Service.post(transactionsIssueFiat.Request(from = username, to = toAddress, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas)))
                  } catch {
                    case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                    case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                      blockchainTransactionIssueFiats.Utility.onFailure(ticketID, blockChainException.message)
                  }
                }
              }
              Ok(views.html.index(success = ticketID))
            } else {
              Ok(views.html.index(failure = Messages(constants.Error.REQUEST_ALREADY_APPROVED_OR_REJECTED)))
            }
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
            case blockChainException: BlockChainException => masterTransactionIssueFiatRequests.Service.updateComment(issueFiatData.requestID, blockChainException.message)
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
            Ok(views.html.index(success = transactionsIssueFiat.Service.kafkaPost(transactionsIssueFiat.Request(from = issueFiatData.from, to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas)).ticketID))
          } else {
            Ok(views.html.index(success = transactionsIssueFiat.Service.post(transactionsIssueFiat.Request(from = issueFiatData.from, to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas)).TxHash))
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
