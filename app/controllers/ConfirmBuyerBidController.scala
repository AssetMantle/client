package controllers

import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.ConfirmBuyerBids
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.ConfirmBuyerBid
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class ConfirmBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, transactionConfirmBuyerBid: transactions.ConfirmBuyerBid, confirmBuyerBids: ConfirmBuyerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def confirmBuyerBidForm: Action[AnyContent] = withLoginAction { implicit request =>
    Ok(views.html.component.master.confirmBuyerBid(master.ConfirmBuyerBid.form))
  }

  def confirmBuyerBid: Action[AnyContent] = withLoginAction { implicit request =>
    master.ConfirmBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.confirmBuyerBid(formWithErrors))
      },
      confirmBuyerBidData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionConfirmBuyerBid.Service.kafkaPost( transactionConfirmBuyerBid.Request(from = request.session.get(constants.Security.USERNAME).get, to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas))
            confirmBuyerBids.Service.addConfirmBuyerBidKafka(from = request.session.get(constants.Security.USERNAME).get, to = confirmBuyerBidData.to, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionConfirmBuyerBid.Service.post( transactionConfirmBuyerBid.Request(from = request.session.get(constants.Security.USERNAME).get, to = confirmBuyerBidData.to, password = confirmBuyerBidData.password,  bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas))
            confirmBuyerBids.Service.addConfirmBuyerBid(from = request.session.get(constants.Security.USERNAME).get, to = confirmBuyerBidData.to, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionConfirmBuyerBid.Service.kafkaPost( transactionConfirmBuyerBid.Request(from = confirmBuyerBidData.from, to = confirmBuyerBidData.to, password = confirmBuyerBidData.password, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas))
            confirmBuyerBids.Service.addConfirmBuyerBidKafka(from = confirmBuyerBidData.from, to = confirmBuyerBidData.to, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionConfirmBuyerBid.Service.post( transactionConfirmBuyerBid.Request(from = confirmBuyerBidData.from,to = confirmBuyerBidData.to, password = confirmBuyerBidData.password,  bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas))
            confirmBuyerBids.Service.addConfirmBuyerBid(from = confirmBuyerBidData.from, to = confirmBuyerBidData.to, bid = confirmBuyerBidData.bid, time = confirmBuyerBidData.time, pegHash = confirmBuyerBidData.pegHash, gas = confirmBuyerBidData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
