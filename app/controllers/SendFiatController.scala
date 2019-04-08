package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.SendFiats
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.SendFiat
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class SendFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionSendFiat: transactions.SendFiat, sendFiats: SendFiats)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def sendFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendFiat(master.SendFiat.form))
  }

  def sendFiat: Action[AnyContent] = withTraderLoginAction { implicit request =>
    master.SendFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.sendFiat(formWithErrors))
      },
      sendFiatData => {
        try {
          if (kafkaEnabled) {
            val response = transactionSendFiat.Service.kafkaPost( transactionSendFiat.Request(from = request.session.get(constants.Security.USERNAME).get, to = sendFiatData.to, password = sendFiatData.password, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas))
            sendFiats.Service.addSendFiatKafka(from = request.session.get(constants.Security.USERNAME).get, to = sendFiatData.to, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionSendFiat.Service.post( transactionSendFiat.Request(from = request.session.get(constants.Security.USERNAME).get, to = sendFiatData.to, password = sendFiatData.password, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas))
            sendFiats.Service.addSendFiat(from = request.session.get(constants.Security.USERNAME).get, to = sendFiatData.to, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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

  def blockchainSendFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sendFiat(SendFiat.form))
  }

  def blockchainSendFiat: Action[AnyContent] = Action { implicit request =>
    SendFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.sendFiat(formWithErrors))
      },
      sendFiatData => {
        try {
          if (kafkaEnabled) {
            val response = transactionSendFiat.Service.kafkaPost( transactionSendFiat.Request(from = sendFiatData.from, to = sendFiatData.to, password = sendFiatData.password, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas))
            sendFiats.Service.addSendFiatKafka(from = sendFiatData.from, to = sendFiatData.to, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionSendFiat.Service.post( transactionSendFiat.Request(from = sendFiatData.from, to = sendFiatData.to, password = sendFiatData.password, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas))
            sendFiats.Service.addSendFiat(from = sendFiatData.from, to = sendFiatData.to, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
