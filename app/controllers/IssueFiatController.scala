package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class IssueFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, withZoneLoginAction: WithZoneLoginAction, masterTransactionIssueFiatRequests: masterTransaction.IssueFiatRequests, blockchainAclAccounts: blockchain.ACLAccounts, masterZones: master.Zones, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, masterAccounts: master.Accounts, blockchainFiats: models.blockchain.Fiats, transactionsIssueFiat: transactions.IssueFiat, blockchainTransactionIssueFiats: blockchainTransaction.IssueFiats, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  def issueFiatRequestForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueFiatRequest(views.companion.master.IssueFiatRequest.form))
  }

  def issueFiatRequest: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueFiatRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.issueFiatRequest(formWithErrors))
        },
        issueFiatRequestData => {
          try {
            masterTransactionIssueFiatRequests.Service.create(accountID = loginState.username, transactionID = issueFiatRequestData.transactionID, transactionAmount = issueFiatRequestData.transactionAmount)
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ISSUE_FIAT_REQUEST_SENT)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def viewPendingIssueFiatRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.viewPendingIssueFiatRequests(masterTransactionIssueFiatRequests.Service.getPendingIssueFiatRequests(masterAccounts.Service.getIDsForAddresses(blockchainAclAccounts.Service.getAddressesUnderZone(masterZones.Service.getZoneId(loginState.username))))))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def rejectIssueFiatRequestForm(requestID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.rejectIssueFiatRequest(views.companion.master.RejectIssueFiatRequest.form, requestID))
  }

  def rejectIssueFiatRequest: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectIssueFiatRequest.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.rejectIssueFiatRequest(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID)))
        },
        rejectIssueFiatRequestData => {
          try {
            masterTransactionIssueFiatRequests.Service.reject(id = rejectIssueFiatRequestData.requestID, comment = rejectIssueFiatRequestData.comment)
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ISSUE_FIAT_REQUEST_REJECTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def issueFiatForm(requestID: String, accountID: String, transactionID: String, transactionAmount: Int): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueFiat(views.companion.master.IssueFiat.form, requestID, accountID, transactionID, transactionAmount))
  }

  def issueFiat: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueFiat.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.issueFiat(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID), formWithErrors.data(constants.Form.ACCOUNT_ID), formWithErrors.data(constants.Form.TRANSACTION_ID), formWithErrors.data(constants.Form.TRANSACTION_AMOUNT).toInt))
        },
        issueFiatData => {
          try {
            if (masterTransactionIssueFiatRequests.Service.getStatus(issueFiatData.requestID).isEmpty) {
              val toAddress = masterAccounts.Service.getAddress(issueFiatData.accountID)
              val ticketID = transaction.process[blockchainTransaction.IssueFiat, transactionsIssueFiat.Request](
                entity = blockchainTransaction.IssueFiat(from = loginState.address, to = toAddress, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount, gas = issueFiatData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionIssueFiats.Service.create,
                request = transactionsIssueFiat.Request(transactionsIssueFiat.BaseRequest(from = loginState.address, gas = issueFiatData.gas.toString), to = toAddress, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount.toString, mode = transactionMode),
                action = transactionsIssueFiat.Service.post,
                onSuccess = blockchainTransactionIssueFiats.Utility.onSuccess,
                onFailure = blockchainTransactionIssueFiats.Utility.onFailure,
                updateTransactionHash = blockchainTransactionIssueFiats.Service.updateTransactionHash
              )
              masterTransactionIssueFiatRequests.Service.accept(requestID = issueFiatData.requestID, ticketID = ticketID, gas = issueFiatData.gas)
              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.FIAT_ISSUED)))
            } else {
              Unauthorized(views.html.index(failures = Seq(constants.Response.REQUEST_ALREADY_APPROVED_OR_REJECTED)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
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
          transactionsIssueFiat.Service.post(transactionsIssueFiat.Request(transactionsIssueFiat.BaseRequest(from = issueFiatData.from, gas = issueFiatData.gas.toString), to = issueFiatData.to, password = issueFiatData.password, transactionID = issueFiatData.transactionID, transactionAmount = issueFiatData.transactionAmount.toString, mode = issueFiatData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.FIAT_ISSUED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
