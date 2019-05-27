package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class SendFiatController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainFiats: blockchain.Fiats, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, withLoginAction: WithLoginAction, transactionsSendFiat: transactions.SendFiat, blockchainTransactionSendFiats: blockchainTransaction.SendFiats)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def sendFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendFiat(views.companion.master.SendFiat.form))
  }

  def sendFiat: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.SendFiat.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.sendFiat(formWithErrors))
        },
        sendFiatData => {
          try {
            val ticketID: String = if (kafkaEnabled) transactionsSendFiat.Service.kafkaPost(transactionsSendFiat.Request(from = username, to = sendFiatData.sellerAddress, password = sendFiatData.password, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionSendFiats.Service.create(from = username, to = sendFiatData.sellerAddress, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas, null, null, ticketID = ticketID, null)
            if(!kafkaEnabled){
              Future{
                try {
                  blockchainTransactionSendFiats.Utility.onSuccess(ticketID, transactionsSendFiat.Service.post(transactionsSendFiat.Request(from = username, to = sendFiatData.sellerAddress, password = sendFiatData.password, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                    blockchainTransactionSendFiats.Utility.onFailure(ticketID, blockChainException.message)
                }
              }
            }
            Ok(views.html.index(success = ticketID))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
            case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
          }
        }
      )
  }

  def blockchainSendFiatForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sendFiat(views.companion.blockchain.SendFiat.form))
  }

  def blockchainSendFiat: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.SendFiat.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.sendFiat(formWithErrors))
      },
      sendFiatData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsSendFiat.Service.kafkaPost(transactionsSendFiat.Request(from = sendFiatData.from, to = sendFiatData.to, password = sendFiatData.password, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas))
            blockchainTransactionSendFiats.Service.create(from = sendFiatData.from, to = sendFiatData.to, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsSendFiat.Service.post(transactionsSendFiat.Request(from = sendFiatData.from, to = sendFiatData.to, password = sendFiatData.password, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas))
            blockchainTransactionSendFiats.Service.create(from = sendFiatData.from, to = sendFiatData.to, amount = sendFiatData.amount, pegHash = sendFiatData.pegHash, gas = sendFiatData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
