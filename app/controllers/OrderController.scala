package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction, WithoutLoginAction, WithoutLoginActionAsync}
import play.api.libs.json.Json
import play.api.mvc._
import constants.Response.Success
import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.blockchain.ACL
import models.common.Serializable._
import models.master.{Asset, Negotiation, Order}
import models.masterTransaction.AssetFile
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.MicroNumber
import views.companion.{blockchain => blockchainCompanion}
import views.html.component.blockchain.{txForms => blockchainForms}

import javax.inject.{Inject, Singleton}
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
                                 withLoginActionAsync: WithLoginActionAsync,
                                 withUnknownLoginAction: WithUnknownLoginAction,
                                 blockchainOrders: blockchain.Orders,
                                 blockchainClassifications: blockchain.Classifications,
                                 masterClassifications: master.Classifications,
                                 masterProperties: master.Properties,
                                 transactionsOrderDefine: transactions.blockchain.OrderDefine,
                                 blockchainTransactionOrderDefines: blockchainTransaction.OrderDefines,
                                 transactionsOrderMake: transactions.blockchain.OrderMake,
                                 blockchainTransactionOrderMakes: blockchainTransaction.OrderMakes,
                                 transactionsOrderTake: transactions.blockchain.OrderTake,
                                 blockchainTransactionOrderTakes: blockchainTransaction.OrderTakes,
                                 transactionsOrderCancel: transactions.blockchain.OrderCancel,
                                 blockchainTransactionOrderCancels: blockchainTransaction.OrderCancels,
                                 withUserLoginAction: WithUserLoginAction,
                                 blockchainIdentities: blockchain.Identities,
                                 withUsernameToken: WithUsernameToken,
                                 withoutLoginAction: WithoutLoginAction,
                                 withoutLoginActionAsync: WithoutLoginActionAsync
                               )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ORDER

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def moderatedBuyerExecuteForm(orderID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.moderatedBuyerExecuteOrder(orderID = orderID))
  }

  def moderatedBuyerExecute: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
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
                  _ <- utilitiesNotification.send(loginState.username, constants.Notification.MODERATED_BUY_ORDER_EXECUTED, ticketID)()
                  _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.MODERATED_BUY_ORDER_EXECUTED, ticketID)()
                  _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.MODERATED_BUY_ORDER_EXECUTED, ticketID)()
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

  def moderatedSellerExecuteForm(orderID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.moderatedSellerExecuteOrder(orderID = orderID))
  }

  def moderatedSellerExecute: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
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
                  _ <- utilitiesNotification.send(loginState.username, constants.Notification.MODERATED_SELL_ORDER_EXECUTED, ticketID)()
                  _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.MODERATED_SELL_ORDER_EXECUTED, ticketID)()
                  _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.MODERATED_SELL_ORDER_EXECUTED, ticketID)()
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

  def buyerExecuteForm(orderID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.buyerExecuteOrder(orderID = orderID))
  }

  def buyerExecute: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.BuyerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.buyerExecuteOrder(formWithErrors, orderID = formWithErrors.data(constants.FormField.ORDER_ID.name))))
        },
        buyerExecuteData => {
          val negotiation = masterNegotiations.Service.tryGet(buyerExecuteData.orderID)
          val order = masterOrders.Service.tryGet(buyerExecuteData.orderID)

          def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

          def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def sendTransaction(buyerAddress: String, sellerAddress: String, asset: Asset, order: Order): Future[String] = {
            if (asset.status == constants.Status.Asset.IN_ORDER && Seq(constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING, constants.Status.Order.BUYER_EXECUTE_ORDER_PENDING).contains(order.status) /*&& loginState.acl.getOrElse(throw new BaseException(constants.Response.UNAUTHORIZED)).buyerExecuteOrder*/) {
              asset.pegHash match {
                case Some(pegHash) => transaction.process[blockchainTransaction.BuyerExecuteOrder, transactionsBuyerExecuteOrder.Request](
                  entity = blockchainTransaction.BuyerExecuteOrder(from = loginState.address, buyerAddress = buyerAddress, sellerAddress = sellerAddress, fiatProofHash = buyerExecuteData.fiatProof, pegHash = pegHash, gas = buyerExecuteData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionBuyerExecuteOrders.Service.create,
                  request = transactionsBuyerExecuteOrder.Request(transactionsBuyerExecuteOrder.BaseReq(from = loginState.address, gas = buyerExecuteData.gas), password = buyerExecuteData.password, buyerAddress = buyerAddress, sellerAddress = sellerAddress, fiatProofHash = buyerExecuteData.fiatProof, pegHash = pegHash, mode = transactionMode),
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
            negotiation <- negotiation
            order <- order
            asset <- getAsset(negotiation.assetID)
            sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
            sellerAddress <- getAddress(sellerAccountID)
            ticketID <- sendTransaction(buyerAddress = loginState.address, sellerAddress = sellerAddress, asset = asset, order = order)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.BUYER_ORDER_EXECUTED, ticketID)()
            _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.BUYER_ORDER_EXECUTED, ticketID)()
            _ <- masterTransactionTradeActivities.Service.create(negotiation.id, constants.TradeActivity.BUYER_ORDER_EXECUTED, ticketID)
            result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = buyerExecuteData.orderID, successes = Seq(constants.Response.BUYER_ORDER_EXECUTED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = buyerExecuteData.orderID, failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def sellerExecuteForm(orderID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.master.sellerExecuteOrder(orderID = orderID))
  }

  def sellerExecute: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.SellerExecuteOrder.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.sellerExecuteOrder(formWithErrors, formWithErrors.data(constants.FormField.ORDER_ID.name))))
        },
        sellerExecuteData => {
          val negotiation = masterNegotiations.Service.tryGet(sellerExecuteData.orderID)
          val order = masterOrders.Service.tryGet(sellerExecuteData.orderID)

          def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

          def getBillOfLading(assetID: String): Future[AssetFile] = masterTransactionAssetFiles.Service.tryGet(id = assetID, documentType = constants.File.Asset.BILL_OF_LADING)

          def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def sendTransaction(buyerAddress: String, sellerAddress: String, asset: Asset, order: Order, billOfLading: AssetFile): Future[String] = {
            if (asset.status == constants.Status.Asset.IN_ORDER && billOfLading.status.getOrElse(throw new BaseException(constants.Response.BILL_OF_LADING_VERIFICATION_STATUS_PENDING)) && Seq(constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING, constants.Status.Order.SELLER_EXECUTE_ORDER_PENDING).contains(order.status) /*&& loginState.acl.getOrElse(throw new BaseException(constants.Response.UNAUTHORIZED)).sellerExecuteOrder*/) {
              val awbProofHash = utilities.String.sha256Sum(Json.toJson(billOfLading.documentContent.getOrElse(throw new BaseException(constants.Response.BILL_OF_LADING_NOT_FOUND))).toString)
              asset.pegHash match {
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
            } else throw new BaseException(constants.Response.UNAUTHORIZED)
          }


          (for {
            negotiation <- negotiation
            order <- order
            asset <- getAsset(negotiation.assetID)
            billOfLading <- getBillOfLading(negotiation.assetID)
            buyerAccountID <- getTraderAccountID(negotiation.buyerTraderID)
            buyerAddress <- getAddress(buyerAccountID)
            ticketID <- sendTransaction(buyerAddress = buyerAddress, sellerAddress = loginState.address, asset = asset, order = order, billOfLading = billOfLading)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.SELLER_ORDER_EXECUTED, ticketID)()
            _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.SELLER_ORDER_EXECUTED, ticketID)()
            _ <- masterTransactionTradeActivities.Service.create(negotiation.id, constants.TradeActivity.SELLER_ORDER_EXECUTED, ticketID)
            result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = sellerExecuteData.orderID, successes = Seq(constants.Response.SELLER_ORDER_EXECUTED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = sellerExecuteData.orderID, failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainBuyerExecuteForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.blockchain.buyerExecuteOrder())
  }

  def blockchainBuyerExecute: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
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

  def blockchainSellerExecuteForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
    Ok(views.html.component.blockchain.sellerExecuteOrder())
  }

  def blockchainSellerExecute: Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
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


  private def getNumberOfFields(addField: Boolean, currentNumber: Int) = if (addField) currentNumber + 1 else currentNumber

  def defineForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.orderDefine())
  }

  def define: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.OrderDefine.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.orderDefine(formWithErrors)))
        },
        defineData => {
          if (defineData.addImmutableMetaField || defineData.addImmutableField || defineData.addMutableMetaField || defineData.addMutableField) {
            Future(PartialContent(blockchainForms.orderDefine(
              orderDefineForm = blockchainCompanion.OrderDefine.form.fill(defineData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
              numImmutableMetaForms = getNumberOfFields(defineData.addImmutableMetaField, defineData.immutableMetaTraits.fold(0)(_.flatten.length)),
              numImmutableForms = getNumberOfFields(defineData.addImmutableField, defineData.immutableTraits.fold(0)(_.flatten.length)),
              numMutableMetaForms = getNumberOfFields(defineData.addMutableMetaField, defineData.mutableMetaTraits.fold(0)(_.flatten.length)),
              numMutableForms = getNumberOfFields(defineData.addMutableField, defineData.mutableTraits.fold(0)(_.flatten.length)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = defineData.password.getOrElse(""))
            val immutableMetas = defineData.immutableMetaTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val immutables = defineData.immutableTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutableMetas = defineData.mutableMetaTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutables = defineData.mutableTraits.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val broadcastTx = transaction.process[blockchainTransaction.OrderDefine, transactionsOrderDefine.Request](
                entity = blockchainTransaction.OrderDefine(from = loginState.address, fromID = defineData.fromID, immutableMetaTraits = immutableMetas, immutableTraits = immutables, mutableMetaTraits = mutableMetas, mutableTraits = mutables, gas = defineData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionOrderDefines.Service.create,
                request = transactionsOrderDefine.Request(transactionsOrderDefine.Message(transactionsOrderDefine.BaseReq(from = loginState.address, gas = defineData.gas), fromID = defineData.fromID, immutableMetaTraits = immutableMetas, immutableTraits = immutables, mutableMetaTraits = mutableMetas, mutableTraits = mutables)),
                action = transactionsOrderDefine.Service.post,
                onSuccess = blockchainTransactionOrderDefines.Utility.onSuccess,
                onFailure = blockchainTransactionOrderDefines.Utility.onFailure,
                updateTransactionHash = blockchainTransactionOrderDefines.Service.updateTransactionHash)

              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.order(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.orderDefine(blockchainCompanion.OrderDefine.form.fill(defineData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message))))

            (for {
              verifyPassword <- verifyPassword
              result <- broadcastTxAndGetResult(verifyPassword)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
            }
          }
        }
      )
  }

  def makeForm(classificationID: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val properties = masterProperties.Service.getAll(entityID = classificationID, entityType = constants.Blockchain.Entity.ORDER_DEFINITION)
      val maintainerIDs = masterClassifications.Service.getMaintainerIDs(classificationID)
      val identityIDs = blockchainIdentities.Service.getAllIDsByProvisioned(loginState.address)

      (for {
        properties <- properties
        maintainerIDs <- maintainerIDs
        identityIDs <- identityIDs
      } yield {
        if (properties.nonEmpty && maintainerIDs.intersect(identityIDs).nonEmpty) {
          val immutableMetaProperties = Option(properties.filter(x => x.isMeta && !x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          val immutableProperties = Option(properties.filter(x => !x.isMeta && !x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          //Special Case need to remove expiry and makerOwnableSplit from Mutables Meta
          val mutableMetaProperties = Option(properties.filter(x => x.isMeta && x.isMutable && x.name != constants.Blockchain.Properties.Expiry && x.name != constants.Blockchain.Properties.MakerOwnableSplit).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          val mutableProperties = Option(properties.filter(x => !x.isMeta && x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          Ok(blockchainForms.orderMake(blockchainCompanion.OrderMake.form.fill(blockchainCompanion.OrderMake.Data(fromID = maintainerIDs.intersect(identityIDs).headOption.getOrElse(""), classificationID = classificationID, makerOwnableID = "", takerOwnableID = "", expiresIn = 0, makerOwnableSplit = 0.0, immutableMetaProperties = immutableMetaProperties, addImmutableMetaField = false, immutableProperties = immutableProperties, addImmutableField = false, mutableMetaProperties = mutableMetaProperties, addMutableMetaField = false, mutableProperties = mutableProperties, addMutableField = false, gas = MicroNumber.zero, password = None)), classificationID = classificationID, numImmutableMetaForms = immutableMetaProperties.fold(0)(_.length), numImmutableForms = immutableProperties.fold(0)(_.length), numMutableMetaForms = mutableMetaProperties.fold(0)(_.length), numMutableForms = mutableProperties.fold(0)(_.length)))
        } else {
          Ok(blockchainForms.orderMake(classificationID = classificationID))
        }
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def make: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.OrderMake.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.orderMake(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.CLASSIFICATION_ID.name, ""))))
        },
        makeData => {
          if (makeData.addImmutableMetaField || makeData.addImmutableField || makeData.addMutableMetaField || makeData.addMutableField) {
            Future(PartialContent(blockchainForms.orderMake(
              orderMakeForm = blockchainCompanion.OrderMake.form.fill(makeData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
              numImmutableMetaForms = getNumberOfFields(makeData.addImmutableMetaField, makeData.immutableMetaProperties.fold(0)(_.flatten.length)),
              classificationID = makeData.classificationID,
              numImmutableForms = getNumberOfFields(makeData.addImmutableField, makeData.immutableProperties.fold(0)(_.flatten.length)),
              numMutableMetaForms = getNumberOfFields(makeData.addMutableMetaField, makeData.mutableMetaProperties.fold(0)(_.flatten.length)),
              numMutableForms = getNumberOfFields(makeData.addMutableField, makeData.mutableProperties.fold(0)(_.flatten.length)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = makeData.password.getOrElse(""))
            val immutableMetas = makeData.immutableMetaProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val immutables = makeData.immutableProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutableMetas = makeData.mutableMetaProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutables = makeData.mutableProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val broadcastTx = transaction.process[blockchainTransaction.OrderMake, transactionsOrderMake.Request](
                entity = blockchainTransaction.OrderMake(from = loginState.address, fromID = makeData.fromID, classificationID = makeData.classificationID, makerOwnableID = makeData.makerOwnableID, takerOwnableID = makeData.takerOwnableID, makerOwnableSplit = makeData.makerOwnableSplit, expiresIn = makeData.expiresIn, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables, gas = makeData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionOrderMakes.Service.create,
                request = transactionsOrderMake.Request(transactionsOrderMake.Message(transactionsOrderMake.BaseReq(from = loginState.address, gas = makeData.gas), fromID = makeData.fromID, classificationID = makeData.classificationID, makerOwnableID = makeData.makerOwnableID, takerOwnableID = makeData.takerOwnableID, expiresIn = makeData.expiresIn, makerOwnableSplit = makeData.makerOwnableSplit, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                action = transactionsOrderMake.Service.post,
                onSuccess = blockchainTransactionOrderMakes.Utility.onSuccess,
                onFailure = blockchainTransactionOrderMakes.Utility.onFailure,
                updateTransactionHash = blockchainTransactionOrderMakes.Service.updateTransactionHash)

              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.order(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.orderMake(blockchainCompanion.OrderMake.form.fill(makeData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), makeData.classificationID)))

            (for {
              verifyPassword <- verifyPassword
              result <- broadcastTxAndGetResult(verifyPassword)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
            }
          }
        }
      )
  }

  def takeForm(orderID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.orderTake(orderID = orderID))
  }

  def take: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.OrderTake.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.orderTake(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ORDER_ID.name, ""))))
        },
        takeData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = takeData.password)

          def broadcastTx = transaction.process[blockchainTransaction.OrderTake, transactionsOrderTake.Request](
            entity = blockchainTransaction.OrderTake(from = loginState.address, fromID = takeData.fromID, orderID = takeData.orderID, takerOwnableSplit = takeData.takerOwnableSplit, gas = takeData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionOrderTakes.Service.create,
            request = transactionsOrderTake.Request(transactionsOrderTake.Message(transactionsOrderTake.BaseReq(from = loginState.address, gas = takeData.gas), fromID = takeData.fromID, orderID = takeData.orderID, takerOwnableSplit = takeData.takerOwnableSplit)),
            action = transactionsOrderTake.Service.post,
            onSuccess = blockchainTransactionOrderTakes.Utility.onSuccess,
            onFailure = blockchainTransactionOrderTakes.Utility.onFailure,
            updateTransactionHash = blockchainTransactionOrderTakes.Service.updateTransactionHash
          )

          def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
            for {
              ticketID <- broadcastTx
              result <- withUsernameToken.Ok(views.html.order(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.orderTake(blockchainCompanion.OrderTake.form.fill(takeData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), takeData.orderID)))

          (for {
            verifyPassword <- verifyPassword
            result <- broadcastTxAndGetResult(verifyPassword)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def cancelForm(orderID: String, makerID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.orderCancel(orderID = orderID, makerID = makerID))
  }

  def cancel: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.OrderCancel.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.orderCancel(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ORDER_ID.name, ""), formWithErrors.data.getOrElse(constants.FormField.FROM_ID.name, ""))))
        },
        cancelData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = cancelData.password)

          def broadcastTx = transaction.process[blockchainTransaction.OrderCancel, transactionsOrderCancel.Request](
            entity = blockchainTransaction.OrderCancel(from = loginState.address, fromID = cancelData.fromID, orderID = cancelData.orderID, gas = cancelData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionOrderCancels.Service.create,
            request = transactionsOrderCancel.Request(transactionsOrderCancel.Message(transactionsOrderCancel.BaseReq(from = loginState.address, gas = cancelData.gas), fromID = cancelData.fromID, orderID = cancelData.orderID)),
            action = transactionsOrderCancel.Service.post,
            onSuccess = blockchainTransactionOrderCancels.Utility.onSuccess,
            onFailure = blockchainTransactionOrderCancels.Utility.onFailure,
            updateTransactionHash = blockchainTransactionOrderCancels.Service.updateTransactionHash
          )

          def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
            for {
              ticketID <- broadcastTx
              result <- withUsernameToken.Ok(views.html.order(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.orderCancel(blockchainCompanion.OrderCancel.form.fill(cancelData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), cancelData.orderID, cancelData.fromID)))

          (for {
            verifyPassword <- verifyPassword
            result <- broadcastTxAndGetResult(verifyPassword)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.dashboard(failures = Seq(baseException.failure)))
          }
        }
      )
  }


}
