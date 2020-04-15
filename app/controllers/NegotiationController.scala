package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.DocumentContent
import models.common.Serializable
import models.master.{Asset, Negotiation, Trader}
import models.masterTransaction.NegotiationFile
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.libs.Comet
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NegotiationController @Inject()(
                                       messagesControllerComponents: MessagesControllerComponents,
                                       transaction: utilities.Transaction,
                                       masterAccounts: master.Accounts,
                                       masterAssets: master.Assets,
                                       masterTradeRelations: master.TraderRelations,
                                       withTraderLoginAction: WithTraderLoginAction,
                                       withZoneLoginAction: WithZoneLoginAction,
                                       transactionsSellerExecuteOrder: transactions.SellerExecuteOrder,
                                       blockchainTransactionSellerExecuteOrders: blockchainTransaction.SellerExecuteOrders,
                                       accounts: master.Accounts,
                                       masterTraders: master.Traders,
                                       blockchainACLAccounts: blockchain.ACLAccounts,
                                       blockchainZones: blockchain.Zones,
                                       blockchainNegotiations: blockchain.Negotiations,
                                       withUsernameToken: WithUsernameToken,
                                       masterNegotiations: master.Negotiations,
                                       blockchainAssets: blockchain.Assets,
                                       transactionsChangeBuyerBid: transactions.ChangeBuyerBid,
                                       blockchainTransactionChangeBuyerBids: blockchainTransaction.ChangeBuyerBids,
                                       utilitiesNotification: utilities.Notification,
                                       masterTransactionChats: masterTransaction.Chats,
                                       masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles

                                     )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_NEGOTIATION

  private val negotiationDefaultTime: Int = configuration.get[Int]("blockchain.negotiation.defaultTime")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def requestForm(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getAllTradableAssets(traderID: String): Future[Seq[Asset]] = masterAssets.Service.getAllTradableAssets(traderID)

      def getCounterPartyList(traderID: String): Future[Seq[String]] = masterTradeRelations.Service.getAllCounterParties(traderID)

      def getCounterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      (for {
        traderID <- traderID
        tradableAssets <- getAllTradableAssets(traderID)
        counterPartyList <- getCounterPartyList(traderID)
        counterPartyTraders <- getCounterPartyTraders(counterPartyList)
      } yield Ok(views.html.component.master.negotiationRequest(tradableAssets = tradableAssets, counterPartyTraders = counterPartyTraders))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def request(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.NegotiationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          def getAssets(traderID: String): Future[Seq[Asset]] = masterAssets.Service.getAllAssets(traderID)

          def getCounterPartyList(traderID: String): Future[Seq[String]] = masterTradeRelations.Service.getAllCounterParties(traderID)

          def getCounterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

          (for {
            traderID <- traderID
            assets <- getAssets(traderID)
            counterPartyList <- getCounterPartyList(traderID)
            counterPartyTraders <- getCounterPartyTraders(counterPartyList)
          } yield BadRequest(views.html.component.master.negotiationRequest(formWithErrors, assets, counterPartyTraders))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        },
        requestData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val asset: Future[Asset] = masterAssets.Service.tryGet(id = requestData.assetID)

          def checkRelationExists(traderID: String): Future[Boolean] = masterTradeRelations.Service.checkRelationExists(fromID = traderID, toID = requestData.counterParty)

          def insert(traderID: String, asset: Asset, checkRelationExists: Boolean): Future[String] = {
            if (traderID != asset.ownerID || !checkRelationExists) throw new BaseException(constants.Response.UNAUTHORIZED)
            asset.status match {
              case constants.Status.Asset.REQUESTED_TO_ZONE | constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE => masterNegotiations.Service.createWithIssueAssetPending(buyerTraderID = requestData.counterParty, sellerTraderID = traderID, assetID = asset.id, description = asset.description, price = asset.price, quantity = asset.quantity, quantityUnit = asset.quantityUnit, shippingPeriod = asset.shippingPeriod)
              case constants.Status.Asset.ISSUED | constants.Status.Asset.TRADE_COMPLETED => masterNegotiations.Service.createWithFormIncomplete(buyerTraderID = requestData.counterParty, sellerTraderID = traderID, assetID = asset.id, description = asset.description, price = asset.price, quantity = asset.quantity, quantityUnit = asset.quantityUnit, shippingPeriod = asset.shippingPeriod)
              case constants.Status.Asset.REJECTED_BY_ZONE | constants.Status.Asset.ISSUE_ASSET_FAILED | constants.Status.Asset.IN_ORDER | constants.Status.Asset.REDEEMED => throw new BaseException(constants.Response.ASSET_PEG_NOT_FOUND)
              case _ => throw new BaseException(constants.Response.ASSET_PEG_NOT_FOUND)
            }
          }

          (for {
            traderID <- traderID
            asset <- asset
            checkRelationExists <- checkRelationExists(traderID)
            id <- insert(traderID = traderID, asset = asset, checkRelationExists = checkRelationExists)
            result <- withUsernameToken.PartialContent(views.html.component.master.paymentTerms(id = id))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def paymentTermsForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(traderID: String, negotiation: Negotiation): Future[Result] = if (traderID == negotiation.sellerTraderID) {
        withUsernameToken.Ok(views.html.component.master.paymentTerms(views.companion.master.PaymentTerms.form.fill(views.companion.master.PaymentTerms.Data(id = id, advancePayment = negotiation.paymentTerms.advancePayment.getOrElse(false), advancePercentage = negotiation.paymentTerms.advancePercentage, credit = negotiation.paymentTerms.credit.getOrElse(false), tenure = negotiation.paymentTerms.tenure, tentativeDate = if (negotiation.paymentTerms.tentativeDate.isDefined) Option(utilities.Date.sqlDateToUtilDate(negotiation.paymentTerms.tentativeDate.get)) else None, refrence = negotiation.paymentTerms.reference)), id = id))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(traderID = traderID, negotiation = negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def paymentTerms(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.PaymentTerms.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.paymentTerms(formWithErrors, id = formWithErrors.data(constants.FormField.ID.name))))
        },
        paymentTermsData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(paymentTermsData.id)

          def assetStatus(id: String): Future[String] = masterAssets.Service.tryGetStatus(id)

          def update(traderID: String, assetStatus: String, negotiation: Negotiation): Future[Int] = {
            if (traderID != negotiation.sellerTraderID) throw new BaseException(constants.Response.UNAUTHORIZED)
            negotiation.status match {
              case constants.Status.Negotiation.ISSUE_ASSET_FAILED | constants.Status.Negotiation.FORM_INCOMPLETE | constants.Status.Negotiation.ISSUE_ASSET_PENDING | constants.Status.Negotiation.REQUEST_SENDING_WAITING_FOR_ISSUE_ASSET =>
                assetStatus match {
                  case constants.Status.Asset.REQUESTED_TO_ZONE | constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE | constants.Status.Asset.ISSUED | constants.Status.Asset.TRADE_COMPLETED => masterNegotiations.Service.updatePaymentTerms(id = paymentTermsData.id, advancePayment = paymentTermsData.advancePayment, advancePercentage = paymentTermsData.advancePercentage, credit = paymentTermsData.credit, tenure = paymentTermsData.tenure, tentativeDate = if (paymentTermsData.tentativeDate.isDefined) Option(utilities.Date.utilDateToSQLDate(paymentTermsData.tentativeDate.get)) else None, refrence = paymentTermsData.refrence)
                  case _ => throw new BaseException(constants.Response.ASSET_PEG_NOT_FOUND)
                }
              case _ => throw new BaseException(constants.Response.UNAUTHORIZED)
            }
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            assetStatus <- assetStatus(negotiation.assetID)
            _ <- update(traderID = traderID, assetStatus = assetStatus, negotiation = negotiation)
            result <- withUsernameToken.PartialContent(views.html.component.master.documentsCheckList(views.companion.master.DocumentsCheckList.form.fill(views.companion.master.DocumentsCheckList.Data(id = negotiation.id, billOfExchangeRequired = negotiation.documentsCheckList.billOfExchangeRequired.getOrElse(false), obl = negotiation.documentsCheckList.obl.getOrElse(false), coo = negotiation.documentsCheckList.coo.getOrElse(false), coa = negotiation.documentsCheckList.coa.getOrElse(false), otherDocuments = negotiation.documentsCheckList.otherDocuments)), id = negotiation.id))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def documentsCheckListForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(traderID: String, negotiation: Negotiation): Future[Result] = if (traderID == negotiation.sellerTraderID) {
        withUsernameToken.Ok(views.html.component.master.documentsCheckList(views.companion.master.DocumentsCheckList.form.fill(views.companion.master.DocumentsCheckList.Data(id = id, billOfExchangeRequired = negotiation.documentsCheckList.billOfExchangeRequired.getOrElse(false), obl = negotiation.documentsCheckList.obl.getOrElse(false), coo = negotiation.documentsCheckList.coo.getOrElse(false), coa = negotiation.documentsCheckList.coa.getOrElse(false), otherDocuments = negotiation.documentsCheckList.otherDocuments)), id = id))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(traderID = traderID, negotiation = negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def documentsCheckList(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.DocumentsCheckList.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.documentsCheckList(formWithErrors, id = formWithErrors.data(constants.FormField.ID.name))))
        },
        documentsCheckListData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(documentsCheckListData.id)

          def assetStatus(id: String): Future[String] = masterAssets.Service.tryGetStatus(id)

          def update(traderID: String, assetStatus: String, negotiation: Negotiation): Future[Int] = {
            if (traderID != negotiation.sellerTraderID) throw new BaseException(constants.Response.UNAUTHORIZED)
            negotiation.status match {
              case constants.Status.Negotiation.ISSUE_ASSET_FAILED | constants.Status.Negotiation.FORM_INCOMPLETE | constants.Status.Negotiation.ISSUE_ASSET_PENDING | constants.Status.Negotiation.REQUEST_SENDING_WAITING_FOR_ISSUE_ASSET =>
                assetStatus match {
                  case constants.Status.Asset.REQUESTED_TO_ZONE | constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE | constants.Status.Asset.ISSUED | constants.Status.Asset.TRADE_COMPLETED => masterNegotiations.Service.updateDocumentsCheckList(id = documentsCheckListData.id, billOfExchangeRequired = documentsCheckListData.billOfExchangeRequired, obl = documentsCheckListData.obl, coo = documentsCheckListData.coo, coa = documentsCheckListData.coa, otherDocuments = documentsCheckListData.otherDocuments)
                  case _ => throw new BaseException(constants.Response.ASSET_PEG_NOT_FOUND)
                }
              case _ => throw new BaseException(constants.Response.UNAUTHORIZED)
            }
          }

          def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

          def counterPartyTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

          (for {
            traderID <- traderID
            negotiation <- negotiation
            assetStatus <- assetStatus(negotiation.assetID)
            _ <- update(traderID = traderID, assetStatus = assetStatus, negotiation)
            asset <- getAsset(negotiation.assetID)
            counterPartyTrader <- counterPartyTrader(negotiation.buyerTraderID)
            result <- withUsernameToken.PartialContent(views.html.component.master.reviewNegotiationRequest(asset = asset, negotiation = negotiation, counterPartyTrader = counterPartyTrader))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def reviewRequestForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getAsset(traderID: String, negotiation: Negotiation): Future[Asset] = if (traderID == negotiation.sellerTraderID) {
        masterAssets.Service.tryGet(negotiation.assetID)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      def counterPartyTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

      (for {
        traderID <- traderID
        negotiation <- negotiation
        asset <- getAsset(traderID, negotiation)
        counterPartyTrader <- counterPartyTrader(negotiation.buyerTraderID)
        result <- withUsernameToken.Ok(views.html.component.master.reviewNegotiationRequest(asset = asset, negotiation = negotiation, counterPartyTrader = counterPartyTrader))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def reviewRequest(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ReviewNegotiationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(formWithErrors.data(constants.FormField.ID.name))

          def getAsset(traderID: String, negotiation: Negotiation): Future[Asset] = if (traderID == negotiation.sellerTraderID) {
            masterAssets.Service.tryGet(negotiation.assetID)
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          def counterPartyTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

          (for {
            traderID <- traderID
            negotiation <- negotiation
            asset <- getAsset(traderID, negotiation)
            counterPartyTrader <- counterPartyTrader(negotiation.buyerTraderID)
          } yield BadRequest(views.html.component.master.reviewNegotiationRequest(formWithErrors, asset = asset, negotiation = negotiation, counterPartyTrader = counterPartyTrader))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        },
        reviewRequestData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(reviewRequestData.id)

          def assetStatus(id: String): Future[String] = masterAssets.Service.tryGetStatus(id)

          def update(traderID: String, assetStatus: String, negotiation: Negotiation): Future[Int] = {
            negotiation.status match {
              case constants.Status.Negotiation.ISSUE_ASSET_FAILED | constants.Status.Negotiation.FORM_INCOMPLETE | constants.Status.Negotiation.ISSUE_ASSET_PENDING | constants.Status.Negotiation.REQUEST_SENDING_WAITING_FOR_ISSUE_ASSET =>
                assetStatus match {
                  case constants.Status.Asset.REQUESTED_TO_ZONE | constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE => masterNegotiations.Service.markStatusIssueAssetPendingRequestSent(reviewRequestData.id)
                  case constants.Status.Asset.ISSUED | constants.Status.Asset.TRADE_COMPLETED => masterNegotiations.Service.markStatusRequestSent(reviewRequestData.id)
                  case _ => throw new BaseException(constants.Response.ASSET_PEG_NOT_FOUND)
                }
              case _ => throw new BaseException(constants.Response.UNAUTHORIZED)
            }
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            assetStatus <- assetStatus(negotiation.assetID)
            _ <- update(traderID = traderID, assetStatus = assetStatus, negotiation = negotiation)
            result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.NEGOTIATION_REQUEST_SENT)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def acceptRequestForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def sellerName(sellerTraderID: String): Future[String] = masterTraders.Service.tryGetTraderName(sellerTraderID)

      def getResult(traderID: String, negotiation: Negotiation, sellerName: String): Future[Result] = if (traderID == negotiation.buyerTraderID) {
        withUsernameToken.Ok(views.html.component.master.acceptNegotiationRequest(negotiation = negotiation, sellerName = sellerName))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        sellerName <- sellerName(negotiation.sellerTraderID)
        result <- getResult(traderID = traderID, negotiation = negotiation, sellerName = sellerName)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def acceptRequest: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AcceptNegotiationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          val negotiation = masterNegotiations.Service.tryGet(formWithErrors.data(constants.FormField.ID.name))

          def sellerName(sellerTraderID: String): Future[String] = masterTraders.Service.tryGetTraderName(sellerTraderID)

          def getResult(traderID: String, negotiation: Negotiation, sellerName: String): Future[Result] = if (traderID == negotiation.buyerTraderID) {
            Future(BadRequest(views.html.component.master.acceptNegotiationRequest(formWithErrors, negotiation = negotiation, sellerName = sellerName)))
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            sellerName <- sellerName(negotiation.sellerTraderID)
            result <- getResult(traderID = traderID, negotiation = negotiation, sellerName = sellerName)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        },
        acceptRequestData => {
          val negotiation = masterNegotiations.Service.tryGet(acceptRequestData.id)

          def assetPegHash(assetID: String): Future[String] = masterAssets.Service.tryGetPegHash(assetID)

          def sellerAccountID(sellerTraderID: String): Future[String] = masterTraders.Service.tryGetAccountId(sellerTraderID)

          def sellerAddress(sellerAccountID: String): Future[String] = masterAccounts.Service.getAddress(sellerAccountID)

          def sendTxAndGetTicketID(sellerAddress: String, pegHash: String, negotiation: Negotiation): Future[String] = transaction.process[blockchainTransaction.ChangeBuyerBid, transactionsChangeBuyerBid.Request](
            entity = blockchainTransaction.ChangeBuyerBid(from = loginState.address, to = sellerAddress, bid = negotiation.price, time = negotiation.time.getOrElse(negotiationDefaultTime), pegHash = pegHash, gas = acceptRequestData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionChangeBuyerBids.Service.create,
            request = transactionsChangeBuyerBid.Request(transactionsChangeBuyerBid.BaseReq(from = loginState.address, gas = acceptRequestData.gas.toString), to = sellerAddress, password = acceptRequestData.password, bid = negotiation.price.toString, time = negotiation.time.getOrElse(negotiationDefaultTime).toString(), pegHash = pegHash, mode = transactionMode),
            action = transactionsChangeBuyerBid.Service.post,
            onSuccess = blockchainTransactionChangeBuyerBids.Utility.onSuccess,
            onFailure = blockchainTransactionChangeBuyerBids.Utility.onFailure,
            updateTransactionHash = blockchainTransactionChangeBuyerBids.Service.updateTransactionHash
          )

          def updateTicketID(id: String, ticketID: String): Future[Int] = masterNegotiations.Service.updateTicketID(id = id, ticketID = ticketID)

          def createChatIDAndChatRoom(sellerAccountID: String, negotiationID: String): Future[Unit] = {
            val chatID = masterTransactionChats.Service.createGroupChat(loginState.username, sellerAccountID)

            def insertChatID(chatID: String): Future[Int] = masterNegotiations.Service.insertChatID(id = negotiationID, chatID = chatID)

            for {
              chatID <- chatID
              _ <- insertChatID(chatID)
            } yield ()

          }

          (for {
            negotiation <- negotiation
            pegHash <- assetPegHash(negotiation.assetID)
            sellerAccountID <- sellerAccountID(negotiation.sellerTraderID)
            sellerAddress <- sellerAddress(sellerAccountID)
            ticketID <- sendTxAndGetTicketID(sellerAddress = sellerAddress, pegHash = pegHash, negotiation = negotiation)
            _ <- updateTicketID(id = negotiation.id, ticketID = ticketID)
            _ <- createChatIDAndChatRoom(sellerAccountID = sellerAccountID, negotiationID = negotiation.id)
            _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.NEGOTIATION_REQUEST_ACCEPTED_BLOCKCHAIN_TRANSACTION_PENDING, ticketID)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_REQUEST_ACCEPTED_BLOCKCHAIN_TRANSACTION_PENDING, ticketID)
            result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.NEGOTIATION_REQUEST_ACCEPTED_BLOCKCHAIN_TRANSACTION_PENDING)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def rejectRequestForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def sellerName(sellerTraderID: String): Future[String] = masterTraders.Service.tryGetTraderName(sellerTraderID)

      def getResult(traderID: String, negotiation: Negotiation, sellerName: String): Future[Result] = if (traderID == negotiation.buyerTraderID) {
        withUsernameToken.Ok(views.html.component.master.rejectNegotiationRequest(negotiation = negotiation, sellerName = sellerName))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        sellerName <- sellerName(negotiation.sellerTraderID)
        result <- getResult(traderID = traderID, negotiation = negotiation, sellerName = sellerName)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def rejectRequest: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RejectNegotiationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          val negotiation = masterNegotiations.Service.tryGet(formWithErrors.data(constants.FormField.ID.name))

          def sellerName(sellerTraderID: String): Future[String] = masterTraders.Service.tryGetTraderName(sellerTraderID)

          def getResult(traderID: String, negotiation: Negotiation, sellerName: String): Future[Result] = if (traderID == negotiation.buyerTraderID) {
            Future(BadRequest(views.html.component.master.rejectNegotiationRequest(formWithErrors, negotiation = negotiation, sellerName = sellerName)))
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            sellerName <- sellerName(negotiation.sellerTraderID)
            result <- getResult(traderID = traderID, negotiation = negotiation, sellerName = sellerName)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        },
        rejectRequestData => {
          val markNegotiationRejected = masterNegotiations.Service.markRequestRejected(id = rejectRequestData.id, comment = rejectRequestData.comment)
          val negotiation = masterNegotiations.Service.tryGet(rejectRequestData.id)

          def getAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          (for {
            _ <- markNegotiationRejected
            negotiation <- negotiation
            sellerAccountID <- getAccountID(negotiation.sellerTraderID)
            _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.NEGOTIATION_REQUEST_REJECTED, negotiation.id)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_REQUEST_REJECTED, negotiation.id)
            result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.NEGOTIATION_REQUEST_REJECTED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def updateAssetTermsForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(traderID: String, negotiation: Negotiation): Future[Result] = if (traderID == negotiation.sellerTraderID) {
        withUsernameToken.Ok(views.html.component.master.updateNegotiationAssetTerms(views.companion.master.UpdateNegotiationAssetTerms.form.fill(views.companion.master.UpdateNegotiationAssetTerms.Data(id = negotiation.id, description = negotiation.assetDescription, price = negotiation.price, quantity = negotiation.quantity, shippingPeriod = negotiation.shippingPeriod))))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(traderID = traderID, negotiation = negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def updateAssetTerms(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateNegotiationAssetTerms.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.updateNegotiationAssetTerms(formWithErrors)))
        },
        updateAssetTermsData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(updateAssetTermsData.id)

          def update(traderID: String, negotiation: Negotiation): Future[Int] = if (traderID == negotiation.sellerTraderID) {
            val updateDescription = if (updateAssetTermsData.description != negotiation.assetDescription) masterNegotiations.Service.updateAssetDescriptionAndStatus(updateAssetTermsData.id, updateAssetTermsData.description, false) else Future(0)
            val updatePrice = if (updateAssetTermsData.price != negotiation.price) masterNegotiations.Service.updatePriceAndStatus(updateAssetTermsData.id, updateAssetTermsData.price, false) else Future(0)
            val updateQuantity = if (updateAssetTermsData.quantity != negotiation.quantity) masterNegotiations.Service.updateQuantityAndStatus(updateAssetTermsData.id, updateAssetTermsData.quantity, false) else Future(0)
            val updateShippingPeriod = if (updateAssetTermsData.shippingPeriod != negotiation.shippingPeriod) masterNegotiations.Service.updateShippingPeriodAndStatus(updateAssetTermsData.id, updateAssetTermsData.shippingPeriod, false) else Future(0)
            for {
              _ <- updateDescription
              _ <- updatePrice
              _ <- updateQuantity
              _ <- updateShippingPeriod
            } yield 0
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          def getAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          (for {
            traderID <- traderID
            negotiation <- negotiation
            buyerAccountID <- getAccountID(negotiation.buyerTraderID)
            _ <- update(traderID = traderID, negotiation = negotiation)
            _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_ASSET_TERMS_UPDATED, negotiation.id)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_ASSET_TERMS_UPDATED, negotiation.id)
            result <- withUsernameToken.Ok(views.html.tradeRoom(id = updateAssetTermsData.id, successes = Seq(constants.Response.NEGOTIATION_ASSET_TERMS_UPDATED)))
          } yield {
            masterNegotiations.Service.sendMessageToNegotiationTermsActor(buyerAccountID, negotiation.id)
            result
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def updatePaymentTermsForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(traderID: String, negotiation: Negotiation): Future[Result] = if (traderID == negotiation.sellerTraderID) {
        withUsernameToken.Ok(views.html.component.master.updatePaymentTerms(views.companion.master.PaymentTerms.form.fill(views.companion.master.PaymentTerms.Data(id = id, advancePayment = negotiation.paymentTerms.advancePayment.getOrElse(false), advancePercentage = negotiation.paymentTerms.advancePercentage, credit = negotiation.paymentTerms.credit.getOrElse(false), tenure = negotiation.paymentTerms.tenure, tentativeDate = if (negotiation.paymentTerms.tentativeDate.isDefined) Option(utilities.Date.sqlDateToUtilDate(negotiation.paymentTerms.tentativeDate.get)) else None, refrence = negotiation.paymentTerms.reference))))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(traderID = traderID, negotiation = negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def updatePaymentTerms(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.PaymentTerms.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.updatePaymentTerms(formWithErrors)))
        },
        updatePaymentTermsData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(updatePaymentTermsData.id)

          def update(traderID: String, negotiation: Negotiation): Future[Int] = if (traderID == negotiation.sellerTraderID) {
            val updateAdvancePayment = if (Option(updatePaymentTermsData.advancePayment) != negotiation.paymentTerms.advancePayment || updatePaymentTermsData.advancePercentage != negotiation.paymentTerms.advancePercentage) masterNegotiations.Service.updateAdvancePaymentAndStatus(updatePaymentTermsData.id, updatePaymentTermsData.advancePayment, updatePaymentTermsData.advancePercentage, false) else Future(0)
            val updateCredit = if (Option(updatePaymentTermsData.credit) != negotiation.paymentTerms.credit || updatePaymentTermsData.tentativeDate != negotiation.paymentTerms.tentativeDate || updatePaymentTermsData.tenure != negotiation.paymentTerms.tenure || updatePaymentTermsData.refrence != negotiation.paymentTerms.reference) masterNegotiations.Service.updateCreditAndStatus(updatePaymentTermsData.id, updatePaymentTermsData.credit, updatePaymentTermsData.tentativeDate.map(date => utilities.Date.utilDateToSQLDate(date)), updatePaymentTermsData.tenure, updatePaymentTermsData.refrence, false) else Future(0)
            for {
              _ <- updateAdvancePayment
              _ <- updateCredit
            } yield 0
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          def getAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          (for {
            traderID <- traderID
            negotiation <- negotiation
            buyerAccountID <- getAccountID(negotiation.buyerTraderID)
            _ <- update(traderID = traderID, negotiation = negotiation)
            _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_PAYMENT_TERMS_UPDATED, negotiation.id)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_PAYMENT_TERMS_UPDATED, negotiation.id)
            result <- withUsernameToken.Ok(views.html.tradeRoom(id = updatePaymentTermsData.id, successes = Seq(constants.Response.NEGOTIATION_PAYMENT_TERMS_UPDATED)))
          } yield {
            masterNegotiations.Service.sendMessageToNegotiationTermsActor(buyerAccountID, negotiation.id)
            result
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def updateDocumentCheckListForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(traderID: String, negotiation: Negotiation): Future[Result] = if (traderID == negotiation.sellerTraderID) {
        withUsernameToken.Ok(views.html.component.master.updateDocumentsCheckList(views.companion.master.DocumentsCheckList.form.fill(views.companion.master.DocumentsCheckList.Data(id = id, billOfExchangeRequired = negotiation.documentsCheckList.billOfExchangeRequired.getOrElse(false), obl = negotiation.documentsCheckList.obl.getOrElse(false), coo = negotiation.documentsCheckList.coo.getOrElse(false), coa = negotiation.documentsCheckList.coa.getOrElse(false), otherDocuments = negotiation.documentsCheckList.otherDocuments))))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(traderID = traderID, negotiation = negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def updateDocumentCheckList(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.DocumentsCheckList.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.updateDocumentsCheckList(formWithErrors)))
        },
        updateDocumentCheckListData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(updateDocumentCheckListData.id)

          def update(traderID: String, negotiation: Negotiation): Future[Int] = if (traderID == negotiation.sellerTraderID) {
            val updateBillOfExchangeRequired = if (Option(updateDocumentCheckListData.billOfExchangeRequired) != negotiation.documentsCheckList.billOfExchangeRequired) masterNegotiations.Service.updateBillOfExchangeRequiredAndStatus(updateDocumentCheckListData.id, updateDocumentCheckListData.billOfExchangeRequired, false) else Future(0)
            val updateDocumentList = if (Option(updateDocumentCheckListData.obl) != negotiation.documentsCheckList.obl || Option(updateDocumentCheckListData.coo) != negotiation.documentsCheckList.coo || Option(updateDocumentCheckListData.coa) != negotiation.documentsCheckList.coa || updateDocumentCheckListData.otherDocuments != negotiation.documentsCheckList.otherDocuments) masterNegotiations.Service.updateDocumentListAndStatus(updateDocumentCheckListData.id, updateDocumentCheckListData.obl, updateDocumentCheckListData.coo, updateDocumentCheckListData.coa, updateDocumentCheckListData.otherDocuments, false) else Future(0)
            for {
              _ <- updateBillOfExchangeRequired
              _ <- updateDocumentList
            } yield {
              0
            }
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          def getAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          (for {
            traderID <- traderID
            negotiation <- negotiation
            buyerAccountID <- getAccountID(negotiation.buyerTraderID)
            _ <- update(traderID = traderID, negotiation = negotiation)
            _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED, negotiation.id)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED, negotiation.id)
            result <- withUsernameToken.Ok(views.html.tradeRoom(id = updateDocumentCheckListData.id, successes = Seq(constants.Response.NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED)))
          } yield {
            masterNegotiations.Service.sendMessageToNegotiationTermsActor(buyerAccountID, negotiation.id)
            result
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def acceptOrRejectNegotiationTermsForm(id: String, termType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(traderID: String, negotiation: Negotiation): Future[Result] = if (traderID == negotiation.sellerTraderID) {
        withUsernameToken.Ok(views.html.component.master.acceptOrRejectNegotiationTerms(negotiationID = negotiation.id, termType = termType, status = false))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(traderID = traderID, negotiation = negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def acceptOrRejectNegotiationTerms(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AcceptOrRejectNegotiationTerms.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.acceptOrRejectNegotiationTerms(formWithErrors, negotiationID = formWithErrors.data(constants.FormField.ID.name), termType = formWithErrors.data(constants.FormField.TERM_TYPE.name), status = false)))
        },
        acceptOrRejectNegotiationTermsData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(acceptOrRejectNegotiationTermsData.id)

          def updateStatus(traderID: String, negotiation: Negotiation): Future[Int] = if (traderID == negotiation.buyerTraderID) {
            if (negotiation.status == constants.Status.Negotiation.NEGOTIATION_STARTED) {
              acceptOrRejectNegotiationTermsData.termType match {
                case constants.View.ASSET_DESCRIPTION => masterNegotiations.Service.updateBuyerAcceptedAssetDescription(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
                case constants.View.PRICE => masterNegotiations.Service.updateBuyerAcceptedPrice(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
                case constants.View.QUANTITY => masterNegotiations.Service.updateBuyerAcceptedQuantity(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
                case constants.View.SHIPPING_PERIOD => masterNegotiations.Service.updateBuyerAcceptedShippingPeriod(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
                case constants.View.ADVANCE_PAYMENT => masterNegotiations.Service.updateBuyerAcceptedAdvancePayment(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
                case constants.View.CREDIT => masterNegotiations.Service.updateBuyerAcceptedCredit(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
                case constants.View.BILL_OF_EXCHANGE_REQUIRED => masterNegotiations.Service.updateBuyerAcceptedBillOfExchangeRequired(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
                case constants.View.DOCUMENT_LIST => masterNegotiations.Service.updateBuyerAcceptedDocumentList(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
                case _ => throw new BaseException(constants.Response.UNAUTHORIZED)
              }
            } else {
              throw new BaseException(constants.Response.ALL_NEGOTIATION_TERMS_CONFIRMED)
            }
          }
          else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          def sellerAccountID(negotiation: Negotiation) = masterTraders.Service.tryGetAccountId(negotiation.sellerTraderID)

          (for {
            traderID <- traderID
            negotiation <- negotiation
            _ <- updateStatus(traderID = traderID, negotiation = negotiation)
            sellerAccountID <- sellerAccountID(negotiation)
            result <- withUsernameToken.PartialContent(views.html.component.master.acceptOrRejectNegotiationTerms(negotiationID = negotiation.id, termType = acceptOrRejectNegotiationTermsData.termType, status = acceptOrRejectNegotiationTermsData.status))
          } yield {
            masterNegotiations.Service.sendMessageToNegotiationTermsActor(sellerAccountID, negotiation.id)
            result
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def oblDetailsForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(id, constants.File.OBL)

      def getResult(documentContent: Option[DocumentContent]) = {
        documentContent match {
          case Some(content) => {
            val obl = content.asInstanceOf[Serializable.OBL]
            withUsernameToken.Ok(views.html.component.master.oblDetails(views.companion.master.OBLDetails.form.fill(views.companion.master.OBLDetails.Data(id = id, billOfLadingNumber = obl.billOfLadingID, portOfLoading = obl.portOfLoading, shipperName = obl.shipperName, shipperAddress = obl.shipperAddress, notifyPartyName = obl.notifyPartyName, notifyPartyAddress = obl.notifyPartyAddress, shipmentDate = obl.dateOfShipping, deliveryTerm = obl.deliveryTerm, assetQuantity = obl.weightOfConsignment, assetPrice = obl.declaredAssetValue)), id = id))
          }
          case None => withUsernameToken.Ok(views.html.component.master.oblDetails(id = id))
        }
      }

      (for {
        documentContent <- documentContent
        result <- getResult(documentContent)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(id = id, failures = Seq(baseException.failure)))
      }
  }

  def oblDetails: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.OBLDetails.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.oblDetails(formWithErrors, formWithErrors.data(constants.FormField.TRADE_ID.name))))
        },
        updateOBLDetailsData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val sellerTraderID = masterNegotiations.Service.tryGetSellerTraderID(updateOBLDetailsData.id)
          val buyerTraderID = masterNegotiations.Service.tryGetBuyerTraderID(updateOBLDetailsData.id)

          def updateAndGetResult(traderID: String, sellerTraderID: String, buyerTraderID: String) = {
            if (traderID == sellerTraderID) {
              val updateOBLDetails = masterTransactionNegotiationFiles.Service.updateDocumentContent(updateOBLDetailsData.id, constants.File.OBL, Serializable.OBL(updateOBLDetailsData.billOfLadingNumber, updateOBLDetailsData.portOfLoading, updateOBLDetailsData.shipperName, updateOBLDetailsData.shipperAddress, updateOBLDetailsData.notifyPartyName, updateOBLDetailsData.notifyPartyAddress, updateOBLDetailsData.shipmentDate, updateOBLDetailsData.deliveryTerm, updateOBLDetailsData.assetQuantity, updateOBLDetailsData.assetPrice))
              val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(updateOBLDetailsData.id)
              val buyerAccountID = masterTraders.Service.tryGetAccountId(buyerTraderID)
              for {
                _ <- updateOBLDetails
                negotiationFiles <- negotiationFiles
                buyerAccountID <- buyerAccountID
                _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.OBL_DETAILS_ADDED, updateOBLDetailsData.id)
                _ <- utilitiesNotification.send(loginState.username, constants.Notification.OBL_DETAILS_ADDED, updateOBLDetailsData.id)
                result <- withUsernameToken.PartialContent(views.html.component.master.traderNegotiationDocumentUpload(updateOBLDetailsData.id, negotiationFiles))
              } yield result
            } else {
              Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
            }
          }

          (for {
            traderID <- traderID
            sellerTraderID <- sellerTraderID
            buyerTraderID <- buyerTraderID
            result <- updateAndGetResult(traderID, sellerTraderID, buyerTraderID)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def invoiceDetailsForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(id, constants.File.INVOICE)

      def getResult(documentContent: Option[DocumentContent]) = {
        documentContent match {
          case Some(content) => {
            val invoice = content.asInstanceOf[Serializable.Invoice]
            withUsernameToken.Ok(views.html.component.master.invoiceDetails(views.companion.master.InvoiceDetails.form.fill(views.companion.master.InvoiceDetails.Data(id = id, invoiceNumber = invoice.invoiceNumber, invoiceDate = invoice.invoiceDate)), id = id))
          }
          case None => withUsernameToken.Ok(views.html.component.master.invoiceDetails(id = id))
        }
      }

      (for {
        documentContent <- documentContent
        result <- getResult(documentContent)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(id = id, failures = Seq(baseException.failure)))
      }
  }

  def invoiceDetails: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.InvoiceDetails.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.invoiceDetails(formWithErrors, formWithErrors.data(constants.FormField.TRADE_ID.name))))
        },
        updateInvoiceDetailsData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val sellerTraderID = masterNegotiations.Service.tryGetSellerTraderID(updateInvoiceDetailsData.id)
          val buyerTraderID = masterNegotiations.Service.tryGetBuyerTraderID(updateInvoiceDetailsData.id)

          def updateAndGetResult(traderID: String, sellerTraderID: String, buyerTraderID: String) = {
            if (traderID == sellerTraderID) {
              val updateInvoiceDetails = masterTransactionNegotiationFiles.Service.updateDocumentContent(updateInvoiceDetailsData.id, constants.File.INVOICE, Serializable.Invoice(updateInvoiceDetailsData.invoiceNumber, updateInvoiceDetailsData.invoiceDate))
              val negotiationFiles = masterTransactionNegotiationFiles.Service.getAllDocuments(updateInvoiceDetailsData.id)
              val buyerAccountID = masterTraders.Service.tryGetAccountId(buyerTraderID)
              for {
                _ <- updateInvoiceDetails
                negotiationFiles <- negotiationFiles
                buyerAccountID <- buyerAccountID
                _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.INVOICE_DETAILS_ADDED, updateInvoiceDetailsData.id)
                _ <- utilitiesNotification.send(loginState.username, constants.Notification.INVOICE_DETAILS_ADDED, updateInvoiceDetailsData.id)
                result <- withUsernameToken.PartialContent(views.html.component.master.traderNegotiationDocumentUpload(updateInvoiceDetailsData.id, negotiationFiles))
              } yield result
            } else {
              Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
            }
          }

          (for {
            traderID <- traderID
            sellerTraderID <- sellerTraderID
            buyerTraderID <- buyerTraderID
            result <- updateAndGetResult(traderID, sellerTraderID, buyerTraderID)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def negotiationDocumentContent(id: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(id, documentType)
      (for {
        documentContent <- documentContent
      } yield Ok(views.html.component.master.negotiationDocumentContent(documentType, documentContent))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def confirmAllNegotiationTermsForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      withUsernameToken.Ok(views.html.component.master.confirmAllNegotiationTerms(id = id))
  }

  def confirmAllNegotiationTerms: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmAllNegotiationTerms.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.confirmAllNegotiationTerms(formWithErrors, formWithErrors.data(constants.FormField.ID.name))))
        },
        confirmAllNegotiationTermsData => {
          if (confirmAllNegotiationTermsData.confirm) {
            val traderID = masterTraders.Service.tryGetID(loginState.username)
            val negotiation = masterNegotiations.Service.tryGet(confirmAllNegotiationTermsData.id)

            def getResult(traderID: String, negotiation: Negotiation) = {
              if (traderID == negotiation.buyerTraderID) {
                if (negotiation.buyerAcceptedAssetDetails == master.BuyerAcceptedAssetDetails(description = true, price = true, quantity = true, shippingPeriod = true) && negotiation.buyerAcceptedPaymentTerms == master.BuyerAcceptedPaymentTerms(advancePayment = true, credit = true) && negotiation.buyerAcceptedDocumentsCheckList == master.BuyerAcceptedDocumentsCheckList(billOfExchangeRequired = true, documentList = true)) {
                  val updateStatus = masterNegotiations.Service.markBuyerAcceptedAllNegotiationTerms(confirmAllNegotiationTermsData.id)
                  val buyerAccountID = masterTraders.Service.tryGetAccountId(negotiation.buyerTraderID)
                  for {
                    _ <- updateStatus
                    buyerAccountID <- buyerAccountID
                    _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.BUYER_CONFIRMED_ALL_NEGOTIATION_TERMS, confirmAllNegotiationTermsData.id)
                    _ <- utilitiesNotification.send(loginState.username, constants.Notification.BUYER_CONFIRMED_ALL_NEGOTIATION_TERMS, confirmAllNegotiationTermsData.id)
                    result <- withUsernameToken.Ok(views.html.tradeRoom(confirmAllNegotiationTermsData.id))
                  } yield result
                } else {
                  throw new BaseException(constants.Response.ALL_NEGOTIATION_TERMS_NOT_CONFIRMED)
                }
              } else {
                throw new BaseException(constants.Response.UNAUTHORIZED)
              }
            }

            (for {
              traderID <- traderID
              negotiation <- negotiation
              result <- getResult(traderID, negotiation)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.tradeRoom(id = confirmAllNegotiationTermsData.id, failures = Seq(baseException.failure)))
            }
          } else {
            Future(BadRequest(views.html.component.master.confirmAllNegotiationTerms(id = confirmAllNegotiationTermsData.id)))
          }
        }

      )
  }

  def negotiationContract(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def checkTraderPartOfNegotiation(traderID: String) = masterNegotiations.Service.checkTraderNegotiationExists(id, traderID)

      def getResult(traderPartOfNegotiation: Boolean) = {
        if (traderPartOfNegotiation) {
          val negotiationFile = masterTransactionNegotiationFiles.Service.get(id, constants.File.CONTRACT)
          for {
            negotiationFile <- negotiationFile
            result <- withUsernameToken.Ok(views.html.component.master.traderUploadContract(id, negotiationFile))
          } yield result
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }

      (for {
        traderID <- traderID
        traderPartOfNegotiation <- checkTraderPartOfNegotiation(traderID)
        result <- getResult(traderPartOfNegotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

}
