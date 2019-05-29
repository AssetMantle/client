package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.master.Accounts
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class SellerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, withZoneLoginAction: WithZoneLoginAction, transactionsSellerExecuteOrder: transactions.SellerExecuteOrder, blockchainTransactionSellerExecuteOrders: blockchainTransaction.SellerExecuteOrders)(implicit exec: ExecutionContext, configuration: Configuration, accounts: Accounts) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def sellerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sellerExecuteOrder(views.companion.master.SellerExecuteOrder.form))
  }

  def sellerExecuteOrder: Action[AnyContent] = withZoneLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.SellerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.sellerExecuteOrder(formWithErrors))
        },
        sellerExecuteOrderData => {
          try {
            val ticketID: String = if (kafkaEnabled) transactionsSellerExecuteOrder.Service.kafkaPost(transactionsSellerExecuteOrder.Request(from = username, password = sellerExecuteOrderData.password, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionSellerExecuteOrders.Service.create(from = username, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas, null, null, ticketID = ticketID, null)
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionSellerExecuteOrders.Utility.onSuccess(ticketID, transactionsSellerExecuteOrder.Service.post(transactionsSellerExecuteOrder.Request(from = username, password = sellerExecuteOrderData.password, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionSellerExecuteOrders.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
            }
            Ok(views.html.index(successes = Seq(constants.Response.SELLER_ORDER_EXECUTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

          }
        }
      )
  }

  def unmoderatedSellerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.unmoderatedSellerExecuteOrder(views.companion.master.UnmoderatedSellerExecuteOrder.form))
  }

  def unmoderatedSellerExecuteOrder: Action[AnyContent] = withTraderLoginAction.authenticated { username =>
    implicit request =>
      views.companion.master.UnmoderatedSellerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.unmoderatedSellerExecuteOrder(formWithErrors))
        },
        unmoderatedSellerExecuteOrderData => {
          try {
            val sellerAddress = masterAccounts.Service.getAddress(username)
            val ticketID: String = if (kafkaEnabled) transactionsSellerExecuteOrder.Service.kafkaPost(transactionsSellerExecuteOrder.Request(from = username, password = unmoderatedSellerExecuteOrderData.password, buyerAddress = unmoderatedSellerExecuteOrderData.buyerAddress, sellerAddress = sellerAddress, awbProofHash = unmoderatedSellerExecuteOrderData.awbProofHash, pegHash = unmoderatedSellerExecuteOrderData.pegHash, gas = unmoderatedSellerExecuteOrderData.gas)).ticketID else Random.nextString(32)
            blockchainTransactionSellerExecuteOrders.Service.create(from = username, buyerAddress = unmoderatedSellerExecuteOrderData.buyerAddress, sellerAddress = sellerAddress, awbProofHash = unmoderatedSellerExecuteOrderData.awbProofHash, pegHash = unmoderatedSellerExecuteOrderData.pegHash, gas = unmoderatedSellerExecuteOrderData.gas, null, null, ticketID = ticketID, null)
            if (!kafkaEnabled) {
              Future {
                try {
                  blockchainTransactionSellerExecuteOrders.Utility.onSuccess(ticketID, transactionsSellerExecuteOrder.Service.post(transactionsSellerExecuteOrder.Request(from = username, password = unmoderatedSellerExecuteOrderData.password, buyerAddress = unmoderatedSellerExecuteOrderData.buyerAddress, sellerAddress = sellerAddress, awbProofHash = unmoderatedSellerExecuteOrderData.awbProofHash, pegHash = unmoderatedSellerExecuteOrderData.pegHash, gas = unmoderatedSellerExecuteOrderData.gas)))
                } catch {
                  case baseException: BaseException => logger.error(baseException.failure.message, baseException)
                  case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
                    blockchainTransactionSellerExecuteOrders.Utility.onFailure(ticketID, blockChainException.failure.message)
                }
              }
            }
            Ok(views.html.index(successes = Seq(constants.Response.SELLER_ORDER_EXECUTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

          }
        }
      )
  }

  def blockchainSellerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sellerExecuteOrder(views.companion.blockchain.SellerExecuteOrder.form))
  }

  def blockchainSellerExecuteOrder: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.SellerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.sellerExecuteOrder(formWithErrors))
      },
      sellerExecuteOrderData => {
        try {
          if (kafkaEnabled) {
            transactionsSellerExecuteOrder.Service.kafkaPost(transactionsSellerExecuteOrder.Request(from = sellerExecuteOrderData.from, password = sellerExecuteOrderData.password, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas))
          } else {
            transactionsSellerExecuteOrder.Service.post(transactionsSellerExecuteOrder.Request(from = sellerExecuteOrderData.from, password = sellerExecuteOrderData.password, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas))
          }
          Ok(views.html.index(successes = Seq(constants.Response.SELLER_ORDER_EXECUTED)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))

        }
      }
    )
  }
}
