package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class BuyerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withZoneLoginAction: WithZoneLoginAction, withTraderLoginAction: WithTraderLoginAction, transactionsBuyerExecuteOrder: transactions.BuyerExecuteOrder, blockchainTransactionBuyerExecuteOrders: blockchainTransaction.BuyerExecuteOrders, blockchainACLAccounts: blockchain.ACLAccounts, blockchainZones: blockchain.Zones, blockchainNegotiations: blockchain.Negotiations)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  def buyerExecuteOrderForm(sellerAddress: String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.buyerExecuteOrder(views.companion.master.BuyerExecuteOrder.form, sellerAddress, pegHash))
  }

  def buyerExecuteOrder: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.BuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.buyerExecuteOrder(formWithErrors, formWithErrors.data(constants.Form.SELLER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
        },
        buyerExecuteOrderData => {
          try {
            transaction.process[blockchainTransaction.BuyerExecuteOrder, transactionsBuyerExecuteOrder.Request](
              entity = blockchainTransaction.BuyerExecuteOrder(from = loginState.address, buyerAddress = loginState.address, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
              blockchainTransactionCreate = blockchainTransactionBuyerExecuteOrders.Service.create,
              request = transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseRequest(from = loginState.address), password = buyerExecuteOrderData.password, buyerAddress = loginState.address, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, mode = transactionMode),
              action = transactionsBuyerExecuteOrder.Service.post,
              onSuccess = blockchainTransactionBuyerExecuteOrders.Utility.onSuccess,
              onFailure = blockchainTransactionBuyerExecuteOrders.Utility.onFailure,
              updateTransactionHash = blockchainTransactionBuyerExecuteOrders.Service.updateTransactionHash
            )
            Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => Ok(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def moderatedBuyerExecuteOrderList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.moderatedBuyerExecuteOrderList(blockchainNegotiations.Service.getBuyerNegotiationsByOrderAndZone(blockchainOrders.Service.getAllOrderIdsWithoutFiatProofHash, blockchainACLAccounts.Service.getAddressesUnderZone(blockchainZones.Service.getID(loginState.address)))))
      } catch {
        case baseException: BaseException => Ok(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def moderatedBuyerExecuteOrderForm(buyerAddress: String, sellerAddress: String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.moderatedBuyerExecuteOrder(views.companion.master.ModeratedBuyerExecuteOrder.form, buyerAddress, sellerAddress, pegHash))
  }

  def moderatedBuyerExecuteOrder: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ModeratedBuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.moderatedBuyerExecuteOrder(formWithErrors, formWithErrors.data(constants.Form.BUYER_ADDRESS), formWithErrors.data(constants.Form.SELLER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
        },
        moderatedBuyerExecuteOrderData => {
          try {
            transaction.process[blockchainTransaction.BuyerExecuteOrder, transactionsBuyerExecuteOrder.Request](
              entity = blockchainTransaction.BuyerExecuteOrder(from = loginState.address, buyerAddress = moderatedBuyerExecuteOrderData.buyerAddress, sellerAddress = moderatedBuyerExecuteOrderData.sellerAddress, fiatProofHash = moderatedBuyerExecuteOrderData.fiatProofHash, pegHash = moderatedBuyerExecuteOrderData.pegHash, status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
              blockchainTransactionCreate = blockchainTransactionBuyerExecuteOrders.Service.create,
              request = transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseRequest(from = loginState.address), password = moderatedBuyerExecuteOrderData.password, buyerAddress = moderatedBuyerExecuteOrderData.buyerAddress, sellerAddress = moderatedBuyerExecuteOrderData.sellerAddress, fiatProofHash = moderatedBuyerExecuteOrderData.fiatProofHash, pegHash = moderatedBuyerExecuteOrderData.pegHash, mode = transactionMode),
              action = transactionsBuyerExecuteOrder.Service.post,
              onSuccess = blockchainTransactionBuyerExecuteOrders.Utility.onSuccess,
              onFailure = blockchainTransactionBuyerExecuteOrders.Utility.onFailure,
              updateTransactionHash = blockchainTransactionBuyerExecuteOrders.Service.updateTransactionHash
            )
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
          transactionsBuyerExecuteOrder.Service.post(transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseRequest(from = buyerExecuteOrderData.from), password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, mode = buyerExecuteOrderData.mode))
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
