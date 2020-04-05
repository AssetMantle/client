package controllers

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.{Asset, Negotiation, Trader}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NegotiationController @Inject()(
                                       messagesControllerComponents: MessagesControllerComponents,
                                       transaction: utilities.Transaction,
                                       masterAccounts: master.Accounts,
                                       masterAssets: master.Assets,
                                       masterTradeRelations: master.TraderRelations,
                                       masterTransactionTradeTerms: masterTransaction.TradeTerms,
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
                                       masterTransactionChats: masterTransaction.Chats
                                     )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_NEGOTIATION

  private val negotiationDefaultTime: Int = configuration.get[Int]("blockchain.negotiation.defaultTime")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def requestForm(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getAssets(traderID: String): Future[Seq[Asset]] = masterAssets.Service.getAllAssets(traderID)

      def getCounterPartyList(traderID: String): Future[Seq[String]] = masterTradeRelations.Service.getAllCounterParties(traderID)

      def getCounterPartyTraders(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      (for {
        traderID <- traderID
        assets <- getAssets(traderID)
        counterPartyList <- getCounterPartyList(traderID)
        counterPartyTraders <- getCounterPartyTraders(counterPartyList)
      } yield Ok(views.html.component.master.negotiationRequest(assets = assets, counterPartyTraders = counterPartyTraders))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def request(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.Negotiation.form.bindFromRequest().fold(
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
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        requestData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          val asset: Future[Asset] = masterAssets.Service.tryGet(id = requestData.assetID)

          def checkRelationExists(traderID: String): Future[Boolean] = masterTradeRelations.Service.checkRelationExists(fromID = traderID, toID = requestData.counterParty)

          def insert(traderID: String, asset: Asset, checkRelationExists: Boolean): Future[String] = {
            if (traderID != asset.ownerID || !checkRelationExists) throw new BaseException(constants.Response.UNAUTHORIZED)
            asset.status match {
              case constants.Status.Asset.REQUESTED_TO_ZONE | constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE => masterNegotiations.Service.create(buyerTraderID = requestData.counterParty, sellerTraderID = traderID, assetID = asset.id, price = asset.price, quantity = asset.quantity, quantityUnit = asset.quantityUnit, status = constants.Status.Negotiation.ISSUE_ASSET_PENDING)
              case constants.Status.Asset.ISSUED | constants.Status.Asset.TRADE_COMPLETED => masterNegotiations.Service.create(buyerTraderID = requestData.counterParty, sellerTraderID = traderID, assetID = asset.id, price = asset.price, quantity = asset.quantity, quantityUnit = asset.quantityUnit, status = constants.Status.Negotiation.FORM_INCOMPLETE)
              case constants.Status.Asset.REJECTED_BY_ZONE | constants.Status.Asset.ISSUE_ASSET_FAILED | constants.Status.Asset.IN_ORDER | constants.Status.Asset.REDEEMED => throw new BaseException(constants.Response.ASSET_PEG_NOT_FOUND)
              case _ => throw new BaseException(constants.Response.ASSET_PEG_NOT_FOUND)
            }
          }

          (for {
            traderID <- traderID
            asset <- asset
            checkRelationExists <- checkRelationExists(traderID)
            id <- insert(traderID = traderID, asset = asset, checkRelationExists = checkRelationExists)
            result <- withUsernameToken.PartialContent(views.html.component.master.negotiationPaymentTerms(id = id))
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
        withUsernameToken.Ok(views.html.component.master.negotiationPaymentTerms(views.companion.master.NegotiationPaymentTerms.form.fill(views.companion.master.NegotiationPaymentTerms.Data(id = id, advancePayment = negotiation.paymentTerms.advancePayment.getOrElse(false), advancePercentage = negotiation.paymentTerms.advancePercentage, credit = negotiation.paymentTerms.credit.getOrElse(false), tenure = negotiation.paymentTerms.tenure, tentativeDate = if (negotiation.paymentTerms.tentativeDate.isDefined) Option(utilities.Date.sqlDateToUtilDate(negotiation.paymentTerms.tentativeDate.get)) else None, refrence = negotiation.paymentTerms.reference)), id = id))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(traderID, negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def paymentTerms(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.NegotiationPaymentTerms.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.negotiationPaymentTerms(formWithErrors, id = formWithErrors.data(constants.FormField.ID.name))))
        },
        paymentTermsData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          def assetStatus(id: String): Future[String] = masterAssets.Service.tryGetStatus(id)

          val negotiation = masterNegotiations.Service.tryGet(paymentTermsData.id)

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
            result <- withUsernameToken.PartialContent(views.html.component.master.negotiationDocumentsCheckList(views.companion.master.NegotiationDocumentsCheckList.form.fill(views.companion.master.NegotiationDocumentsCheckList.Data(id = negotiation.id, billOfExchange = negotiation.documentsCheckList.billOfExchange.getOrElse(false), coo = negotiation.documentsCheckList.coo.getOrElse(false), coa = negotiation.documentsCheckList.coa.getOrElse(false), otherDocuments = negotiation.documentsCheckList.otherDocuments)), id = negotiation.id))
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
        withUsernameToken.Ok(views.html.component.master.negotiationDocumentsCheckList(views.companion.master.NegotiationDocumentsCheckList.form.fill(views.companion.master.NegotiationDocumentsCheckList.Data(id = id, billOfExchange = negotiation.documentsCheckList.billOfExchange.getOrElse(false), coo = negotiation.documentsCheckList.coo.getOrElse(false), coa = negotiation.documentsCheckList.coa.getOrElse(false), otherDocuments = negotiation.documentsCheckList.otherDocuments)), id = id))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(traderID, negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def documentsCheckList(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.NegotiationDocumentsCheckList.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.negotiationDocumentsCheckList(formWithErrors, id = formWithErrors.data(constants.FormField.ID.name))))
        },
        documentsCheckListData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          def assetStatus(id: String): Future[String] = masterAssets.Service.tryGetStatus(id)

          val negotiation = masterNegotiations.Service.tryGet(documentsCheckListData.id)

          def update(traderID: String, assetStatus: String, negotiation: Negotiation): Future[Int] = {
            if (traderID != negotiation.sellerTraderID) throw new BaseException(constants.Response.UNAUTHORIZED)
            negotiation.status match {
              case constants.Status.Negotiation.ISSUE_ASSET_FAILED | constants.Status.Negotiation.FORM_INCOMPLETE | constants.Status.Negotiation.ISSUE_ASSET_PENDING | constants.Status.Negotiation.REQUEST_SENDING_WAITING_FOR_ISSUE_ASSET =>
                assetStatus match {
                  case constants.Status.Asset.REQUESTED_TO_ZONE | constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE | constants.Status.Asset.ISSUED | constants.Status.Asset.TRADE_COMPLETED => masterNegotiations.Service.updateDocumentsCheckList(id = documentsCheckListData.id, billOfExchange = documentsCheckListData.billOfExchange, coo = documentsCheckListData.coo, coa = documentsCheckListData.coa, otherDocuments = documentsCheckListData.otherDocuments)
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

            def insertChatID(chatID: String): Future[String] = masterNegotiations.Service.insertChatID(id = negotiationID, chatID = chatID)

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

          def sellerAccountID(sellerTraderID: String): Future[String] = masterTraders.Service.tryGetAccountId(sellerTraderID)

          (for {
            _ <- markNegotiationRejected
            negotiation <- negotiation
            sellerAccountID <- sellerAccountID(negotiation.sellerTraderID)
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


}
