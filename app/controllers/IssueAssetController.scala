package controllers

import java.util.Date

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable
import models.master.Asset
import models.masterTransaction.{AssetFile, IssueAssetRequest}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IssueAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, utilitiesNotification: utilities.Notification, masterTraders: master.Traders, transaction: utilities.Transaction, blockchainAclAccounts: blockchain.ACLAccounts, masterZones: master.Zones, masterAccounts: master.Accounts, masterAssets: master.Assets, masterTransactionAssetFiles: masterTransaction.AssetFiles, withTraderLoginAction: WithTraderLoginAction, withZoneLoginAction: WithZoneLoginAction, blockchainAssets: blockchain.Assets, transactionsIssueAsset: transactions.IssueAsset, blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_ISSUE_ASSET

  def issueAssetRequestForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val asset = masterTransactionIssueAssetRequests.Service.getIssueAssetByID(id)
      (for {
        asset <- asset
      } yield {
        if (asset.accountID == loginState.username) {
          Ok(views.html.component.master.issueAssetRequest(asset = asset, requestID = id))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def issueAssetRequest: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueAssetRequest.form.bindFromRequest().fold(
        formWithErrors => {
          val issueAsset = masterTransactionIssueAssetRequests.Service.getIssueAssetByID(formWithErrors.data(constants.FormField.REQUEST_ID.name))
          for {
            issueAsset <- issueAsset
          } yield BadRequest(views.html.component.master.issueAssetRequest(formWithErrors, asset = issueAsset, requestID = formWithErrors.data(constants.FormField.REQUEST_ID.name)))
        },
        issueAssetRequestData => {
          val transactionAssetFiles = masterTransactionAssetFiles.Service.getAllDocuments(issueAssetRequestData.requestID)

          def updateDocumentHash(transactionAssetFiles: Seq[AssetFile]): Future[Int] = masterTransactionIssueAssetRequests.Service.updateDocumentHash(issueAssetRequestData.requestID, Option(utilities.FileOperations.combinedHash(transactionAssetFiles)))

          def asset: Future[IssueAssetRequest] = masterTransactionIssueAssetRequests.Service.getIssueAssetByID(issueAssetRequestData.requestID)

          def getResult(asset: IssueAssetRequest): Future[Result] = {
            if (asset.physicalDocumentsHandledVia == constants.Form.COMDEX) {
              val updateCompletionStatusAndComment = masterTransactionIssueAssetRequests.Service.updateCompletionStatus(issueAssetRequestData.requestID)
              for {
                _ <- updateCompletionStatusAndComment
                result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ISSUE_ASSET_REQUEST_SENT)))
              } yield result
            } else {
              val ticketID = transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                entity = blockchainTransaction.IssueAsset(from = loginState.address, to = loginState.address, documentHash = asset.documentHash.getOrElse(throw new BaseException(constants.Response.DOCUMENT_NOT_FOUND)), assetType = asset.assetType, assetPrice = asset.assetPrice, quantityUnit = asset.quantityUnit, assetQuantity = asset.assetQuantity, moderated = false, takerAddress = asset.takerAddress, gas = issueAssetRequestData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)), ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseReq(from = loginState.address, gas = issueAssetRequestData.gas.getOrElse(throw new BaseException(constants.Response.GAS_NOT_GIVEN)).toString), to = loginState.address, password = issueAssetRequestData.password.getOrElse(throw new BaseException(constants.Response.PASSWORD_NOT_GIVEN)), documentHash = asset.documentHash.getOrElse(throw new BaseException(constants.Response.DOCUMENT_NOT_FOUND)), assetType = asset.assetType, assetPrice = asset.assetPrice.toString, quantityUnit = asset.quantityUnit, assetQuantity = asset.assetQuantity.toString, moderated = false, takerAddress = asset.takerAddress.getOrElse(""), mode = transactionMode),
                action = transactionsIssueAsset.Service.post,
                onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
                onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
                updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
              )

              def updateTicketID(ticketID: String): Future[Int] = masterTransactionIssueAssetRequests.Service.updateTicketID(issueAssetRequestData.requestID, ticketID)

              for {
                ticketID <- ticketID
                _ <- updateTicketID(ticketID)
                result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
              } yield result
            }
          }

          (for {
            transactionAssetFiles <- transactionAssetFiles
            _ <- updateDocumentHash(transactionAssetFiles)
            asset <- asset
            result <- getResult(asset)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }

      )
  }

  def issueAssetDetailForm(id: Option[String]): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      (id match {
        case Some(requestID) =>
          val assetRequest = masterTransactionIssueAssetRequests.Service.getIssueAssetByID(requestID)

          def getResult(assetRequest: IssueAssetRequest) = {
            if (assetRequest.accountID == loginState.username) {
              withUsernameToken.Ok(views.html.component.master.issueAssetDetail(views.companion.master.IssueAssetDetail.form.fill(views.companion.master.IssueAssetDetail.Data(Option(assetRequest.id), assetRequest.assetType, assetRequest.quantityUnit, assetRequest.assetQuantity, assetRequest.assetPrice, assetRequest.takerAddress, assetRequest.shipmentDetails.commodityName, assetRequest.shipmentDetails.quality, assetRequest.shipmentDetails.deliveryTerm, assetRequest.shipmentDetails.tradeType, assetRequest.shipmentDetails.portOfLoading, assetRequest.shipmentDetails.portOfDischarge, assetRequest.shipmentDetails.shipmentDate, assetRequest.physicalDocumentsHandledVia, assetRequest.paymentTerms))))
            } else {
              Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
            }
          }

          for {
            assetRequest <- assetRequest
            result <- getResult(assetRequest)
          } yield result
        case None => withUsernameToken.Ok(views.html.component.master.issueAssetDetail())
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def issueAssetDetail: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueAssetDetail.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.issueAssetDetail(formWithErrors)))
        },
        issueAssetDetailData => {
          val id = if (issueAssetDetailData.requestID.isEmpty) utilities.IDGenerator.requestID() else issueAssetDetailData.requestID.get
          val insertOrUpdate = masterTransactionIssueAssetRequests.Service.insertOrUpdate(id = id, ticketID = None, pegHash = None, accountID = loginState.username, documentHash = None, assetType = issueAssetDetailData.assetType, quantityUnit = issueAssetDetailData.quantityUnit, assetQuantity = issueAssetDetailData.assetQuantity, assetPrice = issueAssetDetailData.assetPrice, takerAddress = issueAssetDetailData.takerAddress, shipmentDetails = Serializable.ShipmentDetails(issueAssetDetailData.commodityName, issueAssetDetailData.quality, issueAssetDetailData.deliveryTerm, issueAssetDetailData.tradeType, issueAssetDetailData.portOfLoading, issueAssetDetailData.portOfDischarge, utilities.Date.utilDateToSQLDate(issueAssetDetailData.shipmentDate)), physicalDocumentsHandledVia = issueAssetDetailData.physicalDocumentsHandledVia, paymentTerms = issueAssetDetailData.comdexPaymentTerms, completionStatus = false, verificationStatus = null)
          val assetFile = masterTransactionAssetFiles.Service.getOrEmpty(id, constants.File.OBL)
          val optionAssetFile = masterTransactionAssetFiles.Service.getOrNone(id, constants.File.OBL)

          def getResult(assetFile: AssetFile, optionAssetFile: Option[AssetFile]) = {
            val obl = assetFile.documentContent.getOrElse(Serializable.OBL("", "", "", "", "", "", utilities.Date.utilDateToSQLDate(new Date), "", 0, 0)).asInstanceOf[Serializable.OBL]
            withUsernameToken.PartialContent(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(id, obl.billOfLadingID, obl.portOfLoading, obl.shipperName, obl.shipperAddress, obl.notifyPartyName, obl.notifyPartyAddress, obl.dateOfShipping, obl.deliveryTerm, obl.weightOfConsignment, obl.declaredAssetValue)), optionAssetFile))
          }

          (for {
            _ <- insertOrUpdate
            assetFile <- assetFile
            optionAssetFile <- optionAssetFile
            result <- getResult(assetFile, optionAssetFile)
          } yield result).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def issueAssetOBLForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountID = masterTransactionIssueAssetRequests.Service.getAccountID(id)

      def getResult(accountID: String): Future[Result] = {
        if (accountID == loginState.username) {
          val assetFile = masterTransactionAssetFiles.Service.getOrEmpty(id, constants.File.OBL)
          val optionAssetFile = masterTransactionAssetFiles.Service.getOrNone(id, constants.File.OBL)

          def result(assetFile: AssetFile, optionAssetFile: Option[AssetFile]) = {
            val obl = assetFile.documentContent.getOrElse(Serializable.OBL("", "", "", "", "", "", utilities.Date.utilDateToSQLDate(new Date), "", 0, 0)).asInstanceOf[Serializable.OBL]
            withUsernameToken.PartialContent(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(id, obl.billOfLadingID, obl.portOfLoading, obl.shipperName, obl.shipperAddress, obl.notifyPartyName, obl.notifyPartyAddress, obl.dateOfShipping, obl.deliveryTerm, obl.weightOfConsignment, obl.declaredAssetValue)), optionAssetFile))
          }

          for {
            assetFile <- assetFile
            optionAssetFile <- optionAssetFile
            result <- result(assetFile, optionAssetFile)
          } yield result
        } else {
          Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }

      for {
        accountID <- accountID
        result <- getResult(accountID)
      } yield result
  }

  def issueAssetOBL: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueAssetOBL.form.bindFromRequest().fold(
        formWithErrors => {
          val optionAssetFile = masterTransactionAssetFiles.Service.getOrNone(formWithErrors.data(constants.FormField.REQUEST_ID.name), constants.File.OBL)
          for {
            optionAssetFile <- optionAssetFile
          } yield BadRequest(views.html.component.master.issueAssetOBL(formWithErrors, optionAssetFile))
        },
        issueAssetOBLData => {
          val checkFileExists = masterTransactionAssetFiles.Service.checkFileExists(issueAssetOBLData.requestID, constants.File.OBL)

          def getResult(checkFileExists: Boolean): Future[Result] = {
            if (checkFileExists) {
              val insertOrUpdate = masterTransactionAssetFiles.Service.insertOrUpdateContext(masterTransaction.AssetFile(issueAssetOBLData.requestID, constants.File.OBL, "", None, Option(Serializable.OBL(issueAssetOBLData.billOfLadingNumber, issueAssetOBLData.portOfLoading, issueAssetOBLData.shipperName, issueAssetOBLData.shipperAddress, issueAssetOBLData.notifyPartyName, issueAssetOBLData.notifyPartyAddress, utilities.Date.utilDateToSQLDate(issueAssetOBLData.shipmentDate), issueAssetOBLData.deliveryTerm, issueAssetOBLData.assetQuantity, issueAssetOBLData.assetPrice)), None))
              val assetFile = masterTransactionAssetFiles.Service.getOrEmpty(issueAssetOBLData.requestID, constants.File.INVOICE)
              val optionAssetFile = masterTransactionAssetFiles.Service.getOrNone(issueAssetOBLData.requestID, constants.File.INVOICE)

              def result(assetFile: AssetFile, optionAssetFile: Option[AssetFile]) = {
                val invoice = assetFile.documentContent.getOrElse(Serializable.Invoice("", utilities.Date.utilDateToSQLDate(new Date))).asInstanceOf[Serializable.Invoice]
                withUsernameToken.PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(issueAssetOBLData.requestID, invoice.invoiceNumber, invoice.invoiceDate)), optionAssetFile))
              }

              for {
                _ <- insertOrUpdate
                assetFile <- assetFile
                optionAssetFile <- optionAssetFile
                result <- result(assetFile, optionAssetFile)
              } yield result
            } else {
              Future(InternalServerError(views.html.index(failures = Seq(constants.Response.DOCUMENT_NOT_FOUND))))
            }
          }

          (for {
            checkFileExists <- checkFileExists
            result <- getResult(checkFileExists)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def issueAssetInvoiceForm(id: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val accountID = masterTransactionIssueAssetRequests.Service.getAccountID(id)

      def getResult(accountID: String): Future[Result] = {
        if (accountID == loginState.username) {
          val assetFile = masterTransactionAssetFiles.Service.getOrEmpty(id, constants.File.INVOICE)
          val optionAssetFile = masterTransactionAssetFiles.Service.getOrNone(id, constants.File.INVOICE)

          def result(assetFile: AssetFile, optionAssetFile: Option[AssetFile]) = {
            val invoice = assetFile.documentContent.getOrElse(Serializable.Invoice("", utilities.Date.utilDateToSQLDate(new Date))).asInstanceOf[Serializable.Invoice]
            withUsernameToken.PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(id, invoice.invoiceNumber, invoice.invoiceDate)), optionAssetFile))
          }

          for {
            assetFile <- assetFile
            optionAssetFile <- optionAssetFile
            result <- result(assetFile, optionAssetFile)
          } yield result
        } else {
          Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }

      (for {
        accountID <- accountID
        result <- getResult(accountID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def issueAssetInvoice: Action[AnyContent] = withTraderLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        views.companion.master.IssueAssetInvoice.form.bindFromRequest().fold(
          formWithErrors => {
            val optionAssetFiles = masterTransactionAssetFiles.Service.getOrNone(formWithErrors.data(constants.FormField.REQUEST_ID.name), constants.File.INVOICE)
            for {
              optionAssetFiles <- optionAssetFiles
            } yield BadRequest(views.html.component.master.issueAssetInvoice(formWithErrors, optionAssetFiles))
          },
          issueAssetInvoiceData => {
            val checkFileExists = masterTransactionAssetFiles.Service.checkFileExists(issueAssetInvoiceData.requestID, constants.File.INVOICE)

            def getResult(checkFileExists: Boolean): Future[Result] = {
              if (checkFileExists) {
                val insertOrUpdate = masterTransactionAssetFiles.Service.insertOrUpdateContext(masterTransaction.AssetFile(issueAssetInvoiceData.requestID, constants.File.INVOICE, "", None, Option(Serializable.Invoice(issueAssetInvoiceData.invoiceNumber, utilities.Date.utilDateToSQLDate(issueAssetInvoiceData.invoiceDate))), None))
                val documents = masterTransactionAssetFiles.Service.getDocuments(issueAssetInvoiceData.requestID, constants.File.TRADER_ASSET_DOCUMENT_TYPES_UPLOAD_PAGE)
                for {
                  _ <- insertOrUpdate
                  documents <- documents
                  result <- withUsernameToken.PartialContent(views.html.component.master.issueAssetDocument(issueAssetInvoiceData.requestID, documents))
                } yield result
              } else {
                Future(InternalServerError(views.html.index(failures = Seq(constants.Response.DOCUMENT_NOT_FOUND))))
              }
            }

            (for {
              checkFileExists <- checkFileExists
              result <- getResult(checkFileExists)
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          }
        )
  }

  def viewPendingIssueAssetRequests: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.getID(loginState.username)

      def traderIDsUnderZone(zoneID: String): Future[Seq[String]] = masterTraders.Service.getTraderIDsByZoneID(zoneID)

      def pendingIssueAssetRequests(traderIDs: Seq[String]): Future[Seq[Asset]] = masterAssets.Service.getPendingIssueAssetRequests(traderIDs)

      (for {
        zoneID <- zoneID
        traderIDs <- traderIDsUnderZone(zoneID)
        pendingIssueAssetRequests <- pendingIssueAssetRequests(traderIDs)
      } yield Ok(views.html.component.master.viewPendingIssueAssetRequests(pendingIssueAssetRequests))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.trades(failures = Seq(baseException.failure)))
      }
  }

  def viewAssetDocuments(id: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val verificationTraderAssetDouments = masterTransactionAssetFiles.Service.getAllDocuments(id)
      (for {
        verificationTraderAssetDouments <- verificationTraderAssetDouments
      } yield Ok(views.html.component.master.viewVerificationTraderAssetDouments(verificationTraderAssetDouments))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateAssetDocumentStatusForm(requestID: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val userZoneID = masterZones.Service.getID(loginState.username)
      val id = masterTransactionIssueAssetRequests.Service.getAccountID(requestID)

      def traderZoneID(id: String): Future[String] = masterTraders.Service.tryGetZoneIDByAccountID(id)

      def getResult(userZoneID: String, traderZoneID: String): Future[Result] = {
        if (userZoneID == traderZoneID) {
          val file = masterTransactionAssetFiles.Service.get(id = requestID, documentType = documentType)
          for {
            file <- file
          } yield Ok(views.html.component.master.updateAssetDocumentStatus(file = file))
        } else {
          Future(Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED))))
        }
      }

      (for {
        userZoneID <- userZoneID
        id <- id
        traderZoneID <- traderZoneID(id)
        result <- getResult(userZoneID, traderZoneID)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateAssetDocumentStatus(): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.UpdateAssetDocumentStatus.form.bindFromRequest().fold(
        formWithErrors => {
          val file = masterTransactionAssetFiles.Service.get(id = formWithErrors(constants.FormField.REQUEST_ID.name).value.get, documentType = formWithErrors(constants.FormField.DOCUMENT_TYPE.name).value.get)
          (for {
            file <- file
          } yield BadRequest(views.html.component.master.updateAssetDocumentStatus(formWithErrors, file))
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        },
        updateAssetDocumentStatusData => {
          val acceptOrReject = {
            if (updateAssetDocumentStatusData.status) {
              val accept = masterTransactionAssetFiles.Service.accept(id = updateAssetDocumentStatusData.fileID, documentType = updateAssetDocumentStatusData.documentType)
              val id = masterTransactionIssueAssetRequests.Service.getAccountID(updateAssetDocumentStatusData.fileID)
              for {
                _ <- accept
                id <- id
                _ <- utilitiesNotification.send(id, constants.Notification.SUCCESS, Messages(constants.Response.DOCUMENT_APPROVED.message))
              } yield {}
            } else {
              val reject = masterTransactionAssetFiles.Service.reject(id = updateAssetDocumentStatusData.fileID, documentType = updateAssetDocumentStatusData.documentType)
              val id = masterTransactionIssueAssetRequests.Service.getAccountID(updateAssetDocumentStatusData.fileID)
              for {
                _ <- reject
                id <- id
                _ <- utilitiesNotification.send(id, constants.Notification.FAILURE, Messages(constants.Response.DOCUMENT_REJECTED.message))
              } yield {}
            }
          }

          def file: Future[AssetFile] = masterTransactionAssetFiles.Service.get(id = updateAssetDocumentStatusData.fileID, documentType = updateAssetDocumentStatusData.documentType)

          (for {
            _ <- acceptOrReject
            file <- file
            result <- withUsernameToken.PartialContent(views.html.component.master.updateAssetDocumentStatus(file = file))
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def rejectIssueAssetRequestForm(requestID: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(views.html.component.master.rejectIssueAssetRequest(requestID = requestID))
  }

  def rejectIssueAssetRequest: Action[AnyContent] = withZoneLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        views.companion.master.RejectIssueAssetRequest.form.bindFromRequest().fold(
          formWithErrors => {
            Future(BadRequest(views.html.component.master.rejectIssueAssetRequest(formWithErrors, formWithErrors.data(constants.FormField.REQUEST_ID.name))))
          },
          rejectIssueAssetRequestData => {
            val reject = masterTransactionIssueAssetRequests.Service.reject(id = rejectIssueAssetRequestData.requestID, comment = rejectIssueAssetRequestData.comment)
            (for {
              _ <- reject
              result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ISSUE_ASSET_REQUEST_REJECTED)))
            } yield result
              ).recover {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          }
        )
  }

  def issueAssetForm(assetID: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val asset = masterAssets.Service.tryGet(assetID)
      (for {
        asset <- asset
      } yield Ok(views.html.component.master.issueAssetOld(views.companion.master.IssueAssetOld.form.fill(views.companion.master.IssueAssetOld.Data(id = assetID, tarderID = asset.ownerID, documentHash = asset.documentHash, assetType = asset.assetType, assetPrice = asset.price, quantityUnit = asset.quantityUnit, assetQuantity = asset.quantity, takerAddress = None, gas = constants.FormField.GAS.minimumValue, password = ""))))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def issueAsset: Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueAssetOld.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.issueAssetOld(formWithErrors)))
        },
        issueAssetData => {
          val traderAccountID = masterTraders.Service.tryGetAccountId(issueAssetData.tarderID)

          def toAddress(toAccountID: String): Future[String] = masterAccounts.Service.getAddress(toAccountID)

          val verifyRequestedStatus = masterAssets.Service.verifyAssetPendingRequestStatus(issueAssetData.id)

          def getResult(toAddress: String, verifyRequestedStatus: Boolean): Future[Result] = {
            if (verifyRequestedStatus) {
              val ticketID = transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                entity = blockchainTransaction.IssueAsset(from = loginState.address, to = toAddress, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, moderated = true, gas = issueAssetData.gas, takerAddress = issueAssetData.takerAddress, ticketID = "", mode = transactionMode),
                blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseReq(from = loginState.address, gas = issueAssetData.gas.toString), to = toAddress, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, moderated = true, takerAddress = issueAssetData.takerAddress.getOrElse(""), mode = transactionMode),
                action = transactionsIssueAsset.Service.post,
                onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
                onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
                updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
              )

              for {
                ticketID <- ticketID
                result <- withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
              } yield result
            } else {
              Future(PreconditionFailed(views.html.index(failures = Seq(constants.Response.ALL_ASSET_FILES_NOT_VERIFIED))))
            }
          }

          (for {
            traderAccountID <- traderAccountID
            toAddress <- toAddress(traderAccountID)
            verifyRequestedStatus <- verifyRequestedStatus
            result <- getResult(toAddress, verifyRequestedStatus)
          } yield result
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }

  def blockchainIssueAssetForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.blockchain.issueAsset())
  }

  def blockchainIssueAsset: Action[AnyContent] = Action.async { implicit request =>
    views.companion.blockchain.IssueAsset.form.bindFromRequest().fold(
      formWithErrors => {
        Future(BadRequest(views.html.component.blockchain.issueAsset(formWithErrors)))
      },
      issueAssetData => {
        val post = transactionsIssueAsset.Service.post(transactionsIssueAsset.Request(transactionsIssueAsset.BaseReq(from = issueAssetData.from, gas = issueAssetData.gas.toString), to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, moderated = issueAssetData.moderated, takerAddress = issueAssetData.takerAddress, mode = issueAssetData.mode))
        (for {
          _ <- post
        } yield Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
          ).recover {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}