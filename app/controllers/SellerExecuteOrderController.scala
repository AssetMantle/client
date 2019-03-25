package controllers

import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.blockchainTransaction.SellerExecuteOrders
import models.master.Accounts
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.blockchain.SellerExecuteOrder
import views.companion.master

import scala.concurrent.ExecutionContext
import scala.util.Random

class SellerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, withLoginAction: WithLoginAction, transactionSellerExecuteOrder: transactions.SellerExecuteOrder, sellerExecuteOrders: SellerExecuteOrders)(implicit exec: ExecutionContext,configuration: Configuration, accounts: Accounts) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def sellerExecuteOrderForm: Action[AnyContent] = withLoginAction { implicit request =>
    Ok(views.html.component.master.sellerExecuteOrder(master.SellerExecuteOrder.form))
  }

  def sellerExecuteOrder: Action[AnyContent] = withLoginAction { implicit request =>
    master.SellerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.sellerExecuteOrder(formWithErrors))
      },
      sellerExecuteOrderData => {
        try {
          if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
            val response = transactionSellerExecuteOrder.Service.kafkaPost( transactionSellerExecuteOrder.Request(from = request.session.get(constants.Security.USERNAME).get, password = sellerExecuteOrderData.password, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = accounts.Service.getId(request.session.get(constants.Security.USERNAME).get), awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas))
            sellerExecuteOrders.Service.addSellerExecuteOrderKafka(from = request.session.get(constants.Security.USERNAME).get, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = accounts.Service.getId(request.session.get(constants.Security.USERNAME).get), awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas, null, null, ticketID = response.ticketID, null)
            Ok(views.html.index(success = response.ticketID))
          } else {
            val response = transactionSellerExecuteOrder.Service.post( transactionSellerExecuteOrder.Request(from = request.session.get(constants.Security.USERNAME).get, password = sellerExecuteOrderData.password, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = accounts.Service.getId(request.session.get(constants.Security.USERNAME).get), awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas))
            sellerExecuteOrders.Service.addSellerExecuteOrder(from = request.session.get(constants.Security.USERNAME).get, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = accounts.Service.getId(request.session.get(constants.Security.USERNAME).get), awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas, null, txHash = Option(response.TxHash), ticketID = (Random.nextInt(899999999) + 100000000).toString, null)
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

  def blockchainSellerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sellerExecuteOrder(SellerExecuteOrder.form))
  }

  def blockchainSellerExecuteOrder: Action[AnyContent] = Action { implicit request =>
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
      }
    )
  }
}
