package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction
import models.{blockchain, master}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.util.Random

class ChangeSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainNegotiations: blockchain.Negotiations, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsChangeSellerBid: transactions.ChangeSellerBid, blockchainTransactionChangeSellerBids: blockchainTransaction.ChangeSellerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

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
            if (kafkaEnabled) {
              val response = transactionsChangeSellerBid.Service.kafkaPost(transactionsChangeSellerBid.Request(from = username, to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas))
              blockchainTransactionChangeSellerBids.Service.addChangeSellerBidKafka(from = username, to = changeSellerBidData.to, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, null, null, ticketID = response.ticketID, null)
              Ok(views.html.index(success = response.ticketID))
            } else {
              val response = transactionsChangeSellerBid.Service.post(transactionsChangeSellerBid.Request(from = username, to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas))
              val fromAddress = masterAccounts.Service.getAddress(username)
              blockchainTransactionChangeSellerBids.Service.addChangeSellerBid(from = username, to = changeSellerBidData.to, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
              blockchainAccounts.Service.updateSequence(fromAddress, blockchainAccounts.Service.getSequence(fromAddress) + 1)
              //TODO: InsertOrUpdate Negotiation Table
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
            val response = transactionsChangeSellerBid.Service.kafkaPost(transactionsChangeSellerBid.Request(from = changeSellerBidData.from, to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas))
            blockchainTransactionChangeSellerBids.Service.addChangeSellerBidKafka(from = changeSellerBidData.from, to = changeSellerBidData.to, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsChangeSellerBid.Service.post(transactionsChangeSellerBid.Request(from = changeSellerBidData.from, to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas))
            blockchainTransactionChangeSellerBids.Service.addChangeSellerBid(from = changeSellerBidData.from, to = changeSellerBidData.to, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
