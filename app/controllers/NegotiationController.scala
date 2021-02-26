package controllers

import controllers.actions.{WithLoginActionAsync, WithTraderLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.NegotiationDocumentContent
import models._
import models.common.Serializable._
import models.master.{Asset, Negotiation, Organization, Split, Trader}
import models.masterTransaction.{AssetFile, NegotiationFile, TradeActivity, TradeActivityHistory}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}
import utilities.MicroNumber

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NegotiationController @Inject()(
                                       blockchainAccounts: blockchain.Accounts,
                                       messagesControllerComponents: MessagesControllerComponents,
                                       masterAccounts: master.Accounts,
                                       masterAssets: master.Assets,
                                       masterOrganizations: master.Organizations,
                                       masterTraderRelations: master.TraderRelations,
                                       masterTraders: master.Traders,
                                       masterProperties: master.Properties,
                                       masterSplits: master.Splits,
                                       masterZones: master.Zones,
                                       masterNegotiations: master.Negotiations,
                                       masterNegotiationHistories: master.NegotiationHistories,
                                       masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                       masterTransactionChats: masterTransaction.Chats,
                                       masterTransactionDocusignEnvelopes: docusign.Envelopes,
                                       masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                       masterTransactionTradeActivities: masterTransaction.TradeActivities,
                                       masterTransactionTradeActivityHistories: masterTransaction.TradeActivityHistories,
                                       transaction: utilities.Transaction,
                                       utilitiesNotification: utilities.Notification,
                                       withTraderLoginAction: WithTraderLoginAction,
                                       withUsernameToken: WithUsernameToken,
                                       withLoginActionAsync: WithLoginActionAsync,
                                     )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_NEGOTIATION

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def requestForm(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def getAllAssetSplits(traderID:String) = masterSplits.Service.getAllAssetsByOwnerIDs(Seq(traderID))

      def getAllTradableAssetProperties(assetIDs: Seq[String]): Future[Map[String, Map[String,Option[String]]]] = masterProperties.Service.getPropertyListMap(assetIDs)

      def getAllTradableAssetList(assetIDs: Seq[String])= masterAssets.Service.getAllByIDs(assetIDs)

      def getCounterPartyList(traderID: String): Future[Seq[String]] = masterTraderRelations.Service.getAllCounterParties(traderID)

      def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

      def getCounterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      (for {
        traderID <- traderID
        allAssetSplits<-getAllAssetSplits(traderID)
        tradableAssetProperties <- getAllTradableAssetProperties(allAssetSplits.map(_.ownableID))
        tradableAssetList<-getAllTradableAssetList(allAssetSplits.map(_.ownableID))
        counterPartyList <- getCounterPartyList(traderID)
        counterPartyTraderList <- getCounterPartyTraderList(counterPartyList)
        counterPartyOrganizationList <- getCounterPartyOrganizations(counterPartyTraderList.map(_.organizationID))
      } yield Ok(views.html.component.master.negotiationRequest(tradableAssetProperties = tradableAssetProperties,tradableAssetList= tradableAssetList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def request(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.NegotiationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          def getAllAssetSplits(traderID:String) = masterSplits.Service.getAllAssetsByOwnerIDs(Seq(traderID))

          def getAllTradableAssetProperties(assetIDs: Seq[String]):Future[Map[String, Map[String,Option[String]]]] = masterProperties.Service.getPropertyListMap(assetIDs)

          def getAllTradableAssetList(assetIDs: Seq[String])= masterAssets.Service.getAllByIDs(assetIDs)

          def getCounterPartyList(traderID: String): Future[Seq[String]] = masterTraderRelations.Service.getAllCounterParties(traderID)

          def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

          def getCounterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

          (for {
            traderID <- traderID
            allAssetSplits<-getAllAssetSplits(traderID)
            tradableAssetProperties <- getAllTradableAssetProperties(allAssetSplits.map(_.ownableID))
            tradableAssetList<-getAllTradableAssetList(allAssetSplits.map(_.ownableID))
            counterPartyList <- getCounterPartyList(traderID)
            counterPartyTraderList <- getCounterPartyTraderList(counterPartyList)
            counterPartyOrganizationList <- getCounterPartyOrganizations(counterPartyTraderList.map(_.organizationID))
          } yield BadRequest(views.html.component.master.negotiationRequest(formWithErrors, tradableAssetProperties,tradableAssetList, counterPartyTraderList, counterPartyOrganizationList))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        },
        requestData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val asset= masterAssets.Service.tryGet(requestData.assetID)

          val assetProperties= masterProperties.Service.getPropertyMap(requestData.assetID)

          def getAssetSplit(traderID:String): Future[Split] = masterSplits.Service.tryGet(requestData.assetID,traderID)

          def checkRelationExists(traderID: String): Future[Boolean] = masterTraderRelations.Service.checkRelationExists(fromID = traderID, toID = requestData.counterParty)

          def insert(traderID: String, assetSplit: Split,asset: Asset,assetProperties:Map[String,Option[String]], checkRelationExists: Boolean): Future[String] = {
            if (traderID != assetSplit.ownerID || !checkRelationExists) throw new BaseException(constants.Response.UNAUTHORIZED)
            asset.status match {
              case constants.Status.Asset.REQUESTED_TO_ZONE | constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE | constants.Status.Asset.ISSUED => masterNegotiations.Service.create(buyerTraderID = requestData.counterParty, sellerTraderID = traderID, assetID = asset.id, description = assetProperties.getOrElse(constants.Property.ASSET_DESCRIPTION.dataName,Some("")).getOrElse(""), price = MicroNumber(assetProperties.getOrElse(constants.Property.PRICE.dataName,Some("")).getOrElse("")), quantity = MicroNumber(assetProperties.getOrElse(constants.Property.QUANTITY.dataName,Some("")).getOrElse("")), quantityUnit = assetProperties.getOrElse(constants.Property.QUANTITY_UNIT.dataName,Some("")).getOrElse(""), assetOtherDetails = AssetOtherDetails(ShippingDetails(assetProperties.getOrElse(constants.Property.SHIPPING_PERIOD.dataName,Some("")).getOrElse("").toInt,assetProperties.getOrElse(constants.Property.PORT_OF_LOADING.dataName,Some("")).getOrElse(""),assetProperties.getOrElse(constants.Property.PORT_OF_DISCHARGE.dataName,Some("")).getOrElse(""))))
              case _ => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
            }
          }

          (for {
            traderID <- traderID
            asset<-asset
            assetProperties<-assetProperties
            assetSplit <- getAssetSplit(traderID)
            checkRelationExists <- checkRelationExists(traderID)
            id <- insert(traderID = traderID, assetSplit = assetSplit,asset = asset,assetProperties = assetProperties, checkRelationExists = checkRelationExists)
            result <- withUsernameToken.PartialContent(views.html.component.master.paymentTerms(id = id))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def paymentTermsForm(id: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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

  def paymentTerms(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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

  def documentListForm(id: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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

  def documentList(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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

          def getAssetProperties(assetID: String):Future[Map[String,Option[String]]]= masterProperties.Service.getPropertyMap(assetID)

          def getTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

          def getResult(assetProperties: Map[String,Option[String]], negotiation: Negotiation, counterPartyTrader: Trader): Future[Result] = if (documentListData.documentListCompleted) {
            withUsernameToken.PartialContent(views.html.component.master.reviewNegotiationRequest(assetProperties = assetProperties, negotiation = negotiation, counterPartyTrader = counterPartyTrader))
          } else {
            withUsernameToken.PartialContent(views.html.component.master.documentList(views.companion.master.DocumentList.form.fill(views.companion.master.DocumentList.Data(id = documentListData.id, documentList = documentListData.documentList, documentListCompleted = true, physicalDocumentsHandledVia = documentListData.physicalDocumentsHandledVia)), id = negotiation.id))
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            assetStatus <- getAssetStatus(negotiation.assetID)
            _ <- updateDocumentList(traderID = traderID, assetStatus = assetStatus, negotiation)
            assetProperties <- getAssetProperties(negotiation.assetID)
            counterPartyTrader <- getTrader(negotiation.buyerTraderID)
            result <- getResult(assetProperties = assetProperties, negotiation = negotiation, counterPartyTrader = counterPartyTrader)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def reviewRequestForm(id: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getAssetProperties(traderID: String, negotiation: Negotiation): Future[Map[String,Option[String]]] = if (traderID == negotiation.sellerTraderID) {
        masterProperties.Service.getPropertyMap(negotiation.assetID)
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      def getTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

      (for {
        traderID <- traderID
        negotiation <- negotiation
        assetProperties <- getAssetProperties(traderID, negotiation)
        counterPartyTrader <- getTrader(negotiation.buyerTraderID)
        result <- withUsernameToken.Ok(views.html.component.master.reviewNegotiationRequest(assetProperties = assetProperties, negotiation = negotiation, counterPartyTrader = counterPartyTrader))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def reviewRequest(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.ReviewNegotiationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(formWithErrors.data(constants.FormField.ID.name))

          def getAssetProperties(traderID: String, negotiation: Negotiation): Future[Map[String,Option[String]]] = if (traderID == negotiation.sellerTraderID) {
            masterProperties.Service.getPropertyMap(negotiation.assetID)
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          def getTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

          (for {
            traderID <- traderID
            negotiation <- negotiation
            assetProperties <- getAssetProperties(traderID, negotiation)
            counterPartyTrader <- getTrader(negotiation.buyerTraderID)
          } yield BadRequest(views.html.component.master.reviewNegotiationRequest(formWithErrors, assetProperties = assetProperties, negotiation = negotiation, counterPartyTrader = counterPartyTrader))
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

  def acceptRequestForm(id: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

      def getResult(traderID: String, negotiation: Negotiation, sellerAccountID: String): Future[Result] = if (traderID == negotiation.buyerTraderID) {
        withUsernameToken.Ok(views.html.component.master.acceptNegotiationRequest(negotiation = negotiation, sellerAccountID = sellerAccountID))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
        result <- getResult(traderID = traderID, negotiation = negotiation, sellerAccountID = sellerAccountID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def acceptRequest: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.AcceptNegotiationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)

          val negotiation = masterNegotiations.Service.tryGet(formWithErrors.data(constants.FormField.ID.name))

          def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          def getResult(traderID: String, negotiation: Negotiation, sellerAccountID: String): Future[Result] = if (traderID == negotiation.buyerTraderID) {
            Future(BadRequest(views.html.component.master.acceptNegotiationRequest(formWithErrors, negotiation = negotiation, sellerAccountID = sellerAccountID)))
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
            result <- getResult(traderID = traderID, negotiation = negotiation, sellerAccountID = sellerAccountID)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        },
        acceptRequestData => {
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = acceptRequestData.password)
          val negotiation = masterNegotiations.Service.tryGet(acceptRequestData.id)

          def getTrader(traderID: String) = masterTraders.Service.tryGet(traderID)

          def markNegotiationAccepted(id: String): Future[Int] = masterNegotiations.Service.markAccepted(id = id)

          def createChatIDAndChatRoom(sellerAccountID: String, negotiationID: String): Future[Unit] = {
            val chatID = masterTransactionChats.Service.createGroupChat(loginState.username, sellerAccountID)

            def insertChatID(chatID: String): Future[Int] = masterNegotiations.Service.insertChatID(id = negotiationID, chatID = chatID)

            for {
              chatID <- chatID
              _ <- insertChatID(chatID)
            } yield ()
          }

          def acceptNegotiationAndGetResult(validateUsernamePassword: Boolean, negotiation: Negotiation, sellerTrader:Trader): Future[Result] =
            if (validateUsernamePassword) {
              if (negotiation.status == constants.Status.Negotiation.REQUEST_SENT) {

                def getOrganization(organizationID: String): Future[Organization] = masterOrganizations.Service.tryGet(organizationID)

                for {
                  _ <- markNegotiationAccepted(negotiation.id)
                  buyerTrader <- getTrader(negotiation.buyerTraderID)
                  sellerOrganization <- getOrganization(sellerTrader.organizationID)
                  buyerOrganization <- getOrganization(buyerTrader.organizationID)
                  _ <- createChatIDAndChatRoom(sellerAccountID = sellerTrader.accountID, negotiationID = negotiation.id)
                  _ <- utilitiesNotification.send(sellerTrader.accountID, constants.Notification.NEGOTIATION_ACCEPTED, negotiation.id, negotiation.assetDescription)()
                  _ <- utilitiesNotification.send(buyerTrader.accountID, constants.Notification.NEGOTIATION_ACCEPTED, negotiation.id, negotiation.assetDescription)()
                  _ <- utilitiesNotification.send(buyerOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_NEGOTIATION_STARTED, negotiation.id, negotiation.assetDescription, sellerTrader.accountID, buyerTrader.accountID, sellerOrganization.name)()
                  _ <- utilitiesNotification.send(sellerOrganization.accountID, constants.Notification.ORGANIZATION_NOTIFY_NEGOTIATION_STARTED, negotiation.id, negotiation.assetDescription, sellerTrader.accountID, buyerTrader.accountID, buyerOrganization.name)()
                  result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.NEGOTIATION_REQUEST_ACCEPTED)))
                } yield result
              }
              else {
                throw new BaseException(constants.Response.UNAUTHORIZED)
              }
            } else {
              Future(BadRequest(views.html.component.master.acceptNegotiationRequest(views.companion.master.AcceptNegotiationRequest.form.fill(acceptRequestData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), negotiation = negotiation, sellerAccountID =  sellerTrader.accountID)))
            }

          for {
            validateUsernamePassword <- validateUsernamePassword
            negotiation <- negotiation
            sellerTrader<- getTrader(negotiation.sellerTraderID)
            result <- acceptNegotiationAndGetResult(validateUsernamePassword = validateUsernamePassword, negotiation = negotiation,sellerTrader=sellerTrader)
          } yield result
        }
      )
  }

  def rejectRequestForm(id: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

      def getResult(traderID: String, negotiation: Negotiation, sellerAccountID: String): Future[Result] = if (traderID == negotiation.buyerTraderID) {
        withUsernameToken.Ok(views.html.component.master.rejectNegotiationRequest(negotiation = negotiation, sellerAccountID = sellerAccountID))
      } else {
        throw new BaseException(constants.Response.UNAUTHORIZED)
      }

      (for {
        traderID <- traderID
        negotiation <- negotiation
        sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
        result <- getResult(traderID = traderID, negotiation = negotiation, sellerAccountID = sellerAccountID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def rejectRequest: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.RejectNegotiationRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(formWithErrors.data(constants.FormField.ID.name))

          def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          def getResult(traderID: String, negotiation: Negotiation, sellerAccountID: String): Future[Result] = if (traderID == negotiation.buyerTraderID) {
            Future(BadRequest(views.html.component.master.rejectNegotiationRequest(formWithErrors, negotiation = negotiation, sellerAccountID = sellerAccountID)))
          } else {
            throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          (for {
            traderID <- traderID
            negotiation <- negotiation
            sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
            result <- getResult(traderID = traderID, negotiation = negotiation, sellerAccountID = sellerAccountID)
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
            _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.NEGOTIATION_REQUEST_REJECTED, negotiation.id)()
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_REQUEST_REJECTED, negotiation.id)()
            result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.NEGOTIATION_REQUEST_REJECTED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def updateAssetTermsForm(id: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(traderID: String, negotiation: Negotiation): Future[Result] = if (traderID == negotiation.sellerTraderID) {
        withUsernameToken.Ok(views.html.component.master.updateNegotiationAssetTerms(views.companion.master.UpdateNegotiationAssetTerms.form.fill(views.companion.master.UpdateNegotiationAssetTerms.Data(id = negotiation.id, description = negotiation.assetDescription, pricePerUnit = negotiation.price / negotiation.quantity, quantity = negotiation.quantity, quantityUnit = negotiation.quantityUnit, gas = constants.FormField.GAS.maximumValue, password = ""))))
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

  def updateAssetTerms(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateNegotiationAssetTerms.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.updateNegotiationAssetTerms(formWithErrors)))
        },
        updateAssetTermsData => {
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = updateAssetTermsData.password)
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(updateAssetTermsData.id)

          def getAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

          def updateAssetTermsAndGetResult(validateUsernamePassword: Boolean, traderID: String, buyerAccountID: String, negotiation: Negotiation): Future[Result] = {
            if (traderID == negotiation.sellerTraderID) {
              if (validateUsernamePassword) {
                val updateNegotiation = masterNegotiations.Service.updateAssetTerms(negotiation.id, updateAssetTermsData.description, updateAssetTermsData.pricePerUnit * updateAssetTermsData.quantity,  updateAssetTermsData.quantity, updateAssetTermsData.quantityUnit,  if (negotiation.status == constants.Status.Negotiation.STARTED && updateAssetTermsData.description != negotiation.assetDescription) false else negotiation.buyerAcceptedAssetDescription, if (negotiation.status == constants.Status.Negotiation.STARTED && updateAssetTermsData.pricePerUnit * updateAssetTermsData.quantity != negotiation.price) false else negotiation.buyerAcceptedPrice, if (negotiation.status == constants.Status.Negotiation.STARTED && updateAssetTermsData.quantity != negotiation.quantity) false else negotiation.buyerAcceptedQuantity)

                for {
                  _ <- updateNegotiation
                  _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_ASSET_TERMS_UPDATED, negotiation.id)()
                  _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_ASSET_TERMS_UPDATED, negotiation.id)()
                  _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, constants.TradeActivity.ASSET_DETAILS_UPDATED, loginState.username)
                  result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = updateAssetTermsData.id, successes = Seq(constants.Response.NEGOTIATION_ASSET_TERMS_UPDATED)))
                } yield result
              } else Future(BadRequest(views.html.component.master.updateNegotiationAssetTerms(views.companion.master.UpdateNegotiationAssetTerms.form.fill(views.companion.master.UpdateNegotiationAssetTerms.Data(id = negotiation.id, description = negotiation.assetDescription, pricePerUnit = (negotiation.price / negotiation.quantity).roundedOff(), quantity = negotiation.quantity, quantityUnit = negotiation.quantityUnit, gas = constants.FormField.GAS.maximumValue, password = "")).withGlobalError(constants.Response.INCORRECT_PASSWORD.message))))
            } else throw new BaseException(constants.Response.UNAUTHORIZED)
          }

          (for {
            validateUsernamePassword <- validateUsernamePassword
            traderID <- traderID
            negotiation <- negotiation
            buyerAccountID <- getAccountID(negotiation.buyerTraderID)
            result <- updateAssetTermsAndGetResult(validateUsernamePassword = validateUsernamePassword, traderID = traderID, buyerAccountID = buyerAccountID, negotiation = negotiation)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def updateAssetOtherDetailsForm(id: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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

  def updateAssetOtherDetails(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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
            _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_OTHER_DETAILS_UPDATED, negotiation.id)()
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_OTHER_DETAILS_UPDATED, negotiation.id)()
            _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.NEGOTIATION_OTHER_DETAILS_UPDATED, loginState.username)
            result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = updateAssetOtherDetailsData.id, successes = Seq(constants.Response.NEGOTIATION_ASSET_TERMS_UPDATED)))
          } yield {
            actors.Service.appWebSocketActor ! actors.Message.WebSocket.Negotiation(buyerAccountID, negotiation.id)
            result
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def updatePaymentTermsForm(id: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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

  def updatePaymentTerms(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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
            _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_PAYMENT_TERMS_UPDATED, negotiation.id)()
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_PAYMENT_TERMS_UPDATED, negotiation.id)()
            _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, constants.TradeActivity.PAYMENT_TERMS_UPDATED, loginState.username)
            result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = updatePaymentTermsData.id, successes = Seq(constants.Response.NEGOTIATION_PAYMENT_TERMS_UPDATED)))
          } yield {
            actors.Service.appWebSocketActor ! actors.Message.WebSocket.Negotiation(buyerAccountID, negotiation.id)
            result
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def updateDocumentListForm(id: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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

  def updateDocumentList(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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
            _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED, negotiation.id)()
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED, negotiation.id)()
            _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, constants.TradeActivity.DOCUMENT_LIST_UPDATED, loginState.username)
            result <- getResult(negotiation)
          } yield {
            actors.Service.appWebSocketActor ! actors.Message.WebSocket.Negotiation(buyerAccountID, negotiation.id)
            result
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def acceptOrRejectNegotiationTermsForm(id: String, termType: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def getResult(traderID: String, negotiation: Negotiation): Future[Result] = if (traderID == negotiation.buyerTraderID) {
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

  def acceptOrRejectNegotiationTerms(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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
            actors.Service.appWebSocketActor ! actors.Message.WebSocket.Negotiation(sellerAccountID, negotiation.id)
            result
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def addInvoiceForm(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(negotiationID, constants.File.Negotiation.INVOICE)
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

      def getOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

      def getResult(documentContent: Option[NegotiationDocumentContent], negotiation: Negotiation, traderList: Seq[Trader], organizationList: Seq[Organization]) = {
        documentContent match {
          case Some(content) => {
            val invoice: Invoice = content match {
              case x: Invoice => x
              case _ => throw new BaseException(constants.Response.CONTENT_CONVERSION_ERROR)
            }
            withUsernameToken.Ok(views.html.component.master.addInvoice(views.companion.master.AddInvoice.form.fill(views.companion.master.AddInvoice.Data(negotiationID = negotiationID, invoiceNumber = invoice.invoiceNumber, invoiceAmount = invoice.invoiceAmount, invoiceDate = utilities.Date.sqlDateToUtilDate(invoice.invoiceDate))), negotiationID = negotiationID, negotiation = negotiation, traderList = traderList, organizationList = organizationList))
          }
          case None => withUsernameToken.Ok(views.html.component.master.addInvoice(negotiationID = negotiationID, negotiation = negotiation, traderList = traderList, organizationList = organizationList))
        }
      }

      (for {
        documentContent <- documentContent
        negotiation <- negotiation
        traderList <- getTraderList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
        organizationList <- getOrganizationList(traderList.map(_.organizationID))
        result <- getResult(documentContent, negotiation, traderList, organizationList)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
      }
  }

  def addInvoice(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.AddInvoice.form.bindFromRequest().fold(
        formWithErrors => {
          val negotiation = masterNegotiations.Service.tryGet(formWithErrors.data(constants.FormField.ID.name))

          def getTraderList(traderIDs: Seq[String]) = masterTraders.Service.getTraders(traderIDs)

          def getOrganizationList(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

          for {
            negotiation <- negotiation
            traderList <- getTraderList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
            organizationList <- getOrganizationList(traderList.map(_.organizationID))
          } yield BadRequest(views.html.component.master.addInvoice(formWithErrors, formWithErrors.data(constants.FormField.NEGOTIATION_ID.name), negotiation, traderList, organizationList))
        },
        updateInvoiceContentData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(updateInvoiceContentData.negotiationID)

          def updateAndGetResult(traderID: String, negotiation: Negotiation): Future[Result] = {
            if (traderID == negotiation.sellerTraderID) {
              val updateInvoiceContent = masterTransactionNegotiationFiles.Service.updateDocumentContent(updateInvoiceContentData.negotiationID, constants.File.Negotiation.INVOICE, Invoice(updateInvoiceContentData.invoiceNumber, updateInvoiceContentData.invoiceAmount, utilities.Date.utilDateToSQLDate(updateInvoiceContentData.invoiceDate)))
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
                _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.INVOICE_CONTENT_ADDED, updateInvoiceContentData.negotiationID)()
                _ <- utilitiesNotification.send(loginState.username, constants.Notification.INVOICE_CONTENT_ADDED, updateInvoiceContentData.negotiationID)()
                _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.INVOICE_CONTENT_ADDED, loginState.username)
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

  def addContractForm(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(negotiationID, constants.File.Negotiation.CONTRACT)

      def getResult(documentContent: Option[NegotiationDocumentContent]) = {
        documentContent match {
          case Some(content) => {
            val contract: Contract = content match {
              case x: Contract => x
              case _ => throw new BaseException(constants.Response.CONTENT_CONVERSION_ERROR)
            }
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

  def addContract(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.AddContract.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addContract(formWithErrors, formWithErrors.data(constants.FormField.NEGOTIATION_ID.name))))
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
                _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.CONTRACT_CONTENT_ADDED, updateContractContentData.negotiationID)()
                _ <- utilitiesNotification.send(loginState.username, constants.Notification.CONTRACT_CONTENT_ADDED, updateContractContentData.negotiationID)()
                result <- withUsernameToken.PartialContent(views.html.component.master.tradeDocuments(negotiation, assetFileList, negotiationFileList, negotiationEnvelopeList))
                _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.CONTRACT_CONTENT_ADDED, loginState.username)
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

  def buyerConfirmForm(id: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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

  def buyerConfirm(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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
          val negotiation = masterNegotiations.Service.tryGet(buyerConfirmData.id)
          val negotiationDocumentList = masterTransactionNegotiationFiles.Service.getAllDocuments(buyerConfirmData.id)

          def getAssetDocumentList(assetID: String): Future[Seq[AssetFile]] = masterTransactionAssetFiles.Service.getAllDocuments(assetID)

          def getResult(negotiation: Negotiation, negotiationDocumentList: Seq[NegotiationFile], assetDocumentList: Seq[AssetFile]): Future[Result] = {
            if (negotiationDocumentList.filterNot(_.documentType == constants.File.Negotiation.CONTRACT).map(_.documentType).diff(negotiation.documentList.negotiationDocuments).isEmpty && assetDocumentList.map(_.documentType).diff(negotiation.documentList.assetDocuments).isEmpty) {
              if (assetDocumentList.find(_.documentType == constants.File.Asset.BILL_OF_LADING).getOrElse(throw new BaseException(constants.Response.BILL_OF_LADING_NOT_FOUND)).status == Option(true)) {
                if(negotiation.status == constants.Status.Negotiation.CONTRACT_SIGNED || negotiation.status == constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING) {
                  val buyerTraderID = masterTraders.Service.tryGetID(loginState.username)
                  val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = buyerConfirmData.password)
                  val contract = masterTransactionNegotiationFiles.Service.tryGet(id = buyerConfirmData.id, documentType = constants.File.Negotiation.CONTRACT)

                  def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

                  def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

                  def updateAndGetResult(validateUsernamePassword: Boolean, sellerAccountID: String, buyerTraderID: String, sellerAddress: String, negotiation: Negotiation, contract: NegotiationFile): Future[Result] = {
                    if (validateUsernamePassword) {
                      if (buyerTraderID != negotiation.buyerTraderID) throw new BaseException(constants.Response.UNAUTHORIZED)
                      else if (!(negotiation.buyerAcceptedPrice && negotiation.buyerAcceptedQuantity && negotiation.buyerAcceptedAssetDescription && negotiation.buyerAcceptedAssetOtherDetails && negotiation.buyerAcceptedPaymentTerms && negotiation.buyerAcceptedDocumentList)) throw new BaseException(constants.Response.NEGOTIATION_TERMS_NOT_ACCEPTED)
                      else contract.status match {
                        case Some(status) => if (!status) throw new BaseException(constants.Response.CONTRACT_REJECTED)
                        case None => throw new BaseException(constants.Response.CONTRACT_NOT_VERIFIED)
                      }

                      val updateStatus = masterNegotiations.Service.update(negotiation.copy(status = if (negotiation.status == constants.Status.Negotiation.CONTRACT_SIGNED) constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING else constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED))

                      for {
                        _<-updateStatus
                        _ <- utilitiesNotification.send(loginState.username, constants.Notification.BUYER_BID_CONFIRMED)()
                        _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.BUYER_BID_CONFIRMED)()
                        _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, constants.TradeActivity.BUYER_BID_CONFIRMED)
                        result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = negotiation.id, successes = Seq(constants.Response.BUYER_BID_CONFIRMED)))
                      } yield result
                    } else Future(BadRequest(views.html.component.master.buyerConfirmNegotiation(views.companion.master.ConfirmNegotiation.form.fill(buyerConfirmData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), negotiation = negotiation)))
                  }

                  for {
                    validateUsernamePassword <- validateUsernamePassword
                    buyerTraderID <- buyerTraderID
                    contract <- contract
                    sellerAccountID <- getTraderAccountID(negotiation.sellerTraderID)
                    sellerAddress <- getAddress(sellerAccountID)
                    result <- updateAndGetResult(validateUsernamePassword = validateUsernamePassword, sellerAccountID = sellerAccountID, buyerTraderID = buyerTraderID, sellerAddress = sellerAddress, negotiation = negotiation, contract = contract)
                  } yield result
                }else{
                  throw new BaseException(constants.Response.UNAUTHORIZED)
                }
              } else {
                throw new BaseException(constants.Response.BILL_OF_LADING_VERIFICATION_STATUS_PENDING)
              }
            } else {
              throw new BaseException(constants.Response.ALL_TRADE_DOCUMENTS_NOT_UPLOADED)
            }
          }

          (for {
            negotiation <- negotiation
            negotiationDocumentList <- negotiationDocumentList
            assetDocumentList <- getAssetDocumentList(negotiation.assetID)
            result <- getResult(negotiation, negotiationDocumentList, assetDocumentList)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = buyerConfirmData.id, failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def sellerConfirmForm(id: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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

  def sellerConfirm(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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
          val negotiation = masterNegotiations.Service.tryGet(sellerConfirmData.id)
          val negotiationDocumentList = masterTransactionNegotiationFiles.Service.getAllDocuments(sellerConfirmData.id)

          def getAssetDocumentList(assetID: String): Future[Seq[AssetFile]] = masterTransactionAssetFiles.Service.getAllDocuments(assetID)

          def getResult(negotiation: Negotiation, negotiationDocumentList: Seq[NegotiationFile], assetDocumentList: Seq[AssetFile]): Future[Result] = {
            if (negotiationDocumentList.filterNot(_.documentType == constants.File.Negotiation.CONTRACT).map(_.documentType).diff(negotiation.documentList.negotiationDocuments).isEmpty && assetDocumentList.map(_.documentType).diff(negotiation.documentList.assetDocuments).isEmpty) {
              if (assetDocumentList.find(_.documentType == constants.File.Asset.BILL_OF_LADING).getOrElse(throw new BaseException(constants.Response.BILL_OF_LADING_NOT_FOUND)).status == Option(true)) {
                val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = sellerConfirmData.password)
                val sellerTraderID = masterTraders.Service.tryGetID(loginState.username)
                val contract = masterTransactionNegotiationFiles.Service.tryGet(id = sellerConfirmData.id, documentType = constants.File.Negotiation.CONTRACT)

                def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

                def updateAndGetResult(validateUsernamePassword: Boolean, buyerAccountID: String, sellerTraderID: String, negotiation: Negotiation, contract: NegotiationFile): Future[Result] = {
                  if (validateUsernamePassword) {
                    if (sellerTraderID != negotiation.sellerTraderID) throw new BaseException(constants.Response.UNAUTHORIZED)
                    if (!(negotiation.buyerAcceptedPrice && negotiation.buyerAcceptedQuantity && negotiation.buyerAcceptedAssetDescription && negotiation.buyerAcceptedAssetOtherDetails && negotiation.buyerAcceptedPaymentTerms && negotiation.buyerAcceptedDocumentList)) throw new BaseException(constants.Response.NEGOTIATION_TERMS_NOT_ACCEPTED)
                    contract.status match {
                      case Some(status) => if (!status) throw new BaseException(constants.Response.CONTRACT_REJECTED)
                      case None => throw new BaseException(constants.Response.CONTRACT_NOT_VERIFIED)
                    }

                    val updateStatus = masterNegotiations.Service.update(negotiation.copy(status = if (negotiation.status == constants.Status.Negotiation.CONTRACT_SIGNED) constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING else constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED))

                    for {
                      _ <- updateStatus
                      _ <- utilitiesNotification.send(loginState.username, constants.Notification.SELLER_BID_CONFIRMED)()
                      _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.SELLER_BID_CONFIRMED)()
                      _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, constants.TradeActivity.SELLER_BID_CONFIRMED)
                      result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = negotiation.id, successes = Seq(constants.Response.SELLER_BID_CONFIRMED)))
                    } yield result
                  } else Future(BadRequest(views.html.component.master.sellerConfirmNegotiation(views.companion.master.ConfirmNegotiation.form.fill(sellerConfirmData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), negotiation = negotiation)))
                }

                for {
                  validateUsernamePassword <- validateUsernamePassword
                  sellerTraderID <- sellerTraderID
                  contract <- contract
                  buyerAccountID <- getTraderAccountID(negotiation.buyerTraderID)
                  result <- updateAndGetResult(validateUsernamePassword = validateUsernamePassword, buyerAccountID = buyerAccountID, sellerTraderID = sellerTraderID, negotiation = negotiation, contract = contract)
                } yield result
              } else {
                throw new BaseException(constants.Response.BILL_OF_LADING_VERIFICATION_STATUS_PENDING)
              }
            } else {
              throw new BaseException(constants.Response.ALL_TRADE_DOCUMENTS_NOT_UPLOADED)
            }
          }

          (for {
            negotiation <- negotiation
            negotiationDocumentList <- negotiationDocumentList
            assetDocumentList <- getAssetDocumentList(negotiation.assetID)
            result <- getResult(negotiation, negotiationDocumentList, assetDocumentList)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = sellerConfirmData.id, failures = Seq(baseException.failure)))
          }
        }
      )
  }


  def tradeActivityMessages(negotiationID: String, pageNumber: Int): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
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

  def completedTradeActivityMessages(negotiationID: String, pageNumber: Int): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val buyerTraderID = masterNegotiationHistories.Service.tryGetBuyerTraderID(negotiationID)
      val sellerTraderID = masterNegotiationHistories.Service.tryGetSellerTraderID(negotiationID)

      def getOrganizationID(traderID: String): Future[String] = masterTraders.Service.tryGetOrganizationID(traderID)

      def getZoneID(traderID: String): Future[String] = masterTraders.Service.tryGetZoneID(traderID)

      def getTraderAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

      def getOrganizationAccountID(organizationID: String): Future[String] = masterOrganizations.Service.tryGetAccountID(organizationID)

      def getZoneAccountID(zoneID: String): Future[String] = masterZones.Service.tryGetAccountID(zoneID)

      def getCompletedTradeActivityMessages(accountIDs: String*): Future[Seq[TradeActivityHistory]] = {
        if (!accountIDs.contains(loginState.username)) throw new BaseException(constants.Response.UNAUTHORIZED)
        if (pageNumber < 1) throw new BaseException(constants.Response.INVALID_PAGE_NUMBER)
        masterTransactionTradeActivityHistories.Service.getAllTradeActivities(negotiationID = negotiationID, pageNumber = pageNumber)
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
        completedTradeActivityMessages <- getCompletedTradeActivityMessages(buyerAccountID, buyerOrganizationAccountID, buyerZoneAccountID, sellerAccountID, sellerOrganizationAccountID, sellerZoneAccountID)
      } yield Ok(views.html.component.master.completedTradeActivityMessages(completedTradeActivities = completedTradeActivityMessages))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def confirmAllNegotiationTermsForm(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      withUsernameToken.Ok(views.html.component.master.confirmAllNegotiationTerms(negotiationID = negotiationID))
  }

  def confirmAllNegotiationTerms: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.ConfirmAllNegotiationTerms.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.confirmAllNegotiationTerms(formWithErrors, formWithErrors.data(constants.FormField.ID.name))))
        },
        confirmAllNegotiationTermsData => {
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
                  _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, confirmAllNegotiationTermsData.negotiationID)()
                  _ <- utilitiesNotification.send(loginState.username, constants.Notification.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, confirmAllNegotiationTermsData.negotiationID)()
                  _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS, loginState.username)
                  result <- withUsernameToken.Ok(views.html.tradeRoom(confirmAllNegotiationTermsData.negotiationID))
                } yield {
                  actors.Service.appWebSocketActor ! actors.Message.WebSocket.Negotiation(sellerAccountID, negotiation.id)
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
        }
      )
  }

  def updateContractSignedForm(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
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

  def updateContractSigned: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateContractSigned.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.updateContractSigned(formWithErrors, formWithErrors.data(constants.FormField.NEGOTIATION_ID.name))))
        },
        updateContractSignedData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(updateContractSignedData.negotiationID)

          def markContractSignedAndAccepted(traderID: String, negotiation: Negotiation) = if (negotiation.sellerTraderID == traderID && negotiation.status == constants.Status.Negotiation.BUYER_ACCEPTED_ALL_NEGOTIATION_TERMS) {
            val markContractSigned = masterNegotiations.Service.markContractSigned(updateContractSignedData.negotiationID)
            val markContractAccepted = masterTransactionNegotiationFiles.Service.accept(negotiation.id, constants.File.Negotiation.CONTRACT)
            for {
              _ <- markContractSigned
              _ <- markContractAccepted
            } yield 0
          } else Future(throw new BaseException(constants.Response.UNAUTHORIZED))

          (for {
            traderID <- traderID
            negotiation <- negotiation
            _ <- markContractSignedAndAccepted(traderID, negotiation)
            result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = updateContractSignedData.negotiationID))
            _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.CONTRACT_MARKED_AS_SIGNED, loginState.username)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = updateContractSignedData.negotiationID, failures = Seq(baseException.failure)))
          }
        }
      )
  }
}
