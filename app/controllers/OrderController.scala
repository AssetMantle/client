package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Asset, Negotiation}
import models.masterTransaction.NegotiationFile
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                transaction: utilities.Transaction,
                                masterAccounts: master.Accounts,
                                blockchainOrders: blockchain.Orders,
                                masterTraders: master.Traders,
                                masterZones: master.Zones,
                                masterAssets: master.Assets,
                                masterNegotiations: master.Negotiations,
                                masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                masterTransactionTradeActivities: masterTransaction.TradeActivities,
                                withZoneLoginAction: WithZoneLoginAction,
                                withTraderLoginAction: WithTraderLoginAction,
                                transactionsBuyerExecuteOrder: transactions.BuyerExecuteOrder,
                                blockchainTransactionBuyerExecuteOrders: blockchainTransaction.BuyerExecuteOrders,
                                transactionsSellerExecuteOrder: transactions.SellerExecuteOrder,
                                utilitiesNotification: utilities.Notification,
                                blockchainTransactionSellerExecuteOrders: blockchainTransaction.SellerExecuteOrders,
                                blockchainNegotiations: blockchain.Negotiations,
                                withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ORDER

  def moderatedBuyerExecuteForm(id: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(id)
      //TODO: Integrate with WesterUnion
      val fiatProof = Future("TODO: Integrate with WesterUnion")

      def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

      def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

      def getResult(buyerAccountID: String, sellerAccountID: String, asset: Asset, fiatProof: String): Future[Result] = {
        if (asset.status == constants.Status.Asset.ISSUED) {
          withUsernameToken.Ok(views.html.component.master.moderatedBuyerExecuteOrder(views.companion.master.ModeratedBuyerExecuteOrder.form.fill(views.companion.master.ModeratedBuyerExecuteOrder.Data(buyerAccountID = buyerAccountID, sellerAccountID = sellerAccountID, assetID = asset.id, gas = 0, password = "")), fiatProof = fiatProof))
        } else throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        negotiation <- negotiation
        fiatProof <- fiatProof
        buyerAccountID <- getTraderAccountID(negotiation.buyerTraderID)
        sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
        asset <- getAsset(negotiation.assetID)
        result <- getResult(buyerAccountID = buyerAccountID, sellerAccountID = sellerAccountID, asset = asset, fiatProof = fiatProof)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def moderatedBuyerExecute: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ModeratedBuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          //TODO: Integrate with WesterUnion
          val fiatProof = Future("TODO: Integrate with WesterUnion")

          def getTraderID(accountID: String): Future[String] = masterTraders.Service.tryGetID(accountID)

          def negotiation(buyerTraderID: String, sellerTraderID: String): Future[Negotiation] = masterNegotiations.Service.tryGetByBuyerSellerTraderIDAndAssetID(buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = formWithErrors.data(constants.FormField.ASSET_ID.name))

          def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

          def getResult(buyerAccountID: String, sellerAccountID: String, asset: Asset, fiatProof: String): Result = {
            if (asset.status == constants.Status.Asset.ISSUED) {
              BadRequest(views.html.component.master.moderatedBuyerExecuteOrder(formWithErrors, fiatProof = fiatProof))
            } else throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          (for {
            buyerTraderID <- getTraderID(formWithErrors.data(constants.FormField.BUYER_ACCOUNT_ID.name))
            sellerTraderID <- getTraderID(formWithErrors.data(constants.FormField.SELLER_ACCOUNT_ID.name))
            negotiation <- negotiation(buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID)
            fiatProof <- fiatProof
            asset <- getAsset(negotiation.assetID)
          } yield getResult(buyerAccountID = formWithErrors.data(constants.FormField.BUYER_ACCOUNT_ID.name), sellerAccountID = formWithErrors.data(constants.FormField.SELLER_ACCOUNT_ID.name), asset = asset, fiatProof = fiatProof)
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        moderatedBuyerExecuteData => {
          val asset = masterAssets.Service.tryGet(moderatedBuyerExecuteData.assetID)

          def getTraderID(accountID: String): Future[String] = masterTraders.Service.tryGetID(accountID)

          def negotiation(buyerTraderID: String, sellerTraderID: String): Future[Negotiation] = masterNegotiations.Service.tryGetByBuyerSellerTraderIDAndAssetID(buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = moderatedBuyerExecuteData.assetID)

          def getAddress(accountID: String): Future[String] = masterAccounts.Service.tryGetAddress(accountID)

          //TODO: Integrate with WesterUnion
          def fiatProofHash: Future[String] = Future("fiatProofHash")

          def sendTransaction(buyerAddress: String, sellerAddress: String, asset: Asset, fiatProofHash: String): Future[String] = {
            if (asset.status == constants.Status.Asset.ISSUED) {
              asset.pegHash match {
                case Some(pegHash) => transaction.process[blockchainTransaction.BuyerExecuteOrder, transactionsBuyerExecuteOrder.Request](
                  entity = blockchainTransaction.BuyerExecuteOrder(from = loginState.address, buyerAddress = buyerAddress, sellerAddress = sellerAddress, fiatProofHash = fiatProofHash, pegHash = pegHash, gas = moderatedBuyerExecuteData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionBuyerExecuteOrders.Service.create,
                  request = transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseReq(from = loginState.address, gas = moderatedBuyerExecuteData.gas.toString), password = moderatedBuyerExecuteData.password, buyerAddress = buyerAddress, sellerAddress = sellerAddress, fiatProofHash = fiatProofHash, pegHash = pegHash, mode = transactionMode),
                  action = transactionsBuyerExecuteOrder.Service.post,
                  onSuccess = blockchainTransactionBuyerExecuteOrders.Utility.onSuccess,
                  onFailure = blockchainTransactionBuyerExecuteOrders.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionBuyerExecuteOrders.Service.updateTransactionHash
                )
                case None => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
              }
            } else throw new BaseException(constants.Response.UNAUTHORIZED)

          }

          (for {
            asset <- asset
            buyerTraderID <- getTraderID(moderatedBuyerExecuteData.buyerAccountID)
            sellerTraderID <- getTraderID(moderatedBuyerExecuteData.sellerAccountID)
            negotiation <- negotiation(buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID)
            buyerAddress <- getAddress(moderatedBuyerExecuteData.buyerAccountID)
            sellerAddress <- getAddress(moderatedBuyerExecuteData.sellerAccountID)
            fiatProofHash <- fiatProofHash
            ticketID <- sendTransaction(buyerAddress = buyerAddress, sellerAddress = sellerAddress, asset = asset, fiatProofHash = fiatProofHash)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.MODERATED_BUY_ORDER_EXECUTED, ticketID)
            _ <- utilitiesNotification.send(moderatedBuyerExecuteData.buyerAccountID, constants.Notification.MODERATED_BUY_ORDER_EXECUTED, ticketID)
            _ <- masterTransactionTradeActivities.Service.create(negotiation.id, constants.TradeActivity.MODERATED_BUY_ORDER_EXECUTED, ticketID)
            result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def moderatedSellerExecuteForm(id: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationID = masterNegotiations.Service.tryGetNegotiationIDByID(id)
      val awbProofDocuments = masterTransactionNegotiationFiles.Service.getDocuments(id = id, Seq(constants.File.AWB_PROOF))

      def negotiation(negotiationID: String): Future[Negotiation] = blockchainNegotiations.Service.get(negotiationID)

      (for {
        negotiationID <- negotiationID
        awbProofDocuments <- awbProofDocuments
        negotiation <- negotiation(negotiationID)
        result <- withUsernameToken.Ok(views.html.component.master.moderatedSellerExecuteOrder(views.companion.master.ModeratedSellerExecuteOrder.form.fill(views.companion.master.ModeratedSellerExecuteOrder.Data(buyerAddress = negotiation.buyerAddress, sellerAddress = negotiation.sellerAddress, awbProofHash = utilities.FileOperations.getDocumentsHash(awbProofDocuments: _*), pegHash = negotiation.assetPegHash, gas = 0, password = "")), awbProofDocuments))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def moderatedSellerExecute: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
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
