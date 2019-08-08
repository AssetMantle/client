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
class ChangeSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainNegotiations: blockchain.Negotiations, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, transactionsChangeSellerBid: transactions.ChangeSellerBid, blockchainTransactionChangeSellerBids: blockchainTransaction.ChangeSellerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  def changeSellerBidForm(buyerAddress:String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.changeSellerBid(views.companion.master.ChangeSellerBid.form, buyerAddress,pegHash))
  }

  def changeSellerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ChangeSellerBid.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.changeSellerBid(formWithErrors, formWithErrors.data(constants.Form.BUYER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
        },
        changeSellerBidData => {
          try {
            transaction.process[blockchainTransaction.ChangeSellerBid, transactionsChangeSellerBid.Request](
              entity = blockchainTransaction.ChangeSellerBid(from = loginState.address, to = changeSellerBidData.buyerAddress, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
              blockchainTransactionCreate = blockchainTransactionChangeSellerBids.Service.create,
              request = transactionsChangeSellerBid.Request(transactionsChangeSellerBid.BaseRequest(from = loginState.address), to = changeSellerBidData.buyerAddress, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, mode = transactionMode),
              kafkaAction = transactionsChangeSellerBid.Service.kafkaPost,
              blockAction = transactionsChangeSellerBid.Service.blockPost,
              asyncAction = transactionsChangeSellerBid.Service.asyncPost,
              syncAction = transactionsChangeSellerBid.Service.syncPost,
              onSuccess = blockchainTransactionChangeSellerBids.Utility.onSuccess,
              onFailure = blockchainTransactionChangeSellerBids.Utility.onFailure
            )
            Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CHANGED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

          }
        }
      )
  }

  def blockchainChangeSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.changeSellerBid(views.companion.blockchain.ChangeSellerBid.form))
  }

  def blockchainChangeSellerBid: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.ChangeSellerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.changeSellerBid(formWithErrors))
      },
      changeSellerBidData => {
        try {
          if (kafkaEnabled) {
            transactionsChangeSellerBid.Service.kafkaPost(transactionsChangeSellerBid.Request(transactionsChangeSellerBid.BaseRequest(from = changeSellerBidData.from), to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, mode = transactionMode))
          } else {
            transactionsChangeSellerBid.Service.blockPost(transactionsChangeSellerBid.Request(transactionsChangeSellerBid.BaseRequest(from = changeSellerBidData.from), to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, mode = transactionMode))
          }
          Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CHANGED)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

        }
      }
    )
  }
}
