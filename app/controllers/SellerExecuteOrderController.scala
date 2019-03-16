package controllers

import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.SellerExecuteOrders
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import transactions.SellerExecuteOrder
import views.companion.blockchain.SellerExecuteOrder

import scala.concurrent.ExecutionContext
import scala.util.Random

class SellerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, transactionSellerExecuteOrder: transactions.SellerExecuteOrder, sellerExecuteOrders: SellerExecuteOrders)(implicit exec: ExecutionContext,configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def sellerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sellerExecuteOrder(SellerExecuteOrder.form))
  }

  def sellerExecuteOrder: Action[AnyContent] = Action { implicit request =>
    SellerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.sellerExecuteOrder(formWithErrors))
      },
      sellerExecuteOrderData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionSellerExecuteOrder.Service.kafkaPost( transactionSellerExecuteOrder.Request(from = sellerExecuteOrderData.from, password = sellerExecuteOrderData.password, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas))
            sellerExecuteOrders.Service.addSellerExecuteOrderKafka(from = sellerExecuteOrderData.from, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionSellerExecuteOrder.Service.post( transactionSellerExecuteOrder.Request(from = sellerExecuteOrderData.from, password = sellerExecuteOrderData.password, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas))
            sellerExecuteOrders.Service.addSellerExecuteOrder(from = sellerExecuteOrderData.from, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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
