package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.ChangeBuyerBids
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.ChangeBuyerBid
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class ChangeBuyerBidController @Inject()(messagesControllerComponents: MessagesControllerComponents, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsChangeBuyerBid: transactions.ChangeBuyerBid, changeBuyerBids: ChangeBuyerBids)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def changeBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.changeBuyerBid(master.ChangeBuyerBid.form))
  }

  def changeBuyerBid: Action[AnyContent] = withTraderLoginAction { implicit request =>
    master.ChangeBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.changeBuyerBid(formWithErrors))
      },
      changeBuyerBidData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsChangeBuyerBid.Service.kafkaPost( transactionsChangeBuyerBid.Request(from = request.session.get(constants.Security.USERNAME).get, to = changeBuyerBidData.to, password = changeBuyerBidData.password, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas))
            changeBuyerBids.Service.addChangeBuyerBidKafka(from = request.session.get(constants.Security.USERNAME).get, to = changeBuyerBidData.to, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsChangeBuyerBid.Service.post( transactionsChangeBuyerBid.Request(from = request.session.get(constants.Security.USERNAME).get,to = changeBuyerBidData.to, password = changeBuyerBidData.password,  bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas))
            changeBuyerBids.Service.addChangeBuyerBid(from = request.session.get(constants.Security.USERNAME).get, to = changeBuyerBidData.to, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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

  def blockchainChangeBuyerBidForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.changeBuyerBid(ChangeBuyerBid.form))
  }

  def blockchainChangeBuyerBid: Action[AnyContent] = Action { implicit request =>
    ChangeBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.changeBuyerBid(formWithErrors))
      },
      changeBuyerBidData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsChangeBuyerBid.Service.kafkaPost( transactionsChangeBuyerBid.Request(from = changeBuyerBidData.from, to = changeBuyerBidData.to, password = changeBuyerBidData.password, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas))
            changeBuyerBids.Service.addChangeBuyerBidKafka(from = changeBuyerBidData.from, to = changeBuyerBidData.to, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsChangeBuyerBid.Service.post( transactionsChangeBuyerBid.Request(from = changeBuyerBidData.from,to = changeBuyerBidData.to, password = changeBuyerBidData.password,  bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas))
            changeBuyerBids.Service.addChangeBuyerBid(from = changeBuyerBidData.from, to = changeBuyerBidData.to, bid = changeBuyerBidData.bid, time = changeBuyerBidData.time, pegHash = changeBuyerBidData.pegHash, gas = changeBuyerBidData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
