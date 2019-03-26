package controllers

import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.BuyerExecuteOrders
import models.master.Accounts
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.{blockchain, master}

import scala.concurrent.ExecutionContext
import scala.util.Random

class BuyerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, transactionBuyerExecuteOrder: transactions.BuyerExecuteOrder, buyerExecuteOrders: BuyerExecuteOrders)(implicit exec: ExecutionContext,configuration: Configuration, accounts: Accounts) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def buyerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.buyerExecuteOrder(master.BuyerExecuteOrder.form))
  }

  def buyerExecuteOrder: Action[AnyContent] = withLoginAction { implicit request =>
    master.BuyerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.buyerExecuteOrder(formWithErrors))
      },
      buyerExecuteOrderData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionBuyerExecuteOrder.Service.kafkaPost( transactionBuyerExecuteOrder.Request(from = request.session.get(constants.Security.USERNAME).get, password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas))
            buyerExecuteOrders.Service.addBuyerExecuteOrderKafka(from = request.session.get(constants.Security.USERNAME).get, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionBuyerExecuteOrder.Service.post( transactionBuyerExecuteOrder.Request(from = request.session.get(constants.Security.USERNAME).get, password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas))
            buyerExecuteOrders.Service.addBuyerExecuteOrder(from = request.session.get(constants.Security.USERNAME).get, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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

  def blockchainBuyerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.buyerExecuteOrder(blockchain.BuyerExecuteOrder.form))
  }

  def blockchainBuyerExecuteOrder: Action[AnyContent] = Action { implicit request =>
    blockchain.BuyerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.buyerExecuteOrder(formWithErrors))
      },
      buyerExecuteOrderData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionBuyerExecuteOrder.Service.kafkaPost( transactionBuyerExecuteOrder.Request(from = buyerExecuteOrderData.from, password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas))
            buyerExecuteOrders.Service.addBuyerExecuteOrderKafka(from = buyerExecuteOrderData.from, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionBuyerExecuteOrder.Service.post( transactionBuyerExecuteOrder.Request(from = buyerExecuteOrderData.from, password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas))
            buyerExecuteOrders.Service.addBuyerExecuteOrder(from = buyerExecuteOrderData.from, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
