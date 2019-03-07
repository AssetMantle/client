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
          val sendCoinsResponse = sendCoins.Service.addSendCoin(sendCoinData.from, sendCoinData.to, sendCoinData.amount,sendCoinData.chainID, sendCoinData.gas, null, null, utilities.RandomString.randomStringArray(10), null)
          Ok(views.html.index(transactionSendCoin.Service.post(new transactionSendCoin.Request(sendCoinData.from, sendCoinData.password, sendCoinData.to, sendCoinData.amount, sendCoinData.chainID, sendCoinData.gas)).txHash))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}
