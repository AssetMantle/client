package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.ACL
import models.common.Serializable._
import models.master.{Asset, Negotiation, Order}
import models.masterTransaction.AssetFile
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderController @Inject()(
                                 blockchainTransactionBuyerExecuteOrders: blockchainTransaction.BuyerExecuteOrders,
                                 blockchainTransactionSendFiats: blockchainTransaction.SendFiats,
                                 blockchainTransactionSellerExecuteOrders: blockchainTransaction.SellerExecuteOrders,
                                 blockchainAccounts: blockchain.Accounts,
                                 blockchainACLAccounts: blockchain.ACLAccounts,
                                 blockchainACLHashes: blockchain.ACLHashes,
                                 masterAssets: master.Assets,
                                 masterNegotiations: master.Negotiations,
                                 masterOrders: master.Orders,
                                 masterTraders: master.Traders,
                                 masterZones: master.Zones,
                                 masterAccounts: master.Accounts,
                                 masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                 masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                 masterTransactionTradeActivities: masterTransaction.TradeActivities,
                                 messagesControllerComponents: MessagesControllerComponents,
                                 transaction: utilities.Transaction,
                                 transactionsBuyerExecuteOrder: transactions.BuyerExecuteOrder,
                                 transactionsSellerExecuteOrder: transactions.SellerExecuteOrder,
                                 utilitiesNotification: utilities.Notification,
                                 withTraderLoginAction: WithTraderLoginAction,
                                 withZoneLoginAction: WithZoneLoginAction,
                                 withUsernameToken: WithUsernameToken,
                                 withoutLoginAction: WithoutLoginAction,
                                 withoutLoginActionAsync: WithoutLoginActionAsync,
                               )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ORDER

  def moderatedBuyerExecuteForm(orderID: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.moderatedBuyerExecuteOrder(orderID = orderID))
  }

  def moderatedBuyerExecute: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ModeratedBuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.moderatedBuyerExecuteOrder(formWithErrors, orderID = formWithErrors.data(constants.FormField.ORDER_ID.name))))
        },
        moderatedBuyerExecuteData => {
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = moderatedBuyerExecuteData.password)
          val negotiation = masterNegotiations.Service.tryGet(moderatedBuyerExecuteData.orderID)
          val order = masterOrders.Service.tryGet(moderatedBuyerExecuteData.orderID)

          def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

          def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def getACLHash(address: String) = blockchainACLAccounts.Service.tryGetACLHash(address)

          def getACL(aclHash: String) = blockchainACLHashes.Service.tryGetACL(aclHash)

          def getFiatProofHash(buyerAddress: String, sellerAddress: String, asset: Asset): Future[String] = {
            asset.pegHash match {
              case Some(pegHash) =>
                val fiatProofs = blockchainTransactionSendFiats.Service.getFiatProofs(buyerAddress = buyerAddress, sellerAddress = sellerAddress, pegHash = pegHash)
                for {
                  fiatProofs <- fiatProofs
                } yield utilities.String.sha256Sum(fiatProofs.mkString(""))
              case None => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
            }
          }

          def sendTransactionAndGetResult(validateUsernamePassword: Boolean, buyerAccountID: String, sellerAccountID: String, buyerAddress: String, sellerAddress: String, asset: Asset, order: Order, fiatProofHash: String, buyerACL: ACL, negotiation: Negotiation): Future[Result] = {
            if (validateUsernamePassword) {
              if (asset.status == constants.Status.Asset.IN_ORDER && Seq(constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING, constants.Status.Order.BUYER_EXECUTE_ORDER_PENDING).contains(order.status) && buyerACL.buyerExecuteOrder) {
                val ticketID = asset.pegHash match {
                  case Some(pegHash) => transaction.process[blockchainTransaction.BuyerExecuteOrder, transactionsBuyerExecuteOrder.Request](
                    entity = blockchainTransaction.BuyerExecuteOrder(from = loginState.address, buyerAddress = buyerAddress, sellerAddress = sellerAddress, fiatProofHash = fiatProofHash, pegHash = pegHash, gas = moderatedBuyerExecuteData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionBuyerExecuteOrders.Service.create,
                    request = transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseReq(from = loginState.address, gas = moderatedBuyerExecuteData.gas), password = moderatedBuyerExecuteData.password, buyerAddress = buyerAddress, sellerAddress = sellerAddress, fiatProofHash = fiatProofHash, pegHash = pegHash, mode = transactionMode),
                    action = transactionsBuyerExecuteOrder.Service.post,
                    onSuccess = blockchainTransactionBuyerExecuteOrders.Utility.onSuccess,
                    onFailure = blockchainTransactionBuyerExecuteOrders.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionBuyerExecuteOrders.Service.updateTransactionHash
                  )
                  case None => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
                }
                for {
                  ticketID <- ticketID
                  _ <- utilitiesNotification.send(loginState.username, constants.Notification.MODERATED_BUY_ORDER_EXECUTED, ticketID)
                  _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.MODERATED_BUY_ORDER_EXECUTED, ticketID)
                  _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.MODERATED_BUY_ORDER_EXECUTED, ticketID)
                  _ <- masterTransactionTradeActivities.Service.create(negotiation.id, constants.TradeActivity.MODERATED_BUY_ORDER_EXECUTED, ticketID)
                  result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = moderatedBuyerExecuteData.orderID, successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
                } yield result
              } else throw new BaseException(constants.Response.UNAUTHORIZED)
            } else Future(BadRequest(views.html.component.master.moderatedBuyerExecuteOrder(views.companion.master.ModeratedBuyerExecuteOrder.form.fill(moderatedBuyerExecuteData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), orderID = moderatedBuyerExecuteData.orderID)))
          }

          (for {
            validateUsernamePassword <- validateUsernamePassword
            negotiation <- negotiation
            order <- order
            asset <- getAsset(negotiation.assetID)
            buyerAccountID <- getTraderAccountID(negotiation.buyerTraderID)
            sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
            buyerAddress <- getAddress(buyerAccountID)
            buyerACLHash <- getACLHash(buyerAddress)
            buyerACL <- getACL(buyerACLHash)
            sellerAddress <- getAddress(sellerAccountID)
            fiatProofHash <- getFiatProofHash(buyerAddress = buyerAddress, sellerAddress = sellerAddress, asset = asset)
            result <- sendTransactionAndGetResult(validateUsernamePassword = validateUsernamePassword, buyerAccountID = buyerAccountID, buyerAddress = buyerAddress, sellerAccountID = sellerAccountID, sellerAddress = sellerAddress, asset = asset, order = order, fiatProofHash = fiatProofHash, buyerACL = buyerACL, negotiation = negotiation)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = moderatedBuyerExecuteData.orderID, failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def moderatedSellerExecuteForm(orderID: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.moderatedSellerExecuteOrder(orderID = orderID))
  }

  def moderatedSellerExecute: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ModeratedSellerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.moderatedSellerExecuteOrder(formWithErrors, formWithErrors.data(constants.FormField.ORDER_ID.name))))
        },
        moderatedSellerExecuteOrderData => {
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = moderatedSellerExecuteOrderData.password)
          val negotiation = masterNegotiations.Service.tryGet(moderatedSellerExecuteOrderData.orderID)
          val order = masterOrders.Service.tryGet(moderatedSellerExecuteOrderData.orderID)

          def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

          def getBillOfLading(assetID: String): Future[AssetFile] = masterTransactionAssetFiles.Service.tryGet(id = assetID, documentType = constants.File.Asset.BILL_OF_LADING)

          def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def getACLHash(address: String) = blockchainACLAccounts.Service.tryGetACLHash(address)

          def getACL(aclHash: String) = blockchainACLHashes.Service.tryGetACL(aclHash)

          def sendTransactionAndGetResult(validateUsernamePassword: Boolean, buyerAccountID: String, sellerAccountID: String, buyerAddress: String, sellerAddress: String, asset: Asset, order: Order, billOfLading: AssetFile, sellerACL: ACL, negotiation: Negotiation): Future[Result] = {
            if (validateUsernamePassword) {
              if (asset.status == constants.Status.Asset.IN_ORDER && billOfLading.status.getOrElse(throw new BaseException(constants.Response.BILL_OF_LADING_VERIFICATION_STATUS_PENDING)) && Seq(constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING, constants.Status.Order.SELLER_EXECUTE_ORDER_PENDING).contains(order.status) && sellerACL.sellerExecuteOrder) {
                val awbProofHash = utilities.String.sha256Sum(Json.toJson(billOfLading.documentContent.getOrElse(throw new BaseException(constants.Response.BILL_OF_LADING_NOT_FOUND))).toString)
                val ticketID = asset.pegHash match {
                  case Some(pegHash) => transaction.process[blockchainTransaction.SellerExecuteOrder, transactionsSellerExecuteOrder.Request](
                    entity = blockchainTransaction.SellerExecuteOrder(from = loginState.address, buyerAddress = buyerAddress, sellerAddress = sellerAddress, awbProofHash = awbProofHash, pegHash = pegHash, gas = moderatedSellerExecuteOrderData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionSellerExecuteOrders.Service.create,
                    request = transactionsSellerExecuteOrder.Request(transactionsSellerExecuteOrder.BaseReq(from = loginState.address, gas = moderatedSellerExecuteOrderData.gas), password = moderatedSellerExecuteOrderData.password, buyerAddress = buyerAddress, sellerAddress = sellerAddress, awbProofHash = awbProofHash, pegHash = pegHash, mode = transactionMode),
                    action = transactionsSellerExecuteOrder.Service.post,
                    onSuccess = blockchainTransactionSellerExecuteOrders.Utility.onSuccess,
                    onFailure = blockchainTransactionSellerExecuteOrders.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionSellerExecuteOrders.Service.updateTransactionHash
                  )
                  case None => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
                }
                for {
                  ticketID <- ticketID
                  _ <- utilitiesNotification.send(loginState.username, constants.Notification.MODERATED_SELL_ORDER_EXECUTED, ticketID)
                  _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.MODERATED_SELL_ORDER_EXECUTED, ticketID)
                  _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.MODERATED_SELL_ORDER_EXECUTED, ticketID)
                  _ <- masterTransactionTradeActivities.Service.create(negotiation.id, constants.TradeActivity.MODERATED_SELL_ORDER_EXECUTED, ticketID)
                  result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = moderatedSellerExecuteOrderData.orderID, successes = Seq(constants.Response.SELLER_ORDER_EXECUTED)))
                } yield result
              } else throw new BaseException(constants.Response.UNAUTHORIZED)
            }
            else Future(BadRequest(views.html.component.master.moderatedSellerExecuteOrder(views.companion.master.ModeratedSellerExecuteOrder.form.fill(moderatedSellerExecuteOrderData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), orderID = moderatedSellerExecuteOrderData.orderID)))
          }


          (for {
            validateUsernamePassword <- validateUsernamePassword
            negotiation <- negotiation
            order <- order
            asset <- getAsset(negotiation.assetID)
            billOfLading <- getBillOfLading(negotiation.assetID)
            buyerAccountID <- getTraderAccountID(negotiation.buyerTraderID)
            sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
            buyerAddress <- getAddress(buyerAccountID)
            sellerAddress <- getAddress(sellerAccountID)
            sellerACLHash <- getACLHash(sellerAddress)
            sellerACL <- getACL(sellerACLHash)
            result <- sendTransactionAndGetResult(validateUsernamePassword = validateUsernamePassword, buyerAccountID = buyerAccountID, sellerAccountID = sellerAccountID, buyerAddress = buyerAddress, sellerAddress = sellerAddress, asset = asset, order = order, billOfLading = billOfLading, sellerACL = sellerACL, negotiation = negotiation)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = moderatedSellerExecuteOrderData.orderID, failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def uploadFiatProof(orderID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationFile = masterTransactionNegotiationFiles.Service.get(orderID, constants.File.Negotiation.FIAT_PROOF)
      for {
        negotiationFile <- negotiationFile
      } yield Ok(views.html.component.master.uploadFiatProof(orderID, negotiationFile))
  }

  def buyerExecuteForm(orderID: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.buyerExecuteOrder(orderID = orderID))
  }

  def buyerExecute: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.BuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.buyerExecuteOrder(formWithErrors, orderID = formWithErrors.data(constants.FormField.ORDER_ID.name))))
        },
        buyerExecuteData => {
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = buyerExecuteData.password)
          val negotiation = masterNegotiations.Service.tryGet(buyerExecuteData.orderID)
          val order = masterOrders.Service.tryGet(buyerExecuteData.orderID)
          val negotiationFile = masterTransactionNegotiationFiles.Service.tryGet(buyerExecuteData.orderID, constants.File.Negotiation.FIAT_PROOF)
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

          def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def sendTransactionAndGetResult(validateUsernamePassword: Boolean, traderID: String, buyerAccountID: String, sellerAccountID: String, buyerAddress: String, sellerAddress: String, asset: Asset, order: Order, fiatProofHash: String, negotiation: Negotiation): Future[Result] = {
            if (validateUsernamePassword) {
              if (negotiation.buyerTraderID == traderID && asset.status == constants.Status.Asset.IN_ORDER && Seq(constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING, constants.Status.Order.BUYER_EXECUTE_ORDER_PENDING).contains(order.status) && loginState.acl.getOrElse(throw new BaseException(constants.Response.UNAUTHORIZED)).buyerExecuteOrder) {
                val ticketID = asset.pegHash match {
                  case Some(pegHash) => transaction.process[blockchainTransaction.BuyerExecuteOrder, transactionsBuyerExecuteOrder.Request](
                    entity = blockchainTransaction.BuyerExecuteOrder(from = loginState.address, buyerAddress = buyerAddress, sellerAddress = sellerAddress, fiatProofHash = fiatProofHash, pegHash = pegHash, gas = buyerExecuteData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionBuyerExecuteOrders.Service.create,
                    request = transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseReq(from = loginState.address, gas = buyerExecuteData.gas), password = buyerExecuteData.password, buyerAddress = buyerAddress, sellerAddress = sellerAddress, fiatProofHash = fiatProofHash, pegHash = pegHash, mode = transactionMode),
                    action = transactionsBuyerExecuteOrder.Service.post,
                    onSuccess = blockchainTransactionBuyerExecuteOrders.Utility.onSuccess,
                    onFailure = blockchainTransactionBuyerExecuteOrders.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionBuyerExecuteOrders.Service.updateTransactionHash
                  )
                  case None => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
                }

                for {
                  ticketID <- ticketID
                  _ <- utilitiesNotification.send(loginState.username, constants.Notification.BUYER_ORDER_EXECUTED, ticketID)
                  _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.BUYER_ORDER_EXECUTED, ticketID)
                  _ <- masterTransactionTradeActivities.Service.create(negotiation.id, constants.TradeActivity.BUYER_ORDER_EXECUTED, ticketID)
                  result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = buyerExecuteData.orderID, successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
                } yield result
              } else throw new BaseException(constants.Response.UNAUTHORIZED)
            }
            else Future(BadRequest(views.html.component.master.buyerExecuteOrder(views.companion.master.BuyerExecuteOrder.form.fill(buyerExecuteData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), orderID = buyerExecuteData.orderID)))
          }

          (for {
            validateUsernamePassword <- validateUsernamePassword
            negotiation <- negotiation
            order <- order
            negotiationFile <- negotiationFile
            traderID <- traderID
            asset <- getAsset(negotiation.assetID)
            sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
            sellerAddress <- getAddress(sellerAccountID)
            result <- sendTransactionAndGetResult(validateUsernamePassword = validateUsernamePassword, traderID = traderID, buyerAccountID = loginState.username, sellerAccountID = sellerAccountID, buyerAddress = loginState.address, sellerAddress = sellerAddress, asset = asset, order = order, fiatProofHash = utilities.FileOperations.getFileNameWithoutExtension(negotiationFile.fileName), negotiation)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = buyerExecuteData.orderID, failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def sellerExecuteForm(orderID: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.sellerExecuteOrder(orderID = orderID))
  }

  def sellerExecute: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SellerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.sellerExecuteOrder(formWithErrors, formWithErrors.data(constants.FormField.ORDER_ID.name))))
        },
        sellerExecuteData => {
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = sellerExecuteData.password)
          val negotiation = masterNegotiations.Service.tryGet(sellerExecuteData.orderID)
          val order = masterOrders.Service.tryGet(sellerExecuteData.orderID)
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

          def getBillOfLading(assetID: String): Future[AssetFile] = masterTransactionAssetFiles.Service.tryGet(id = assetID, documentType = constants.File.Asset.BILL_OF_LADING)

          def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def sendTransactionAndGetResult(validateUsernamePassword: Boolean, traderID: String, buyerAccountID: String, sellerAccountID: String, buyerAddress: String, sellerAddress: String, asset: Asset, order: Order, billOfLading: AssetFile, negotiation: Negotiation): Future[Result] = {
            if (validateUsernamePassword) {
              if (negotiation.sellerTraderID == traderID && asset.status == constants.Status.Asset.IN_ORDER && billOfLading.status.getOrElse(throw new BaseException(constants.Response.BILL_OF_LADING_VERIFICATION_STATUS_PENDING)) && Seq(constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING, constants.Status.Order.SELLER_EXECUTE_ORDER_PENDING).contains(order.status) && loginState.acl.getOrElse(throw new BaseException(constants.Response.UNAUTHORIZED)).sellerExecuteOrder) {
                val awbProofHash = utilities.String.sha256Sum(Json.toJson(billOfLading.documentContent.getOrElse(throw new BaseException(constants.Response.BILL_OF_LADING_NOT_FOUND))).toString)
                val ticketID = asset.pegHash match {
                  case Some(pegHash) => transaction.process[blockchainTransaction.SellerExecuteOrder, transactionsSellerExecuteOrder.Request](
                    entity = blockchainTransaction.SellerExecuteOrder(from = loginState.address, buyerAddress = buyerAddress, sellerAddress = sellerAddress, awbProofHash = awbProofHash, pegHash = pegHash, gas = sellerExecuteData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionSellerExecuteOrders.Service.create,
                    request = transactionsSellerExecuteOrder.Request(transactionsSellerExecuteOrder.BaseReq(from = loginState.address, gas = sellerExecuteData.gas), password = sellerExecuteData.password, buyerAddress = buyerAddress, sellerAddress = sellerAddress, awbProofHash = awbProofHash, pegHash = pegHash, mode = transactionMode),
                    action = transactionsSellerExecuteOrder.Service.post,
                    onSuccess = blockchainTransactionSellerExecuteOrders.Utility.onSuccess,
                    onFailure = blockchainTransactionSellerExecuteOrders.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionSellerExecuteOrders.Service.updateTransactionHash
                  )
                  case None => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
                }
                for {
                  ticketID <- ticketID
                  _ <- utilitiesNotification.send(loginState.username, constants.Notification.SELLER_ORDER_EXECUTED, ticketID)
                  _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.SELLER_ORDER_EXECUTED, ticketID)
                  _ <- masterTransactionTradeActivities.Service.create(negotiation.id, constants.TradeActivity.SELLER_ORDER_EXECUTED, ticketID)
                  result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = sellerExecuteData.orderID, successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
                } yield result
              } else throw new BaseException(constants.Response.UNAUTHORIZED)
            }
            else Future(BadRequest(views.html.component.master.sellerExecuteOrder(views.companion.master.SellerExecuteOrder.form.fill(sellerExecuteData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), orderID = sellerExecuteData.orderID)))
          }

          (for {
            validateUsernamePassword <- validateUsernamePassword
            negotiation <- negotiation
            order <- order
            traderID <- traderID
            asset <- getAsset(negotiation.assetID)
            billOfLading <- getBillOfLading(negotiation.assetID)
            buyerAccountID <- getTraderAccountID(negotiation.buyerTraderID)
            buyerAddress <- getAddress(buyerAccountID)
            result <- sendTransactionAndGetResult(validateUsernamePassword = validateUsernamePassword, traderID = traderID, buyerAccountID = buyerAccountID, sellerAccountID = loginState.username, buyerAddress = buyerAddress, sellerAddress = loginState.address, asset = asset, order = order, billOfLading = billOfLading, negotiation = negotiation)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = sellerExecuteData.orderID, failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainBuyerExecuteForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.buyerExecuteOrder())
  }

  def blockchainBuyerExecute: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.BuyerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.buyerExecuteOrder(formWithErrors)))
      },
      buyerExecuteOrderData => {
        val postRequest = transactionsBuyerExecuteOrder.Service.post(transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseReq(from = buyerExecuteOrderData.from, gas = buyerExecuteOrderData.gas), password = buyerExecuteOrderData.password, buyerAddress = buyerExecuteOrderData.buyerAddress, sellerAddress = buyerExecuteOrderData.sellerAddress, fiatProofHash = buyerExecuteOrderData.fiatProofHash, pegHash = buyerExecuteOrderData.pegHash, mode = buyerExecuteOrderData.mode))
        (for {
          _ <- postRequest
        } yield Ok(views.html.index(successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def blockchainSellerExecuteForm: Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.blockchain.sellerExecuteOrder())
  }

  def blockchainSellerExecute: Action[AnyContent] = withoutLoginActionAsync { implicit request =>
    views.companion.blockchain.SellerExecuteOrder.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.sellerExecuteOrder(formWithErrors)))
      },
      sellerExecuteOrderData => {
        val post = transactionsSellerExecuteOrder.Service.post(transactionsSellerExecuteOrder.Request(transactionsSellerExecuteOrder.BaseReq(from = sellerExecuteOrderData.from, gas = sellerExecuteOrderData.gas), password = sellerExecuteOrderData.password, buyerAddress = sellerExecuteOrderData.buyerAddress, sellerAddress = sellerExecuteOrderData.sellerAddress, awbProofHash = sellerExecuteOrderData.awbProofHash, pegHash = sellerExecuteOrderData.pegHash, mode = sellerExecuteOrderData.mode))
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
