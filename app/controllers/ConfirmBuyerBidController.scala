package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.{I18nSupport, Messages}
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
            if(!kafkaEnabled){
              Future{
                try {
                  blockchainTransactionConfirmBuyerBids.Utility.onSuccess(ticketID, transactionsConfirmBuyerBid.Service.post(transactionsConfirmBuyerBid.Request(from = username, to = confirmBuyerBidData.sellerAddress, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                    blockchainTransactionConfirmBuyerBids.Utility.onFailure(ticketID, baseException.message)
                  case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                    blockchainTransactionConfirmBuyerBids.Utility.onFailure(ticketID, blockChainException.message)
                }
              }
            }
            Ok(views.html.index(success = ticketID))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
            case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))

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
            val response = transactionsConfirmBuyerBid.Service.kafkaPost(transactionsConfirmBuyerBid.Request(from = confirmBuyerBidData.from, to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas))
            blockchainTransactionConfirmBuyerBids.Service.addConfirmBuyerBid(from = confirmBuyerBidData.from, to = confirmBuyerBidData.to, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsConfirmBuyerBid.Service.post(transactionsConfirmBuyerBid.Request(from = confirmBuyerBidData.from, to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas))
            blockchainTransactionConfirmBuyerBids.Service.addConfirmBuyerBid(from = confirmBuyerBidData.from, to = confirmBuyerBidData.to, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
            Ok(views.html.index(success = response.TxHash))
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
