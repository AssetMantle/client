package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.ConfirmSellerBids
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.ConfirmSellerBid

import scala.concurrent.ExecutionContext
import scala.util.Random

class ConfirmSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionConfirmSellerBid: transactions.ConfirmSellerBid, confirmSellerBids: ConfirmSellerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def blockchainConfirmSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.confirmSellerBid(ConfirmSellerBid.form))
  }

  def blockchainConfirmSellerBid: Action[AnyContent] = Action { implicit request =>
    ConfirmSellerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.confirmSellerBid(formWithErrors))
      },
      confirmSellerBidData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionConfirmSellerBid.Service.kafkaPost( transactionConfirmSellerBid.Request(from = confirmSellerBidData.from, to = confirmSellerBidData.to, password = confirmSellerBidData.password, bid = confirmSellerBidData.bid, time = confirmSellerBidData.time, pegHash = confirmSellerBidData.pegHash, gas = confirmSellerBidData.gas))
            confirmSellerBids.Service.addConfirmSellerBidKafka(from = confirmSellerBidData.from, to = confirmSellerBidData.to, bid = confirmSellerBidData.bid, time = confirmSellerBidData.time, pegHash = confirmSellerBidData.pegHash, gas = confirmSellerBidData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionConfirmSellerBid.Service.post( transactionConfirmSellerBid.Request(from = confirmSellerBidData.from,to = confirmSellerBidData.to, password = confirmSellerBidData.password,  bid = confirmSellerBidData.bid, time = confirmSellerBidData.time, pegHash = confirmSellerBidData.pegHash, gas = confirmSellerBidData.gas))
            confirmSellerBids.Service.addConfirmSellerBid(from = confirmSellerBidData.from, to = confirmSellerBidData.to, bid = confirmSellerBidData.bid, time = confirmSellerBidData.time, pegHash = confirmSellerBidData.pegHash, gas = confirmSellerBidData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
            Ok(views.html.index(success = response.TxHash))
          }
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))

        }
      })
  }
}
