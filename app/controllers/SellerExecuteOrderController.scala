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
class SellerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, withZoneLoginAction: WithZoneLoginAction, transactionsSellerExecuteOrder: transactions.SellerExecuteOrder, blockchainTransactionSellerExecuteOrders: blockchainTransaction.SellerExecuteOrders)(implicit exec: ExecutionContext, configuration: Configuration, accounts: Accounts, blockchainACLAccounts: blockchain.ACLAccounts, blockchainZones: blockchain.Zones, blockchainNegotiations:blockchain.Negotiations) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  def sellerExecuteOrderForm(buyerAddress:String, pegHash:String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sellerExecuteOrder(views.companion.master.SellerExecuteOrder.form, buyerAddress, pegHash))
  }

  def sellerExecuteOrder: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SellerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.sellerExecuteOrder(formWithErrors, formWithErrors.data(constants.Form.BUYER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
        },
        sellerExecuteOrderData => {
          try {
            transaction.process[blockchainTransaction.SellerExecuteOrder, transactionsSellerExecuteOrder.Request](
              entity = blockchainTransaction.SellerExecuteOrder(from = loginState.address, sellerAddress = loginState.address, buyerAddress = sellerExecuteOrderData.buyerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas, status = null, txHash = null, ticketID = "", mode = transactionMode, code =  null),
              blockchainTransactionCreate = blockchainTransactionSellerExecuteOrders.Service.create,
              request = transactionsSellerExecuteOrder.Request(transactionsSellerExecuteOrder.BaseRequest(from = loginState.address), password = sellerExecuteOrderData.password, sellerAddress = loginState.address, buyerAddress = sellerExecuteOrderData.buyerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas, mode = transactionMode),
              kafkaAction = transactionsSellerExecuteOrder.Service.kafkaPost,
              blockAction = transactionsSellerExecuteOrder.Service.blockPost,
              asyncAction = transactionsSellerExecuteOrder.Service.asyncPost,
              syncAction = transactionsSellerExecuteOrder.Service.syncPost,
              onSuccess = blockchainTransactionSellerExecuteOrders.Utility.onSuccess,
              onFailure = blockchainTransactionSellerExecuteOrders.Utility.onFailure,
              updateTransactionHash = blockchainTransactionSellerExecuteOrders.Service.updateTransactionHash
            )
            Ok(views.html.index(successes = Seq(constants.Response.SELLER_ORDER_EXECUTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def moderatedSellerExecuteOrderList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.moderatedSellerExecuteOrderList(blockchainNegotiations.Service.getSellerNegotiationsByOrderAndZone(blockchainOrders.Service.getAllOrderIds, blockchainACLAccounts.Service.getAddressesUnderZone(blockchainZones.Service.getID(loginState.address)))))
      }catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def moderatedSellerExecuteOrderForm(buyerAddress:String, sellerAddress: String, pegHash:String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.moderatedSellerExecuteOrder(views.companion.master.ModeratedSellerExecuteOrder.form, buyerAddress, sellerAddress, pegHash))
  }

  def moderatedSellerExecuteOrder: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ModeratedSellerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.moderatedSellerExecuteOrder(formWithErrors, formWithErrors.data(constants.Form.BUYER_ADDRESS), formWithErrors.data(constants.Form.SELLER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
        },
        moderatedSellerExecuteOrderData => {
          try {
            transaction.process[blockchainTransaction.SellerExecuteOrder, transactionsSellerExecuteOrder.Request](
              entity = blockchainTransaction.SellerExecuteOrder(from = loginState.address, buyerAddress = loginState.address, sellerAddress = moderatedSellerExecuteOrderData.sellerAddress, awbProofHash = moderatedSellerExecuteOrderData.awbProofHash, pegHash = moderatedSellerExecuteOrderData.pegHash, gas = moderatedSellerExecuteOrderData.gas, status = null, txHash = null, ticketID = "", mode = transactionMode, code =  null),
              blockchainTransactionCreate = blockchainTransactionSellerExecuteOrders.Service.create,
              request = transactionsSellerExecuteOrder.Request(transactionsSellerExecuteOrder.BaseRequest(from = loginState.address), password = moderatedSellerExecuteOrderData.password, buyerAddress = loginState.address, sellerAddress = moderatedSellerExecuteOrderData.sellerAddress, awbProofHash = moderatedSellerExecuteOrderData.awbProofHash, pegHash = moderatedSellerExecuteOrderData.pegHash, gas = moderatedSellerExecuteOrderData.gas, mode = transactionMode),
              kafkaAction = transactionsSellerExecuteOrder.Service.kafkaPost,
              blockAction = transactionsSellerExecuteOrder.Service.blockPost,
              asyncAction = transactionsSellerExecuteOrder.Service.asyncPost,
              syncAction = transactionsSellerExecuteOrder.Service.syncPost,
              onSuccess = blockchainTransactionSellerExecuteOrders.Utility.onSuccess,
              onFailure = blockchainTransactionSellerExecuteOrders.Utility.onFailure,
              updateTransactionHash = blockchainTransactionSellerExecuteOrders.Service.updateTransactionHash
            )
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
            transactionsSellerExecuteOrder.Service.kafkaPost(transactionsSellerExecuteOrder.Request(transactionsSellerExecuteOrder.BaseRequest(from = sellerExecuteOrderData.from), password = sellerExecuteOrderData.password, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas, mode = transactionMode))
          } else {
            transactionsSellerExecuteOrder.Service.blockPost(transactionsSellerExecuteOrder.Request(transactionsSellerExecuteOrder.BaseRequest(from = sellerExecuteOrderData.from), password = sellerExecuteOrderData.password, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas, mode = transactionMode))
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
