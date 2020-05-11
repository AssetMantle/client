package controllers

import controllers.actions.{LoginState, WithLoginAction, WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.AssetDocumentContent
import models.blockchain.ACL
import models.common.Serializable._
import models.docusign
import models.master.{Asset, Negotiation, Trader, Zone}
import models.masterTransaction.AssetFile
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.mvc._
import play.api.{Configuration, Logger}
import play.api.i18n.{I18nSupport, Messages}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AssetController @Inject()(
                                 blockchainAccounts: blockchain.Accounts,
                                 blockchainAssets: blockchain.Assets,
                                 blockchainACLAccounts: blockchain.ACLAccounts,
                                 blockchainACLHash: blockchain.ACLHashes,
                                 blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets,
                                 blockchainTransactionReleaseAssets: blockchainTransaction.ReleaseAssets,
                                 blockchainTransactionSendAssets: blockchainTransaction.SendAssets,
                                 blockchainTransactionRedeemAssets: blockchainTransaction.RedeemAssets,
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
                                 masterTransactionTradeActivities: masterTransaction.TradeActivities,
                                 masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                 withTraderLoginAction: WithTraderLoginAction,
                                 withUsernameToken: WithUsernameToken,
                                 transaction: utilities.Transaction,
                                 transactionsIssueAsset: transactions.IssueAsset,
                                 transactionsRedeemAsset: transactions.RedeemAsset,
                                 transactionsReleaseAsset: transactions.ReleaseAsset,
                                 transactionsSendAsset: transactions.SendAsset,
                                 utilitiesNotification: utilities.Notification,
                                 utilitiesTransaction: utilities.Transaction,
                                 withZoneLoginAction: WithZoneLoginAction,
                               )
                               (implicit
                                executionContext: ExecutionContext,
                                configuration: Configuration
                               ) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_ASSET

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val zonePassword = configuration.get[String]("zone.password")

  private val zoneGas = configuration.get[Int]("zone.gas")

  private def issueModeratedAsset(assetID: String): Future[Unit] = {
    val asset = masterAssets.Service.tryGet(assetID)

    def getTrader(ownerID: String): Future[Trader] = masterTraders.Service.tryGet(ownerID)

    def getZone(zoneID: String): Future[Zone] = masterZones.Service.tryGet(zoneID)

    def getAccountID(traderID: String): Future[String] = masterTraders.Service.tryGetAccountId(traderID)

    def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

    def getTakerAddress(takerID: Option[String]): Future[String] = {
      takerID match {
        case Some(takerID) =>
          for {
            takerAccountID <- getAccountID(takerID)
            takerAddress <- getAddress(takerAccountID)
          } yield takerAddress
        case None => Future("")
      }
    }

    def sendTransaction(traderAddress: String, zoneAddress: String, takerAddress: String, asset: Asset): Future[String] = utilitiesTransaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
      entity = blockchainTransaction.IssueAsset(from = zoneAddress, to = traderAddress, documentHash = asset.documentHash, assetType = asset.assetType, assetPrice = asset.price, quantityUnit = asset.quantityUnit, assetQuantity = asset.quantity, moderated = true, gas = zoneGas, takerAddress = Option(takerAddress), ticketID = "", mode = transactionMode),
      blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
      request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseReq(from = zoneAddress, gas = zoneGas.toString), to = traderAddress, password = zonePassword, documentHash = asset.documentHash, assetType = asset.assetType, assetPrice = asset.price.toString, quantityUnit = asset.quantityUnit, assetQuantity = asset.quantity.toString, moderated = true, takerAddress = takerAddress, mode = transactionMode),
      action = transactionsIssueAsset.Service.post,
      onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
      onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
      updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
    )

    def markAssetStatusAwaitingBlockchainResponse: Future[Int] = masterAssets.Service.markStatusAwaitingBlockchainResponse(assetID)

    (for {
      asset <- asset
      trader <- getTrader(asset.ownerID)
      zone <- getZone(trader.zoneID)
      traderAddress <- getAddress(trader.accountID)
      zoneAddress <- getAddress(zone.accountID)
      takerAddress <- getTakerAddress(asset.takerID)
      ticketID <- sendTransaction(traderAddress = traderAddress, zoneAddress = zoneAddress, takerAddress = takerAddress, asset = asset)
      _ <- markAssetStatusAwaitingBlockchainResponse
      _ <- utilitiesNotification.send(zone.accountID, constants.Notification.ASSET_ISSUED, ticketID)
      _ <- utilitiesNotification.send(trader.accountID, constants.Notification.ASSET_ISSUED, ticketID)
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
    }
  }

  def issueForm(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.issueAsset())
  }

  def issue(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.issueAsset(formWithErrors)))
        },
        issueAssetData => {
          (loginState.acl match {
            case Some(acl) => {
              if (acl.issueAsset) {
                val traderID = masterTraders.Service.tryGetID(loginState.username)

                def getResult(traderID: String): Future[Result] = {

                  def getAllTradableAssetList(traderID: String): Future[Seq[Asset]] = masterAssets.Service.getAllTradableAssets(traderID)

                  def getCounterPartyList(traderID: String): Future[Seq[String]] = masterTradeRelations.Service.getAllCounterParties(traderID)

                  def getCounterPartyTraderList(traderIDs: Seq[String]): Future[Seq[Trader]] = masterTraders.Service.getTraders(traderIDs)

                  def getCounterPartyOrganizations(organizationIDs: Seq[String]) = masterOrganizations.Service.getOrganizations(organizationIDs)

                  if (issueAssetData.moderated) {
                    val addModeratedAsset = masterAssets.Service.addModerated(ownerID = traderID, assetType = issueAssetData.assetType, description = issueAssetData.description, quantity = issueAssetData.quantity, quantityUnit = issueAssetData.quantityUnit, price = issueAssetData.price, shippingPeriod = issueAssetData.shippingPeriod, portOfLoading = issueAssetData.portOfLoading, portOfDischarge = issueAssetData.portOfDischarge)

                    for {
                      assetID <- addModeratedAsset
                      _ <- issueModeratedAsset(assetID)
                      tradableAssetList <- getAllTradableAssetList(traderID)
                      counterPartyList <- getCounterPartyList(traderID)
                      counterPartyTraderList <- getCounterPartyTraderList(counterPartyList)
                      counterPartyOrganizationList <- getCounterPartyOrganizations(counterPartyTraderList.map(_.organizationID))
                      result <- withUsernameToken.PartialContent(views.html.component.master.negotiationRequest(tradableAssetList = tradableAssetList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList))
                    } yield result
                  } else {
                    val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = issueAssetData.password.getOrElse(""))

                    def issueAssetAndGetResult(validateUsernamePassword: Boolean): Future[Result] = if (validateUsernamePassword) {
                      val addUnmoderatedAsset = masterAssets.Service.addUnmoderated(ownerID = traderID, assetType = issueAssetData.assetType, description = issueAssetData.description, quantity = issueAssetData.quantity, quantityUnit = issueAssetData.quantityUnit, price = issueAssetData.price, shippingPeriod = issueAssetData.shippingPeriod, portOfLoading = issueAssetData.portOfLoading, portOfDischarge = issueAssetData.portOfDischarge)

                      def sendTransaction(documentHash: String): Future[String] = transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                        entity = blockchainTransaction.IssueAsset(from = loginState.address, to = loginState.address, documentHash = documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.price, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.quantity, moderated = false, takerAddress = None, gas = issueAssetData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)), ticketID = "", mode = transactionMode),
                        blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                        request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseReq(from = loginState.address, gas = issueAssetData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)).toString), to = loginState.address, password = issueAssetData.password.getOrElse(throw new BaseException(constants.Response.PASSWORD_NOT_GIVEN)), documentHash = documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.price.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.quantity.toString, moderated = false, takerAddress = "", mode = transactionMode),
                        action = transactionsIssueAsset.Service.post,
                        onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
                        onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
                        updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
                      )

                      for {
                        documentHash <- addUnmoderatedAsset
                        ticketID <- sendTransaction(documentHash)
                        tradableAssetList <- getAllTradableAssetList(traderID)
                        counterPartyList <- getCounterPartyList(traderID)
                        counterPartyTraderList <- getCounterPartyTraderList(counterPartyList)
                        counterPartyOrganizationList <- getCounterPartyOrganizations(counterPartyTraderList.map(_.organizationID))
                        result <- withUsernameToken.PartialContent(views.html.component.master.negotiationRequest(tradableAssetList = tradableAssetList, counterPartyTraderList = counterPartyTraderList, counterPartyOrganizationList = counterPartyOrganizationList))
                      } yield result
                    }
                    else {
                      Future(BadRequest(views.html.component.master.issueAsset(views.companion.master.IssueAsset.form.fill(issueAssetData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message))))
                    }

                    for {
                      validateUsernamePassword <- validateUsernamePassword
                      result <- issueAssetAndGetResult(validateUsernamePassword)
                    } yield result
                  }
                }

                for {
                  traderID <- traderID
                  result <- getResult(traderID)
                } yield result
              } else {
                throw new BaseException(constants.Response.UNAUTHORIZED)
              }
            }
            case None => {
              throw new BaseException(constants.Response.UNAUTHORIZED)
            }
          }).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def addBillOfLadingForm(negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getDocumentContent(assetID: String) = masterTransactionAssetFiles.Service.getDocumentContent(assetID, constants.File.Asset.BILL_OF_LADING)

      def getResult(documentContent: Option[AssetDocumentContent]) = {
        documentContent match {
          case Some(content) => {
            val billOfLading = content.asInstanceOf[BillOfLading]
            withUsernameToken.Ok(views.html.component.master.addBillOfLading(views.companion.master.AddBillOfLading.form.fill(views.companion.master.AddBillOfLading.Data(negotiationID = negotiationID, billOfLadingNumber = billOfLading.id, vesselName = billOfLading.vesselName, portOfLoading = billOfLading.portOfLoading, shipperName = billOfLading.shipperName, shipperAddress = billOfLading.shipperAddress, notifyPartyName = billOfLading.notifyPartyName, notifyPartyAddress = billOfLading.notifyPartyAddress, shipmentDate = utilities.Date.sqlDateToUtilDate(billOfLading.dateOfShipping), deliveryTerm = billOfLading.deliveryTerm, assetDescription = billOfLading.assetDescription, assetQuantity = billOfLading.weightOfConsignment, assetPrice = billOfLading.declaredAssetValue)), negotiationID = negotiationID))
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

  def addBillOfLading(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.AddBillOfLading.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.addBillOfLading(formWithErrors, formWithErrors.data(constants.FormField.TRADE_ID.name))))
        },
        billOfLadingContentData => {
          val traderID = masterTraders.Service.tryGetID(loginState.username)
          val negotiation = masterNegotiations.Service.tryGet(billOfLadingContentData.negotiationID)

          def updateAndGetResult(traderID: String, negotiation: Negotiation) = {
            if (traderID == negotiation.sellerTraderID) {
              val updateBillOfLadingContent = masterTransactionAssetFiles.Service.updateDocumentContent(negotiation.assetID, constants.File.Asset.BILL_OF_LADING, BillOfLading(billOfLadingContentData.billOfLadingNumber, billOfLadingContentData.vesselName, billOfLadingContentData.portOfLoading, billOfLadingContentData.shipperName, billOfLadingContentData.shipperAddress, billOfLadingContentData.notifyPartyName, billOfLadingContentData.notifyPartyAddress, utilities.Date.utilDateToSQLDate(billOfLadingContentData.shipmentDate), billOfLadingContentData.deliveryTerm, billOfLadingContentData.assetDescription, billOfLadingContentData.assetQuantity, billOfLadingContentData.assetPrice))
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
                _ <- utilitiesNotification.send(buyerAccountID, constants.Notification.BILL_OF_LADING_CONTENT_ADDED, billOfLadingContentData.negotiationID)
                _ <- utilitiesNotification.send(loginState.username, constants.Notification.BILL_OF_LADING_CONTENT_ADDED, billOfLadingContentData.negotiationID)
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

  def releaseForm(assetID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.releaseAsset(assetID = assetID))
  }

  def release: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.ReleaseAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.releaseAsset(formWithErrors, formWithErrors.data(constants.FormField.ASSET_ID.name))))
        },
        releaseData => {
          val zoneID = masterZones.Service.tryGetID(loginState.username)
          val asset = masterAssets.Service.tryGet(releaseData.assetID)
          val billOfLading = masterTransactionAssetFiles.Service.tryGet(id = releaseData.assetID, documentType = constants.File.Asset.BILL_OF_LADING)

          def getTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def getACL(address: String): Future[ACL] = {
            val aclHash = blockchainACLAccounts.Service.tryGetACLHash(address)
            for {
              aclHash <- aclHash
              acl <- blockchainACLHash.Service.tryGetACL(aclHash)
            } yield acl
          }

          def getLockedStatus(pegHash: Option[String]): Future[Boolean] = if (pegHash.isDefined) blockchainAssets.Service.tryGetLockedStatus(pegHash.get) else throw new BaseException(constants.Response.ASSET_NOT_FOUND)

          def sendTransaction(seller: Trader, zoneID: String, sellerAddress: String, billOfLading: AssetFile, asset: Asset, lockedStatus: Boolean, acl: ACL): Future[String] = {
            if (seller.zoneID != zoneID || asset.ownerID != seller.id || !acl.releaseAsset) throw new BaseException(constants.Response.UNAUTHORIZED)
            else if (!lockedStatus) throw new BaseException(constants.Response.ASSET_ALREADY_UNLOCKED)
            else if (billOfLading.status.isEmpty) throw new BaseException(constants.Response.BILL_OF_LADING_VERIFICATION_STATUS_PENDING)
            else if (billOfLading.status.contains(false)) throw new BaseException(constants.Response.BILL_OF_LADING_REJECTED)
            else asset.pegHash match {
              case Some(pegHash) =>
                transaction.process[blockchainTransaction.ReleaseAsset, transactionsReleaseAsset.Request](
                  entity = blockchainTransaction.ReleaseAsset(from = loginState.address, to = sellerAddress, pegHash = pegHash, gas = releaseData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionReleaseAssets.Service.create,
                  request = transactionsReleaseAsset.Request(transactionsReleaseAsset.BaseReq(from = loginState.address, gas = releaseData.gas.toString), to = sellerAddress, password = releaseData.password, pegHash = pegHash, mode = transactionMode),
                  action = transactionsReleaseAsset.Service.post,
                  onSuccess = blockchainTransactionReleaseAssets.Utility.onSuccess,
                  onFailure = blockchainTransactionReleaseAssets.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionReleaseAssets.Service.updateTransactionHash
                )
              case None => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
            }
          }

          (for {
            zoneID <- zoneID
            asset <- asset
            seller <- getTrader(asset.ownerID)
            sellerAddress <- getAddress(seller.accountID)
            acl <- getACL(sellerAddress)
            lockedStatus <- getLockedStatus(asset.pegHash)
            billOfLading <- billOfLading
            ticketID <- sendTransaction(seller = seller, zoneID = zoneID, sellerAddress = sellerAddress, billOfLading = billOfLading, asset = asset, lockedStatus = lockedStatus, acl = acl)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.ZONE_RELEASED_ASSET, ticketID)
            _ <- utilitiesNotification.send(seller.accountID, constants.Notification.ZONE_RELEASED_ASSET, ticketID)
            result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.ZONE_RELEASED_ASSET)))
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def sendForm(orderID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.sendAsset(orderID = orderID))
  }

  def send: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.SendAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.sendAsset(formWithErrors, formWithErrors.data(constants.FormField.ORDER_ID.name))))
        },
        sendAssetData => {
          val negotiation = masterNegotiations.Service.tryGet(sendAssetData.orderID)

          def getAsset(assetID: String): Future[Asset] = masterAssets.Service.tryGet(assetID)

          def getLockedStatus(pegHash: Option[String]): Future[Boolean] = if (pegHash.isDefined) blockchainAssets.Service.tryGetLockedStatus(pegHash.get) else throw new BaseException(constants.Response.ASSET_NOT_FOUND)

          def getTrader(traderID: String): Future[Trader] = masterTraders.Service.tryGet(traderID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def sendTransaction(buyerAddress: String, sellerAddress: String, asset: Asset, assetLocked: Boolean, sellerTraderID: String, negotiation: Negotiation): Future[String] = {
            if (asset.ownerID != sellerTraderID || !loginState.acl.getOrElse(throw new BaseException(constants.Response.UNAUTHORIZED)).sendAsset) throw new BaseException(constants.Response.UNAUTHORIZED)
            else if (assetLocked) throw new BaseException(constants.Response.ASSET_LOCKED)
            else if (negotiation.status != constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED) throw new BaseException(constants.Response.CONFIRM_TRANSACTION_PENDING)
            else asset.pegHash match {
              case Some(pegHash) => if (asset.status == constants.Status.Asset.ISSUED) {
                transaction.process[blockchainTransaction.SendAsset, transactionsSendAsset.Request](
                  entity = blockchainTransaction.SendAsset(from = sellerAddress, to = buyerAddress, pegHash = pegHash, gas = sendAssetData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionSendAssets.Service.create,
                  request = transactionsSendAsset.Request(transactionsSendAsset.BaseReq(from = sellerAddress, gas = sendAssetData.gas.toString), to = buyerAddress, password = sendAssetData.password, pegHash = pegHash, mode = transactionMode),
                  action = transactionsSendAsset.Service.post,
                  onSuccess = blockchainTransactionSendAssets.Utility.onSuccess,
                  onFailure = blockchainTransactionSendAssets.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionSendAssets.Service.updateTransactionHash
                )
              } else throw new BaseException(constants.Response.UNAUTHORIZED)
              case None => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
            }

          }

          (for {
            negotiation <- negotiation
            asset <- getAsset(negotiation.assetID)
            assetLocked <- getLockedStatus(asset.pegHash)
            buyer <- getTrader(negotiation.buyerTraderID)
            buyerAddress <- getAddress(buyer.accountID)
            ticketID <- sendTransaction(buyerAddress = buyerAddress, sellerAddress = loginState.address, asset = asset, assetLocked = assetLocked, sellerTraderID = negotiation.sellerTraderID, negotiation = negotiation)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.BLOCKCHAIN_TRANSACTION_SEND_ASSET_TO_ORDER_SENT, ticketID)
            _ <- utilitiesNotification.send(buyer.accountID, constants.Notification.BLOCKCHAIN_TRANSACTION_SEND_ASSET_TO_ORDER_SENT, ticketID)
            result <- withUsernameToken.Ok(views.html.tradeRoom(negotiationID = sendAssetData.orderID, successes = Seq(constants.Response.ASSET_SENT)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.tradeRoom(negotiationID = sendAssetData.orderID, failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def redeemForm(assetID: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.redeemAsset(assetID = assetID))
  }

  def redeem: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.RedeemAsset.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.redeemAsset(formWithErrors, formWithErrors.data(constants.FormField.ASSET_ID.name))))
        },
        redeemAssetData => {
          val asset = masterAssets.Service.tryGet(redeemAssetData.assetID)
          val trader = masterTraders.Service.tryGetByAccountID(loginState.username)

          def getLockedStatus(asset: Asset): Future[Boolean] = if (asset.pegHash.isDefined) blockchainAssets.Service.tryGetLockedStatus(asset.pegHash.get) else throw new BaseException(constants.Response.ASSET_NOT_FOUND)

          def getZoneAccountID(zoneID: String): Future[String] = masterZones.Service.tryGetAccountID(zoneID)

          def getAddress(accountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(accountID)

          def sendTransaction(ownerAddress: String, zoneAddress: String, asset: Asset, assetLocked: Boolean, trader: Trader): Future[String] = {
            if (asset.ownerID != trader.id || !loginState.acl.getOrElse(throw new BaseException(constants.Response.UNAUTHORIZED)).redeemAsset) throw new BaseException(constants.Response.UNAUTHORIZED)
            else if (assetLocked) throw new BaseException(constants.Response.ASSET_LOCKED)
            else asset.pegHash match {
              case Some(pegHash) =>
                transaction.process[blockchainTransaction.RedeemAsset, transactionsRedeemAsset.Request](
                  entity = blockchainTransaction.RedeemAsset(from = ownerAddress, to = zoneAddress, pegHash = pegHash, gas = redeemAssetData.gas, ticketID = "", mode = transactionMode),
                  blockchainTransactionCreate = blockchainTransactionRedeemAssets.Service.create,
                  request = transactionsRedeemAsset.Request(transactionsRedeemAsset.BaseReq(from = ownerAddress, gas = redeemAssetData.gas.toString), to = zoneAddress, password = redeemAssetData.password, pegHash = pegHash, mode = transactionMode),
                  action = transactionsRedeemAsset.Service.post,
                  onSuccess = blockchainTransactionRedeemAssets.Utility.onSuccess,
                  onFailure = blockchainTransactionRedeemAssets.Utility.onFailure,
                  updateTransactionHash = blockchainTransactionRedeemAssets.Service.updateTransactionHash
                )
              case None => throw new BaseException(constants.Response.ASSET_NOT_FOUND)
            }

          }

          (for {
            asset <- asset
            trader <- trader
            assetLocked <- getLockedStatus(asset)
            zoneAccountID <- getZoneAccountID(trader.zoneID)
            zoneAddress <- getAddress(zoneAccountID)
            ticketID <- sendTransaction(ownerAddress = loginState.address, zoneAddress = zoneAddress, asset = asset, assetLocked = assetLocked, trader = trader)
            _ <- utilitiesNotification.send(loginState.username, constants.Notification.BLOCKCHAIN_TRANSACTION_REDEEM_ASSET_SENT, ticketID)
            _ <- utilitiesNotification.send(zoneAccountID, constants.Notification.BLOCKCHAIN_TRANSACTION_REDEEM_ASSET_SENT, ticketID)
            result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.ASSET_REDEEMED)))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def updateAssetDocumentStatusForm(negotiationID: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def getAssetFile(assetID: String) = masterTransactionAssetFiles.Service.tryGet(id = assetID, documentType = documentType)

      (for {
        negotiation <- negotiation
        assetFile <- getAssetFile(negotiation.assetID)
      } yield Ok(views.html.component.master.updateAssetDocumentStatus(negotiationID = negotiationID, assetFile = assetFile))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
      }
  }

  def updateAssetDocumentStatus(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateAssetDocumentStatus.form.bindFromRequest().fold(
        formWithErrors => {
          val negotiation = masterNegotiations.Service.tryGet(formWithErrors.data(constants.FormField.NEGOTIATION_ID.name))

          def getAssetFile(assetID: String) = masterTransactionAssetFiles.Service.tryGet(id = assetID, documentType = formWithErrors(constants.FormField.DOCUMENT_TYPE.name).value.get)

          (for {
            negotiation <- negotiation
            assetFile <- getAssetFile(negotiation.assetID)
          } yield BadRequest(views.html.component.master.updateAssetDocumentStatus(formWithErrors, negotiation.id, assetFile))
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
              _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
            } yield {}
          } else {
            val reject = masterTransactionAssetFiles.Service.reject(id = negotiation.assetID, documentType = updateAssetDocumentStatusData.documentType)
            val sellerAccountID = masterTraders.Service.tryGetAccountId(negotiation.sellerTraderID)
            for {
              _ <- reject
              sellerAccountID <- sellerAccountID
              _ <- utilitiesNotification.send(sellerAccountID, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
            } yield {}
          }

          def getAssetFile(assetID: String): Future[AssetFile] = masterTransactionAssetFiles.Service.tryGet(id = assetID, documentType = updateAssetDocumentStatusData.documentType)

          (for {
            negotiation <- negotiation
            _ <- verifyOrRejectAndSendNotification(negotiation)
            assetFile <- getAssetFile(negotiation.assetID)
            result <- withUsernameToken.PartialContent(views.html.component.master.updateAssetDocumentStatus(negotiationID = negotiation.id, assetFile = assetFile))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.account(failures = Seq(baseException.failure)))
          }
        }
      )
  }

}
