package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.ConfirmBuyerBids
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import views.companion.blockchain.ConfirmBuyerBid
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class ConfirmBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsConfirmBuyerBid: transactions.ConfirmBuyerBid, confirmBuyerBids: ConfirmBuyerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private implicit val logger: Logger = Logger(this.getClass)

  def confirmBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.confirmBuyerBid(master.ConfirmBuyerBid.form))
  }

  def confirmBuyerBid: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      master.ConfirmBuyerBid.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.confirmBuyerBid(formWithErrors))
        },
        confirmBuyerBidData => {
          try {
            if (kafkaEnabled) {
              val response = transactionsConfirmBuyerBid.Service.kafkaPost(transactionsConfirmBuyerBid.Request(from = username, to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas))
              confirmBuyerBids.Service.addConfirmBuyerBidKafka(from = username, to = confirmBuyerBidData.to, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas, null, null, ticketID = response.ticketID, null)
              Ok(views.html.index(success = response.ticketID))
            } else {
              val response = transactionsConfirmBuyerBid.Service.post(transactionsConfirmBuyerBid.Request(from = username, to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas))
              confirmBuyerBids.Service.addConfirmBuyerBid(from = username, to = confirmBuyerBidData.to, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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

  def blockchainConfirmBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.confirmBuyerBid(ConfirmBuyerBid.form))
  }

  def blockchainConfirmBuyerBid: Action[AnyContent] = Action { implicit request =>
    ConfirmBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.confirmBuyerBid(formWithErrors))
      },
      confirmBuyerBidData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsConfirmBuyerBid.Service.kafkaPost(transactionsConfirmBuyerBid.Request(from = confirmBuyerBidData.from, to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas))
            confirmBuyerBids.Service.addConfirmBuyerBidKafka(from = confirmBuyerBidData.from, to = confirmBuyerBidData.to, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsConfirmBuyerBid.Service.post(transactionsConfirmBuyerBid.Request(from = confirmBuyerBidData.from, to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas))
            confirmBuyerBids.Service.addConfirmBuyerBid(from = confirmBuyerBidData.from, to = confirmBuyerBidData.to, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
