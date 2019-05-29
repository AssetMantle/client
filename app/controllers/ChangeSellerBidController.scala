package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class ChangeSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainNegotiations: blockchain.Negotiations, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsChangeSellerBid: transactions.ChangeSellerBid, blockchainTransactionChangeSellerBids: blockchainTransaction.ChangeSellerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private implicit val logger: Logger = Logger(this.getClass)

  def changeSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.changeSellerBid(views.companion.master.ChangeSellerBid.form))
  }

  def changeSellerBid: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.ChangeSellerBid.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.changeSellerBid(formWithErrors))
        },
        changeSellerBidData => {
          try {
            val ticketID: String = if (kafkaEnabled) transactionsChangeSellerBid.Service.kafkaPost(transactionsChangeSellerBid.Request(from = username, to = changeSellerBidData.buyerAddress, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionChangeSellerBids.Service.create(from = username, to = changeSellerBidData.buyerAddress, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, null, null, ticketID = ticketID, null)
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionChangeSellerBids.Utility.onSuccess(ticketID, transactionsChangeSellerBid.Service.post(transactionsChangeSellerBid.Request(from = username, to = changeSellerBidData.buyerAddress, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionChangeSellerBids.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
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
            transactionsChangeSellerBid.Service.kafkaPost(transactionsChangeSellerBid.Request(from = changeSellerBidData.from, to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas))
          } else {
            transactionsChangeSellerBid.Service.post(transactionsChangeSellerBid.Request(from = changeSellerBidData.from, to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas))
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
