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
class ChangeBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainNegotiations: blockchain.Negotiations, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, transactionsChangeBuyerBid: transactions.ChangeBuyerBid, blockchainTransactionChangeBuyerBids: blockchainTransaction.ChangeBuyerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  def changeBuyerBidForm(sellerAddress:String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.changeBuyerBid(views.companion.master.ChangeBuyerBid.form, sellerAddress, pegHash))
  }

  def changeBuyerBid: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ChangeBuyerBid.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.changeBuyerBid(formWithErrors,formWithErrors.data(constants.Form.SELLER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
        },
        changeBuyerBidData => {
          try {
            transaction.process[blockchainTransaction.ChangeBuyerBid, transactionsChangeBuyerBid.Request](
              entity = blockchainTransaction.ChangeBuyerBid(from = loginState.address, to = changeBuyerBidData.sellerAddress, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas, status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
              blockchainTransactionCreate = blockchainTransactionChangeBuyerBids.Service.create,
              request = transactionsChangeBuyerBid.Request(transactionsChangeBuyerBid.BaseRequest(from = loginState.address), to = changeBuyerBidData.sellerAddress, password = changeBuyerBidData.password, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas, mode = transactionMode),
              kafkaAction = transactionsChangeBuyerBid.Service.kafkaPost,
              blockAction = transactionsChangeBuyerBid.Service.blockPost,
              asyncAction = transactionsChangeBuyerBid.Service.asyncPost,
              syncAction = transactionsChangeBuyerBid.Service.syncPost,
              onSuccess = blockchainTransactionChangeBuyerBids.Utility.onSuccess,
              onFailure = blockchainTransactionChangeBuyerBids.Utility.onFailure
            )
            Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CHANGED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def blockchainChangeBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.changeBuyerBid(views.companion.blockchain.ChangeBuyerBid.form))
  }

  def blockchainChangeBuyerBid: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.ChangeBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.changeBuyerBid(formWithErrors))
      },
      changeBuyerBidData => {
        try {
          if (kafkaEnabled) {
            transactionsChangeBuyerBid.Service.kafkaPost(transactionsChangeBuyerBid.Request(transactionsChangeBuyerBid.BaseRequest(from = changeBuyerBidData.from), to = changeBuyerBidData.to, password = changeBuyerBidData.password, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas, mode = transactionMode))
          } else {
            transactionsChangeBuyerBid.Service.blockPost(transactionsChangeBuyerBid.Request(transactionsChangeBuyerBid.BaseRequest(from = changeBuyerBidData.from), to = changeBuyerBidData.to, password = changeBuyerBidData.password, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas, mode = transactionMode))
          }
          Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CHANGED)))

        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
