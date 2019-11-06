package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BuyerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withZoneLoginAction: WithZoneLoginAction, withTraderLoginAction: WithTraderLoginAction, transactionsBuyerExecuteOrder: transactions.BuyerExecuteOrder, blockchainTransactionBuyerExecuteOrders: blockchainTransaction.BuyerExecuteOrders, blockchainACLAccounts: blockchain.ACLAccounts, blockchainZones: blockchain.Zones, blockchainNegotiations: blockchain.Negotiations, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_BUYER_EXECUTE_ORDER

  def buyerExecuteOrderForm(orderID: String): Action[AnyContent] = Action.async { implicit request =>
    val negotiation = blockchainNegotiations.Service.get(orderID)
    for {
      negotiation <- negotiation
    } yield Ok(views.html.component.master.buyerExecuteOrder(views.companion.master.BuyerExecuteOrder.form, negotiation.sellerAddress, negotiation.assetPegHash))
  }

  def buyerExecuteOrder: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.BuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.buyerExecuteOrder(formWithErrors, formWithErrors.data(constants.Form.SELLER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
          }
        },
        buyerExecuteOrderData => {
          transaction.process[blockchainTransaction.BuyerExecuteOrder, transactionsBuyerExecuteOrder.Request](
            entity = blockchainTransaction.BuyerExecuteOrder(from = loginState.address, buyerAddress = loginState.address, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionBuyerExecuteOrders.Service.create,
            request = transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseReq(from = loginState.address, gas = buyerExecuteOrderData.gas.toString), password = buyerExecuteOrderData.password, buyerAddress = loginState.address, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, mode = transactionMode),
            action = transactionsBuyerExecuteOrder.Service.post,
            onSuccess = blockchainTransactionBuyerExecuteOrders.Utility.onSuccess,
            onFailure = blockchainTransactionBuyerExecuteOrders.Utility.onFailure,
            updateTransactionHash = blockchainTransactionBuyerExecuteOrders.Service.updateTransactionHash
          )
          Future {
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
          }.recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def moderatedBuyerExecuteOrderList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = blockchainZones.Service.getID(loginState.address)
      val allOrderIdsWithoutFiatProofHash = blockchainOrders.Service.getAllOrderIdsWithoutFiatProofHash

      def addressesUnderZone(id: String) = blockchainACLAccounts.Service.getAddressesUnderZone(id)

      def getBuyerNegotiationsByOrderAndZone(ids: Seq[String], addresses: Seq[String]) = blockchainNegotiations.Service.getBuyerNegotiationsByOrderAndZone(ids, addresses)

      (for {
        id <- id
        allOrderIdsWithoutFiatProofHash <- allOrderIdsWithoutFiatProofHash
        addressesUnderZone <- addressesUnderZone(id)
        getBuyerNegotiationsByOrderAndZone <- getBuyerNegotiationsByOrderAndZone(allOrderIdsWithoutFiatProofHash, addressesUnderZone)
      } yield withUsernameToken.Ok(views.html.component.master.moderatedBuyerExecuteOrderList(getBuyerNegotiationsByOrderAndZone))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def moderatedBuyerExecuteOrderForm(buyerAddress: String, sellerAddress: String, pegHash: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.moderatedBuyerExecuteOrder(buyerAddress = buyerAddress, sellerAddress = sellerAddress, pegHash = pegHash))
  }

  def moderatedBuyerExecuteOrder: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ModeratedBuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          Future {
            BadRequest(views.html.component.master.moderatedBuyerExecuteOrder(formWithErrors, formWithErrors.data(constants.Form.BUYER_ADDRESS), formWithErrors.data(constants.Form.SELLER_ADDRESS), formWithErrors.data(constants.Form.PEG_HASH)))
          }
        },
        moderatedBuyerExecuteOrderData => {
          transaction.process[blockchainTransaction.BuyerExecuteOrder, transactionsBuyerExecuteOrder.Request](
            entity = blockchainTransaction.BuyerExecuteOrder(from = loginState.address, buyerAddress = moderatedBuyerExecuteOrderData.buyerAddress, sellerAddress = moderatedBuyerExecuteOrderData.sellerAddress, fiatProofHash = moderatedBuyerExecuteOrderData.fiatProofHash, pegHash = moderatedBuyerExecuteOrderData.pegHash, gas = moderatedBuyerExecuteOrderData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionBuyerExecuteOrders.Service.create,
            request = transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseReq(from = loginState.address, gas = moderatedBuyerExecuteOrderData.gas.toString), password = moderatedBuyerExecuteOrderData.password, buyerAddress = moderatedBuyerExecuteOrderData.buyerAddress, sellerAddress = moderatedBuyerExecuteOrderData.sellerAddress, fiatProofHash = moderatedBuyerExecuteOrderData.fiatProofHash, pegHash = moderatedBuyerExecuteOrderData.pegHash, mode = transactionMode),
            action = transactionsBuyerExecuteOrder.Service.post,
            onSuccess = blockchainTransactionBuyerExecuteOrders.Utility.onSuccess,
            onFailure = blockchainTransactionBuyerExecuteOrders.Utility.onFailure,
            updateTransactionHash = blockchainTransactionBuyerExecuteOrders.Service.updateTransactionHash
          )
          Future {
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
          }
            .recover {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
        }
      )
  }

  def blockchainBuyerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.buyerExecuteOrder())
  }

  def blockchainBuyerExecuteOrder: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.BuyerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        Future {
          BadRequest(views.html.component.blockchain.buyerExecuteOrder(formWithErrors))
        }
      },
      buyerExecuteOrderData => {
        val postRequest = transactionsBuyerExecuteOrder.Service.post(transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseReq(from = buyerExecuteOrderData.from, gas = buyerExecuteOrderData.gas.toString), password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, mode = buyerExecuteOrderData.mode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
