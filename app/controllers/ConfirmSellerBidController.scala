package controllers

import controllers.actions.WithTraderLoginAction
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class ConfirmSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, transactionsConfirmSellerBid: transactions.ConfirmSellerBid, blockchainTransactionConfirmSellerBids: blockchainTransaction.ConfirmSellerBids, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_CONFIRM_SELLER_BID

  def confirmSellerBidForm(buyerAddress: String, pegHash: String, bid: Int): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.confirmSellerBid(views.companion.master.ConfirmSellerBid.form, buyerAddress, pegHash, bid))
  }

  def confirmSellerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmSellerBid.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.confirmSellerBid(formWithErrors, formWithErrors.data(constants.Form.BUYER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH), formWithErrors.data(constants.Form.BID).toInt))
        },
        confirmSellerBidData => {
          try {
            transaction.process[blockchainTransaction.ConfirmSellerBid, transactionsConfirmSellerBid.Request](
              entity = blockchainTransaction.ConfirmSellerBid(from = loginState.address, to = confirmSellerBidData.buyerAddress, bid = confirmSellerBidData.bid, time = confirmSellerBidData.time, pegHash = confirmSellerBidData.pegHash, sellerContractHash = confirmSellerBidData.sellerContractHash, gas = confirmSellerBidData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionConfirmSellerBids.Service.create,
              request = transactionsConfirmSellerBid.Request(transactionsConfirmSellerBid.BaseReq(from = loginState.address, gas = confirmSellerBidData.gas.toString), to = confirmSellerBidData.buyerAddress, password = confirmSellerBidData.password, bid = confirmSellerBidData.bid.toString, time = confirmSellerBidData.time.toString, pegHash = confirmSellerBidData.pegHash, sellerContractHash = confirmSellerBidData.sellerContractHash, mode = transactionMode),
              action = transactionsConfirmSellerBid.Service.post,
              onSuccess = blockchainTransactionConfirmSellerBids.Utility.onSuccess,
              onFailure = blockchainTransactionConfirmSellerBids.Utility.onFailure,
              updateTransactionHash = blockchainTransactionConfirmSellerBids.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CONFIRMED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def blockchainConfirmSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.confirmSellerBid(views.companion.blockchain.ConfirmSellerBid.form))
  }

  def blockchainConfirmSellerBid: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.ConfirmSellerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.confirmSellerBid(formWithErrors))
      },
      confirmSellerBidData => {
        try {
          transactionsConfirmSellerBid.Service.post(transactionsConfirmSellerBid.Request(transactionsConfirmSellerBid.BaseReq(from = confirmSellerBidData.from, gas = confirmSellerBidData.gas.toString), to = confirmSellerBidData.to, password = confirmSellerBidData.password, bid = confirmSellerBidData.bid.toString, time = confirmSellerBidData.time.toString, pegHash = confirmSellerBidData.pegHash, sellerContractHash = confirmSellerBidData.sellerContractHash, mode = confirmSellerBidData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CONFIRMED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
