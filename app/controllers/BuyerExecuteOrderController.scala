package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BuyerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, masterTransactionNegotiationRequests: masterTransaction.NegotiationRequests, masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles, withZoneLoginAction: WithZoneLoginAction, withTraderLoginAction: WithTraderLoginAction, transactionsBuyerExecuteOrder: transactions.BuyerExecuteOrder, blockchainTransactionBuyerExecuteOrders: blockchainTransaction.BuyerExecuteOrders, blockchainACLAccounts: blockchain.ACLAccounts, blockchainZones: blockchain.Zones, blockchainNegotiations: blockchain.Negotiations, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_BUYER_EXECUTE_ORDER

  //TODO username instead of Addresses
  def buyerExecuteOrderDocument(orderID: String) = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val requestID = masterTransactionNegotiationRequests.Service.getIDByNegotiationID(orderID)

      def negotiationFiles(requestID: String) = masterTransactionNegotiationFiles.Service.getOrNone(requestID, constants.File.FIAT_PROOF)

      (for {
        requestID <- requestID
        negotiationFiles <- negotiationFiles(requestID)
      } yield withUsernameToken.Ok(views.html.component.master.buyerExecuteOrderDocument(negotiationFiles, requestID, constants.File.FIAT_PROOF))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def buyerExecuteOrderForm(requestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationID = masterTransactionNegotiationRequests.Service.getNegotiationIDByID(requestID)
      val fiatProofDocument = masterTransactionNegotiationFiles.Service.getDocuments(requestID, Seq(constants.File.FIAT_PROOF))

      def negotiation(negotiationID: String) = blockchainNegotiations.Service.get(negotiationID)

      (for {
        negotiationID <- negotiationID
        fiatProofDocument <- fiatProofDocument
        negotiation <- negotiation(negotiationID)
      } yield withUsernameToken.Ok(views.html.component.master.buyerExecuteOrder(views.companion.master.BuyerExecuteOrder.form.fill(views.companion.master.BuyerExecuteOrder.Data(negotiation.sellerAddress, utilities.FileOperations.combinedHash(fiatProofDocument), negotiation.assetPegHash, 0, "")), fiatProofDocument))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def buyerExecuteOrder: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.BuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          val negotiationID = blockchainNegotiations.Service.getNegotiationID(loginState.address, formWithErrors.data(constants.FormField.SELLER_ADDRESS.name), formWithErrors.data(constants.FormField.PEG_HASH.name))

          def getNegotiationRequestID(negotiationID: String) = masterTransactionNegotiationRequests.Service.getIDByNegotiationID(negotiationID)

          def getNegotiationFiles(negotiationRequestID: String) = masterTransactionNegotiationFiles.Service.getDocuments(negotiationRequestID, Seq(constants.File.FIAT_PROOF))

          (for {
            negotiationID <- negotiationID
            negotiationRequestID <- getNegotiationRequestID(negotiationID)
            negotiationFiles <- getNegotiationFiles(negotiationRequestID)
          } yield BadRequest(views.html.component.master.buyerExecuteOrder(formWithErrors, negotiationFiles))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        buyerExecuteOrderData => {
          val transactionProcess = transaction.process[blockchainTransaction.BuyerExecuteOrder, transactionsBuyerExecuteOrder.Request](
            entity = blockchainTransaction.BuyerExecuteOrder(from = loginState.address, buyerAddress = loginState.address, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, gas = buyerExecuteOrderData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionBuyerExecuteOrders.Service.create,
            request = transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseReq(from = loginState.address, gas = buyerExecuteOrderData.gas.toString), password = buyerExecuteOrderData.password, buyerAddress = loginState.address, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, mode = transactionMode),
            action = transactionsBuyerExecuteOrder.Service.post,
            onSuccess = blockchainTransactionBuyerExecuteOrders.Utility.onSuccess,
            onFailure = blockchainTransactionBuyerExecuteOrders.Utility.onFailure,
            updateTransactionHash = blockchainTransactionBuyerExecuteOrders.Service.updateTransactionHash
          )
          (for {
            _ <- transactionProcess
          } yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def moderatedBuyerExecuteOrderList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = blockchainZones.Service.getID(loginState.address)
      val allOrderIdsWithoutFiatProofHash = blockchainOrders.Service.getAllOrderIdsWithoutFiatProofHash

      def getAddressesUnderZone(id: String) = blockchainACLAccounts.Service.getAddressesUnderZone(id)

      def getBuyerNegotiationsByOrderAndZone(ids: Seq[String], addresses: Seq[String]) = blockchainNegotiations.Service.getBuyerNegotiationsByOrderAndZone(ids, addresses)

      (for {
        id <- id
        allOrderIdsWithoutFiatProofHash <- allOrderIdsWithoutFiatProofHash
        addressesUnderZone <- getAddressesUnderZone(id)
        buyerNegotiationsByOrderAndZone <- getBuyerNegotiationsByOrderAndZone(allOrderIdsWithoutFiatProofHash, addressesUnderZone)
      } yield Ok(views.html.component.master.moderatedBuyerExecuteOrderList(buyerNegotiationsByOrderAndZone))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  //TODO username instead of Addresses
  def moderatedBuyerExecuteOrderDocument(buyerAddress: String, sellerAddress: String, pegHash: String) = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress, sellerAddress, pegHash)

      def getNegotiationRequestID(negotiationID: String) = masterTransactionNegotiationRequests.Service.getIDByNegotiationID(negotiationID)

      def getNegotiationFiles(requestID: String) = masterTransactionNegotiationFiles.Service.getOrNone(requestID, constants.File.FIAT_PROOF)

      (for {
        negotiationID <- negotiationID
        requestID <- getNegotiationRequestID(negotiationID)
        negotiationFiles <- getNegotiationFiles(requestID)
      } yield withUsernameToken.Ok(views.html.component.master.moderatedBuyerExecuteOrderDocument(negotiationFiles, requestID, constants.File.FIAT_PROOF))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def moderatedBuyerExecuteOrderForm(requestID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val negotiationID = masterTransactionNegotiationRequests.Service.getNegotiationIDByID(requestID)
      val fiatProofDocument = masterTransactionNegotiationFiles.Service.getDocuments(requestID, Seq(constants.File.FIAT_PROOF))

      def negotiation(negotiationID: String) = blockchainNegotiations.Service.get(negotiationID)

      (for {
        negotiationID <- negotiationID
        fiatProofDocument <- fiatProofDocument
        negotiation <- negotiation(negotiationID)
      } yield withUsernameToken.Ok(views.html.component.master.moderatedBuyerExecuteOrder(views.companion.master.ModeratedBuyerExecuteOrder.form.fill(views.companion.master.ModeratedBuyerExecuteOrder.Data(negotiation.buyerAddress, negotiation.sellerAddress, utilities.FileOperations.combinedHash(fiatProofDocument), negotiation.assetPegHash, 0, "")), fiatProofDocument))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def moderatedBuyerExecuteOrder: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ModeratedBuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {

          val negotiationID = blockchainNegotiations.Service.getNegotiationID(formWithErrors.data(constants.FormField.BUYER_ADDRESS.name), formWithErrors.data(constants.FormField.SELLER_ADDRESS.name), formWithErrors.data(constants.FormField.PEG_HASH.name))

          def getNegotiationRequestID(negotiationID: String) = masterTransactionNegotiationRequests.Service.getIDByNegotiationID(negotiationID)

          def getNegotiationFiles(requestID: String) = masterTransactionNegotiationFiles.Service.getDocuments(requestID, Seq(constants.File.FIAT_PROOF))

          (for {
            negotiationID <- negotiationID
            requestID <- getNegotiationRequestID(negotiationID)
            negotiationFiles <- getNegotiationFiles(requestID)
          } yield BadRequest(views.html.component.master.moderatedBuyerExecuteOrder(formWithErrors, negotiationFiles))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        moderatedBuyerExecuteOrderData => {
          val transactionProcess = transaction.process[blockchainTransaction.BuyerExecuteOrder, transactionsBuyerExecuteOrder.Request](
            entity = blockchainTransaction.BuyerExecuteOrder(from = loginState.address, buyerAddress = moderatedBuyerExecuteOrderData.buyerAddress, sellerAddress = moderatedBuyerExecuteOrderData.sellerAddress, fiatProofHash = moderatedBuyerExecuteOrderData.fiatProofHash, pegHash = moderatedBuyerExecuteOrderData.pegHash, gas = moderatedBuyerExecuteOrderData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionBuyerExecuteOrders.Service.create,
            request = transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseReq(from = loginState.address, gas = moderatedBuyerExecuteOrderData.gas.toString), password = moderatedBuyerExecuteOrderData.password, buyerAddress = moderatedBuyerExecuteOrderData.buyerAddress, sellerAddress = moderatedBuyerExecuteOrderData.sellerAddress, fiatProofHash = moderatedBuyerExecuteOrderData.fiatProofHash, pegHash = moderatedBuyerExecuteOrderData.pegHash, mode = transactionMode),
            action = transactionsBuyerExecuteOrder.Service.post,
            onSuccess = blockchainTransactionBuyerExecuteOrders.Utility.onSuccess,
            onFailure = blockchainTransactionBuyerExecuteOrders.Utility.onFailure,
            updateTransactionHash = blockchainTransactionBuyerExecuteOrders.Service.updateTransactionHash
          )
          (for {
            _ <- transactionProcess
          } yield withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
            ).recover {
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
