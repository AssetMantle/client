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
class ConfirmSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsConfirmSellerBid: transactions.ConfirmSellerBid, blockchainTransactionConfirmSellerBids: blockchainTransaction.ConfirmSellerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private implicit val logger: Logger = Logger(this.getClass)

  def confirmSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.confirmSellerBid(views.companion.master.ConfirmSellerBid.form))
  }

  def confirmSellerBid: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.ConfirmSellerBid.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.confirmSellerBid(formWithErrors))
        },
        confirmSellerBidData => {
          try {
            val ticketID: String = if (kafkaEnabled) transactionsConfirmSellerBid.Service.kafkaPost(transactionsConfirmSellerBid.Request(from = username, to = confirmSellerBidData.buyerAddress, password = confirmSellerBidData.password, bid = confirmSellerBidData.bid, time = confirmSellerBidData.time, pegHash = confirmSellerBidData.pegHash, gas = confirmSellerBidData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionConfirmSellerBids.Service.addConfirmSellerBid(from = username, to = confirmSellerBidData.buyerAddress, bid = confirmSellerBidData.bid, time = confirmSellerBidData.time, pegHash = confirmSellerBidData.pegHash, gas = confirmSellerBidData.gas, null, null, ticketID = ticketID, null)
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionConfirmSellerBids.Utility.onSuccess(ticketID, transactionsConfirmSellerBid.Service.post(transactionsConfirmSellerBid.Request(from = username, to = confirmSellerBidData.buyerAddress, password = confirmSellerBidData.password, bid = confirmSellerBidData.bid, time = confirmSellerBidData.time, pegHash = confirmSellerBidData.pegHash, gas = confirmSellerBidData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Response.BASE_EXCEPTION.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionConfirmSellerBids.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
            }
            Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CONFIRMED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

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
          if (kafkaEnabled) {
            transactionsConfirmSellerBid.Service.kafkaPost(transactionsConfirmSellerBid.Request(from = confirmSellerBidData.from, to = confirmSellerBidData.to, password = confirmSellerBidData.password, bid = confirmSellerBidData.bid, time = confirmSellerBidData.time, pegHash = confirmSellerBidData.pegHash, gas = confirmSellerBidData.gas))
          } else {
            transactionsConfirmSellerBid.Service.post(transactionsConfirmSellerBid.Request(from = confirmSellerBidData.from, to = confirmSellerBidData.to, password = confirmSellerBidData.password, bid = confirmSellerBidData.bid, time = confirmSellerBidData.time, pegHash = confirmSellerBidData.pegHash, gas = confirmSellerBidData.gas))
          }
          Ok(views.html.index(successes = Seq(constants.Response.SELLER_BID_CONFIRMED)))

        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

        }
      }
    )
  }
}
