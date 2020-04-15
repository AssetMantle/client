package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.Negotiation
import models.master.{Accounts, Negotiations}
import models.masterTransaction.NegotiationFile
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SellerExecuteOrderController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, masterAccounts: master.Accounts, masterNegotiations: Negotiations, masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles, blockchainOrders: blockchain.Orders, blockchainAccounts: blockchain.Accounts, withTraderLoginAction: WithTraderLoginAction, withZoneLoginAction: WithZoneLoginAction, transactionsSellerExecuteOrder: transactions.SellerExecuteOrder, blockchainTransactionSellerExecuteOrders: blockchainTransaction.SellerExecuteOrders, accounts: Accounts, blockchainACLAccounts: blockchain.ACLAccounts, blockchainZones: blockchain.Zones, blockchainNegotiations: blockchain.Negotiations, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val module: String = constants.Module.CONTROLLERS_SELLER_EXECUTE_ORDER

  //TODO username instead of Addresses
  def sellerExecuteOrderDocument(orderID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val requestID = masterNegotiations.Service.tryGetNegotiationIDByID(orderID)

      def getNegotiationFiles(requestID: String): Future[Option[NegotiationFile]] = masterTransactionNegotiationFiles.Service.getOrNone(requestID, constants.File.AWB_PROOF)

      (for {
        requestID <- requestID
        negotiationFiles <- getNegotiationFiles(requestID)
        result <- withUsernameToken.Ok(views.html.component.master.sellerExecuteOrderDocument(negotiationFiles, requestID, constants.File.AWB_PROOF))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def sellerExecuteOrderForm(requestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationID = masterNegotiations.Service.tryGetNegotiationIDByID(requestID)

      def negotiation(negotiationID: String): Future[Negotiation] = blockchainNegotiations.Service.get(negotiationID)

      val awbProofDocument = masterTransactionNegotiationFiles.Service.getDocuments(requestID, Seq(constants.File.AWB_PROOF))
      (for {
        negotiationID <- negotiationID
        negotiation <- negotiation(negotiationID)
        awbProofDocument <- awbProofDocument
        result <- withUsernameToken.Ok(views.html.component.master.sellerExecuteOrder(views.companion.master.SellerExecuteOrder.form.fill(views.companion.master.SellerExecuteOrder.Data(negotiation.buyerAddress, utilities.FileOperations.combinedHash(awbProofDocument), negotiation.assetPegHash, 0, "")), awbProofDocument))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def sellerExecuteOrder: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SellerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          val negotiationID = blockchainNegotiations.Service.getNegotiationID(formWithErrors.data(constants.FormField.BUYER_ADDRESS.name), loginState.address, formWithErrors.data(constants.FormField.PEG_HASH.name)).map(_.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND)))

          def getNegotiationRequestID(negotiationID: String): Future[String] = masterNegotiations.Service.tryGetNegotiationIDByID(negotiationID)

          def getNegotiationFiles(requestID: String): Future[Seq[NegotiationFile]] = masterTransactionNegotiationFiles.Service.getDocuments(requestID, Seq(constants.File.FIAT_PROOF))

          (for {
            negotiationID <- negotiationID
            requestID <- getNegotiationRequestID(negotiationID)
            negotiationFiles <- getNegotiationFiles(requestID)
          } yield BadRequest(views.html.component.master.sellerExecuteOrder(formWithErrors, negotiationFiles))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        sellerExecuteOrderData => {
          val transactionProcess = transaction.process[blockchainTransaction.SellerExecuteOrder, transactionsSellerExecuteOrder.Request](
            entity = blockchainTransaction.SellerExecuteOrder(from = loginState.address, sellerAddress = loginState.address, buyerAddress = sellerExecuteOrderData.buyerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, gas = sellerExecuteOrderData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionSellerExecuteOrders.Service.create,
            request = transactionsSellerExecuteOrder.Request(transactionsSellerExecuteOrder.BaseReq(from = loginState.address, gas = sellerExecuteOrderData.gas.toString), password = sellerExecuteOrderData.password, sellerAddress = loginState.address, buyerAddress = sellerExecuteOrderData.buyerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, mode = transactionMode),
            action = transactionsSellerExecuteOrder.Service.post,
            onSuccess = blockchainTransactionSellerExecuteOrders.Utility.onSuccess,
            onFailure = blockchainTransactionSellerExecuteOrders.Utility.onFailure,
            updateTransactionHash = blockchainTransactionSellerExecuteOrders.Service.updateTransactionHash
          )
          (for {
            _ <- transactionProcess
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SELLER_ORDER_EXECUTED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def moderatedSellerExecuteOrderList: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = blockchainZones.Service.getID(loginState.address)
      val allOrderIdsWithoutAWBProofHash = blockchainOrders.Service.getAllOrderIdsWithoutAWBProofHash

      def addressesUnderZone(id: String): Future[Seq[String]] = blockchainACLAccounts.Service.getAddressesUnderZone(id)

      def getSellerNegotiationsByOrderAndZone(allOrderIdsWithoutAWBProofHash: Seq[String], addresses: Seq[String]): Future[Seq[Negotiation]] = blockchainNegotiations.Service.getSellerNegotiationsByOrderAndZone(allOrderIdsWithoutAWBProofHash, addresses)

      (for {
        id <- id
        allOrderIdsWithoutAWBProofHash <- allOrderIdsWithoutAWBProofHash
        addressesUnderZone <- addressesUnderZone(id)
        sellerNegotiationsByOrderAndZone <- getSellerNegotiationsByOrderAndZone(allOrderIdsWithoutAWBProofHash, addressesUnderZone)
      } yield Ok(views.html.component.master.moderatedSellerExecuteOrderList(sellerNegotiationsByOrderAndZone))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  //TODO username instead of Addresses
  def moderatedSellerExecuteOrderDocument(buyerAddress: String, sellerAddress: String, pegHash: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress, sellerAddress, pegHash).map(_.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND)))

      def getNegotiationRequestID(negotiationID: String): Future[String] = masterNegotiations.Service.tryGetNegotiationIDByID(negotiationID)

      def negotiationFiles(requestID: String): Future[Option[NegotiationFile]] = masterTransactionNegotiationFiles.Service.getOrNone(requestID, constants.File.AWB_PROOF)

      (for {
        negotiationID <- negotiationID
        requestID <- getNegotiationRequestID(negotiationID)
        negotiationFiles <- negotiationFiles(requestID)
        result <- withUsernameToken.Ok(views.html.component.master.moderatedSellerExecuteOrderDocument(negotiationFiles, requestID, constants.File.AWB_PROOF))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def moderatedSellerExecuteOrderForm(requestID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationID = masterNegotiations.Service.tryGetNegotiationIDByID(requestID)

      def negotiation(negotiationID: String): Future[Negotiation] = blockchainNegotiations.Service.get(negotiationID)

      val awbProofDocument = masterTransactionNegotiationFiles.Service.getDocuments(requestID, Seq(constants.File.AWB_PROOF))
      (for {
        negotiationID <- negotiationID
        negotiation <- negotiation(negotiationID)
        awbProofDocument <- awbProofDocument
        result <- withUsernameToken.Ok(views.html.component.master.moderatedSellerExecuteOrder(views.companion.master.ModeratedSellerExecuteOrder.form.fill(views.companion.master.ModeratedSellerExecuteOrder.Data(negotiation.buyerAddress, negotiation.sellerAddress, utilities.FileOperations.combinedHash(awbProofDocument), negotiation.assetPegHash, 0, "")), awbProofDocument))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def moderatedSellerExecuteOrder: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ModeratedSellerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          val negotiationID = blockchainNegotiations.Service.getNegotiationID(formWithErrors.data(constants.FormField.BUYER_ADDRESS.name), formWithErrors.data(constants.FormField.SELLER_ADDRESS.name), formWithErrors.data(constants.FormField.PEG_HASH.name)).map(_.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND)))

          def getNegotiationRequestID(negotiationID: String): Future[String] = masterNegotiations.Service.tryGetNegotiationIDByID(negotiationID)

          def negotiationFiles(requestID: String): Future[Seq[NegotiationFile]] = masterTransactionNegotiationFiles.Service.getDocuments(requestID, Seq(constants.File.FIAT_PROOF))

          (for {
            negotiationID <- negotiationID
            requestID <- getNegotiationRequestID(negotiationID)
            negotiationFiles <- negotiationFiles(requestID)
          } yield BadRequest(views.html.component.master.moderatedSellerExecuteOrder(formWithErrors, negotiationFiles))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        moderatedSellerExecuteOrderData => {
          val transactionProcess = transaction.process[blockchainTransaction.SellerExecuteOrder, transactionsSellerExecuteOrder.Request](
            entity = blockchainTransaction.SellerExecuteOrder(from = loginState.address, buyerAddress = moderatedSellerExecuteOrderData.buyerAddress, sellerAddress = moderatedSellerExecuteOrderData.sellerAddress, awbProofHash = moderatedSellerExecuteOrderData.awbProofHash, pegHash = moderatedSellerExecuteOrderData.pegHash, gas = moderatedSellerExecuteOrderData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionSellerExecuteOrders.Service.create,
            request = transactionsSellerExecuteOrder.Request(transactionsSellerExecuteOrder.BaseReq(from = loginState.address, gas = moderatedSellerExecuteOrderData.gas.toString), password = moderatedSellerExecuteOrderData.password, buyerAddress = moderatedSellerExecuteOrderData.buyerAddress, sellerAddress = moderatedSellerExecuteOrderData.sellerAddress, awbProofHash = moderatedSellerExecuteOrderData.awbProofHash, pegHash = moderatedSellerExecuteOrderData.pegHash, mode = transactionMode),
            action = transactionsSellerExecuteOrder.Service.post,
            onSuccess = blockchainTransactionSellerExecuteOrders.Utility.onSuccess,
            onFailure = blockchainTransactionSellerExecuteOrders.Utility.onFailure,
            updateTransactionHash = blockchainTransactionSellerExecuteOrders.Service.updateTransactionHash
          )
          (for {
            _ <- transactionProcess
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SELLER_ORDER_EXECUTED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainSellerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.sellerExecuteOrder())
  }

  def blockchainSellerExecuteOrder: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.SellerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.sellerExecuteOrder(formWithErrors)))
      },
      sellerExecuteOrderData => {
        val post = transactionsSellerExecuteOrder.Service.post(transactionsSellerExecuteOrder.Request(transactionsSellerExecuteOrder.BaseReq(from = sellerExecuteOrderData.from, gas = sellerExecuteOrderData.gas.toString), password = sellerExecuteOrderData.password, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, mode = sellerExecuteOrderData.mode))
        (for {
          _ <- post
        } yield Ok(views.html.index(successes = Seq(constants.Response.SELLER_ORDER_EXECUTED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
