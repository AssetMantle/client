package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.SendCoins
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SendCoin
import views.companion.blockchain.SendCoin

import scala.concurrent.ExecutionContext
import scala.util.Random

class SendCoinController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionSendCoin: SendCoin, sendCoins: SendCoins)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {
  def sendCoinForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sendCoin(SendCoin.form))
  }

  def sendCoin: Action[AnyContent] = Action { implicit request =>
    SendCoin.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.sendCoin(formWithErrors))
      },
      sendCoinData => {
        try {
          val sendCoinsResponse = transactionSendCoin.Service.post(transactionSendCoin.Request(from = sendCoinData.from, password = sendCoinData.password, to = sendCoinData.to, amount = Seq(transactionSendCoin.Amount("comdex", sendCoinData.amount.toString)), gas = sendCoinData.gas)).txHash
          val txHashTicketID  = if (configuration.get[Boolean]("blockchain.kafkaEnabled")) (null, sendCoinsResponse) else (Option(sendCoinsResponse), (Random.nextInt(899999999) + 100000000).toString)
          sendCoins.Service.addSendCoin(sendCoinData.from, sendCoinData.to, sendCoinData.amount, sendCoinData.gas, null, txHashTicketID._1, txHashTicketID._2, null)
          Ok(views.html.index(sendCoinsResponse))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}
