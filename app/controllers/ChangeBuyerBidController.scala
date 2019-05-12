package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class ChangeBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainNegotiations: blockchain.Negotiations, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsChangeBuyerBid: transactions.ChangeBuyerBid, blockchainTransactionChangeBuyerBids: blockchainTransaction.ChangeBuyerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private implicit val logger: Logger = Logger(this.getClass)

  def changeBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.changeBuyerBid(views.companion.master.ChangeBuyerBid.form))
  }

  def changeBuyerBid: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.ChangeBuyerBid.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.changeBuyerBid(formWithErrors))
        },
        changeBuyerBidData => {
          try {
            val toAddress = masterAccounts.Service.getAddress(changeBuyerBidData.accountID)
            val ticketID: String = if (kafkaEnabled) transactionsChangeBuyerBid.Service.kafkaPost(transactionsChangeBuyerBid.Request(from = username, to = toAddress, password = changeBuyerBidData.password, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionChangeBuyerBids.Service.addChangeBuyerBid(from = username, to = toAddress, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas, null, null, ticketID = ticketID, null)
            if(!kafkaEnabled){
              Future{
                try {
                  blockchainTransactionChangeBuyerBids.Utility.onSuccess(ticketID, transactionsChangeBuyerBid.Service.post(transactionsChangeBuyerBid.Request(from = username, to = toAddress, password = changeBuyerBidData.password, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                    blockchainTransactionChangeBuyerBids.Utility.onFailure(ticketID, baseException.message)
                  case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                    blockchainTransactionChangeBuyerBids.Utility.onFailure(ticketID, blockChainException.message)
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
            val response = transactionsChangeBuyerBid.Service.kafkaPost(transactionsChangeBuyerBid.Request(from = changeBuyerBidData.from, to = changeBuyerBidData.to, password = changeBuyerBidData.password, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas))
            blockchainTransactionChangeBuyerBids.Service.addChangeBuyerBid(from = changeBuyerBidData.from, to = changeBuyerBidData.to, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsChangeBuyerBid.Service.post(transactionsChangeBuyerBid.Request(from = changeBuyerBidData.from, to = changeBuyerBidData.to, password = changeBuyerBidData.password, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas))
            blockchainTransactionChangeBuyerBids.Service.addChangeBuyerBid(from = changeBuyerBidData.from, to = changeBuyerBidData.to, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
