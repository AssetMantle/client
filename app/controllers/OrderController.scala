package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.Negotiation
import models.masterTransaction.NegotiationFile
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                transaction: utilities.Transaction, masterAccounts: master.Accounts,
                                blockchainOrders: blockchain.Orders,
                                blockchainAccounts: blockchain.Accounts,
                                masterTraders: master.Traders,
                                masterZones: master.Zones,
                                masterNegotiations: master.Negotiations,
                                masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                withZoneLoginAction: WithZoneLoginAction,
                                withTraderLoginAction: WithTraderLoginAction,
                                transactionsBuyerExecuteOrder: transactions.BuyerExecuteOrder,
                                blockchainTransactionBuyerExecuteOrders: blockchainTransaction.BuyerExecuteOrders,
                                transactionsSellerExecuteOrder: transactions.SellerExecuteOrder,
                                blockchainTransactionSellerExecuteOrders: blockchainTransaction.SellerExecuteOrders,
                                blockchainACLAccounts: blockchain.ACLAccounts,
                                blockchainZones: blockchain.Zones, blockchainNegotiations:
                                blockchain.Negotiations,
                                withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ORDER

  def buyerExecuteOrderDocument(orderID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val requestID = masterNegotiations.Service.tryGetNegotiationIDByID(orderID)

      def negotiationFiles(requestID: String): Future[Option[NegotiationFile]] = masterTransactionNegotiationFiles.Service.getOrNone(requestID, constants.File.FIAT_PROOF)

      (for {
        requestID <- requestID
        negotiationFiles <- negotiationFiles(requestID)
        result <- withUsernameToken.Ok(views.html.component.master.buyerExecuteOrderDocument(negotiationFiles, requestID, constants.File.FIAT_PROOF))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def buyerExecuteOrderForm(requestID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationID = masterNegotiations.Service.tryGetNegotiationIDByID(requestID)
      val fiatProofDocument = masterTransactionNegotiationFiles.Service.getDocuments(requestID, Seq(constants.File.FIAT_PROOF))

      def negotiation(negotiationID: String): Future[Negotiation] = blockchainNegotiations.Service.get(negotiationID)

      (for {
        negotiationID <- negotiationID
        fiatProofDocument <- fiatProofDocument
        negotiation <- negotiation(negotiationID)
        result <- withUsernameToken.Ok(views.html.component.master.buyerExecuteOrder(views.companion.master.BuyerExecuteOrder.form.fill(views.companion.master.BuyerExecuteOrder.Data(negotiation.sellerAddress, utilities.FileOperations.combinedHash(fiatProofDocument), negotiation.assetPegHash, 0, "")), fiatProofDocument))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def buyerExecuteOrder: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.BuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          val negotiationID = blockchainNegotiations.Service.getNegotiationID(loginState.address, formWithErrors.data(constants.FormField.SELLER_ADDRESS.name), formWithErrors.data(constants.FormField.PEG_HASH.name)).map(_.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND)))

          def getNegotiationRequestID(negotiationID: String): Future[String] = masterNegotiations.Service.tryGetNegotiationIDByID(negotiationID)

          def getNegotiationFiles(negotiationRequestID: String): Future[Seq[NegotiationFile]] = masterTransactionNegotiationFiles.Service.getDocuments(negotiationRequestID, Seq(constants.File.FIAT_PROOF))

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
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
          } yield result
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

      def getAddressesUnderZone(id: String): Future[Seq[String]] = blockchainACLAccounts.Service.getAddressesUnderZone(id)

      def getBuyerNegotiationsByOrderAndZone(ids: Seq[String], addresses: Seq[String]): Future[Seq[Negotiation]] = blockchainNegotiations.Service.getBuyerNegotiationsByOrderAndZone(ids, addresses)

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

  def moderatedBuyerExecuteOrderDocument(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      def tradersZoneIDs(traderIDs: Seq[String]): Future[Seq[String]] = masterTraders.Service.tryGetZoneIDs(traderIDs)

      def getResult(zoneID: String, tradersZoneIDs: Seq[String]): Future[Result] = {
        if (tradersZoneIDs contains zoneID){
          val getNegotiationFiles: Future[Option[NegotiationFile]] = masterTransactionNegotiationFiles.Service.getOrNone(negotiationID, constants.File.FIAT_PROOF)
          for{
            negotiationFiles <- getNegotiationFiles
            result <- withUsernameToken.Ok(views.html.component.master.moderatedBuyerExecuteOrderDocument(negotiationFiles, negotiationID, constants.File.FIAT_PROOF))
          } yield result
        } else {
          Future(Unauthorized(views.html.trades(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }
      (for{
        zoneID <- zoneID
        negotiation <- negotiation
        tradersZoneIDs <- tradersZoneIDs(Seq(negotiation.buyerTraderID, negotiation.sellerTraderID))
        result <- getResult(zoneID, tradersZoneIDs)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def moderatedBuyerExecuteOrderForm(requestID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationID = masterNegotiations.Service.tryGetNegotiationIDByID(requestID)
      val fiatProofDocument = masterTransactionNegotiationFiles.Service.getDocuments(requestID, Seq(constants.File.FIAT_PROOF))

      def negotiation(negotiationID: String): Future[Negotiation] = blockchainNegotiations.Service.get(negotiationID)

      (for {
        negotiationID <- negotiationID
        fiatProofDocument <- fiatProofDocument
        negotiation <- negotiation(negotiationID)
        result <- withUsernameToken.Ok(views.html.component.master.moderatedBuyerExecuteOrder(views.companion.master.ModeratedBuyerExecuteOrder.form.fill(views.companion.master.ModeratedBuyerExecuteOrder.Data(negotiation.buyerAddress, negotiation.sellerAddress, utilities.FileOperations.combinedHash(fiatProofDocument), negotiation.assetPegHash, 0, "")), fiatProofDocument))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def moderatedBuyerExecuteOrder: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ModeratedBuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          val negotiationID = blockchainNegotiations.Service.getNegotiationID(formWithErrors.data(constants.FormField.BUYER_ADDRESS.name), formWithErrors.data(constants.FormField.SELLER_ADDRESS.name), formWithErrors.data(constants.FormField.PEG_HASH.name)).map(_.getOrElse(throw new BaseException(constants.Response.NEGOTIATION_NOT_FOUND)))

          def getNegotiationRequestID(negotiationID: String): Future[String] = masterNegotiations.Service.tryGetNegotiationIDByID(negotiationID)

          def getNegotiationFiles(requestID: String): Future[Seq[NegotiationFile]] = masterTransactionNegotiationFiles.Service.getDocuments(requestID, Seq(constants.File.FIAT_PROOF))

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
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

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

  def moderatedSellerExecuteOrderDocument(negotiationID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      def tradersZoneIDs(traderIDs: Seq[String]): Future[Seq[String]] = masterTraders.Service.tryGetZoneIDs(traderIDs)

      def getResult(zoneID: String, tradersZoneIDs: Seq[String]): Future[Result] = {
        if (tradersZoneIDs contains zoneID){
          val negotiationFiles: Future[Option[NegotiationFile]] = masterTransactionNegotiationFiles.Service.getOrNone(negotiationID, constants.File.AWB_PROOF)
          for{
            negotiationFiles <- negotiationFiles
            result <- withUsernameToken.Ok(views.html.component.master.moderatedSellerExecuteOrderDocument(negotiationFiles, negotiationID, constants.File.AWB_PROOF))
          } yield result
        } else {
          Future(Unauthorized(views.html.trades(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }

      (for{
        zoneID <- zoneID
        negotiation <- negotiation
        tradersZoneIDs <- tradersZoneIDs(Seq(negotiation.buyerTraderID, negotiation.sellerTraderID))
        result <- getResult(zoneID, tradersZoneIDs)
      } yield result).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
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

  def blockchainBuyerExecuteOrderForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.buyerExecuteOrder())
  }

  def blockchainBuyerExecuteOrder: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.BuyerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.buyerExecuteOrder(formWithErrors)))
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
