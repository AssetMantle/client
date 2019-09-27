package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class ConfirmBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                          transaction: utilities.Transaction,
                                          blockchainAccounts: blockchain.Accounts,
                                          masterAccounts: master.Accounts,
                                          withTraderLoginAction: WithTraderLoginAction,
                                          transactionsConfirmBuyerBid: transactions.ConfirmBuyerBid,
                                          masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests,
                                          masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                          blockchainTransactionConfirmBuyerBids: blockchainTransaction.ConfirmBuyerBids,
                                          withUsernameToken: WithUsernameToken)
                                         (implicit executionContext: ExecutionContext,
                                          configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONFIRM_BUYER_BID

  def confirmBuyerBidDetailForm(sellerAddress: String, pegHash: String, bid: Int): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        masterTransactionNegotiationRequests.Service.getIDByPegHashAndBuyerAccountID(pegHash, loginState.username) match {
          case Some(id) => Ok(views.html.component.master.confirmBuyerBidDetail(views.companion.master.ConfirmBuyerBidDetail.form.fill(views.companion.master.ConfirmBuyerBidDetail.Data(Option(id), sellerAddress, bid, pegHash))))
          case None => Ok(views.html.component.master.confirmBuyerBidDetail(views.companion.master.ConfirmBuyerBidDetail.form.fill(views.companion.master.ConfirmBuyerBidDetail.Data(None, sellerAddress, bid, pegHash))))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def confirmBuyerBidDetail: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmBuyerBidDetail.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.confirmBuyerBidDetail(formWithErrors))
        },
        confirmBuyerBidData => {
          try {
            //            transaction.process[blockchainTransaction.ConfirmBuyerBid, transactionsConfirmBuyerBid.Request](
            //              entity = blockchainTransaction.ConfirmBuyerBid(from = loginState.address, to = confirmBuyerBidData.sellerAddress, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, buyerContractHash = confirmBuyerBidData.buyerContractHash, gas = confirmBuyerBidData.gas, ticketID = "", mode = transactionMode),
            //              blockchainTransactionCreate = blockchainTransactionConfirmBuyerBids.Service.create,
            //              request = transactionsConfirmBuyerBid.Request(transactionsConfirmBuyerBid.BaseReq(from = loginState.address, gas = confirmBuyerBidData.gas.toString), to = confirmBuyerBidData.sellerAddress, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid.toString, time = confirmBuyerBidData.time.toString, pegHash = confirmBuyerBidData.pegHash, buyerContractHash = confirmBuyerBidData.buyerContractHash, mode = transactionMode),
            //              action = transactionsConfirmBuyerBid.Service.post,
            //              onSuccess = blockchainTransactionConfirmBuyerBids.Utility.onSuccess,
            //              onFailure = blockchainTransactionConfirmBuyerBids.Utility.onFailure,
            //              updateTransactionHash = blockchainTransactionConfirmBuyerBids.Service.updateTransactionHash
            //            )
            PartialContent(views.html.index(successes = Seq(constants.Response.BUYER_BID_CONFIRMED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def confirmBuyerBidForm(requestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val negotiation = masterTransactionNegotiationRequests.Service.getNegotiationByID(requestID)
        if (negotiation.buyerAccountID == loginState.username) {

          Ok
          //          Ok(views.html.component.master.confirmBuyerBid(views.companion.master.ConfirmBuyerBid.form.fill(views.companion.master.ConfirmBuyerBid.Data(Option(negotiation.id), "", sellerAddress, bid, 0, pegHash, constants.FormField.GAS.minimumValue))))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def confirmBuyerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmBidTransaction.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.confirmBuyerBid(formWithErrors))
        },
        confirmBidTransaction => {
          try {
            val masterNegotiation = masterTransactionNegotiationRequests.Service.getNegotiationByID(confirmBidTransaction.requestID)
            if (masterNegotiation.buyerAccountID == loginState.username) {
              val sellerAddress = masterAccounts.Service.getAddress(masterNegotiation.sellerAccountID)
              val buyerContractHash = utilities.FileOperations.combinedHash(Seq(masterTransactionNegotiationFiles.Service.get(confirmBidTransaction.requestID, constants.File.BUYER_CONTRACT)))
              transaction
              .process[blockchainTransaction.ConfirmBuyerBid, transactionsConfirmBuyerBid.Request](
                entity = blockchainTransaction.ConfirmBuyerBid(from = loginState.address, to = sellerAddress, bid = masterNegotiation.amount, time = confirmBidTransaction.time, pegHash = masterNegotiation.pegHash, buyerContractHash = buyerContractHash, gas = confirmBidTransaction.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionConfirmBuyerBids.Service.create,
                request = transactionsConfirmBuyerBid.Request(transactionsConfirmBuyerBid.BaseReq(from = loginState.address, gas = confirmBidTransaction.gas.toString), to = sellerAddress, password = confirmBidTransaction.password, bid = masterNegotiation.amount.toString, time = confirmBidTransaction.time.toString, pegHash = masterNegotiation.pegHash, buyerContractHash = buyerContractHash, mode = transactionMode),
                action = transactionsConfirmBuyerBid.Service.post,
                onSuccess = blockchainTransactionConfirmBuyerBids.Utility.onSuccess,
                onFailure = blockchainTransactionConfirmBuyerBids.Utility.onFailure,
                updateTransactionHash = blockchainTransactionConfirmBuyerBids.Service.updateTransactionHash
              )
              PartialContent(views.html.index(successes = Seq(constants.Response.BUYER_BID_CONFIRMED)))
            } else {
              Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
            }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainConfirmBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.confirmBuyerBid(views.companion.blockchain.ConfirmBuyerBid.form))
  }

  def blockchainConfirmBuyerBid: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.ConfirmBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.confirmBuyerBid(formWithErrors))
      },
      confirmBuyerBidData => {
        try {
          transactionsConfirmBuyerBid.Service.post(transactionsConfirmBuyerBid.Request(transactionsConfirmBuyerBid.BaseReq(from = confirmBuyerBidData.from, gas = confirmBuyerBidData.gas.toString), to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid.toString, time = confirmBuyerBidData.time.toString, pegHash = confirmBuyerBidData.pegHash, buyerContractHash = confirmBuyerBidData.buyerContractHash, mode = confirmBuyerBidData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CONFIRMED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
