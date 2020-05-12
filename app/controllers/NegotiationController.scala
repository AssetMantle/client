package controllers

import controllers.actions.{WithLoginAction, WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.AssetDocumentContent
import models.Abstract.NegotiationDocumentContent
import models.common.Serializable._
import models.docusign
import models.master.{Asset, Negotiation, Trader}
import models.masterTransaction.{NegotiationFile, TradeActivity}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import models.masterTransaction.{NegotiationFile, TradeActivity}
import models.{blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NegotiationController @Inject()(
                                       blockchainAccounts: blockchain.Accounts,
                                       messagesControllerComponents: MessagesControllerComponents,
                                       blockchainTransactionChangeBuyerBids: blockchainTransaction.ChangeBuyerBids,
                                       masterAccounts: master.Accounts,
                                       masterAssets: master.Assets,
                                       masterOrganizations: master.Organizations,
                                       masterTraderRelations: master.TraderRelations,
                                       masterTraders: master.Traders,
                                       masterZones: master.Zones,
                                       masterNegotiations: master.Negotiations,
                                       masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                       masterTransactionChats: masterTransaction.Chats,
                                       masterTransactionDocusignEnvelopes: docusign.Envelopes,
                                       masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                       masterTransactionTradeActivities: masterTransaction.TradeActivities,
                                       transaction: utilities.Transaction,
                                       utilitiesNotification: utilities.Notification,
                                       transactionsChangeBuyerBid: transactions.ChangeBuyerBid,
                                       withLoginAction: WithLoginAction,
                                       withTraderLoginAction: WithTraderLoginAction,
                                       withUsernameToken: WithUsernameToken,
                                       blockchainTransactionConfirmBuyerBids: blockchainTransaction.ConfirmBuyerBids,
                                       transactionsConfirmBuyerBid: transactions.ConfirmBuyerBid,
                                       transactionsConfirmSellerBid: transactions.ConfirmSellerBid,
                                       blockchainTransactionConfirmSellerBids: blockchainTransaction.ConfirmSellerBids,
                                     )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_NEGOTIATION

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def requestForm(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getAllTradableAssetList(traderID: String): Future[Seq[Asset]] = masterAssets.Service.getAllTradableAssets(traderID)

      def getCounterPartyList(traderID: String): Future[Seq[String]] = masterTraderRelations.Service.getAllCounterParties(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      (for {
        traderID <- traderID
        tradableAssetList <- getAllTradableAssetList(traderID)
        counterPartyList <- getCounterPartyList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(counterPartyList)
        counterPartyOrganizationList <- getCounterPartyOrganizations(counterPartyTraderList.map(_.organizationID))
      } yield Ok(views.html.component.master.negotiationRequest(tradableAssetList = tradableAssetList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def request(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.NegotiationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          def getAllTradableAssetList(traderID: String): Future[Seq[Asset]] = masterAssets.Service.getAllTradableAssets(traderID)

          def getCounterPartyList(traderID: String): Future[Seq[String]] = masterTraderRelations.Service.getAllCounterParties(traderID)

          def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

          def getCounterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

          (for {
            traderID <- traderID
            tradableAssetList <- getAllTradableAssetList(traderID)
            counterPartyList <- getCounterPartyList(traderID)
            counterPartyTraderList <- getCounterPartyTraderList(counterPartyList)
            counterPartyOrganizationList <- getCounterPartyOrganizations(counterPartyTraderList.map(_.organizationID))
          } yield BadRequest(views.html.component.master.negotiationRequest(formWithErrors, tradableAssetList, counterPartyTraderList, counterPartyOrganizationList))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        },
        requestData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val asset: Future[Asset] = masterAssets.Service.tryGet(id = requestData.assetID)

          def checkRelationExists(traderID: String): Future[Boolean] = masterTraderRelations.Service.checkRelationExists(fromID = traderID, toID = requestData.counterParty)

          def insert(traderID: String, asset: Asset, checkRelationExists: Boolean): Future[String] = {
            if (traderID != asset.ownerID || !checkRelationExists) throw new BaseException(constants.Response.UNAUTHORIZED)
            asset.status match {
              case constants.Status.Asset.REQUESTED_TO_ZONE | constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE | constants.Status.Asset.ISSUED => masterNegotiations.Service.create(buyerTraderID = requestData.counterParty, sellerTraderID = traderID, assetID = asset.id, description = asset.description, price = asset.price, quantity = asset.quantity, quantityUnit = asset.quantityUnit, assetOtherDetails = asset.otherDetails)
              case _ => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
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
        withUsernameToken.Ok(views.html.component.master.paymentTerms(views.companion.master.PaymentTerms.form.fill(views.companion.master.PaymentTerms.Data(id = id, advancePercentage = negotiation.paymentTerms.advancePercentage, credit = negotiation.paymentTerms.credit.map(credit => views.companion.master.PaymentTerms.CreditData(tenure = credit.tenure, tentativeDate = credit.tentativeDate.map(date => utilities.Date.sqlDateToUtilDate(date)), reference = credit.reference)))), id = id))
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

          def getAssetStatus(id: String): Future[String] = masterAssets.Service.tryGetStatus(id)

          def updatePaymentTerms(traderID: String, assetStatus: String, negotiation: Negotiation): Future[Int] = {
            if (traderID == negotiation.sellerTraderID) {
              negotiation.status match {
                case constants.Status.Negotiation.FORM_INCOMPLETE | constants.Status.Negotiation.ISSUE_ASSET_PENDING =>
                  assetStatus match {
                    case constants.Status.Asset.REQUESTED_TO_ZONE | constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE | constants.Status.Asset.ISSUED => masterNegotiations.Service.updatePaymentTerms(id = paymentTermsData.id, paymentTerms = PaymentTerms(advancePercentage = paymentTermsData.advancePercentage, credit = paymentTermsData.credit.map(creditData => Credit(tenure = creditData.tenure, tentativeDate = creditData.tentativeDate.map(date => utilities.Date.utilDateToSQLDate(date)), reference = creditData.reference))))
                    case _ => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
                  }
                case _ => throw new BaseException(constants.Response.UNAUTHORIZED)
              }
            } else throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            assetStatus <- getAssetStatus(negotiation.assetID)
            _ <- updatePaymentTerms(traderID = traderID, assetStatus = assetStatus, negotiation = negotiation)
            result <- withUsernameToken.PartialContent(views.html.component.master.documentList(views.companion.master.DocumentList.form.fill(views.companion.master.DocumentList.Data(id = negotiation.id, (negotiation.documentList.assetDocuments ++ negotiation.documentList.negotiationDocuments).map(document => Option(document)), documentListCompleted = true, physicalDocumentsHandledVia = negotiation.physicalDocumentsHandledVia)), id = negotiation.id))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def documentListForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(traderID: String, negotiation: Negotiation): Future[Result] = if (traderID == negotiation.sellerTraderID) {
        withUsernameToken.Ok(views.html.component.master.documentList(views.companion.master.DocumentList.form.fill(views.companion.master.DocumentList.Data(id = id, documentList = (negotiation.documentList.assetDocuments ++ negotiation.documentList.negotiationDocuments).map(document => Option(document)), documentListCompleted = true, physicalDocumentsHandledVia = negotiation.physicalDocumentsHandledVia)), id = id))
      } else throw new BaseException(constants.Response.UNAUTHORIZED)

      (for {
        traderID <- traderID
        negotiation <- negotiation
        result <- getResult(traderID = traderID, negotiation = negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def documentList(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.DocumentList.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.documentList(formWithErrors, id = formWithErrors.data(constants.FormField.ID.name))))
        },
        documentListData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(documentListData.id)

          def getAssetStatus(id: String): Future[String] = masterAssets.Service.tryGetStatus(id)

          def updateDocumentList(traderID: String, assetStatus: String, negotiation: Negotiation): Future[Int] = {
            if (traderID == negotiation.sellerTraderID) {
              negotiation.status match {
                case constants.Status.Negotiation.FORM_INCOMPLETE | constants.Status.Negotiation.ISSUE_ASSET_PENDING =>
                  assetStatus match {
                    case constants.Status.Asset.REQUESTED_TO_ZONE | constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE | constants.Status.Asset.ISSUED => masterNegotiations.Service.updateDocumentList(id = documentListData.id, documentList = DocumentList(assetDocuments = documentListData.documentList.flatten.filter(documentType => constants.File.ASSET_DOCUMENTS.contains(documentType)), negotiationDocuments = documentListData.documentList.flatten.filterNot(documentType => constants.File.ASSET_DOCUMENTS.contains(documentType))), physicalDocumentsHandledVia = documentListData.physicalDocumentsHandledVia.getOrElse(""))
                    case _ => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
                  }
                case _ => throw new BaseException(constants.Response.UNAUTHORIZED)
              }
            } else throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

          def getTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

          def getResult(asset: Asset, negotiation: Negotiation, counterPartyTrader: Trader): Future[Result] = if (documentListData.documentListCompleted) {
            withUsernameToken.PartialContent(views.html.component.master.reviewNegotiationRequest(asset = asset, negotiation = negotiation, counterPartyTrader = counterPartyTrader))
          } else {
            withUsernameToken.PartialContent(views.html.component.master.documentList(views.companion.master.DocumentList.form.fill(views.companion.master.DocumentList.Data(id = documentListData.id, documentList = documentListData.documentList, documentListCompleted = true, physicalDocumentsHandledVia = documentListData.physicalDocumentsHandledVia)), id = negotiation.id))
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            assetStatus <- getAssetStatus(negotiation.assetID)
            _ <- updateDocumentList(traderID = traderID, assetStatus = assetStatus, negotiation)
            asset <- getAsset(negotiation.assetID)
            counterPartyTrader <- getTrader(negotiation.buyerTraderID)
            result <- getResult(asset = asset, negotiation = negotiation, counterPartyTrader = counterPartyTrader)
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

      def getTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

      (for {
        traderID <- traderID
        negotiation <- negotiation
        asset <- getAsset(traderID, negotiation)
        counterPartyTrader <- getTrader(negotiation.buyerTraderID)
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

          def getTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

          (for {
            traderID <- traderID
            negotiation <- negotiation
            asset <- getAsset(traderID, negotiation)
            counterPartyTrader <- getTrader(negotiation.buyerTraderID)
          } yield BadRequest(views.html.component.master.reviewNegotiationRequest(formWithErrors, asset = asset, negotiation = negotiation, counterPartyTrader = counterPartyTrader))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        },
        reviewRequestData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(reviewRequestData.id)

          def getAssetStatus(assetID: String): Future[String] = masterAssets.Service.tryGetStatus(assetID)

          def update(traderID: String, assetStatus: String, negotiation: Negotiation): Future[Int] = {
            if (traderID == negotiation.sellerTraderID) {
              negotiation.status match {
                case constants.Status.Negotiation.FORM_INCOMPLETE =>
                  assetStatus match {
                    case constants.Status.Asset.REQUESTED_TO_ZONE | constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE => masterNegotiations.Service.markStatusIssueAssetPending(reviewRequestData.id)
                    case constants.Status.Asset.ISSUED => masterNegotiations.Service.markStatusRequestSent(reviewRequestData.id)
                    case _ => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
                  }
                case _ => throw new BaseException(constants.Response.UNAUTHORIZED)
              }
            } else throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            assetStatus <- getAssetStatus(negotiation.assetID)
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

      def getTraderName(traderID: String): Future[String] = masterTraders.Service.tryGetTraderName(traderID)

      def getResult(traderID: String, negotiation: Negotiation, sellerName: String): Future[Result] = if (traderID == negotiation.buyerTraderID) {
        withUsernameToken.Ok(views.html.component.master.acceptNegotiationRequest(negotiation = negotiation, sellerName = sellerName))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        sellerName <- getTraderName(negotiation.sellerTraderID)
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

          def getTraderName(traderID: String): Future[String] = masterTraders.Service.tryGetTraderName(traderID)

          def getResult(traderID: String, negotiation: Negotiation, sellerName: String): Future[Result] = if (traderID == negotiation.buyerTraderID) {
            Future(BadRequest(views.html.component.master.acceptNegotiationRequest(formWithErrors, negotiation = negotiation, sellerName = sellerName)))
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            sellerName <- getTraderName(negotiation.sellerTraderID)
            result <- getResult(traderID = traderID, negotiation = negotiation, sellerName = sellerName)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        },
        acceptRequestData => {
          (loginState.acl match {
            case Some(acl) =>
              if (acl.changeBuyerBid) {
                val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = acceptRequestData.password)
                val negotiation = masterNegotiations.Service.tryGet(acceptRequestData.id)

                def getTraderName(traderID: String): Future[String] = masterTraders.Service.tryGetTraderName(traderID)

                def getAssetPegHash(assetID: String): Future[String] = masterAssets.Service.tryGetPegHash(assetID)

                def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

                def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

                def sendTransaction(sellerAddress: String, pegHash: String, negotiation: Negotiation): Future[String] = {
                  transaction.process[blockchainTransaction.ChangeBuyerBid, transactionsChangeBuyerBid.Request](
                    entity = blockchainTransaction.ChangeBuyerBid(from = loginState.address, to = sellerAddress, bid = negotiation.price, time = negotiation.time.getOrElse(constants.Negotiation.DefaultTime), pegHash = pegHash, gas = acceptRequestData.gas, ticketID = "", mode = transactionMode),
                    blockchainTransactionCreate = blockchainTransactionChangeBuyerBids.Service.create,
                    request = transactionsChangeBuyerBid.Request(transactionsChangeBuyerBid.BaseReq(from = loginState.address, gas = acceptRequestData.gas.toString), to = sellerAddress, password = acceptRequestData.password, bid = negotiation.price.toString, time = negotiation.time.getOrElse(constants.Negotiation.DefaultTime).toString, pegHash = pegHash, mode = transactionMode),
                    action = transactionsChangeBuyerBid.Service.post,
                    onSuccess = blockchainTransactionChangeBuyerBids.Utility.onSuccess,
                    onFailure = blockchainTransactionChangeBuyerBids.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionChangeBuyerBids.Service.updateTransactionHash
                  )
                }

                def createChatIDAndChatRoom(sellerAccountID: String, negotiationID: String): Future[Unit] = {
                  val chatID = masterTransactionChats.Service.createGroupChat(loginState.username, sellerAccountID)

                  def insertChatID(chatID: String): Future[Int] = masterNegotiations.Service.insertChatID(id = negotiationID, chatID = chatID)

                  for {
                    chatID <- chatID
                    _ <- insertChatID(chatID)
                  } yield ()

                }

                def acceptNegotiationAndGetResult(validateUsernamePassword: Boolean, negotiation: Negotiation, sellerName: String): Future[Result] = if (validateUsernamePassword) {
                  for {
                    pegHash <- getAssetPegHash(negotiation.assetID)
                    sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
                    sellerAddress <- getAddress(sellerAccountID)
                    ticketID <- sendTransaction(sellerAddress = sellerAddress, pegHash = pegHash, negotiation = negotiation)
                    _ <- createChatIDAndChatRoom(sellerAccountID = sellerAccountID, negotiationID = negotiation.id)
                    _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.NEGOTIATION_REQUEST_ACCEPTED_BLOCKCHAIN_TRANSACTION_PENDING, sellerName, ticketID)
                    _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_REQUEST_ACCEPTED_BLOCKCHAIN_TRANSACTION_PENDING, ticketID)
                    result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.NEGOTIATION_REQUEST_ACCEPTED_BLOCKCHAIN_TRANSACTION_PENDING)))
                  } yield result
                }
                else {
                  Future(BadRequest(views.html.component.master.acceptNegotiationRequest(views.companion.master.AcceptNegotiationRequest.form.fill(acceptRequestData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), negotiation = negotiation, sellerName = sellerName)))
                }

                for {
                  validateUsernamePassword <- validateUsernamePassword
                  negotiation <- negotiation
                  sellerName <- getTraderName(negotiation.sellerTraderID)
                  result <- acceptNegotiationAndGetResult(validateUsernamePassword = validateUsernamePassword, negotiation = negotiation, sellerName = sellerName)
                } yield result
              } else throw new BaseException(constants.Response.UNAUTHORIZED)
            case None => throw new BaseException(constants.Response.UNAUTHORIZED)
          }).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def rejectRequestForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getTraderName(traderID: String): Future[String] = masterTraders.Service.tryGetTraderName(traderID)

      def getResult(traderID: String, negotiation: Negotiation, sellerName: String): Future[Result] = if (traderID == negotiation.buyerTraderID) {
        withUsernameToken.Ok(views.html.component.master.rejectNegotiationRequest(negotiation = negotiation, sellerName = sellerName))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        sellerName <- getTraderName(negotiation.sellerTraderID)
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

          def getTraderName(traderID: String): Future[String] = masterTraders.Service.tryGetTraderName(traderID)

          def getResult(traderID: String, negotiation: Negotiation, sellerName: String): Future[Result] = if (traderID == negotiation.buyerTraderID) {
            Future(BadRequest(views.html.component.master.rejectNegotiationRequest(formWithErrors, negotiation = negotiation, sellerName = sellerName)))
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            sellerName <- getTraderName(negotiation.sellerTraderID)
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
        withUsernameToken.Ok(views.html.component.master.updateNegotiationAssetTerms(views.companion.master.UpdateNegotiationAssetTerms.form.fill(views.companion.master.UpdateNegotiationAssetTerms.Data(id = negotiation.id, description = negotiation.assetDescription, price = negotiation.price, quantity = negotiation.quantity, quantityUnit = negotiation.quantityUnit))))
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

          def updateAssetTerms(traderID: String, negotiation: Negotiation): Future[Int] = if (traderID == negotiation.sellerTraderID) {
            val updateDescription = if (updateAssetTermsData.description != negotiation.assetDescription) masterNegotiations.Service.updateAssetDescription(id = updateAssetTermsData.id, assetDescription = updateAssetTermsData.description) else Future(0)
            val updatePrice = if (updateAssetTermsData.price != negotiation.price) masterNegotiations.Service.updatePrice(id = updateAssetTermsData.id, price = updateAssetTermsData.price) else Future(0)
            val updateQuantity = if (updateAssetTermsData.quantity != negotiation.quantity || updateAssetTermsData.quantityUnit != negotiation.quantityUnit) masterNegotiations.Service.updateQuantity(id = updateAssetTermsData.id, quantity = updateAssetTermsData.quantity, quantityUnit = updateAssetTermsData.quantityUnit) else Future(0)
            for {
              _ <- updateDescription
              _ <- updatePrice
              _ <- updateQuantity
            } yield 0
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          def getAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          (for {
            traderID <- traderID
            negotiation <- negotiation
            buyerAccountID <- getAccountID(negotiation.buyerTraderID)
            _ <- updateAssetTerms(traderID = traderID, negotiation = negotiation)
            _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_ASSET_TERMS_UPDATED, negotiation.id)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_ASSET_TERMS_UPDATED, negotiation.id)
            _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, constants.TradeActivity.ASSET_DETAILS_UPDATED, negotiation.sellerTraderID)
            result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = updateAssetTermsData.id, successes = Seq(constants.Response.NEGOTIATION_ASSET_TERMS_UPDATED)))
          } yield {
            actors.Service.cometActor ! actors.Message.makeCometMessage(username = buyerAccountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(Option(negotiation.id)))
            result
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def updateAssetOtherDetailsForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(traderID: String, negotiation: Negotiation): Future[Result] = if (traderID == negotiation.sellerTraderID) {
        withUsernameToken.Ok(views.html.component.master.updateNegotiationAssetOtherDetails(views.companion.master.UpdateNegotiationAssetOtherDetails.form.fill(views.companion.master.UpdateNegotiationAssetOtherDetails.Data(id = negotiation.id, shippingPeriod = negotiation.assetOtherDetails.shippingDetails.shippingPeriod, portOfLoading = negotiation.assetOtherDetails.shippingDetails.portOfLoading, portOfDischarge = negotiation.assetOtherDetails.shippingDetails.portOfDischarge))))
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

  def updateAssetOtherDetails(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateNegotiationAssetOtherDetails.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.updateNegotiationAssetOtherDetails(formWithErrors)))
        },
        updateAssetOtherDetailsData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(updateAssetOtherDetailsData.id)

          def updateAssetOtherDetails(traderID: String, negotiation: Negotiation): Future[Int] = if (traderID == negotiation.sellerTraderID) {
            masterNegotiations.Service.updateAssetOtherDetails(id = updateAssetOtherDetailsData.id, assetOtherDetails = AssetOtherDetails(shippingDetails = ShippingDetails(shippingPeriod = updateAssetOtherDetailsData.shippingPeriod, portOfLoading = updateAssetOtherDetailsData.portOfLoading, portOfDischarge = updateAssetOtherDetailsData.portOfDischarge)))
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          def getAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          (for {
            traderID <- traderID
            negotiation <- negotiation
            buyerAccountID <- getAccountID(negotiation.buyerTraderID)
            _ <- updateAssetOtherDetails(traderID = traderID, negotiation = negotiation)
            _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_OTHER_DETAILS_UPDATED, negotiation.id)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_OTHER_DETAILS_UPDATED, negotiation.id)
            result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = updateAssetOtherDetailsData.id, successes = Seq(constants.Response.NEGOTIATION_ASSET_TERMS_UPDATED)))
          } yield {
            actors.Service.cometActor ! actors.Message.makeCometMessage(username = buyerAccountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(Option(negotiation.id)))
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
        withUsernameToken.Ok(views.html.component.master.updatePaymentTerms(views.companion.master.PaymentTerms.form.fill(views.companion.master.PaymentTerms.Data(id = id, advancePercentage = negotiation.paymentTerms.advancePercentage, credit = negotiation.paymentTerms.credit.map(credit => views.companion.master.PaymentTerms.CreditData(tenure = credit.tenure, tentativeDate = credit.tentativeDate.map(date => utilities.Date.sqlDateToUtilDate(date)), reference = credit.reference))))))
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

          def updatePaymentTerms(traderID: String, negotiation: Negotiation): Future[Int] = if (traderID == negotiation.sellerTraderID) {
            masterNegotiations.Service.updatePaymentTerms(id = updatePaymentTermsData.id, paymentTerms = PaymentTerms(advancePercentage = updatePaymentTermsData.advancePercentage, credit = updatePaymentTermsData.credit.map(creditData => Credit(tenure = creditData.tenure, tentativeDate = creditData.tentativeDate.map(date => utilities.Date.utilDateToSQLDate(date)), reference = creditData.reference))))
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          def getAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          (for {
            traderID <- traderID
            negotiation <- negotiation
            buyerAccountID <- getAccountID(negotiation.buyerTraderID)
            _ <- updatePaymentTerms(traderID = traderID, negotiation = negotiation)
            _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_PAYMENT_TERMS_UPDATED, negotiation.id)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_PAYMENT_TERMS_UPDATED, negotiation.id)
            _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, constants.TradeActivity.PAYMENT_TERMS_UPDATED, negotiation.sellerTraderID)
            result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = updatePaymentTermsData.id, successes = Seq(constants.Response.NEGOTIATION_PAYMENT_TERMS_UPDATED)))
          } yield {
            actors.Service.cometActor ! actors.Message.makeCometMessage(username = buyerAccountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(Option(negotiation.id)))
            result
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def updateDocumentListForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(traderID: String, negotiation: Negotiation): Future[Result] = if (traderID == negotiation.sellerTraderID) {
        withUsernameToken.Ok(views.html.component.master.updateDocumentList(views.companion.master.DocumentList.form.fill(views.companion.master.DocumentList.Data(id = id, documentList = negotiation.documentList.assetDocuments.map(document => Option(document)) ++ negotiation.documentList.negotiationDocuments.map(document => Option(document)), documentListCompleted = true, physicalDocumentsHandledVia = negotiation.physicalDocumentsHandledVia))))
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

  def updateDocumentList(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.DocumentList.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.updateDocumentList(formWithErrors)))
        },
        updateDocumentListData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(updateDocumentListData.id)

          def updateDocumentList(traderID: String, negotiation: Negotiation): Future[Int] = if (traderID == negotiation.sellerTraderID) {
            masterNegotiations.Service.updateDocumentList(id = updateDocumentListData.id, documentList = DocumentList(assetDocuments = updateDocumentListData.documentList.flatten.filter(documentType => constants.File.ASSET_DOCUMENTS.contains(documentType)), negotiationDocuments = updateDocumentListData.documentList.flatten.filterNot(documentType => constants.File.ASSET_DOCUMENTS.contains(documentType))), physicalDocumentsHandledVia = updateDocumentListData.physicalDocumentsHandledVia.getOrElse(""))
          } else throw new BaseException(constants.Response.UNAUTHORIZED)

          def getAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          def getResult(negotiation: Negotiation): Future[Result] = if (updateDocumentListData.documentListCompleted) {
            withUsernameToken.Ok(views.html.tradeRoom(negotiationID = negotiation.id, successes = Seq(constants.Response.NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED)))
          } else {
            withUsernameToken.PartialContent(views.html.component.master.updateDocumentList(views.companion.master.DocumentList.form.fill(views.companion.master.DocumentList.Data(id = updateDocumentListData.id, documentList = updateDocumentListData.documentList, documentListCompleted = true, physicalDocumentsHandledVia = updateDocumentListData.physicalDocumentsHandledVia))))
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            buyerAccountID <- getAccountID(negotiation.buyerTraderID)
            _ <- updateDocumentList(traderID = traderID, negotiation = negotiation)
            _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED, negotiation.id)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED, negotiation.id)
            _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, constants.TradeActivity.DOCUMENT_LIST_UPDATED, negotiation.sellerTraderID)
            result <- getResult(negotiation)
          } yield {
            actors.Service.cometActor ! actors.Message.makeCometMessage(username = buyerAccountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(Option(negotiation.id)))
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
            acceptOrRejectNegotiationTermsData.termType match {
              case constants.View.ASSET_DESCRIPTION => masterNegotiations.Service.updateBuyerAcceptedAssetDescription(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
              case constants.View.PRICE => masterNegotiations.Service.updateBuyerAcceptedPrice(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
              case constants.View.QUANTITY => masterNegotiations.Service.updateBuyerAcceptedQuantity(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
              case constants.View.ASSET_OTHER_DETAILS => masterNegotiations.Service.updateBuyerAcceptedAssetOtherDetails(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
              case constants.View.PAYMENT_TERMS => masterNegotiations.Service.updateBuyerAcceptedPaymentTerms(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
              case constants.View.DOCUMENT_LIST => masterNegotiations.Service.updateBuyerAcceptedDocumentList(acceptOrRejectNegotiationTermsData.id, acceptOrRejectNegotiationTermsData.status)
              case _ => throw new BaseException(constants.Response.UNAUTHORIZED)
            }

          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          (for {
            traderID <- traderID
            negotiation <- negotiation
            _ <- updateStatus(traderID = traderID, negotiation = negotiation)
            sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
            result <- withUsernameToken.PartialContent(views.html.component.master.acceptOrRejectNegotiationTerms(negotiationID = negotiation.id, termType = acceptOrRejectNegotiationTermsData.termType, status = acceptOrRejectNegotiationTermsData.status))
          } yield {
            actors.Service.cometActor ! actors.Message.makeCometMessage(username = sellerAccountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(Option(negotiation.id)))
            result
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def addInvoiceForm(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(negotiationID, constants.File.Negotiation.INVOICE)

      def getResult(documentContent: Option[NegotiationDocumentContent]) = {
        documentContent match {
          case Some(content) => {
            val invoice = content.asInstanceOf[Invoice]
            withUsernameToken.Ok(views.html.component.master.addInvoice(views.companion.master.AddInvoice.form.fill(views.companion.master.AddInvoice.Data(negotiationID = negotiationID, invoiceNumber = invoice.invoiceNumber, invoiceDate = utilities.Date.sqlDateToUtilDate(invoice.invoiceDate))), negotiationID = negotiationID))
          }
          case None => withUsernameToken.Ok(views.html.component.master.addInvoice(negotiationID = negotiationID))
        }
      }

      (for {
        documentContent <- documentContent
        result <- getResult(documentContent)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
      }
  }

  def addInvoice(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddInvoice.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addInvoice(formWithErrors, formWithErrors.data(constants.FormField.ID.name))))
        },
        updateInvoiceContentData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(updateInvoiceContentData.negotiationID)

          def updateAndGetResult(traderID: String, negotiation: Negotiation): Future[Result] = {
            if (traderID == negotiation.sellerTraderID) {
              val updateInvoiceContent = masterTransactionNegotiationFiles.Service.updateDocumentContent(updateInvoiceContentData.negotiationID, constants.File.Negotiation.INVOICE, Invoice(updateInvoiceContentData.invoiceNumber, utilities.Date.utilDateToSQLDate(updateInvoiceContentData.invoiceDate)))
              val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(updateInvoiceContentData.negotiationID)
              val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
              val negotiationEnvelopeList = masterTransactionDocusignEnvelopes.Service.getAll(updateInvoiceContentData.negotiationID)
              val buyerAccountID = masterTraders.Service.tryGetAccountId(negotiation.buyerTraderID)
              for {
                _ <- updateInvoiceContent
                negotiationFileList <- negotiationFileList
                assetFileList <- assetFileList
                negotiationEnvelopeList <- negotiationEnvelopeList
                buyerAccountID <- buyerAccountID
                _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.INVOICE_CONTENT_ADDED, updateInvoiceContentData.negotiationID)
                _ <- utilitiesNotification.send(loginState.username, constants.Notification.INVOICE_CONTENT_ADDED, updateInvoiceContentData.negotiationID)
                result <- withUsernameToken.PartialContent(views.html.component.master.tradeDocuments(negotiation, assetFileList, negotiationFileList, negotiationEnvelopeList))
              } yield result
            } else {
              Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
            }
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            result <- updateAndGetResult(traderID, negotiation)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def addContractForm(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(negotiationID, constants.File.Negotiation.CONTRACT)

      def getResult(documentContent: Option[NegotiationDocumentContent]) = {
        documentContent match {
          case Some(content) => {
            val contract = content.asInstanceOf[Contract]
            withUsernameToken.Ok(views.html.component.master.addContract(views.companion.master.AddContract.form.fill(views.companion.master.AddContract.Data(negotiationID = negotiationID, contractNumber = contract.contractNumber)), negotiationID = negotiationID))
          }
          case None => withUsernameToken.Ok(views.html.component.master.addContract(negotiationID = negotiationID))
        }
      }

      (for {
        documentContent <- documentContent
        result <- getResult(documentContent)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
      }
  }

  def addContract(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddContract.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addContract(formWithErrors, formWithErrors.data(constants.FormField.ID.name))))
        },
        updateContractContentData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(updateContractContentData.negotiationID)

          def updateAndGetResult(traderID: String, negotiation: Negotiation) = {
            if (traderID == negotiation.sellerTraderID) {
              val updateContractContent = masterTransactionNegotiationFiles.Service.updateDocumentContent(updateContractContentData.negotiationID, constants.File.Negotiation.CONTRACT, Contract(updateContractContentData.contractNumber))
              val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(updateContractContentData.negotiationID)
              val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
              val negotiationEnvelopeList = masterTransactionDocusignEnvelopes.Service.getAll(updateContractContentData.negotiationID)
              val buyerAccountID = masterTraders.Service.tryGetAccountId(negotiation.buyerTraderID)
              for {
                _ <- updateContractContent
                negotiationFileList <- negotiationFileList
                assetFileList <- assetFileList
                negotiationEnvelopeList <- negotiationEnvelopeList
                buyerAccountID <- buyerAccountID
                _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.CONTRACT_CONTENT_ADDED, updateContractContentData.negotiationID)
                _ <- utilitiesNotification.send(loginState.username, constants.Notification.CONTRACT_CONTENT_ADDED, updateContractContentData.negotiationID)
                result <- withUsernameToken.PartialContent(views.html.component.master.tradeDocuments(negotiation, assetFileList, negotiationFileList, negotiationEnvelopeList))
              } yield result
            } else {
              Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
            }
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            result <- updateAndGetResult(traderID, negotiation)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def buyerConfirmForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      (for {
        traderID <- traderID
        negotiation <- negotiation
      } yield if (negotiation.buyerTraderID == traderID) Ok(views.html.component.master.buyerConfirmNegotiation(negotiation = negotiation)) else throw new BaseException(constants.Response.UNAUTHORIZED)
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def buyerConfirm(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmNegotiation.form.bindFromRequest().fold(
        formWithErrors => {
          val negotiation = masterNegotiations.Service.tryGet(formWithErrors.get.id)

          (for {
            negotiation <- negotiation
          } yield BadRequest(views.html.component.master.buyerConfirmNegotiation(formWithErrors, negotiation = negotiation))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        },
        buyerConfirmData => {
          val buyerTraderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(buyerConfirmData.id)
          val contract = masterTransactionNegotiationFiles.Service.tryGet(id = buyerConfirmData.id, documentType = constants.File.Negotiation.CONTRACT)

          def getPegHash(assetID: String): Future[String] = masterAssets.Service.tryGetPegHash(assetID)

          def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def sendTransaction(buyerTraderID: String, sellerAddress: String, pegHash: String, negotiation: Negotiation, contract: NegotiationFile): Future[String] = {
            if (buyerTraderID != negotiation.buyerTraderID || !loginState.acl.getOrElse(throw new BaseException(constants.Response.UNAUTHORIZED)).confirmBuyerBid) throw new BaseException(constants.Response.UNAUTHORIZED)
            else if (!(negotiation.buyerAcceptedPrice && negotiation.buyerAcceptedQuantity && negotiation.buyerAcceptedAssetDescription && negotiation.buyerAcceptedAssetOtherDetails && negotiation.buyerAcceptedPaymentTerms && negotiation.buyerAcceptedDocumentList)) throw new BaseException(constants.Response.NEGOTIATION_TERMS_NOT_ACCEPTED)
            else contract.status match {
              case Some(status) => if (!status) throw new BaseException(constants.Response.CONTRACT_REJECTED)
              case None => throw new BaseException(constants.Response.CONTRACT_NOT_VERIFIED)
            }

            val contractHash = utilities.FileOperations.getDocumentsHash(contract)

            transaction.process[blockchainTransaction.ConfirmBuyerBid, transactionsConfirmBuyerBid.Request](
              entity = blockchainTransaction.ConfirmBuyerBid(from = loginState.address, to = sellerAddress, bid = negotiation.price, time = negotiation.time.getOrElse(constants.Negotiation.DefaultTime), pegHash = pegHash, buyerContractHash = contractHash, gas = buyerConfirmData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionConfirmBuyerBids.Service.create,
              request = transactionsConfirmBuyerBid.Request(transactionsConfirmBuyerBid.BaseReq(from = loginState.address, gas = buyerConfirmData.gas.toString), to = sellerAddress, password = buyerConfirmData.password, bid = negotiation.price.toString, time = negotiation.time.getOrElse(constants.Negotiation.DefaultTime).toString, pegHash = pegHash, buyerContractHash = contractHash, mode = transactionMode),
              action = transactionsConfirmBuyerBid.Service.post,
              onSuccess = blockchainTransactionConfirmBuyerBids.Utility.onSuccess,
              onFailure = blockchainTransactionConfirmBuyerBids.Utility.onFailure,
              updateTransactionHash = blockchainTransactionConfirmBuyerBids.Service.updateTransactionHash
            )
          }

          (for {
            buyerTraderID <- buyerTraderID
            negotiation <- negotiation
            contract <- contract
            pegHash <- getPegHash(negotiation.assetID)
            sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
            sellerAddress <- getAddress(sellerAccountID)
            ticketID <- sendTransaction(buyerTraderID = buyerTraderID, sellerAddress = sellerAddress, pegHash = pegHash, negotiation = negotiation, contract = contract)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.BLOCKCHAIN_TRANSACTION_BUYER_CONFIRM_NEGOTIATION_TRANSACTION_SENT, ticketID)
            _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.BLOCKCHAIN_TRANSACTION_BUYER_CONFIRM_NEGOTIATION_TRANSACTION_SENT, ticketID)
            _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, constants.TradeActivity.BLOCKCHAIN_TRANSACTION_BUYER_CONFIRM_NEGOTIATION_TRANSACTION_SENT, ticketID)
            result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = negotiation.id, successes = Seq(constants.Response.BLOCKCHAIN_TRANSACTION_BUYER_CONFIRM_NEGOTIATION_TRANSACTION_SENT)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = buyerConfirmData.id, failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def sellerConfirmForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)
      (for {
        traderID <- traderID
        negotiation <- negotiation
      } yield if (negotiation.sellerTraderID == traderID) Ok(views.html.component.master.sellerConfirmNegotiation(negotiation = negotiation)) else throw new BaseException(constants.Response.UNAUTHORIZED)
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def sellerConfirm(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmNegotiation.form.bindFromRequest().fold(
        formWithErrors => {
          val negotiation = masterNegotiations.Service.tryGet(formWithErrors.get.id)

          (for {
            negotiation <- negotiation
          } yield BadRequest(views.html.component.master.sellerConfirmNegotiation(formWithErrors, negotiation = negotiation))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        },
        sellerConfirmData => {
          val sellerTraderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(sellerConfirmData.id)
          val contract = masterTransactionNegotiationFiles.Service.tryGet(id = sellerConfirmData.id, documentType = constants.File.Negotiation.CONTRACT)

          def getPegHash(assetID: String): Future[String] = masterAssets.Service.tryGetPegHash(assetID)

          def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def sendTransaction(sellerTraderID: String, buyerAddress: String, pegHash: String, negotiation: Negotiation, contract: NegotiationFile): Future[String] = {
            if (sellerTraderID != negotiation.sellerTraderID || !loginState.acl.getOrElse(throw new BaseException(constants.Response.UNAUTHORIZED)).confirmSellerBid) throw new BaseException(constants.Response.UNAUTHORIZED)
            if (!(negotiation.buyerAcceptedPrice && negotiation.buyerAcceptedQuantity && negotiation.buyerAcceptedAssetDescription && negotiation.buyerAcceptedAssetOtherDetails && negotiation.buyerAcceptedPaymentTerms && negotiation.buyerAcceptedDocumentList)) throw new BaseException(constants.Response.NEGOTIATION_TERMS_NOT_ACCEPTED)
            contract.status match {
              case Some(status) => if (!status) throw new BaseException(constants.Response.CONTRACT_REJECTED)
              case None => throw new BaseException(constants.Response.CONTRACT_NOT_VERIFIED)
            }

            val contractHash = utilities.FileOperations.getDocumentsHash(contract)

            transaction.process[blockchainTransaction.ConfirmSellerBid, transactionsConfirmSellerBid.Request](
              entity = blockchainTransaction.ConfirmSellerBid(from = loginState.address, to = buyerAddress, bid = negotiation.price, time = negotiation.time.getOrElse(constants.Negotiation.DefaultTime), pegHash = pegHash, sellerContractHash = contractHash, gas = sellerConfirmData.gas, ticketID = "", mode = transactionMode),
              blockchainTransactionCreate = blockchainTransactionConfirmSellerBids.Service.create,
              request = transactionsConfirmSellerBid.Request(transactionsConfirmSellerBid.BaseReq(from = loginState.address, gas = sellerConfirmData.gas.toString), to = buyerAddress, password = sellerConfirmData.password, bid = negotiation.price.toString, time = negotiation.time.getOrElse(constants.Negotiation.DefaultTime).toString, pegHash = pegHash, sellerContractHash = contractHash, mode = transactionMode),
              action = transactionsConfirmSellerBid.Service.post,
              onSuccess = blockchainTransactionConfirmSellerBids.Utility.onSuccess,
              onFailure = blockchainTransactionConfirmSellerBids.Utility.onFailure,
              updateTransactionHash = blockchainTransactionConfirmSellerBids.Service.updateTransactionHash
            )
          }

          (for {
            sellerTraderID <- sellerTraderID
            negotiation <- negotiation
            contract <- contract
            pegHash <- getPegHash(negotiation.assetID)
            buyerAccountID <- getTraderAccountID(negotiation.buyerTraderID)
            buyerAddress <- getAddress(buyerAccountID)
            ticketID <- sendTransaction(sellerTraderID = sellerTraderID, buyerAddress = buyerAddress, pegHash = pegHash, negotiation = negotiation, contract = contract)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.BLOCKCHAIN_TRANSACTION_SELLER_CONFIRM_NEGOTIATION_TRANSACTION_SENT, ticketID)
            _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.BLOCKCHAIN_TRANSACTION_SELLER_CONFIRM_NEGOTIATION_TRANSACTION_SENT, ticketID)
            _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, constants.TradeActivity.BLOCKCHAIN_TRANSACTION_SELLER_CONFIRM_NEGOTIATION_TRANSACTION_SENT, ticketID)
            result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = negotiation.id, successes = Seq(constants.Response.BLOCKCHAIN_TRANSACTION_SELLER_CONFIRM_NEGOTIATION_TRANSACTION_SENT)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = sellerConfirmData.id, failures = Seq(baseException.failure)))
          }
        }
      )
  }


  def tradeActivityMessages(negotiationID: String, pageNumber: Int): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val buyerTraderID = masterNegotiations.Service.tryGetBuyerTraderID(negotiationID)
      val sellerTraderID = masterNegotiations.Service.tryGetSellerTraderID(negotiationID)

      def getOrganizationID(traderID: String): Future[String] = masterTraders.Service.tryGetOrganizationID(traderID)

      def getZoneID(traderID: String): Future[String] = masterTraders.Service.tryGetZoneID(traderID)

      def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

      def getOrganizationAccountID(organizationID: String): Future[String] = masterOrganizations.Service.tryGetAccountID(organizationID)

      def getZoneAccountID(zoneID: String): Future[String] = masterZones.Service.tryGetAccountID(zoneID)

      def getTradeActivityMessages(accountIDs: String*): Future[Seq[TradeActivity]] = {
        if (!accountIDs.contains(loginState.username)) throw new BaseException(constants.Response.UNAUTHORIZED)
        if (pageNumber < 1) throw new BaseException(constants.Response.INVALID_PAGE_NUMBER)
        masterTransactionTradeActivities.Service.getAllTradeActivities(negotiationID = negotiationID, pageNumber = pageNumber)
      }

      (for {
        buyerTraderID <- buyerTraderID
        buyerOrganizationID <- getOrganizationID(buyerTraderID)
        buyerZoneID <- getZoneID(buyerTraderID)
        buyerAccountID <- getTraderAccountID(buyerTraderID)
        buyerOrganizationAccountID <- getOrganizationAccountID(buyerOrganizationID)
        buyerZoneAccountID <- getZoneAccountID(buyerZoneID)
        sellerTraderID <- sellerTraderID
        sellerOrganizationID <- getOrganizationID(sellerTraderID)
        sellerZoneID <- getZoneID(sellerTraderID)
        sellerAccountID <- getTraderAccountID(sellerTraderID)
        sellerOrganizationAccountID <- getOrganizationAccountID(sellerOrganizationID)
        sellerZoneAccountID <- getZoneAccountID(sellerZoneID)
        tradeActivityMessages <- getTradeActivityMessages(buyerAccountID, buyerOrganizationAccountID, buyerZoneAccountID, sellerAccountID, sellerOrganizationAccountID, sellerZoneAccountID)
      } yield Ok(views.html.component.master.tradeActivityMessages(tradeActivities = tradeActivityMessages))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def confirmAllNegotiationTermsForm(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      withUsernameToken.Ok(views.html.component.master.confirmAllNegotiationTerms(negotiationID = negotiationID))
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
            val negotiation = masterNegotiations.Service.tryGet(confirmAllNegotiationTermsData.negotiationID)

            def getResult(traderID: String, negotiation: Negotiation) = {
              if (traderID == negotiation.buyerTraderID) {
                if (negotiation.buyerAcceptedAssetDescription && negotiation.buyerAcceptedPrice && negotiation.buyerAcceptedQuantity && negotiation.buyerAcceptedAssetOtherDetails && negotiation.buyerAcceptedPaymentTerms && negotiation.buyerAcceptedDocumentList) {
                  val updateStatus = masterNegotiations.Service.markBuyerAcceptedAllNegotiationTerms(confirmAllNegotiationTermsData.negotiationID)
                  val sellerAccountID = masterTraders.Service.tryGetAccountId(negotiation.sellerTraderID)
                  for {
                    _ <- updateStatus
                    sellerAccountID <- sellerAccountID
                    _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, confirmAllNegotiationTermsData.negotiationID)
                    _ <- utilitiesNotification.send(loginState.username, constants.Notification.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, confirmAllNegotiationTermsData.negotiationID)
                    result <- withUsernameToken.Ok(views.html.tradeRoom(confirmAllNegotiationTermsData.negotiationID))
                  } yield {
                    actors.Service.cometActor ! actors.Message.makeCometMessage(username = sellerAccountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(Option(negotiation.id)))
                    result
                  }
                } else {
                  throw new BaseException(constants.Response.NEGOTIATION_TERMS_NOT_CONFIRMED)
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
              case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = confirmAllNegotiationTermsData.negotiationID, failures = Seq(baseException.failure)))
            }
          } else {
            Future(BadRequest(views.html.component.master.confirmAllNegotiationTerms(negotiationID = confirmAllNegotiationTermsData.negotiationID)))
          }
        }
      )
  }

  def updateContractSignedForm(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)
      (for {
        traderID <- traderID
        negotiation <- negotiation
      } yield {
        if (traderID == negotiation.sellerTraderID && negotiation.status == constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS) {
          Ok(views.html.component.master.updateContractSigned(negotiationID = negotiationID))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
      }
  }

  def updateContractSigned: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateContractSigned.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.updateContractSigned(formWithErrors, formWithErrors.data(constants.FormField.NEGOTIATION_ID.name))))
        },
        updateContractSignedData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(updateContractSignedData.negotiationID)

          def markContractSigned(traderID: String, negotiation: Negotiation) = if (negotiation.sellerTraderID == traderID && negotiation.status == constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS) masterNegotiations.Service.markContractSigned(updateContractSignedData.negotiationID) else Future(throw new BaseException(constants.Response.UNAUTHORIZED))

          (for {
            traderID <- traderID
            negotiation <- negotiation
            _ <- markContractSigned(traderID, negotiation)
            result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = updateContractSignedData.negotiationID))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = updateContractSignedData.negotiationID, failures = Seq(baseException.failure)))
          }
        }
      )
  }
}
