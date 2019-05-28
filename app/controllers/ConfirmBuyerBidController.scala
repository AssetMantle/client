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
class ConfirmBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsConfirmBuyerBid: transactions.ConfirmBuyerBid, blockchainTransactionConfirmBuyerBids: blockchainTransaction.ConfirmBuyerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private implicit val logger: Logger = Logger(this.getClass)

  def confirmBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.confirmBuyerBid(views.companion.master.ConfirmBuyerBid.form))
  }

  def confirmBuyerBid: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.ConfirmBuyerBid.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.confirmBuyerBid(formWithErrors))
        },
        confirmBuyerBidData => {
          try {
            val ticketID: String = if (kafkaEnabled) transactionsConfirmBuyerBid.Service.kafkaPost(transactionsConfirmBuyerBid.Request(from = username, to = confirmBuyerBidData.sellerAddress, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionConfirmBuyerBids.Service.addConfirmBuyerBid(from = username, to = confirmBuyerBidData.sellerAddress, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas, null, null, ticketID = ticketID, null)
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionConfirmBuyerBids.Utility.onSuccess(ticketID, transactionsConfirmBuyerBid.Service.post(transactionsConfirmBuyerBid.Request(from = username, to = confirmBuyerBidData.sellerAddress, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Response.BASE_EXCEPTION.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionConfirmBuyerBids.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
            }
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
          if (kafkaEnabled) {
            transactionsConfirmBuyerBid.Service.kafkaPost(transactionsConfirmBuyerBid.Request(from = confirmBuyerBidData.from, to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas))
          } else {
            transactionsConfirmBuyerBid.Service.post(transactionsConfirmBuyerBid.Request(from = confirmBuyerBidData.from, to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas))
          }
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
