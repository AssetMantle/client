package controllers

import java.util.Date

import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class IssueAssetController @Inject()(messagesControllerComponents: MessagesControllerComponents, transaction: utilities.Transaction, blockchainAclAccounts: blockchain.ACLAccounts, masterZones: master.Zones, masterAccounts: master.Accounts, masterTransactionFiles: masterTransaction.Files, withTraderLoginAction: WithTraderLoginAction, withZoneLoginAction: WithZoneLoginAction, blockchainAssets: blockchain.Assets, transactionsIssueAsset: transactions.IssueAsset, blockchainTransactionIssueAssets: blockchainTransaction.IssueAssets, masterTransactionIssueAssetRequests: masterTransaction.IssueAssetRequests, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module:String= constants.Module.CONTROLLERS_ISSUE_ASSET

  def issueAssetDetailForm(id: Option[String]): Action[AnyContent] = Action { implicit request =>
    try {
      if (id.isDefined) {
        val assetRequest = masterTransactionIssueAssetRequests.Service.getIssueAssetsByID(id.get)
        val shipmentDetails = Json.parse(assetRequest.shipmentDetails).as[masterTransaction.IssueAssetRequestDetails.ShipmentDetails]
        Ok(views.html.component.master.issueAssetDetail(views.companion.master.IssueAssetDetail.form.fill(views.companion.master.IssueAssetDetail.Data(assetRequest.id, assetRequest.documentHash, assetRequest.assetType, assetRequest.quantityUnit, assetRequest.assetQuantity, assetRequest.assetPrice, assetRequest.takerAddress.getOrElse(""), shipmentDetails.commodityName, shipmentDetails.quality, shipmentDetails.deliveryTerm, shipmentDetails.tradeType, shipmentDetails.portOfLoading, shipmentDetails.portOfDischarge, shipmentDetails.shipmentDate, assetRequest.physicalDocumentsHandledVia, assetRequest.paymentTerms))))
      } else {
        Ok(views.html.component.master.issueAssetDetail(views.companion.master.IssueAssetDetail.form))
      }
    } catch {
      case _: BaseException => InternalServerError(views.html.component.master.issueAssetDetail(views.companion.master.IssueAssetDetail.form))
    }
  }

  def issueAssetDetail: Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueAssetDetail.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.issueAssetDetail(formWithErrors))
        },
        issueAssetRequestData => {
          try {
            val id = if (issueAssetRequestData.requestID.isEmpty) utilities.IDGenerator.requestID() else issueAssetRequestData.requestID
            //            if (!issueAssetRequestData.moderated) {
            //                            transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
            //                              entity = blockchainTransaction.IssueAsset(from = loginState.address, to = loginState.address, documentHash = issueAssetRequestData.documentHash, assetType = issueAssetRequestData.assetType, assetPrice = issueAssetRequestData.assetPrice, quantityUnit = issueAssetRequestData.quantityUnit, assetQuantity = issueAssetRequestData.assetQuantity, moderated = issueAssetRequestData.moderated,gas=issueAssetRequestData.gas ,takerAddress = if (issueAssetRequestData.takerAddress == "") null else Option(issueAssetRequestData.takerAddress), status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
            //                              blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
            //                              request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseRequest(from = loginState.address), to = loginState.address, password = issueAssetRequestData.password, documentHash = issueAssetRequestData.documentHash, assetType = issueAssetRequestData.assetType, assetPrice = issueAssetRequestData.assetPrice.toString, quantityUnit = issueAssetRequestData.quantityUnit, assetQuantity = issueAssetRequestData.assetQuantity.toString, moderated = issueAssetRequestData.moderated, gas=issueAssetRequestData.gas.toString,takerAddress = issueAssetRequestData.takerAddress, mode = transactionMode),
            //                              action = transactionsIssueAsset.Service.post,
            //                              onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
            //                              onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
            //                              updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
            //                            )
            //              withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
            //            } else {
            masterTransactionIssueAssetRequests.Service.insertOrUpdate(id = id, ticketID = None, pegHash = None, accountID = loginState.username, documentHash = issueAssetRequestData.documentHash, assetType = issueAssetRequestData.assetType, quantityUnit = issueAssetRequestData.quantityUnit, assetQuantity = issueAssetRequestData.assetQuantity, assetPrice = issueAssetRequestData.assetPrice, takerAddress = if (issueAssetRequestData.takerAddress == "") None else Option(issueAssetRequestData.takerAddress), commodityName = issueAssetRequestData.commodityName, quality = issueAssetRequestData.quality, deliveryTerm = issueAssetRequestData.deliveryTerm, tradeType = issueAssetRequestData.tradeType, portOfLoading = issueAssetRequestData.portOfLoading, portOfDischarge = issueAssetRequestData.portOfDischarge, shipmentDate = issueAssetRequestData.shipmentDate, physicalDocumentsHandledVia = issueAssetRequestData.physicalDocumentsHandledVia, paymentTerms = issueAssetRequestData.comdexPaymentTerms, status = constants.Status.Asset.INCOMPLETE_DETAILS)
//            val obl = Json.parse(autofill(issueAssetRequestData.requestID, constants.File.OBL).getOrElse(masterTransaction.File("", "", "", None, Option(Json.toJson(masterTransaction.FileTypeContext.OBL("", "", "", "", "", "", new Date(0), "", 0, 0)).toString()), None)).context.get).as[masterTransaction.FileTypeContext.OBL]
            val obl = Json.parse(masterTransactionFiles.Service.getOrEmpty(issueAssetRequestData.requestID, constants.File.OBL).context.getOrElse(Json.toJson(masterTransaction.FileTypeContext.OBL("", "", "", "", "", "", new Date(0), "", 0, 0)).toString)).as[masterTransaction.FileTypeContext.OBL]
            PartialContent(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(issueAssetRequestData.requestID, obl.billOfLadingId, obl.portOfLoading, obl.shipperName, obl.shipperAddress, obl.notifyPartyName, obl.notifyPartyAddress, obl.dateOfShipping, obl.deliveryTerm, obl.weightOfConsignment, obl.declaredAssetValue))))

            //              PartialContent(views.html.index(successes = Seq(constants.Response.ISSUE_ASSET_REQUEST_SENT)))
            //           }
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }

      )
  }

    def issueAssetOBLForm(id: String): Action[AnyContent] = Action {
      implicit request =>
        val obl = Json.parse(masterTransactionFiles.Service.getOrEmpty(id, constants.File.OBL).context.getOrElse(Json.toJson(masterTransaction.FileTypeContext.OBL("", "", "", "", "", "",  new Date(0), "", 0, 0)).toString)).as[masterTransaction.FileTypeContext.OBL]
        Ok(views.html.component.master.issueAssetOBL(views.companion.master.IssueAssetOBL.form.fill(views.companion.master.IssueAssetOBL.Data(id, obl.billOfLadingId, obl.portOfLoading, obl.shipperName, obl.shipperAddress, obl.notifyPartyName, obl.notifyPartyAddress, obl.dateOfShipping, obl.deliveryTerm, obl.weightOfConsignment, obl.declaredAssetValue))))
    }

    def issueAssetOBL: Action[AnyContent] = withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
          views.companion.master.IssueAssetOBL.form.bindFromRequest().fold(
            formWithErrors => {
              BadRequest(views.html.component.master.issueAssetOBL(formWithErrors.fill(views.companion.master.IssueAssetOBL.Data(formWithErrors.get.requestID, "", "", "", "", "", "",  new Date(0), "", 0, 0))))
            },
            issueAssetOBLData => {
              try {
                //            PartialContent(views.html.component.master.issueAssetDetails())
                masterTransactionFiles.Service.insertOrUpdateContext(masterTransaction.File(issueAssetOBLData.requestID, constants.File.OBL, "" ,None, Option(Json.toJson(masterTransaction.FileTypeContext.OBL(issueAssetOBLData.billOfLadingNumber, issueAssetOBLData.portOfLoading, issueAssetOBLData.shipperName, issueAssetOBLData.shipperAddress, issueAssetOBLData.notifyPartyName, issueAssetOBLData.notifyPartyAddress, issueAssetOBLData.shipmentDate, issueAssetOBLData.deliveryTerm, issueAssetOBLData.assetQuantity, issueAssetOBLData.assetPrice)).toString),None))
                val invoice = Json.parse(masterTransactionFiles.Service.getOrEmpty(issueAssetOBLData.requestID, constants.File.INVOICE).context.getOrElse(Json.toJson(masterTransaction.FileTypeContext.Invoice("",  new Date(0))).toString)).as[masterTransaction.FileTypeContext.Invoice]
                PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(issueAssetOBLData.requestID, invoice.invoiceNumber, invoice.invoiceDate))))
              }
              catch {
                case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
              }
            }
          )
    }

  def issueAssetInvoiceForm(id: String): Action[AnyContent] = Action {
    implicit request =>
      val invoice = Json.parse(masterTransactionFiles.Service.getOrEmpty(id, constants.File.INVOICE).context.getOrElse(Json.toJson(masterTransaction.FileTypeContext.Invoice("",  new Date(0))).toString)).as[masterTransaction.FileTypeContext.Invoice]
      PartialContent(views.html.component.master.issueAssetInvoice(views.companion.master.IssueAssetInvoice.form.fill(views.companion.master.IssueAssetInvoice.Data(id, invoice.invoiceNumber, invoice.invoiceDate))))
  }

  def issueAssetInvoice: Action[AnyContent] = withTraderLoginAction.authenticated {
    implicit loginState =>
      implicit request =>
        views.companion.master.IssueAssetInvoice.form.bindFromRequest().fold(
          formWithErrors => {
            BadRequest(views.html.component.master.issueAssetInvoice(formWithErrors.fill(views.companion.master.IssueAssetInvoice.Data(formWithErrors.get.requestID, "",new Date(0)))))
          },
          issueAssetInvoiceData => {
            try {
              //            PartialContent(views.html.component.master.issueAssetDetails())
              masterTransactionFiles.Service.insertOrUpdateContext(masterTransaction.File(issueAssetInvoiceData.requestID, constants.File.INVOICE, "" ,None, Option(Json.toJson(masterTransaction.FileTypeContext.Invoice(issueAssetInvoiceData.invoiceNumber,issueAssetInvoiceData.invoiceDate)).toString()), None))
              PartialContent(views.html.component.master.issueAssetDocument(issueAssetInvoiceData.requestID))
            }
            catch {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
            }
          }
        )
  }

    def viewPendingIssueAssetRequests: Action[AnyContent] = withZoneLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
          try {
            withUsernameToken.Ok(views.html.component.master.viewPendingIssueAssetRequests(masterTransactionIssueAssetRequests.Service.getPendingIssueAssetRequests(masterAccounts.Service.getIDsForAddresses(blockchainAclAccounts.Service.getAddressesUnderZone(masterZones.Service.getZoneId(loginState.username))), constants.Status.Asset.REQUESTED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
    }

    def rejectIssueAssetRequestForm(requestID: String): Action[AnyContent] = Action {
      implicit request =>
        Ok(views.html.component.master.rejectIssueAssetRequest(views.companion.master.RejectIssueAssetRequest.form, requestID))
    }

    def rejectIssueAssetRequest: Action[AnyContent] = withZoneLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
          views.companion.master.RejectIssueAssetRequest.form.bindFromRequest().fold(
            formWithErrors => {
              BadRequest(views.html.component.master.rejectIssueAssetRequest(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID)))
            },
            rejectIssueAssetRequestData => {
              try {
                masterTransactionIssueAssetRequests.Service.reject(id = rejectIssueAssetRequestData.requestID, comment = rejectIssueAssetRequestData.comment)
                withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ISSUE_ASSET_REQUEST_REJECTED)))
              }
              catch {
                case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
              }
            }
          )
    }

    def issueAssetForm(requestID: String, accountID: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, takerAddress: Option[String]): Action[AnyContent] = Action {
      implicit request =>
        Ok(views.html.component.master.issueAsset(views.companion.master.IssueAsset.form, requestID, accountID, documentHash, assetType, assetPrice, quantityUnit, assetQuantity, takerAddress))
    }

    def issueAsset: Action[AnyContent] = withZoneLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
          views.companion.master.IssueAsset.form.bindFromRequest().fold(
            formWithErrors => {
              BadRequest(views.html.component.master.issueAsset(formWithErrors, formWithErrors.data(constants.Form.REQUEST_ID), formWithErrors.data(constants.Form.ACCOUNT_ID), formWithErrors.data(constants.Form.DOCUMENT_HASH), formWithErrors.data(constants.Form.ASSET_TYPE), formWithErrors.data(constants.Form.ASSET_PRICE).toInt, formWithErrors.data(constants.Form.QUANTITY_UNIT), formWithErrors.data(constants.Form.ASSET_QUANTITY).toInt, Option(formWithErrors.data(constants.Form.TAKER_ADDRESS))))
            },
            issueAssetData => {
              try {
                val toAddress = masterAccounts.Service.getAddress(issueAssetData.accountID)
                if (masterTransactionIssueAssetRequests.Service.getStatus(issueAssetData.requestID).isEmpty) {
                  val ticketID = transaction.process[blockchainTransaction.IssueAsset, transactionsIssueAsset.Request](
                    entity = blockchainTransaction.IssueAsset(from = loginState.address, to = toAddress, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity, moderated = true, gas = issueAssetData.gas, takerAddress = if (issueAssetData.takerAddress == "") null else Option(issueAssetData.takerAddress), status = null, txHash = null, ticketID = "", mode = transactionMode, code = null),
                    blockchainTransactionCreate = blockchainTransactionIssueAssets.Service.create,
                    request = transactionsIssueAsset.Request(transactionsIssueAsset.BaseRequest(from = loginState.address), to = toAddress, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, moderated = true, gas = issueAssetData.gas.toString, takerAddress = issueAssetData.takerAddress, mode = transactionMode),
                    action = transactionsIssueAsset.Service.post,
                    onSuccess = blockchainTransactionIssueAssets.Utility.onSuccess,
                    onFailure = blockchainTransactionIssueAssets.Utility.onFailure,
                    updateTransactionHash = blockchainTransactionIssueAssets.Service.updateTransactionHash
                  )
                  masterTransactionIssueAssetRequests.Service.accept(issueAssetData.requestID, ticketID)
                  withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
                } else {
                  Unauthorized(views.html.index(failures = Seq(constants.Response.REQUEST_ALREADY_APPROVED_OR_REJECTED)))
                }
              }
              catch {
                case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
                case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
              }
            }
          )
    }

    def blockchainIssueAssetForm: Action[AnyContent] = Action {
      implicit request =>
        Ok(views.html.component.blockchain.issueAsset(views.companion.blockchain.IssueAsset.form))
    }

    def blockchainIssueAsset: Action[AnyContent] = Action {
      implicit request =>
        views.companion.blockchain.IssueAsset.form.bindFromRequest().fold(
          formWithErrors => {
            BadRequest(views.html.component.blockchain.issueAsset(formWithErrors))
          },
          issueAssetData => {
            try {
              transactionsIssueAsset.Service.post(transactionsIssueAsset.Request(transactionsIssueAsset.BaseRequest(from = issueAssetData.from), to = issueAssetData.to, password = issueAssetData.password, documentHash = issueAssetData.documentHash, assetType = issueAssetData.assetType, assetPrice = issueAssetData.assetPrice.toString, quantityUnit = issueAssetData.quantityUnit, assetQuantity = issueAssetData.assetQuantity.toString, gas = issueAssetData.gas.toString, moderated = issueAssetData.moderated, takerAddress = issueAssetData.takerAddress, mode = issueAssetData.mode))
              Ok(views.html.index(successes = Seq(constants.Response.ASSET_ISSUED)))
            }
            catch {
              case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
              case blockChainException: BlockChainException => InternalServerError(views.html.index(failures = Seq(blockChainException.failure)))
            }
          }
        )
    }
  }
