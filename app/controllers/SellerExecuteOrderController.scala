package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.master.Accounts
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class SellerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, withZoneLoginAction: WithZoneLoginAction, transactionsSellerExecuteOrder: transactions.SellerExecuteOrder, blockchainTransactionSellerExecuteOrders: blockchainTransaction.SellerExecuteOrders, accounts: Accounts, blockchainACLAccounts: blockchain.ACLAccounts, blockchainZones: blockchain.Zones, blockchainNegotiations: blockchain.Negotiations, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def sellerExecuteOrderForm(orderID: String): Action[AnyContent] = Action { implicit request =>
    val negotiation = blockchainNegotiations.Service.get(orderID)
    Ok(views.html.component.master.sellerExecuteOrder(views.companion.master.SellerExecuteOrder.form, negotiation.buyerAddress, negotiation.assetPegHash))
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
              entity = blockchainTransaction.SellerExecuteOrder(from = loginState.address, sellerAddress = loginState.address, buyerAddress = sellerExecuteOrderData.buyerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionSellerExecuteOrders.Service.create,
              request = transactionsSellerExecuteOrder.Request(transactionsSellerExecuteOrder.BaseRequest(from = loginState.address, gas = sellerExecuteOrderData.gas.toString), password = sellerExecuteOrderData.password, sellerAddress = loginState.address, buyerAddress = sellerExecuteOrderData.buyerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, mode = transactionMode),
              action = transactionsSellerExecuteOrder.Service.post,
              onSuccess = blockchainTransactionSellerExecuteOrders.Utility.onSuccess,
              onFailure = blockchainTransactionSellerExecuteOrders.Utility.onFailure,
              updateTransactionHash = blockchainTransactionSellerExecuteOrders.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SELLER_ORDER_EXECUTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
          }
        }
      )
  }

  def moderatedSellerExecuteOrderList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.moderatedSellerExecuteOrderList(blockchainNegotiations.Service.getSellerNegotiationsByOrderAndZone(blockchainOrders.Service.getAllOrderIdsWithoutAWBProofHash, blockchainACLAccounts.Service.getAddressesUnderZone(blockchainZones.Service.getID(loginState.address)))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def moderatedSellerExecuteOrderForm(buyerAddress: String, sellerAddress: String, pegHash: String): Action[AnyContent] = Action { implicit request =>
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
              entity = blockchainTransaction.SellerExecuteOrder(from = loginState.address, buyerAddress = moderatedSellerExecuteOrderData.buyerAddress, sellerAddress = moderatedSellerExecuteOrderData.sellerAddress, awbProofHash = moderatedSellerExecuteOrderData.awbProofHash, pegHash = moderatedSellerExecuteOrderData.pegHash, gas = moderatedSellerExecuteOrderData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionSellerExecuteOrders.Service.create,
              request = transactionsSellerExecuteOrder.Request(transactionsSellerExecuteOrder.BaseRequest(from = loginState.address, gas = moderatedSellerExecuteOrderData.gas.toString), password = moderatedSellerExecuteOrderData.password, buyerAddress = moderatedSellerExecuteOrderData.buyerAddress, sellerAddress = moderatedSellerExecuteOrderData.sellerAddress, awbProofHash = moderatedSellerExecuteOrderData.awbProofHash, pegHash = moderatedSellerExecuteOrderData.pegHash, mode = transactionMode),
              action = transactionsSellerExecuteOrder.Service.post,
              onSuccess = blockchainTransactionSellerExecuteOrders.Utility.onSuccess,
              onFailure = blockchainTransactionSellerExecuteOrders.Utility.onFailure,
              updateTransactionHash = blockchainTransactionSellerExecuteOrders.Service.updateTransactionHash
            )
            Ok(views.html.index(successes = Seq(constants.Response.SELLER_ORDER_EXECUTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
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
          transactionsSellerExecuteOrder.Service.post(transactionsSellerExecuteOrder.Request(transactionsSellerExecuteOrder.BaseRequest(from = sellerExecuteOrderData.from, gas = sellerExecuteOrderData.gas.toString), password = sellerExecuteOrderData.password, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, mode = sellerExecuteOrderData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.SELLER_ORDER_EXECUTED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
        }
      }
    )
  }
}
