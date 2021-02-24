package controllers

import constants.Response.Success
import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction, WithoutLoginAction, WithoutLoginActionAsync, _}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.AssetDocumentContent
import models.common.Serializable._
import models.master.{Negotiation, Trader}
import models.masterTransaction.AssetFile
import models.{blockchain, blockchainTransaction, master, _}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, _}
import play.api.{Configuration, Logger}
import transactions.blockchain.{IssueAsset, RedeemAsset, ReleaseAsset, SendAsset}
import utilities.{KeyStore, MicroNumber}
import views.companion.{blockchain => blockchainCompanion}
import views.html.component.blockchain.{txForms => blockchainForms}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AssetController @Inject()(
                                 blockchainAccounts: blockchain.Accounts,
                                 blockchainAssets: blockchain.Assets,
                                 masterAccounts: master.Accounts,
                                 masterOrganizations: master.Organizations,
                                 masterTraders: master.Traders,
                                 masterTradeRelations: master.TraderRelations,
                                 masterZones: master.Zones,
                                 masterAssets: master.Assets,
                                 masterNegotiations: master.Negotiations,
                                 messagesControllerComponents: MessagesControllerComponents,
                                 masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                 docusignEnvelopes: docusign.Envelopes,
                                 masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                 masterTransactionTradeActivities: masterTransaction.TradeActivities,
                                 withTraderLoginAction: WithTraderLoginAction,
                                 withLoginActionAsync: WithLoginActionAsync,
                                 transactionsAssetDefine: transactions.blockchain.AssetDefine,
                                 blockchainTransactionAssetDefines: blockchainTransaction.AssetDefines,
                                 transactionsAssetMint: transactions.blockchain.AssetMint,
                                 blockchainTransactionAssetMints: blockchainTransaction.AssetMints,
                                 transactionsAssetMutate: transactions.blockchain.AssetMutate,
                                 blockchainTransactionAssetMutates: blockchainTransaction.AssetMutates,
                                 transactionsAssetBurn: transactions.blockchain.AssetBurn,
                                 blockchainTransactionAssetBurns: blockchainTransaction.AssetBurns,
                                 masterProperties: master.Properties,
                                 masterSplits: master.Splits,
                                 masterClassifications: master.Classifications,
                                 blockchainIdentities: blockchain.Identities,
                                 withUserLoginAction: WithUserLoginAction,
                                 withUsernameToken: WithUsernameToken,
                                 transaction: utilities.Transaction,
                                 transactionsIssueAsset: IssueAsset,
                                 transactionsRedeemAsset: RedeemAsset,
                                 transactionsReleaseAsset: ReleaseAsset,
                                 transactionsSendAsset: SendAsset,
                                 utilitiesNotification: utilities.Notification,
                                 utilitiesTransaction: utilities.Transaction,
                                 withZoneLoginAction: WithZoneLoginAction,
                                 withGenesisLoginAction: WithGenesisLoginAction,
                                 withoutLoginAction: WithoutLoginAction,
                                 withoutLoginActionAsync: WithoutLoginActionAsync,
                                 keyStore: KeyStore
                               )
                               (implicit
                                executionContext: ExecutionContext,
                                configuration: Configuration
                               ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def issueModeratedAsset: Future[Unit] = {
    //TODO create new flow for moderated trade
    Future()
  }

  def issueForm(): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.issueAsset())
  }

  def issue(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.IssueAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.issueAsset(formWithErrors)))
        },
        issueAssetData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val mutables = Seq(constants.Property.SHIPPING_PERIOD.getBaseProperty(issueAssetData.shippingPeriod.toString),
            constants.Property.PORT_OF_DISCHARGE.getBaseProperty(issueAssetData.portOfDischarge),
            constants.Property.PORT_OF_LOADING.getBaseProperty(issueAssetData.portOfLoading),
            constants.Property.QUANTITY.getBaseProperty(issueAssetData.quantity.toMicroString),
            constants.Property.QUANTITY_UNIT.getBaseProperty(issueAssetData.quantityUnit),
            constants.Property.PRICE.getBaseProperty((issueAssetData.pricePerUnit * issueAssetData.quantity).toMicroString))
          val immutables = Seq(constants.Property.ASSET_TYPE.getBaseProperty(issueAssetData.assetType))
          val immutableMetas = Seq(constants.Property.ASSET_DESCRIPTION.getBaseProperty(issueAssetData.description),
            constants.Property.MODERATED.getBaseProperty(if (issueAssetData.moderated) constants.Boolean.TRUE else constants.Boolean.FALSE))
          val mutableMetas = Seq(constants.Property.LOCKED.getBaseProperty(if (issueAssetData.moderated) constants.Boolean.TRUE else constants.Boolean.FALSE))

          def getResult(traderID: String): Future[Result] = {

            val allAssetSplits = masterSplits.Service.getAllAssetsByOwnerIDs(Seq(traderID))

            def getAllTradableAssetProperties(assetIDs: Seq[String]): Future[Map[String, Map[String, Option[String]]]] = masterProperties.Service.getAllAssets(assetIDs)

            def getAllTradableAssetList(assetIDs: Seq[String]) = masterAssets.Service.getAllByIDs(assetIDs)

            def getCounterPartyList(traderID: String): Future[Seq[String]] = masterTradeRelations.Service.getAllCounterParties(traderID)

            def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

            def getCounterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

            if (issueAssetData.moderated) {
              for {
                _ <- issueModeratedAsset
                allAssetSplits <- allAssetSplits
                tradableAssetProperties <- getAllTradableAssetProperties(allAssetSplits.map(_.ownableID))
                tradableAssetList <- getAllTradableAssetList(allAssetSplits.map(_.ownableID))
                counterPartyList <- getCounterPartyList(traderID)
                counterPartyTraderList <- getCounterPartyTraderList(counterPartyList)
                counterPartyOrganizationList <- getCounterPartyOrganizations(counterPartyTraderList.map(_.organizationID))
                result <- withUsernameToken.PartialContent(views.html.component.master.negotiationRequest(tradableAssetProperties = tradableAssetProperties, tradableAssetList = tradableAssetList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList))
              } yield result
            } else {
              val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = issueAssetData.password.getOrElse(""))

              def issueAssetAndGetResult(validateUsernamePassword: Boolean): Future[Result] = if (validateUsernamePassword) {

                val sendTransaction: Future[String] = transaction.process[blockchainTransaction.AssetMint, transactionsAssetMint.Request](
                  entity = blockchainTransaction.AssetMint(from = loginState.address, fromID = traderID, toID = loginState.address, classificationID = constants.Blockchain.Classification.UNMODERATED_ASSET, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables, gas = issueAssetData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)), ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionAssetMints.Service.create,
                  request = transactionsAssetMint.Request(transactionsAssetMint.Message(transactionsAssetMint.BaseReq(from = loginState.address, gas = issueAssetData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)).toString), toID = loginState.address, fromID = traderID, classificationID = constants.Blockchain.Classification.UNMODERATED_ASSET, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                  action = transactionsAssetMint.Service.post,
                  onSuccess = blockchainTransactionAssetMints.Utility.onSuccess,
                  onFailure = blockchainTransactionAssetMints.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionAssetMints.Service.updateTransactionHash
                )

                for {
                  ticketID <- sendTransaction
                  allAssetSplits <- allAssetSplits
                  tradableAssetProperties <- getAllTradableAssetProperties(allAssetSplits.map(_.ownableID))
                  tradableAssetList <- getAllTradableAssetList(allAssetSplits.map(_.ownableID))
                  counterPartyList <- getCounterPartyList(traderID)
                  counterPartyTraderList <- getCounterPartyTraderList(counterPartyList)
                  counterPartyOrganizationList <- getCounterPartyOrganizations(counterPartyTraderList.map(_.organizationID))
                  result <- withUsernameToken.PartialContent(views.html.component.master.negotiationRequest(tradableAssetProperties = tradableAssetProperties, tradableAssetList = tradableAssetList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList))
                } yield result
              } else Future(BadRequest(views.html.component.master.issueAsset(views.companion.master.IssueAsset.form.fill(issueAssetData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message))))

              for {
                validateUsernamePassword <- validateUsernamePassword
                result <- issueAssetAndGetResult(validateUsernamePassword)
              } yield result
            }
          }

          (for {
            traderID <- traderID
            result <- getResult(traderID)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def addBillOfLadingForm(negotiationID: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getDocumentContent(assetID: String) = masterTransactionAssetFiles.Service.getDocumentContent(assetID, constants.File.Asset.BILL_OF_LADING)

      def getResult(documentContent: Option[AssetDocumentContent]) = {
        documentContent match {
          case Some(content) => {
            val billOfLading = content match {
              case x: BillOfLading => x
              case _ => throw new BaseException(constants.Response.CONTENT_CONVERSION_ERROR)
            }
            withUsernameToken.Ok(views.html.component.master.addBillOfLading(views.companion.master.AddBillOfLading.form.fill(views.companion.master.AddBillOfLading.Data(negotiationID = negotiationID, billOfLadingNumber = billOfLading.id, consigneeTo = billOfLading.consigneeTo, vesselName = billOfLading.vesselName, portOfLoading = billOfLading.portOfLoading, portOfDischarge = billOfLading.portOfDischarge, shipperName = billOfLading.shipperName, shipperAddress = billOfLading.shipperAddress, notifyPartyName = billOfLading.notifyPartyName, notifyPartyAddress = billOfLading.notifyPartyAddress, shipmentDate = utilities.Date.sqlDateToUtilDate(billOfLading.dateOfShipping), deliveryTerm = billOfLading.deliveryTerm, assetDescription = billOfLading.assetDescription, assetQuantity = billOfLading.assetQuantity, quantityUnit = billOfLading.quantityUnit, assetPricePerUnit = billOfLading.declaredAssetValue / billOfLading.assetQuantity)), negotiationID = negotiationID))
          }
          case None => withUsernameToken.Ok(views.html.component.master.addBillOfLading(negotiationID = negotiationID))
        }
      }

      (for {
        negotiation <- negotiation
        documentContent <- getDocumentContent(negotiation.assetID)
        result <- getResult(documentContent)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = negotiationID, failures = Seq(baseException.failure)))
      }
  }

  def addBillOfLading(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.AddBillOfLading.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addBillOfLading(formWithErrors, formWithErrors.data(constants.FormField.NEGOTIATION_ID.name))))
        },
        billOfLadingContentData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(billOfLadingContentData.negotiationID)

          def updateAndGetResult(traderID: String, negotiation: Negotiation) = {
            if (traderID == negotiation.sellerTraderID) {
              val updateBillOfLadingContent = masterTransactionAssetFiles.Service.updateDocumentContent(negotiation.assetID, constants.File.Asset.BILL_OF_LADING, BillOfLading(billOfLadingContentData.billOfLadingNumber, billOfLadingContentData.consigneeTo, billOfLadingContentData.vesselName, billOfLadingContentData.portOfLoading, billOfLadingContentData.portOfDischarge, billOfLadingContentData.shipperName, billOfLadingContentData.shipperAddress, billOfLadingContentData.notifyPartyName, billOfLadingContentData.notifyPartyAddress, utilities.Date.utilDateToSQLDate(billOfLadingContentData.shipmentDate), billOfLadingContentData.deliveryTerm, billOfLadingContentData.assetDescription, billOfLadingContentData.assetQuantity, billOfLadingContentData.quantityUnit, (billOfLadingContentData.assetPricePerUnit * billOfLadingContentData.assetQuantity)))
              val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(billOfLadingContentData.negotiationID)
              val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)
              val negotiationEnvelopeList = docusignEnvelopes.Service.getAll(billOfLadingContentData.negotiationID)
              val buyerAccountID = masterTraders.Service.tryGetAccountId(negotiation.buyerTraderID)

              for {
                _ <- updateBillOfLadingContent
                negotiationFileList <- negotiationFileList
                assetFileList <- assetFileList
                negotiationEnvelopeList <- negotiationEnvelopeList
                buyerAccountID <- buyerAccountID
                _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.BILL_OF_LADING_CONTENT_ADDED, billOfLadingContentData.negotiationID)()
                _ <- utilitiesNotification.send(loginState.username, constants.Notification.BILL_OF_LADING_CONTENT_ADDED, billOfLadingContentData.negotiationID)()
                _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.BILL_OF_LADING_CONTENT_ADDED, loginState.username)
                result <- withUsernameToken.PartialContent(views.html.component.master.tradeDocuments(negotiation, assetFileList, negotiationFileList, negotiationEnvelopeList))
              } yield result
            } else {
              Future(Unauthorized(views.html.tradeRoom(negotiationID = negotiation.id, failures = Seq(constants.Response.UNAUTHORIZED))))
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

  def releaseForm(assetID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.releaseAsset(assetID = assetID))
  }

  def release: Action[AnyContent] = withZoneLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.ReleaseAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.releaseAsset(formWithErrors, formWithErrors.data(constants.FormField.ASSET_ID.name))))
        },
        releaseData => {
          val zoneID = masterZones.Service.tryGetID(loginState.username)
          val assetOwnerID = masterSplits.Service.tryGetOwnerID(releaseData.assetID)
          val billOfLading = masterTransactionAssetFiles.Service.tryGet(id = releaseData.assetID, documentType = constants.File.Asset.BILL_OF_LADING)
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = releaseData.password)

          def getTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def getLockedStatus: Future[Boolean] = masterProperties.Service.getAll(constants.Blockchain.Entity.ASSET, releaseData.assetID).map(_.find(x => x.name == constants.Property.LOCKED.dataName).getOrElse("") == constants.Boolean.TRUE)

          def sendTransactionAndGetResult(validateUsernamePassword: Boolean, seller: Trader, zoneID: String, sellerAddress: String, billOfLading: AssetFile, assetOwnerID: String, lockedStatus: Boolean): Future[Result] = {
            if (seller.zoneID != zoneID || assetOwnerID != seller.id) throw new BaseException(constants.Response.UNAUTHORIZED)
            else if (!lockedStatus) throw new BaseException(constants.Response.ASSET_ALREADY_UNLOCKED)
            else if (billOfLading.status.isEmpty) throw new BaseException(constants.Response.BILL_OF_LADING_VERIFICATION_STATUS_PENDING)
            else if (billOfLading.status.contains(false)) throw new BaseException(constants.Response.BILL_OF_LADING_REJECTED)
            else if (validateUsernamePassword) {

              val classificationProperties = masterProperties.Service.getAll(releaseData.assetID, constants.Blockchain.Entity.ASSET)

              def sendTransaction(classificationProperties: Seq[models.master.Property]) = transaction.process[blockchainTransaction.AssetMutate, transactionsAssetMutate.Request](
                entity = blockchainTransaction.AssetMutate(from = loginState.address, fromID = zoneID, assetID = releaseData.assetID, mutableMetaProperties = classificationProperties.filter(x => x.isMutable && x.isMeta).map(x => if (x.name == constants.Property.LOCKED.dataName) BaseProperty(x.dataType, x.name, Some(false.toString)) else BaseProperty(x.dataType, x.name, x.value)), mutableProperties = classificationProperties.filter(x => x.isMutable && !x.isMeta).map(x => if (x.name == constants.Property.LOCKED.dataName) BaseProperty(x.dataType, x.name, Some(false.toString)) else BaseProperty(x.dataType, x.name, x.value)), gas = releaseData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAssetMutates.Service.create,
                request = transactionsAssetMutate.Request(transactionsAssetMutate.Message(transactionsAssetMutate.BaseReq(from = loginState.address, gas = releaseData.gas), fromID = zoneID, assetID = releaseData.assetID, mutableMetaProperties = classificationProperties.filter(x => x.isMutable && x.isMeta).map(x => if (x.name == constants.Property.LOCKED.dataName) BaseProperty(x.dataType, x.name, Some(false.toString)) else BaseProperty(x.dataType, x.name, x.value)), mutableProperties = classificationProperties.filter(x => x.isMutable && !x.isMeta).map(x => if (x.name == constants.Property.LOCKED.dataName) BaseProperty(x.dataType, x.name, Some(false.toString)) else BaseProperty(x.dataType, x.name, x.value)))),
                action = transactionsAssetMutate.Service.post,
                onSuccess = blockchainTransactionAssetMutates.Utility.onSuccess,
                onFailure = blockchainTransactionAssetMutates.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAssetMutates.Service.updateTransactionHash)

              for {
                classificationProperties <- classificationProperties
                ticketID <- sendTransaction(classificationProperties)
                _ <- utilitiesNotification.send(loginState.username, constants.Notification.ZONE_RELEASED_ASSET, ticketID)()
                _ <- utilitiesNotification.send(seller.accountID, constants.Notification.ZONE_RELEASED_ASSET, ticketID)()
                result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.ZONE_RELEASED_ASSET)))
              } yield result
            } else Future(BadRequest(views.html.component.master.releaseAsset(views.companion.master.ReleaseAsset.form.fill(releaseData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), assetID = releaseData.assetID)))

          }

          (for {
            zoneID <- zoneID
            assetOwnerID <- assetOwnerID
            validateUsernamePassword <- validateUsernamePassword
            seller <- getTrader(assetOwnerID)
            sellerAddress <- getAddress(seller.accountID)
            lockedStatus <- getLockedStatus
            billOfLading <- billOfLading
            result <- sendTransactionAndGetResult(validateUsernamePassword = validateUsernamePassword, seller = seller, zoneID = zoneID, sellerAddress = sellerAddress, billOfLading = billOfLading, assetOwnerID = assetOwnerID, lockedStatus = lockedStatus)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def redeemForm(assetID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(views.html.component.master.redeemAsset(assetID = assetID))
  }

  def redeem: Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.RedeemAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.redeemAsset(formWithErrors, formWithErrors.data(constants.FormField.ASSET_ID.name))))
        },
        redeemAssetData => {
          val assetOwnerID = masterSplits.Service.tryGetOwnerID(redeemAssetData.assetID)
          val trader = masterTraders.Service.tryGetByAccountID(loginState.username)
          val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = redeemAssetData.password)

          def getLockedStatus: Future[Boolean] = masterProperties.Service.getAll(constants.Blockchain.Entity.ASSET, redeemAssetData.assetID).map(_.find(x => x.name == constants.Property.LOCKED.dataName).getOrElse() == "true")

          def getZoneAccountID(zoneID: String): Future[String] = masterZones.Service.tryGetAccountID(zoneID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def sendTransactionAndGetResult(validateUsernamePassword: Boolean, zoneAccountID: String, ownerAddress: String, zoneAddress: String, assetOwnerID: String, assetLocked: Boolean, trader: Trader): Future[Result] = {
            if (assetOwnerID != trader.id) throw new BaseException(constants.Response.UNAUTHORIZED)
            else if (assetLocked) throw new BaseException(constants.Response.ASSET_LOCKED)
            else if (validateUsernamePassword) {

              val ticketID = transaction.process[blockchainTransaction.AssetBurn, transactionsAssetBurn.Request](
                entity = blockchainTransaction.AssetBurn(from = loginState.address, fromID = assetOwnerID, assetID = redeemAssetData.assetID, gas = redeemAssetData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAssetBurns.Service.create,
                request = transactionsAssetBurn.Request(transactionsAssetBurn.Message(transactionsAssetBurn.BaseReq(from = loginState.address, gas = redeemAssetData.gas), fromID = assetOwnerID, assetID = redeemAssetData.assetID)),
                action = transactionsAssetBurn.Service.post,
                onSuccess = blockchainTransactionAssetBurns.Utility.onSuccess,
                onFailure = blockchainTransactionAssetBurns.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAssetBurns.Service.updateTransactionHash
              )

              for {
                ticketID <- ticketID
                _ <- utilitiesNotification.send(loginState.username, constants.Notification.BLOCKCHAIN_TRANSACTION_REDEEM_ASSET_SENT, ticketID)()
                _ <- utilitiesNotification.send(zoneAccountID, constants.Notification.BLOCKCHAIN_TRANSACTION_REDEEM_ASSET_SENT, ticketID)()
                result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.ASSET_REDEEMED)))
              } yield result
            } else Future(BadRequest(views.html.component.master.redeemAsset(views.companion.master.RedeemAsset.form.fill(redeemAssetData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message), assetID = redeemAssetData.assetID)))

          }

          (for {
            assetOwnerID <- assetOwnerID
            validateUsernamePassword <- validateUsernamePassword
            trader <- trader
            assetLocked <- getLockedStatus
            zoneAccountID <- getZoneAccountID(trader.zoneID)
            zoneAddress <- getAddress(zoneAccountID)
            result <- sendTransactionAndGetResult(validateUsernamePassword = validateUsernamePassword, zoneAccountID = zoneAccountID, ownerAddress = loginState.address, zoneAddress = zoneAddress, assetOwnerID = assetOwnerID, assetLocked = assetLocked, trader = trader)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def acceptOrRejectAssetDocumentForm(negotiationID: String, documentType: String): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getAssetFile(assetID: String) = masterTransactionAssetFiles.Service.tryGet(id = assetID, documentType = documentType)

      (for {
        negotiation <- negotiation
        assetFile <- getAssetFile(negotiation.assetID)
      } yield Ok(views.html.component.master.acceptOrRejectAssetDocument(negotiationID = negotiationID, assetFile = assetFile))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def acceptOrRejectAssetDocument(): Action[AnyContent] = withTraderLoginAction { implicit loginState =>
    implicit request =>
      views.companion.master.AcceptOrRejectAssetDocument.form.bindFromRequest().fold(
        formWithErrors => {
          val negotiation = masterNegotiations.Service.tryGet(formWithErrors.data(constants.FormField.NEGOTIATION_ID.name))

          def getAssetFile(assetID: String) = masterTransactionAssetFiles.Service.tryGet(id = assetID, documentType = formWithErrors(constants.FormField.DOCUMENT_TYPE.name).value.get)

          (for {
            negotiation <- negotiation
            assetFile <- getAssetFile(negotiation.assetID)
          } yield BadRequest(views.html.component.master.acceptOrRejectAssetDocument(formWithErrors, negotiation.id, assetFile))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        },
        updateAssetDocumentStatusData => {
          val negotiation = masterNegotiations.Service.tryGet(updateAssetDocumentStatusData.negotiationID)

          def verifyOrRejectAndSendNotification(negotiation: Negotiation) = if (updateAssetDocumentStatusData.status) {
            val verify = masterTransactionAssetFiles.Service.accept(id = negotiation.assetID, documentType = updateAssetDocumentStatusData.documentType)
            val sellerAccountID = masterTraders.Service.tryGetAccountId(negotiation.sellerTraderID)
            for {
              _ <- verify
              sellerAccountID <- sellerAccountID
              _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))()
              _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.ASSET_DOCUMENT_ACCEPTED, loginState.username, Messages(updateAssetDocumentStatusData.documentType))
            } yield {}
          } else {
            val reject = masterTransactionAssetFiles.Service.reject(id = negotiation.assetID, documentType = updateAssetDocumentStatusData.documentType)
            val sellerAccountID = masterTraders.Service.tryGetAccountId(negotiation.sellerTraderID)
            for {
              _ <- reject
              sellerAccountID <- sellerAccountID
              _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))()
              _ <- masterTransactionTradeActivities.Service.create(negotiationID = negotiation.id, tradeActivity = constants.TradeActivity.ASSET_DOCUMENT_REJECTED, loginState.username, Messages(updateAssetDocumentStatusData.documentType))
            } yield {}
          }

          def getAssetFile(assetID: String): Future[AssetFile] = masterTransactionAssetFiles.Service.tryGet(id = assetID, documentType = updateAssetDocumentStatusData.documentType)

          (for {
            negotiation <- negotiation
            _ <- verifyOrRejectAndSendNotification(negotiation)
            assetFile <- getAssetFile(negotiation.assetID)
            result <- withUsernameToken.PartialContent(views.html.component.master.acceptOrRejectAssetDocument(negotiationID = negotiation.id, assetFile = assetFile))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }


  private def getNumberOfFields(addField: Boolean, currentNumber: Int) = if (addField) currentNumber + 1 else currentNumber

  def defineForm: Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.assetDefine())
  }

  def define: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.AssetDefine.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.assetDefine(formWithErrors)))
        },
        defineData => {
          if (defineData.addImmutableMetaField || defineData.addImmutableField || defineData.addMutableMetaField || defineData.addMutableField) {
            Future(PartialContent(blockchainForms.assetDefine(
              assetDefineForm = blockchainCompanion.AssetDefine.form.fill(defineData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
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
              val broadcastTx = transaction.process[blockchainTransaction.AssetDefine, transactionsAssetDefine.Request](
                entity = blockchainTransaction.AssetDefine(from = loginState.address, fromID = defineData.fromID, immutableMetaTraits = immutableMetas, immutableTraits = immutables, mutableMetaTraits = mutableMetas, mutableTraits = mutables, gas = defineData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAssetDefines.Service.create,
                request = transactionsAssetDefine.Request(transactionsAssetDefine.Message(transactionsAssetDefine.BaseReq(from = loginState.address, gas = defineData.gas), fromID = defineData.fromID, immutableMetaTraits = immutableMetas, immutableTraits = immutables, mutableMetaTraits = mutableMetas, mutableTraits = mutables)),
                action = transactionsAssetDefine.Service.post,
                onSuccess = blockchainTransactionAssetDefines.Utility.onSuccess,
                onFailure = blockchainTransactionAssetDefines.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAssetDefines.Service.updateTransactionHash)

              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.asset(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.assetDefine(blockchainCompanion.AssetDefine.form.fill(defineData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message))))

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

  def mintForm(classificationID: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val properties = masterProperties.Service.getAll(entityID = classificationID, entityType = constants.Blockchain.Entity.ASSET_DEFINITION)
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
          val mutableMetaProperties = Option(properties.filter(x => x.isMeta && x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          val mutableProperties = Option(properties.filter(x => !x.isMeta && x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          Ok(blockchainForms.assetMint(blockchainCompanion.AssetMint.form.fill(blockchainCompanion.AssetMint.Data(fromID = maintainerIDs.intersect(identityIDs).headOption.getOrElse(""), classificationID = classificationID, toID = "", immutableMetaProperties = immutableMetaProperties, addImmutableMetaField = false, immutableProperties = immutableProperties, addImmutableField = false, mutableMetaProperties = mutableMetaProperties, addMutableMetaField = false, mutableProperties = mutableProperties, addMutableField = false, gas = MicroNumber.zero, password = None)), classificationID = classificationID, numImmutableMetaForms = immutableMetaProperties.fold(0)(_.length), numImmutableForms = immutableProperties.fold(0)(_.length), numMutableMetaForms = mutableMetaProperties.fold(0)(_.length), numMutableForms = mutableProperties.fold(0)(_.length)))
        } else {
          Ok(blockchainForms.assetMint(classificationID = classificationID))
        }
      }
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def mint: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.AssetMint.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.assetMint(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.CLASSIFICATION_ID.name, ""))))
        },
        mintData => {
          if (mintData.addImmutableMetaField || mintData.addImmutableField || mintData.addMutableMetaField || mintData.addMutableField) {
            Future(PartialContent(blockchainForms.assetMint(
              assetMintForm = blockchainCompanion.AssetMint.form.fill(mintData.copy(addImmutableMetaField = false, addImmutableField = false, addMutableMetaField = false, addMutableField = false)),
              classificationID = mintData.classificationID,
              numImmutableMetaForms = getNumberOfFields(mintData.addImmutableMetaField, mintData.immutableMetaProperties.fold(0)(_.flatten.length)),
              numImmutableForms = getNumberOfFields(mintData.addImmutableField, mintData.immutableProperties.fold(0)(_.flatten.length)),
              numMutableMetaForms = getNumberOfFields(mintData.addMutableMetaField, mintData.mutableMetaProperties.fold(0)(_.flatten.length)),
              numMutableForms = getNumberOfFields(mintData.addMutableField, mintData.mutableProperties.fold(0)(_.flatten.length)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = mintData.password.getOrElse(""))

            val immutableMetas = mintData.immutableMetaProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val immutables = mintData.immutableProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutableMetas = mintData.mutableMetaProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutables = mintData.mutableProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val broadcastTx = transaction.process[blockchainTransaction.AssetMint, transactionsAssetMint.Request](
                entity = blockchainTransaction.AssetMint(from = loginState.address, fromID = mintData.fromID, toID = mintData.toID, classificationID = mintData.classificationID, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables, gas = mintData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAssetMints.Service.create,
                request = transactionsAssetMint.Request(transactionsAssetMint.Message(transactionsAssetMint.BaseReq(from = loginState.address, gas = mintData.gas), fromID = mintData.fromID, toID = mintData.toID, classificationID = mintData.classificationID, immutableMetaProperties = immutableMetas, immutableProperties = immutables, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                action = transactionsAssetMint.Service.post,
                onSuccess = blockchainTransactionAssetMints.Utility.onSuccess,
                onFailure = blockchainTransactionAssetMints.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAssetMints.Service.updateTransactionHash)

              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.asset(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.assetMint(blockchainCompanion.AssetMint.form.fill(mintData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), mintData.classificationID)))

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

  def mutateForm(assetID: String, fromID: String): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      val properties = masterProperties.Service.getAll(entityID = assetID, entityType = constants.Blockchain.Entity.ASSET)
      val ownerID = masterSplits.Service.tryGetOwnerID(assetID)

      def getProvisionedAddresses(ownerID: String) = blockchainIdentities.Service.getAllProvisionAddresses(ownerID)

      (for {
        properties <- properties
        ownerID <- ownerID
        provisionedAddresses <- getProvisionedAddresses(ownerID)
      } yield {
        if (properties.nonEmpty && provisionedAddresses.contains(loginState.address)) {
          val mutableMetaProperties = Option(properties.filter(x => x.isMeta && x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          val mutableProperties = Option(properties.filter(x => !x.isMeta && x.isMutable).map(x => Option(views.companion.common.Property.Data(dataType = x.dataType, dataName = x.name, dataValue = x.value))))
          Ok(blockchainForms.assetMutate(blockchainCompanion.AssetMutate.form.fill(blockchainCompanion.AssetMutate.Data(fromID = fromID, assetID = assetID, mutableMetaProperties = mutableMetaProperties, addMutableMetaField = false, mutableProperties = mutableProperties, addMutableField = false, gas = MicroNumber.zero, password = None)), assetID = assetID, fromID = fromID, numMutableMetaForms = mutableMetaProperties.fold(0)(_.length), numMutableForms = mutableProperties.fold(0)(_.length)))
        } else {
          Ok(blockchainForms.assetMutate(assetID = assetID, fromID = fromID))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def mutate: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.AssetMutate.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.assetMutate(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ASSET_ID.name, ""), formWithErrors.data.getOrElse(constants.FormField.FROM_ID.name, ""))))
        },
        mutateData => {
          if (mutateData.addMutableMetaField || mutateData.addMutableField) {
            Future(PartialContent(blockchainForms.assetMutate(
              assetMutateForm = blockchainCompanion.AssetMutate.form.fill(mutateData.copy(addMutableMetaField = false, addMutableField = false)),
              assetID = mutateData.assetID, fromID = mutateData.fromID,
              numMutableMetaForms = getNumberOfFields(mutateData.addMutableMetaField, mutateData.mutableMetaProperties.fold(0)(_.flatten.length)),
              numMutableForms = getNumberOfFields(mutateData.addMutableField, mutateData.mutableProperties.fold(0)(_.flatten.length)))))
          } else {
            val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = mutateData.password.getOrElse(""))

            val mutableMetas = mutateData.mutableMetaProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)
            val mutables = mutateData.mutableProperties.getOrElse(Seq.empty).flatten.map(_.toBaseProperty)

            def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
              val broadcastTx = transaction.process[blockchainTransaction.AssetMutate, transactionsAssetMutate.Request](
                entity = blockchainTransaction.AssetMutate(from = loginState.address, fromID = mutateData.fromID, assetID = mutateData.assetID, mutableMetaProperties = mutableMetas, mutableProperties = mutables, gas = mutateData.gas, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionAssetMutates.Service.create,
                request = transactionsAssetMutate.Request(transactionsAssetMutate.Message(transactionsAssetMutate.BaseReq(from = loginState.address, gas = mutateData.gas), fromID = mutateData.fromID, assetID = mutateData.assetID, mutableMetaProperties = mutableMetas, mutableProperties = mutables)),
                action = transactionsAssetMutate.Service.post,
                onSuccess = blockchainTransactionAssetMutates.Utility.onSuccess,
                onFailure = blockchainTransactionAssetMutates.Utility.onFailure,
                updateTransactionHash = blockchainTransactionAssetMutates.Service.updateTransactionHash)

              for {
                ticketID <- broadcastTx
                result <- withUsernameToken.Ok(views.html.asset(successes = Seq(new Success(ticketID))))
              } yield result
            } else Future(BadRequest(blockchainForms.assetMutate(blockchainCompanion.AssetMutate.form.fill(mutateData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), mutateData.assetID, mutateData.fromID)))

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

  def burnForm(assetID: String, fromID: String): Action[AnyContent] = withoutLoginAction { implicit loginState =>
    implicit request =>
      Ok(blockchainForms.assetBurn(assetID = assetID, fromID = fromID))
  }

  def burn: Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      blockchainCompanion.AssetBurn.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(blockchainForms.assetBurn(formWithErrors, formWithErrors.data.getOrElse(constants.FormField.ASSET_ID.name, ""), formWithErrors.data.getOrElse(constants.FormField.FROM_ID.name, ""))))
        },
        burnData => {
          val verifyPassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = burnData.password)

          def broadcastTx = transaction.process[blockchainTransaction.AssetBurn, transactionsAssetBurn.Request](
            entity = blockchainTransaction.AssetBurn(from = loginState.address, fromID = burnData.fromID, assetID = burnData.assetID, gas = burnData.gas, ticketID = "", mode = transactionMode),
            blockchainTransactionCreate = blockchainTransactionAssetBurns.Service.create,
            request = transactionsAssetBurn.Request(transactionsAssetBurn.Message(transactionsAssetBurn.BaseReq(from = loginState.address, gas = burnData.gas), fromID = burnData.fromID, assetID = burnData.assetID)),
            action = transactionsAssetBurn.Service.post,
            onSuccess = blockchainTransactionAssetBurns.Utility.onSuccess,
            onFailure = blockchainTransactionAssetBurns.Utility.onFailure,
            updateTransactionHash = blockchainTransactionAssetBurns.Service.updateTransactionHash
          )

          def broadcastTxAndGetResult(verifyPassword: Boolean) = if (verifyPassword) {
            for {
              ticketID <- broadcastTx
              result <- withUsernameToken.Ok(views.html.asset(successes = Seq(new Success(ticketID))))
            } yield result
          } else Future(BadRequest(blockchainForms.assetBurn(blockchainCompanion.AssetBurn.form.fill(burnData).withError(constants.FormField.PASSWORD.name, constants.Response.INCORRECT_PASSWORD.message), burnData.assetID, burnData.fromID)))

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
