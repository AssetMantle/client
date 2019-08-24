package controllers

import controllers.actions.WithTraderLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class ConfirmBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, transactionsConfirmBuyerBid: transactions.ConfirmBuyerBid, blockchainTransactionConfirmBuyerBids: blockchainTransaction.ConfirmBuyerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  def confirmBuyerBidForm(sellerAddress: String, pegHash: String, bid: Int): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.confirmBuyerBid(views.companion.master.ConfirmBuyerBid.form, sellerAddress, pegHash, bid))
  }

  def confirmBuyerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmBuyerBid.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.confirmBuyerBid(formWithErrors, formWithErrors.data(constants.Form.SELLER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH), formWithErrors.data(constants.Form.BID).toInt))
        },
        confirmBuyerBidData => {
          try {
            transaction.process[blockchainTransaction.ConfirmBuyerBid, transactionsConfirmBuyerBid.Request](
              entity = blockchainTransaction.ConfirmBuyerBid(from = loginState.address, to = confirmBuyerBidData.sellerAddress, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, buyerContractHash = confirmBuyerBidData.buyerContractHash, status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
              blockchainTransactionCreate = blockchainTransactionConfirmBuyerBids.Service.create,
              request = transactionsConfirmBuyerBid.Request(transactionsConfirmBuyerBid.BaseRequest(from = loginState.address), to = confirmBuyerBidData.sellerAddress, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid.toString, time = confirmBuyerBidData.time.toString, pegHash = confirmBuyerBidData.pegHash, buyerContractHash = confirmBuyerBidData.buyerContractHash, mode = transactionMode),
              action = transactionsConfirmBuyerBid.Service.post,
              onSuccess = blockchainTransactionConfirmBuyerBids.Utility.onSuccess,
              onFailure = blockchainTransactionConfirmBuyerBids.Utility.onFailure,
              updateTransactionHash = blockchainTransactionConfirmBuyerBids.Service.updateTransactionHash
            )
            Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CONFIRMED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
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
          transactionsConfirmBuyerBid.Service.post(transactionsConfirmBuyerBid.Request(transactionsConfirmBuyerBid.BaseRequest(from = confirmBuyerBidData.from), to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid.toString, time = confirmBuyerBidData.time.toString, pegHash = confirmBuyerBidData.pegHash, buyerContractHash = confirmBuyerBidData.buyerContractHash, mode = confirmBuyerBidData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CONFIRMED)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
