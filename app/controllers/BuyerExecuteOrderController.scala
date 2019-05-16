package controllers

import controllers.actions.WithZoneLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class BuyerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withZoneLoginAction: WithZoneLoginAction, transactionsBuyerExecuteOrder: transactions.BuyerExecuteOrder, blockchainTransactionBuyerExecuteOrders: blockchainTransaction.BuyerExecuteOrders)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private implicit val logger: Logger = Logger(this.getClass)

  def buyerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.buyerExecuteOrder(views.companion.master.BuyerExecuteOrder.form))
  }

  def buyerExecuteOrder: Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.BuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.buyerExecuteOrder(formWithErrors))
        },
        buyerExecuteOrderData => {
          try {
            val ticketID: String = if (kafkaEnabled) transactionsBuyerExecuteOrder.Service.kafkaPost(transactionsBuyerExecuteOrder.Request(from = username, password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionBuyerExecuteOrders.Service.addBuyerExecuteOrder(from = username, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas, null, null, ticketID = ticketID, null)
            if(!kafkaEnabled){
              Future{
                try {
                  blockchainTransactionBuyerExecuteOrders.Utility.onSuccess(ticketID, transactionsBuyerExecuteOrder.Service.post(transactionsBuyerExecuteOrder.Request(from = username, password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
                    blockchainTransactionBuyerExecuteOrders.Utility.onFailure(ticketID, baseException.message)
                  case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
                    blockchainTransactionBuyerExecuteOrders.Utility.onFailure(ticketID, blockChainException.message)
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

  def blockchainBuyerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.buyerExecuteOrder(views.companion.blockchain.BuyerExecuteOrder.form))
  }

  def blockchainBuyerExecuteOrder: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.BuyerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.buyerExecuteOrder(formWithErrors))
      },
      buyerExecuteOrderData => {
        try {
          if (kafkaEnabled) {
            val response = transactionsBuyerExecuteOrder.Service.kafkaPost(transactionsBuyerExecuteOrder.Request(from = buyerExecuteOrderData.from, password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas))
            blockchainTransactionBuyerExecuteOrders.Service.addBuyerExecuteOrder(from = buyerExecuteOrderData.from, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionsBuyerExecuteOrder.Service.post(transactionsBuyerExecuteOrder.Request(from = buyerExecuteOrderData.from, password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas))
            blockchainTransactionBuyerExecuteOrders.Service.addBuyerExecuteOrder(from = buyerExecuteOrderData.from, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas, null, txHash = Option(response.TxHash), ticketID = Random.nextString(32), null)
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
