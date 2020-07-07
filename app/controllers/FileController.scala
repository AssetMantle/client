package controllers

import java.nio.file.Files

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject._
import models.Abstract.{AssetDocumentContent, NegotiationDocumentContent}
import models.master.{AccountFile, AccountKYC, Negotiation, Organization, Trader}
import models.common.Serializable._
import models.docusign
import models.masterTransaction.{AssetFile, NegotiationFile}
import models.{blockchain, master, masterTransaction}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import utilities.MicroNumber
import views.companion.master.FileUpload

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileController @Inject()(
                                messagesControllerComponents: MessagesControllerComponents,
                                fileResourceManager: utilities.FileResourceManager,
                                masterAccountFiles: master.AccountFiles,
                                masterAccountKYCs: master.AccountKYCs,
                                masterZoneKYCs: master.ZoneKYCs,
                                masterOrganizationKYCs: master.OrganizationKYCs,
                                masterNegotiations: master.Negotiations,
                                masterNegotiationHistories: master.NegotiationHistories,
                                masterZones: master.Zones,
                                masterOrganizations: master.Organizations,
                                masterTraders: master.Traders,
                                masterTransactionAssetFiles: masterTransaction.AssetFiles,
                                masterTransactionDocusignEnvelopes: docusign.Envelopes,
                                masterTransactionNegotiationFiles: masterTransaction.NegotiationFiles,
                                withLoginAction: WithLoginAction,
                                withUserLoginAction: WithUserLoginAction,
                                withTraderLoginAction: WithTraderLoginAction,
                                withOrganizationLoginAction: WithOrganizationLoginAction,
                                withZoneLoginAction: WithZoneLoginAction,
                                withGenesisLoginAction: WithGenesisLoginAction,
                                withUsernameToken: WithUsernameToken,
                                withoutLoginAction: WithoutLoginAction,
                                withoutLoginActionAsync: WithoutLoginActionAsync,
                              )(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.FILE_CONTROLLER

  def uploadAccountKYCForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadAccountKYC), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeAccountKYC), documentType))
  }

  def updateAccountKYCForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadAccountKYC), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateAccountKYC), documentType))
  }

  def uploadAccountKYC(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },

      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.profile(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getAccountKYCFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeAccountKYC(name: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val storeFile = fileResourceManager.storeFile[AccountKYC](
        name = name,
        path = fileResourceManager.getAccountKYCFilePath(documentType),
        document = AccountKYC(id = loginState.username, documentType = documentType, status = None, fileName = name, file = None),
        masterCreate = masterAccountKYCs.Service.create
      )

      def accountKYC = masterAccountKYCs.Service.get(loginState.username, documentType)

      def getResult(accountKYC: Option[AccountKYC]) = documentType match {
        case constants.File.AccountKYC.IDENTIFICATION => withUsernameToken.PartialContent(views.html.component.master.userViewUploadOrUpdateIdentification(accountKYC, documentType))
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }

      (for {
        _ <- storeFile
        accountKYC <- accountKYC
        result <- getResult(accountKYC)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def updateAccountKYC(name: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val oldDocument = masterAccountKYCs.Service.tryGet(id = loginState.username, documentType = documentType)

      def updateFile(oldDocument: AccountKYC): Future[Boolean] = fileResourceManager.updateFile[AccountKYC](
        name = name,
        path = fileResourceManager.getAccountKYCFilePath(documentType),
        oldDocument = oldDocument,
        updateOldDocument = masterAccountKYCs.Service.updateOldDocument
      )

      def accountKYC = masterAccountKYCs.Service.get(loginState.username, documentType)

      def getResult(accountKYC: Option[AccountKYC]) = documentType match {
        case constants.File.AccountKYC.IDENTIFICATION => withUsernameToken.PartialContent(views.html.component.master.userViewUploadOrUpdateIdentification(accountKYC, documentType))
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }

      (for {
        oldDocument <- oldDocument
        _ <- updateFile(oldDocument)
        accountKYC <- accountKYC
        result <- getResult(accountKYC)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  def getAccountKYCFile(fileName: String, documentType: String) = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val checkFileNameExists = masterAccountKYCs.Service.checkFileNameExists(id = loginState.username, fileName = fileName)

      (for {
        checkFileNameExists <- checkFileNameExists
      } yield if (checkFileNameExists) Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getAccountKYCFilePath(documentType), fileName = fileName)) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.profile(failures = Seq(baseException.failure)))
      }
  }

  //TODO Shall we verify for genesis
  def genesisAccessedFile(fileName: String, documentType: String): Action[AnyContent] = withGenesisLoginAction.authenticated { implicit loginState =>
    implicit request =>
      Future {
        Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getZoneKYCFilePath(documentType), fileName = fileName))
      }.recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneAccessedOrganizationKYCFile(organizationID: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationZoneID = masterOrganizations.Service.tryGetZoneID(organizationID)
      val userZoneID = masterZones.Service.tryGetID(loginState.username)
      (for {
        organizationZoneID <- organizationZoneID
        userZoneID <- userZoneID
      } yield {
        if (organizationZoneID == userZoneID) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getOrganizationKYCFilePath(documentType), fileName = fileName))
        } else {
          Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadAssetForm(documentType: String, negotiationID: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadAsset), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeAsset), documentType, negotiationID))
  }

  def updateAssetForm(documentType: String, negotiationID: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadAsset), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateAsset), documentType, negotiationID))
  }

  def uploadAsset(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getAssetFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeAsset(name: String, documentType: String, negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def storeFile(assetID: String) = fileResourceManager.storeFile[masterTransaction.AssetFile](
        name = name,
        path = fileResourceManager.getAssetFilePath(documentType),
        document = AssetFile(id = assetID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
        masterCreate = masterTransactionAssetFiles.Service.create
      )

      def getResultByDocumentType(negotiation: Negotiation): Future[Result] = {
        documentType match {
          case constants.File.Asset.BILL_OF_LADING => {
            val negotiation = masterNegotiations.Service.tryGet(negotiationID)

            def getDocumentContent(assetID: String) = masterTransactionAssetFiles.Service.getDocumentContent(assetID, constants.File.Asset.BILL_OF_LADING)

            def getResult(documentContent: Option[AssetDocumentContent]) = {
              documentContent match {
                case Some(content) => {
                  val billOfLading: BillOfLading = content match {
                    case x: BillOfLading => x
                    case _ => throw new BaseException(constants.Response.CONTENT_CONVERSION_ERROR)
                  }
                  withUsernameToken.PartialContent(views.html.component.master.addBillOfLading(views.companion.master.AddBillOfLading.form.fill(views.companion.master.AddBillOfLading.Data(negotiationID = negotiationID, billOfLadingNumber = billOfLading.id, consigneeTo = billOfLading.consigneeTo, vesselName = billOfLading.vesselName, portOfLoading = billOfLading.portOfLoading, portOfDischarge = billOfLading.portOfDischarge, shipperName = billOfLading.shipperName, shipperAddress = billOfLading.shipperAddress, notifyPartyName = billOfLading.notifyPartyName, notifyPartyAddress = billOfLading.notifyPartyAddress, shipmentDate = utilities.Date.sqlDateToUtilDate(billOfLading.dateOfShipping), deliveryTerm = billOfLading.deliveryTerm, assetDescription = billOfLading.assetDescription, assetQuantity = billOfLading.assetQuantity, quantityUnit = billOfLading.quantityUnit, assetPricePerUnit = billOfLading.declaredAssetValue / billOfLading.assetQuantity)), negotiationID = negotiationID))
                }
                case None => withUsernameToken.PartialContent(views.html.component.master.addBillOfLading(negotiationID = negotiationID))
              }
            }

            for {
              negotiation <- negotiation
              documentContent <- getDocumentContent(negotiation.assetID)
              result <- getResult(documentContent)
            } yield result
          }
          case _ => {
            val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
            val negotiationEnvelopeList = masterTransactionDocusignEnvelopes.Service.getAll(negotiationID)
            val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)

            for {
              negotiationFileList <- negotiationFileList
              negotiationEnvelopeList <- negotiationEnvelopeList
              assetFileList <- assetFileList
              result <- withUsernameToken.PartialContent(views.html.component.master.tradeDocuments(negotiation, assetFileList, negotiationFileList, negotiationEnvelopeList))
            } yield result
          }
        }
      }

      (for {
        negotiation <- negotiation
        _ <- storeFile(negotiation.assetID)
        result <- getResultByDocumentType(negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateAsset(name: String, documentType: String, negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiation = masterNegotiations.Service.tryGet(negotiationID)

      def oldDocument(assetID: String) = masterTransactionAssetFiles.Service.tryGet(id = assetID, documentType = documentType)

      def updateFile(oldDocument: AssetFile, assetID: String): Future[Boolean] = fileResourceManager.updateFile[masterTransaction.AssetFile](
        name = name,
        path = fileResourceManager.getAssetFilePath(documentType),
        oldDocument = oldDocument,
        updateOldDocument = masterTransactionAssetFiles.Service.updateOldDocument
      )

      def getResultByDocumentType(negotiation: Negotiation): Future[Result] = {
        documentType match {
          case constants.File.Asset.BILL_OF_LADING => {
            val negotiation = masterNegotiations.Service.tryGet(negotiationID)

            def getDocumentContent(assetID: String) = masterTransactionAssetFiles.Service.getDocumentContent(assetID, constants.File.Asset.BILL_OF_LADING)

            def getResult(documentContent: Option[AssetDocumentContent]) = {
              documentContent match {
                case Some(content) => {
                  val billOfLading: BillOfLading = content match {
                    case x: BillOfLading => x
                    case _ => throw new BaseException(constants.Response.CONTENT_CONVERSION_ERROR)
                  }
                  withUsernameToken.PartialContent(views.html.component.master.addBillOfLading(views.companion.master.AddBillOfLading.form.fill(views.companion.master.AddBillOfLading.Data(negotiationID = negotiationID, billOfLadingNumber = billOfLading.id, consigneeTo = billOfLading.consigneeTo, vesselName = billOfLading.vesselName, portOfLoading = billOfLading.portOfLoading, portOfDischarge = billOfLading.portOfDischarge, shipperName = billOfLading.shipperName, shipperAddress = billOfLading.shipperAddress, notifyPartyName = billOfLading.notifyPartyName, notifyPartyAddress = billOfLading.notifyPartyAddress, shipmentDate = utilities.Date.sqlDateToUtilDate(billOfLading.dateOfShipping), deliveryTerm = billOfLading.deliveryTerm, assetDescription = billOfLading.assetDescription, assetQuantity = billOfLading.assetQuantity, quantityUnit = billOfLading.quantityUnit, assetPricePerUnit = billOfLading.declaredAssetValue / billOfLading.assetQuantity)), negotiationID = negotiationID))
                }
                case None => withUsernameToken.PartialContent(views.html.component.master.addBillOfLading(negotiationID = negotiationID))
              }
            }

            for {
              negotiation <- negotiation
              documentContent <- getDocumentContent(negotiation.assetID)
              result <- getResult(documentContent)
            } yield result
          }
          case _ => {
            val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
            val negotiationEnvelopeList = masterTransactionDocusignEnvelopes.Service.getAll(negotiationID)
            val assetFileList = masterTransactionAssetFiles.Service.getAllDocuments(negotiation.assetID)

            for {
              negotiationFileList <- negotiationFileList
              negotiationEnvelopeList <- negotiationEnvelopeList
              assetFileList <- assetFileList
              result <- withUsernameToken.PartialContent(views.html.component.master.tradeDocuments(negotiation, assetFileList, negotiationFileList, negotiationEnvelopeList))
            } yield result
          }
        }
      }

      (for {
        negotiation <- negotiation
        oldDocument <- oldDocument(negotiation.assetID)
        _ <- updateFile(oldDocument, negotiation.assetID)
        result <- getResultByDocumentType(negotiation)
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadNegotiationForm(documentType: String, negotiationID: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadNegotiation), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeNegotiation), documentType, negotiationID))
  }

  def updateNegotiationForm(documentType: String, negotiationID: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadNegotiation), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateNegotiation), documentType, negotiationID))
  }

  def uploadNegotiation(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(Messages(constants.Response.NO_FILE.message))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getNegotiationFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeNegotiation(name: String, documentType: String, negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val negotiationDocumentList = masterNegotiations.Service.tryGetDocumentList(negotiationID)

      def storeFile = fileResourceManager.storeFile[masterTransaction.NegotiationFile](
        name = name,
        path = fileResourceManager.getNegotiationFilePath(documentType),
        document = NegotiationFile(id = negotiationID, documentType = documentType, fileName = name, file = None, documentContent = None, status = None),
        masterCreate = masterTransactionNegotiationFiles.Service.create
      )

      def getResultByDocumentType: Future[Result] = {
        documentType match {
          case constants.File.Negotiation.CONTRACT => {
            val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(negotiationID, constants.File.Negotiation.CONTRACT)

            def getResult(documentContent: Option[NegotiationDocumentContent]) = {
              documentContent match {
                case Some(content) => {
                  val contract: Contract = content match {
                    case x: Contract => x
                    case _ => throw new BaseException(constants.Response.CONTENT_CONVERSION_ERROR)
                  }
                  withUsernameToken.PartialContent(views.html.component.master.addContract(views.companion.master.AddContract.form.fill(views.companion.master.AddContract.Data(negotiationID = negotiationID, contractNumber = contract.contractNumber)), negotiationID = negotiationID))
                }
                case None => withUsernameToken.PartialContent(views.html.component.master.addContract(negotiationID = negotiationID))
              }
            }

            for {
              documentContent <- documentContent
              result <- getResult(documentContent)
            } yield result
          }
          case constants.File.Negotiation.INVOICE => {
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
                  withUsernameToken.PartialContent(views.html.component.master.addInvoice(views.companion.master.AddInvoice.form.fill(views.companion.master.AddInvoice.Data(negotiationID = negotiationID, invoiceNumber = invoice.invoiceNumber, invoiceAmount = invoice.invoiceAmount, invoiceDate = utilities.Date.sqlDateToUtilDate(invoice.invoiceDate))), negotiationID = negotiationID, negotiation = negotiation, traderList = traderList, organizationList = organizationList))
                }
                case None => withUsernameToken.PartialContent(views.html.component.master.addInvoice(negotiationID = negotiationID, negotiation = negotiation, traderList = traderList, organizationList = organizationList))
              }
            }

            for {
              documentContent <- documentContent
              negotiation <- negotiation
              traderList <- getTraderList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
              organizationList <- getOrganizationList(traderList.map(_.organizationID))
              result <- getResult(documentContent, negotiation, traderList, organizationList)
            } yield result
          }
          case _ => {
            val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
            val negotiation = masterNegotiations.Service.tryGet(negotiationID)
            val negotiationEnvelopeList = masterTransactionDocusignEnvelopes.Service.getAll(negotiationID)

            def getAssetFileList(assetID: String) = masterTransactionAssetFiles.Service.getAllDocuments(assetID)

            for {
              negotiationFileList <- negotiationFileList
              negotiation <- negotiation
              negotiationEnvelopeList <- negotiationEnvelopeList
              assetFileList <- getAssetFileList(negotiation.assetID)
              result <- withUsernameToken.PartialContent(views.html.component.master.tradeDocuments(negotiation, assetFileList, negotiationFileList, negotiationEnvelopeList))
            } yield result
          }
        }
      }

      def storeAndGetResult(fileExistsInNegotiation: Boolean): Future[Result] = {
        if (fileExistsInNegotiation) {
          for {
            _ <- storeFile
            result <- getResultByDocumentType
          } yield result
        } else {
          throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
        }
      }

      (for {
        negotiationDocumentList <- negotiationDocumentList
        result <- storeAndGetResult((negotiationDocumentList.negotiationDocuments ++ Seq(constants.File.Negotiation.CONTRACT)).contains(documentType))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateNegotiation(name: String, documentType: String, negotiationID: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val oldDocument = masterTransactionNegotiationFiles.Service.tryGet(id = negotiationID, documentType = documentType)

      def updateFile(oldDocument: NegotiationFile): Future[Boolean] = fileResourceManager.updateFile[masterTransaction.NegotiationFile](
        name = name,
        path = fileResourceManager.getNegotiationFilePath(documentType),
        oldDocument = oldDocument,
        updateOldDocument = masterTransactionNegotiationFiles.Service.updateOldDocument
      )

      def getResultByDocumentType: Future[Result] = {
        documentType match {
          case constants.File.Negotiation.CONTRACT => {
            val documentContent = masterTransactionNegotiationFiles.Service.getDocumentContent(negotiationID, constants.File.Negotiation.CONTRACT)

            def getResult(documentContent: Option[NegotiationDocumentContent]) = {
              documentContent match {
                case Some(content) => {
                  val contract: Contract = content match {
                    case x: Contract => x
                    case _ => throw new BaseException(constants.Response.CONTENT_CONVERSION_ERROR)
                  }
                  withUsernameToken.PartialContent(views.html.component.master.addContract(views.companion.master.AddContract.form.fill(views.companion.master.AddContract.Data(negotiationID = negotiationID, contractNumber = contract.contractNumber)), negotiationID = negotiationID))
                }
                case None => withUsernameToken.PartialContent(views.html.component.master.addContract(negotiationID = negotiationID))
              }
            }

            for {
              documentContent <- documentContent
              result <- getResult(documentContent)
            } yield result
          }
          case constants.File.Negotiation.INVOICE => {
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
                  withUsernameToken.PartialContent(views.html.component.master.addInvoice(views.companion.master.AddInvoice.form.fill(views.companion.master.AddInvoice.Data(negotiationID = negotiationID, invoiceNumber = invoice.invoiceNumber, invoiceAmount = invoice.invoiceAmount, invoiceDate = utilities.Date.sqlDateToUtilDate(invoice.invoiceDate))), negotiationID = negotiationID, negotiation = negotiation, traderList = traderList, organizationList = organizationList))
                }
                case None => withUsernameToken.PartialContent(views.html.component.master.addInvoice(negotiationID = negotiationID, negotiation = negotiation, traderList = traderList, organizationList = organizationList))
              }
            }

            for {
              documentContent <- documentContent
              negotiation <- negotiation
              traderList <- getTraderList(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
              organizationList <- getOrganizationList(traderList.map(_.organizationID))
              result <- getResult(documentContent, negotiation, traderList, organizationList)
            } yield result
          }
          case _ => {
            val negotiationFileList = masterTransactionNegotiationFiles.Service.getAllDocuments(negotiationID)
            val negotiation = masterNegotiations.Service.tryGet(negotiationID)
            val negotiationEnvelopeList = masterTransactionDocusignEnvelopes.Service.getAll(negotiationID)

            def getAssetFileList(assetID: String) = masterTransactionAssetFiles.Service.getAllDocuments(assetID)

            for {
              negotiationFileList <- negotiationFileList
              negotiation <- negotiation
              negotiationEnvelopeList <- negotiationEnvelopeList
              assetFileList <- getAssetFileList(negotiation.assetID)
              result <- withUsernameToken.PartialContent(views.html.component.master.tradeDocuments(negotiation, assetFileList, negotiationFileList, negotiationEnvelopeList))
            } yield result
          }
        }
      }

      (for {
        oldDocument <- oldDocument
        _ <- updateFile(oldDocument)
        result <- getResultByDocumentType
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  //TODO Shall we check if exists?
  def userAccessedZoneKYCFile(documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterZones.Service.tryGetID(loginState.username)

      def fileName(id: String): Future[String] = masterZoneKYCs.Service.getFileName(id = id, documentType = documentType)

      (for {
        id <- id
        fileName <- fileName(id)
      } yield Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getZoneKYCFilePath(documentType), fileName = fileName))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  //TODO Shall we check if exists?
  def userAccessedOrganizationKYCFile(documentType: String): Action[AnyContent] = withUserLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val id = masterOrganizations.Service.tryGetID(loginState.username)

      def fileName(id: String): Future[String] = masterOrganizationKYCs.Service.tryGetFileName(id = id, documentType = documentType)

      (for {
        id <- id
        fileName <- fileName(id)
      } yield Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getOrganizationKYCFilePath(documentType), fileName = fileName))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }


  def zoneAccessedNegotiationFile(id: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)
      val negotiation = masterNegotiations.Service.tryGet(id)

      def fileName: Future[String] = masterTransactionNegotiationFiles.Service.tryGetFileName(id = id, documentType = documentType)

      def getTraderZOneID(id: String): Future[String] = masterTraders.Service.tryGetZoneID(id)

      (for {
        zoneID <- zoneID
        negotiation <- negotiation
        fileName <- fileName
        sellerTraderZoneID <- getTraderZOneID(negotiation.sellerTraderID)
        buyerTraderZoneID <- getTraderZOneID(negotiation.buyerTraderID)
      } yield {
        if (sellerTraderZoneID == zoneID || buyerTraderZoneID == zoneID) {
          Ok.sendFile(utilities.FileOperations.fetchFile(path = fileResourceManager.getNegotiationFilePath(documentType), fileName = fileName))
        } else Unauthorized(views.html.index(failures = Seq(constants.Response.UNAUTHORIZED)))
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def uploadAccountFileForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.uploadFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadAccountFile), utilities.String.getJsRouteFunction(routes.javascript.FileController.storeAccountFile), documentType))
  }

  def updateAccountFileForm(documentType: String): Action[AnyContent] = withoutLoginAction { implicit request =>
    Ok(views.html.component.master.updateFile(utilities.String.getJsRouteFunction(routes.javascript.FileController.uploadAccountFile), utilities.String.getJsRouteFunction(routes.javascript.FileController.updateAccountFile), documentType))
  }

  def uploadAccountFile(documentType: String) = Action(parse.multipartFormData) { implicit request =>
    FileUpload.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest
      },
      fileUploadInfo => {
        try {
          request.body.file(constants.File.KEY_FILE) match {
            case None => BadRequest(views.html.index(failures = Seq(constants.Response.NO_FILE)))
            case Some(file) => utilities.FileOperations.savePartialFile(Files.readAllBytes(file.ref.path), fileUploadInfo, fileResourceManager.getAccountFilePath(documentType))
              Ok
          }
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }

  def storeAccountFile(name: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val storeFile = fileResourceManager.storeFile[AccountFile](
        name = name,
        path = fileResourceManager.getAccountFilePath(documentType),
        document = AccountFile(id = loginState.username, documentType = documentType, fileName = name, file = None),
        masterCreate = masterAccountFiles.Service.create
      )
      (for {
        _ <- storeFile
        result <- withUsernameToken.Ok(Messages(constants.Response.FILE_UPLOAD_SUCCESSFUL.message))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def updateAccountFile(name: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val oldDocument = masterAccountFiles.Service.tryGet(id = loginState.username, documentType = documentType)

      def updateFile(oldDocument: AccountFile): Future[Boolean] = fileResourceManager.updateFile[AccountFile](
        name = name,
        path = fileResourceManager.getAccountFilePath(documentType),
        oldDocument = oldDocument,
        updateOldDocument = masterAccountFiles.Service.updateOldDocument
      )

      (for {
        oldDocument <- oldDocument
        _ <- updateFile(oldDocument)
        result <- withUsernameToken.Ok(Messages(constants.Response.FILE_UPDATE_SUCCESSFUL.message))
      } yield result
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def file(fileName: String, documentType: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val path: Future[String] = loginState.userType match {
        case constants.User.ZONE =>
          val zoneID = masterZones.Service.tryGetID(loginState.username)

          def checkFileNameExistsZoneKYCs(zoneID: String): Future[Boolean] = masterZoneKYCs.Service.checkFileNameExists(id = zoneID, fileName = fileName)

          for {
            zoneID <- zoneID
            checkFileNameExistsZoneKYCs <- checkFileNameExistsZoneKYCs(zoneID)
          } yield if (checkFileNameExistsZoneKYCs) fileResourceManager.getZoneKYCFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        case constants.User.ORGANIZATION =>
          val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

          def checkFileNameExistsOrganizationKYCs(organizationID: String): Future[Boolean] = masterOrganizationKYCs.Service.checkFileNameExists(id = organizationID, fileName = fileName)

          for {
            organizationID <- organizationID
            checkFileNameExistsOrganizationKYCs <- checkFileNameExistsOrganizationKYCs(organizationID)
          } yield if (checkFileNameExistsOrganizationKYCs) fileResourceManager.getOrganizationKYCFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        case constants.User.USER =>
          val checkFileNameExistsAccountKYCs = masterAccountKYCs.Service.checkFileNameExists(id = loginState.username, fileName = fileName)
          for {
            checkFileNameExistsAccountKYCs <- checkFileNameExistsAccountKYCs
          } yield if (checkFileNameExistsAccountKYCs) fileResourceManager.getAccountKYCFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
        case _ =>
          val checkFileNameExistsAccountFiles = masterAccountFiles.Service.checkFileNameExists(id = loginState.username, fileName = fileName)
          for {
            checkFileNameExistsAccountFiles <- checkFileNameExistsAccountFiles
          } yield if (checkFileNameExistsAccountFiles) fileResourceManager.getAccountFilePath(documentType) else throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION)
      }
      (for {
        path <- path
      } yield Ok.sendFile(utilities.FileOperations.fetchFile(path = path, fileName = fileName))
        ).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def tradingFile(id: String, fileName: String, documentType: String): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val traderID = masterTraders.Service.tryGetID(loginState.username)

      def checkTraderNegotiationExists(traderID: String) = {
        val negotiation = masterNegotiations.Service.get(id)

        def checkTraderPartOfNegotiation(negotiation: Option[Negotiation]) = {
          negotiation match {
            case Some(negotiation) => {
              Future(traderID == negotiation.sellerTraderID || traderID == negotiation.buyerTraderID)
            }
            case None => {
              masterNegotiationHistories.Service.tryGet(id: String).map(negotiationHistory => negotiationHistory.sellerTraderID == traderID || negotiationHistory.buyerTraderID == traderID)
            }
          }
        }

        for {
          negotiation <- negotiation
          traderPartOfNegotiation <- checkTraderPartOfNegotiation(negotiation)
        } yield traderPartOfNegotiation
      }

      (for {
        traderID <- traderID
        traderNegotiationExists <- checkTraderNegotiationExists(traderID)
      } yield {
        if (traderNegotiationExists) {
          val path = documentType match {
            case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA => fileResourceManager.getAssetFilePath(documentType)
            case constants.File.Negotiation.CONTRACT | constants.File.Negotiation.INVOICE | constants.File.Negotiation.BILL_OF_EXCHANGE => fileResourceManager.getNegotiationFilePath(documentType)
            case _ => fileResourceManager.getNegotiationFilePath(documentType)
          }
          Ok.sendFile(utilities.FileOperations.fetchFile(path = path, fileName = fileName))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def organizationAccessedTradingFile(id: String, fileName: String, documentType: String): Action[AnyContent] = withOrganizationLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val organizationID = masterOrganizations.Service.tryGetID(loginState.username)

      def traderOrganizationIDs = {
        val negotiation = masterNegotiations.Service.get(id)

        def getTraderOrganizationIDs(negotiation: Option[Negotiation]) = {
          negotiation match {
            case Some(negotiation) => {
              masterTraders.Service.tryGetOrganizationIDs(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
            }
            case None => {
              val negotiationHistory = masterNegotiationHistories.Service.tryGet(id)

              def traderOrganizationIDs(traderIDs: Seq[String]) = masterTraders.Service.tryGetOrganizationIDs(traderIDs)

              for {
                negotiationHistory <- negotiationHistory
                traderOrganizationIDs <- traderOrganizationIDs(Seq(negotiationHistory.sellerTraderID, negotiationHistory.buyerTraderID))
              } yield traderOrganizationIDs
            }
          }
        }

        for {
          negotiation <- negotiation
          traderOrganizationIDs <- getTraderOrganizationIDs(negotiation)
        } yield traderOrganizationIDs
      }

      (for {
        organizationID <- organizationID
        traderOrganizationIDs <- traderOrganizationIDs
      } yield {
        if (traderOrganizationIDs contains organizationID) {
          val path = documentType match {
            case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA => fileResourceManager.getAssetFilePath(documentType)
            case constants.File.Negotiation.CONTRACT | constants.File.Negotiation.INVOICE | constants.File.Negotiation.BILL_OF_EXCHANGE => fileResourceManager.getNegotiationFilePath(documentType)
            case _ => fileResourceManager.getNegotiationFilePath(documentType)
          }
          Ok.sendFile(utilities.FileOperations.fetchFile(path = path, fileName = fileName))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def zoneAccessedTradingFile(id: String, fileName: String, documentType: String): Action[AnyContent] = withZoneLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val zoneID = masterZones.Service.tryGetID(loginState.username)

      def traderZoneIDs = {
        val negotiation = masterNegotiations.Service.get(id)

        def getTraderZoneIDs(negotiation: Option[Negotiation]) = {
          negotiation match {
            case Some(negotiation) => {
              masterTraders.Service.tryGetZoneIDs(Seq(negotiation.sellerTraderID, negotiation.buyerTraderID))
            }
            case None => {
              val negotiationHistory = masterNegotiationHistories.Service.tryGet(id)

              def traderZoneIDs(traderIDs: Seq[String]) = masterTraders.Service.tryGetZoneIDs(traderIDs)

              for {
                negotiationHistory <- negotiationHistory
                traderZoneIDs <- traderZoneIDs(Seq(negotiationHistory.sellerTraderID, negotiationHistory.buyerTraderID))
              } yield traderZoneIDs
            }
          }
        }

        for {
          negotiation <- negotiation
          traderZoneIDs <- getTraderZoneIDs(negotiation)
        } yield traderZoneIDs
      }


      (for {
        zoneID <- zoneID
        traderZoneIDs <- traderZoneIDs
      } yield {
        if (traderZoneIDs contains zoneID) {
          val path = documentType match {
            case constants.File.Asset.BILL_OF_LADING | constants.File.Asset.COO | constants.File.Asset.COA => fileResourceManager.getAssetFilePath(documentType)
            case constants.File.Negotiation.CONTRACT | constants.File.Negotiation.INVOICE | constants.File.Negotiation.BILL_OF_EXCHANGE => fileResourceManager.getNegotiationFilePath(documentType)
            case _ => fileResourceManager.getNegotiationFilePath(documentType)
          }
          Ok.sendFile(utilities.FileOperations.fetchFile(path = path, fileName = fileName))
        } else {
          throw new BaseException(constants.Response.UNAUTHORIZED)
        }
      }).recover {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }
}
