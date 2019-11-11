package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class BuyerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests, masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles, withZoneLoginAction: WithZoneLoginAction, withTraderLoginAction: WithTraderLoginAction, transactionsBuyerExecuteOrder: transactions.BuyerExecuteOrder, blockchainTransactionBuyerExecuteOrders: blockchainTransaction.BuyerExecuteOrders, blockchainACLAccounts: blockchain.ACLAccounts, blockchainZones: blockchain.Zones, blockchainNegotiations: blockchain.Negotiations, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_BUYER_EXECUTE_ORDER

  //TODO username instead of Addresses
  def buyerExecuteOrderDocument(orderID: String) = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val requestID = masterTransactionNegotiationRequests.Service.getIDByNegotiationID(orderID)
        withUsernameToken.Ok(views.html.component.master.buyerExecuteOrderDocument(masterTransactionNegotiationFiles.Service.getOrNone(requestID, constants.File.FIAT_PROOF), requestID, constants.File.FIAT_PROOF))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def buyerExecuteOrderForm(requestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val negotiation = blockchainNegotiations.Service.get(masterTransactionNegotiationRequests.Service.getNegotiationIDByID(requestID))
        val fiatProofDocument = masterTransactionNegotiationFiles.Service.getDocuments(requestID, Seq(constants.File.FIAT_PROOF))
        withUsernameToken.Ok(views.html.component.master.buyerExecuteOrder(views.companion.master.BuyerExecuteOrder.form.fill(views.companion.master.BuyerExecuteOrder.Data(negotiation.sellerAddress, utilities.FileOperations.combinedHash(fiatProofDocument), negotiation.assetPegHash, 0, "")), fiatProofDocument))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def buyerExecuteOrder: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.BuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          try {
            BadRequest(views.html.component.master.buyerExecuteOrder(formWithErrors, masterTransactionNegotiationFiles.Service.getDocuments(masterTransactionNegotiationRequests.Service.getIDByNegotiationID(blockchainNegotiations.Service.getNegotiationID(loginState.address, formWithErrors.data(constants.FormField.SELLER_ADDRESS.name), formWithErrors.data(constants.FormField.PEG_HASH.name))), Seq(constants.File.FIAT_PROOF))))
          } catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        buyerExecuteOrderData => {
          try {
            transaction.process[blockchainTransaction.BuyerExecuteOrder, transactionsBuyerExecuteOrder.Request](
              entity = blockchainTransaction.BuyerExecuteOrder(from = loginState.address, buyerAddress = loginState.address, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionBuyerExecuteOrders.Service.create,
              request = transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseReq(from = loginState.address, gas = buyerExecuteOrderData.gas.toString), password = buyerExecuteOrderData.password, buyerAddress = loginState.address, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, mode = transactionMode),
              action = transactionsBuyerExecuteOrder.Service.post,
              onSuccess = blockchainTransactionBuyerExecuteOrders.Utility.onSuccess,
              onFailure = blockchainTransactionBuyerExecuteOrders.Utility.onFailure,
              updateTransactionHash = blockchainTransactionBuyerExecuteOrders.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def moderatedBuyerExecuteOrderList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        Ok(views.html.component.master.moderatedBuyerExecuteOrderList(blockchainNegotiations.Service.getBuyerNegotiationsByOrderAndZone(blockchainOrders.Service.getAllOrderIdsWithoutFiatProofHash, blockchainACLAccounts.Service.getAddressesUnderZone(blockchainZones.Service.getID(loginState.address)))))
      } catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  //TODO username instead of Addresses
  def moderatedBuyerExecuteOrderDocument(buyerAddress: String, sellerAddress: String, pegHash: String) = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val requestID = masterTransactionNegotiationRequests.Service.getIDByNegotiationID(blockchainNegotiations.Service.getNegotiationID(buyerAddress,sellerAddress,pegHash))
        withUsernameToken.Ok(views.html.component.master.moderatedBuyerExecuteOrderDocument(masterTransactionNegotiationFiles.Service.getOrNone(requestID, constants.File.FIAT_PROOF), requestID, constants.File.FIAT_PROOF))

      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def moderatedBuyerExecuteOrderForm(requestID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
    try {
      val negotiation = blockchainNegotiations.Service.get(masterTransactionNegotiationRequests.Service.getNegotiationIDByID(requestID))
      val fiatProofDocument = masterTransactionNegotiationFiles.Service.getDocuments(requestID, Seq(constants.File.FIAT_PROOF))
      withUsernameToken.Ok(views.html.component.master.moderatedBuyerExecuteOrder(views.companion.master.ModeratedBuyerExecuteOrder.form.fill(views.companion.master.ModeratedBuyerExecuteOrder.Data(negotiation.buyerAddress, negotiation.sellerAddress, utilities.FileOperations.combinedHash(fiatProofDocument), negotiation.assetPegHash, 0,"")), fiatProofDocument))
    }catch{
      case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
    }
  }

  def moderatedBuyerExecuteOrder: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ModeratedBuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          try{
          BadRequest(views.html.component.master.moderatedBuyerExecuteOrder(formWithErrors, masterTransactionNegotiationFiles.Service.getDocuments(masterTransactionNegotiationRequests.Service.getIDByNegotiationID(blockchainNegotiations.Service.getNegotiationID(formWithErrors.data(constants.FormField.BUYER_ADDRESS.name), formWithErrors.data(constants.FormField.SELLER_ADDRESS.name), formWithErrors.data(constants.FormField.PEG_HASH.name))), Seq(constants.File.FIAT_PROOF))))
          }catch{
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        moderatedBuyerExecuteOrderData => {
          try {
            transaction.process[blockchainTransaction.BuyerExecuteOrder, transactionsBuyerExecuteOrder.Request](
              entity = blockchainTransaction.BuyerExecuteOrder(from = loginState.address, buyerAddress = moderatedBuyerExecuteOrderData.buyerAddress, sellerAddress = moderatedBuyerExecuteOrderData.sellerAddress, fiatProofHash = moderatedBuyerExecuteOrderData.fiatProofHash, pegHash = moderatedBuyerExecuteOrderData.pegHash, gas = moderatedBuyerExecuteOrderData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionBuyerExecuteOrders.Service.create,
              request = transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseReq(from = loginState.address, gas = moderatedBuyerExecuteOrderData.gas.toString), password = moderatedBuyerExecuteOrderData.password, buyerAddress = moderatedBuyerExecuteOrderData.buyerAddress, sellerAddress = moderatedBuyerExecuteOrderData.sellerAddress, fiatProofHash = moderatedBuyerExecuteOrderData.fiatProofHash, pegHash = moderatedBuyerExecuteOrderData.pegHash, mode = transactionMode),
              action = transactionsBuyerExecuteOrder.Service.post,
              onSuccess = blockchainTransactionBuyerExecuteOrders.Utility.onSuccess,
              onFailure = blockchainTransactionBuyerExecuteOrders.Utility.onFailure,
              updateTransactionHash = blockchainTransactionBuyerExecuteOrders.Service.updateTransactionHash
            )
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainBuyerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.buyerExecuteOrder())
  }

  def blockchainBuyerExecuteOrder: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.BuyerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.buyerExecuteOrder(formWithErrors))
      },
      buyerExecuteOrderData => {
        try {
          transactionsBuyerExecuteOrder.Service.post(transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseReq(from = buyerExecuteOrderData.from, gas = buyerExecuteOrderData.gas.toString), password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, mode = buyerExecuteOrderData.mode))
          Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
