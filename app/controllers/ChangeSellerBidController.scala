package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.ChangeSellerBids
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.ChangeSellerBid
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class ChangeSellerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsChangeSellerBid: transactions.ChangeSellerBid, changeSellerBids: ChangeSellerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def changeSellerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.changeSellerBid(master.ChangeSellerBid.form))
  }

  def changeSellerBid: Action[AnyContent] = withTraderLoginAction { implicit request =>
    master.ChangeSellerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.changeSellerBid(formWithErrors))
      },
      changeSellerBidData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsChangeSellerBid.Service.kafkaPost( transactionsChangeSellerBid.Request(from = request.session.get(constants.Security.USERNAME).get, to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas))
            changeSellerBids.Service.addChangeSellerBidKafka(from = request.session.get(constants.Security.USERNAME).get, to = changeSellerBidData.to, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsChangeSellerBid.Service.post( transactionsChangeSellerBid.Request(from = request.session.get(constants.Security.USERNAME).get,to = changeSellerBidData.to, password = changeSellerBidData.password,  bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas))
            changeSellerBids.Service.addChangeSellerBid(from = request.session.get(constants.Security.USERNAME).get, to = changeSellerBidData.to, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
    Ok(views.html.component.blockchain.changeSellerBid(ChangeSellerBid.form))
  }

  def blockchainChangeSellerBid: Action[AnyContent] = Action { implicit request =>
    ChangeSellerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.changeSellerBid(formWithErrors))
      },
      changeSellerBidData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsChangeSellerBid.Service.kafkaPost( transactionsChangeSellerBid.Request(from = changeSellerBidData.from, to = changeSellerBidData.to, password = changeSellerBidData.password, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas))
            changeSellerBids.Service.addChangeSellerBidKafka(from = changeSellerBidData.from, to = changeSellerBidData.to, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsChangeSellerBid.Service.post( transactionsChangeSellerBid.Request(from = changeSellerBidData.from,to = changeSellerBidData.to, password = changeSellerBidData.password,  bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas))
            changeSellerBids.Service.addChangeSellerBid(from = changeSellerBidData.from, to = changeSellerBidData.to, bid = changeSellerBidData.bid, time = changeSellerBidData.time, pegHash = changeSellerBidData.pegHash, gas = changeSellerBidData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
