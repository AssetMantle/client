package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.LoginState

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class BuyerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withZoneLoginAction: WithZoneLoginAction, withTraderLoginAction: WithTraderLoginAction, transactionsBuyerExecuteOrder: transactions.BuyerExecuteOrder, blockchainTransactionBuyerExecuteOrders: blockchainTransaction.BuyerExecuteOrders)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private implicit val logger: Logger = Logger(this.getClass)

  def buyerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.buyerExecuteOrder(views.companion.master.BuyerExecuteOrder.form))
  }

  def buyerExecuteOrder: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.BuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.buyerExecuteOrder(formWithErrors))
        },
        buyerExecuteOrderData => {

          try {
            val ticketID: String = if (kafkaEnabled) transactionsBuyerExecuteOrder.Service.kafkaPost(transactionsBuyerExecuteOrder.Request(from = loginState.username, password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionBuyerExecuteOrders.Service.create(from = loginState.username, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas, null, null, ticketID = ticketID, null)
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionBuyerExecuteOrders.Utility.onSuccess(ticketID, transactionsBuyerExecuteOrder.Service.post(transactionsBuyerExecuteOrder.Request(from = loginState.username, password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionBuyerExecuteOrders.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
            }
            Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

          }
        }
      )
  }

  def unmoderatedBuyerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.unmoderatedBuyerExecuteOrder(views.companion.master.UnmoderatedBuyerExecuteOrder.form))
  }

  def unmoderatedBuyerExecuteOrder: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UnmoderatedBuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.unmoderatedBuyerExecuteOrder(formWithErrors))
        },
        unmoderatedBuyerExecuteOrderData => {

          try {
            val buyerAddress = masterAccounts.Service.getAddress(loginState.username)
            val ticketID: String = if (kafkaEnabled) transactionsBuyerExecuteOrder.Service.kafkaPost(transactionsBuyerExecuteOrder.Request(from = loginState.username, password = unmoderatedBuyerExecuteOrderData.password, buyerAddress = buyerAddress, sellerAddress = unmoderatedBuyerExecuteOrderData.sellerAddress, fiatProofHash = unmoderatedBuyerExecuteOrderData.fiatProofHash, pegHash = unmoderatedBuyerExecuteOrderData.pegHash, gas = unmoderatedBuyerExecuteOrderData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionBuyerExecuteOrders.Service.create(from = loginState.username, buyerAddress = buyerAddress, sellerAddress = unmoderatedBuyerExecuteOrderData.sellerAddress, fiatProofHash = unmoderatedBuyerExecuteOrderData.fiatProofHash, pegHash = unmoderatedBuyerExecuteOrderData.pegHash, gas = unmoderatedBuyerExecuteOrderData.gas, null, null, ticketID = ticketID, null)
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionBuyerExecuteOrders.Utility.onSuccess(ticketID, transactionsBuyerExecuteOrder.Service.post(transactionsBuyerExecuteOrder.Request(from = loginState.username, password = unmoderatedBuyerExecuteOrderData.password, buyerAddress = buyerAddress, sellerAddress = unmoderatedBuyerExecuteOrderData.sellerAddress, fiatProofHash = unmoderatedBuyerExecuteOrderData.fiatProofHash, pegHash = unmoderatedBuyerExecuteOrderData.pegHash, gas = unmoderatedBuyerExecuteOrderData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionBuyerExecuteOrders.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
            }
            Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

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
            transactionsBuyerExecuteOrder.Service.kafkaPost(transactionsBuyerExecuteOrder.Request(from = buyerExecuteOrderData.from, password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas))
          } else {
            transactionsBuyerExecuteOrder.Service.post(transactionsBuyerExecuteOrder.Request(from = buyerExecuteOrderData.from, password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas))
          }
          Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))

        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

        }
      }
    )
  }
}
