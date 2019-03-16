package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.ChangeSellerBids
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.ChangeSellerBid

import scala.concurrent.ExecutionContext
import scala.util.Random

class ChangeSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionChangeSellerBid: transactions.ChangeSellerBid, changeSellerBids: ChangeSellerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def changeSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.changeSellerBid(ChangeSellerBid.form))
  }

  def changeSellerBid: Action[AnyContent] = Action { implicit request =>
    ChangeSellerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.changeSellerBid(formWithErrors))
      },
      changeSellerBidData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionChangeSellerBid.Service.kafkaPost( transactionChangeSellerBid.Request(from = changeSellerBidData.from, to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas))
            changeSellerBids.Service.addChangeSellerBidKafka(from = changeSellerBidData.from, to = changeSellerBidData.to, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionChangeSellerBid.Service.post( transactionChangeSellerBid.Request(from = changeSellerBidData.from,to = changeSellerBidData.to, password = changeSellerBidData.password,  bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas))
            changeSellerBids.Service.addChangeSellerBid(from = changeSellerBidData.from, to = changeSellerBidData.to, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
