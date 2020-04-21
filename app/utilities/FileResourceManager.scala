package utilities

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import org.apache.commons.codec.binary.Base64
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileResourceManager @Inject()()(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.FILE_RESOURCE_MANAGER

  private val logger: Logger = Logger(this.getClass)

  private val uploadAccountKYCBankAccountDetailPath = configuration.get[String]("upload.account.bankAccountDetailPath")

  private val uploadAccountKYCIdentificationPath = configuration.get[String]("upload.account.identificationPath")

  private val uploadAccountProfilePicturePath = configuration.get[String]("upload.account.profilePicturePath")

  private val uploadZoneKYCBankAccountDetailPath = configuration.get[String]("upload.zone.bankAccountDetailPath")

  private val uploadZoneKYCIdentificationPath = configuration.get[String]("upload.zone.identificationPath")

  private val uploadOrganizationAgreementPath = configuration.get[String]("upload.organization.agreementPath")

  private val uploadOrganizationKYCBankAccountDetailPath = configuration.get[String]("upload.organization.bankAccountDetailPath")

  private val uploadOrganizationKYCAdminProfileIdentificationPath = configuration.get[String]("upload.organization.adminProfileIdentificationPath")

  private val uploadOrganizationKYCLatestAuditedFinancialReportPath = configuration.get[String]("upload.organization.latestAuditedFinancialReportPath")

  private val uploadOrganizationKYCLastYearAuditedFinancialReportPath = configuration.get[String]("upload.organization.lastYearAuditedFinancialReportPath")

  private val uploadOrganizationKYCManagementPath = configuration.get[String]("upload.organization.managementPath")

  private val uploadOrganizationKYCACRAPath = configuration.get[String]("upload.organization.acraPath")

  private val uploadOrganizationKYCIncorporationDocumentPath = configuration.get[String]("upload.organization.incorporationDocument")

  private val uploadOrganizationKYCShareStructurePath = configuration.get[String]("upload.organization.shareStructurePath")

  private val uploadOrganizationWorldCheck = configuration.get[String]("upload.backgroundCheck.organizationWorldCheck")

  private val uploadTraderKYCIdentificationPath = configuration.get[String]("upload.trader.identificationPath")

  private val uploadTraderAgreementPath = configuration.get[String]("upload.trader.agreementPath")

  private val uploadTraderEmploymentProofPath = configuration.get[String]("upload.trader.employmentProofPath")

  private val uploadTraderWorldCheck = configuration.get[String]("upload.backgroundCheck.traderWorldCheck")

  private val uploadTraderAssetContractPath: String = configuration.get[String]("upload.asset.contract")

  private val uploadTraderAssetOBLPath: String = configuration.get[String]("upload.asset.obl")

  private val uploadTraderAssetPackingListPath: String = configuration.get[String]("upload.asset.packingList")

  private val uploadTraderAssetCOOPath: String = configuration.get[String]("upload.asset.coo")

  private val uploadTraderAssetCOAPath: String = configuration.get[String]("upload.asset.coa")

  private val uploadTraderAssetOtherPath: String = configuration.get[String]("upload.asset.other")

  private val uploadTraderNegotiationInvoicePath: String = configuration.get[String]("upload.negotiation.invoice")

  private val uploadTraderNegotiationInsurancePath: String = configuration.get[String]("upload.negotiation.insurance")

  private val uploadTraderNegotiationOtherPath: String = configuration.get[String]("upload.negotiation.other")

  private val uploadTraderNegotiationContractPath: String = configuration.get[String]("upload.negotiation.contract")

  private val uploadTraderNegotiationBuyerContractPath: String = configuration.get[String]("upload.negotiation.buyerContract")

  private val uploadTraderNegotiationSellerContractOtherPath: String = configuration.get[String]("upload.negotiation.sellerContract")

  private val uploadTraderNegotiationAWBProofPath: String = configuration.get[String]("upload.negotiation.awbProof")

  private val uploadTraderNegotiationFiatProofPath: String = configuration.get[String]("upload.negotiation.fiatProof")

  def getAccountKYCFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_ACCOUNT_DETAIL => uploadAccountKYCBankAccountDetailPath
      case constants.File.IDENTIFICATION => uploadAccountKYCIdentificationPath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getBackgroundCheckFilePath(documentType: String): String = {
    documentType match {
      case constants.File.TRADER_WORLD_CHECK => uploadTraderWorldCheck
      case constants.File.ORGANIZATION_WORLD_CHECK => uploadOrganizationWorldCheck
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getZoneKYCFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_ACCOUNT_DETAIL => uploadZoneKYCBankAccountDetailPath
      case constants.File.IDENTIFICATION => uploadZoneKYCIdentificationPath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getOrganizationKYCFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_ACCOUNT_DETAIL => uploadOrganizationKYCBankAccountDetailPath
      case constants.File.ADMIN_PROFILE_IDENTIFICATION => uploadOrganizationKYCAdminProfileIdentificationPath
      case constants.File.LATEST_AUDITED_FINANCIAL_REPORT => uploadOrganizationKYCLatestAuditedFinancialReportPath
      case constants.File.LAST_YEAR_AUDITED_FINANCIAL_REPORT => uploadOrganizationKYCLastYearAuditedFinancialReportPath
      case constants.File.MANAGEMENT => uploadOrganizationKYCManagementPath
      case constants.File.ACRA => uploadOrganizationKYCACRAPath
      case constants.File.SHARE_STRUCTURE => uploadOrganizationKYCShareStructurePath
      case constants.File.ORGANIZATION_AGREEMENT => uploadOrganizationAgreementPath
      case constants.File.INCORPORATION_DOCUMENT => uploadOrganizationKYCIncorporationDocumentPath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getTraderKYCFilePath(documentType: String): String = {
    documentType match {
      case constants.File.TRADER_IDENTIFICATION => uploadTraderKYCIdentificationPath
      case constants.File.TRADER_AGREEMENT => uploadTraderAgreementPath
      case constants.File.EMPLOYMENT_PROOF => uploadTraderEmploymentProofPath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getTraderAssetFilePath(documentType: String): String = {
    documentType match {
      case constants.File.CONTRACT => uploadTraderAssetContractPath
      case constants.File.OBL => uploadTraderAssetOBLPath
      case constants.File.PACKING_LIST => uploadTraderAssetPackingListPath
      case constants.File.COO => uploadTraderAssetCOOPath
      case constants.File.COA => uploadTraderAssetCOAPath
      case constants.File.OTHER => uploadTraderAssetOtherPath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getTraderNegotiationFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BUYER_CONTRACT => uploadTraderNegotiationBuyerContractPath
      case constants.File.SELLER_CONTRACT => uploadTraderNegotiationSellerContractOtherPath
      case constants.File.AWB_PROOF => uploadTraderNegotiationAWBProofPath
      case constants.File.FIAT_PROOF => uploadTraderNegotiationFiatProofPath
      case constants.File.INVOICE => uploadTraderNegotiationInvoicePath
      case constants.File.INSURANCE => uploadTraderNegotiationInsurancePath
      case constants.File.OTHER => uploadTraderNegotiationOtherPath
      case constants.File.CONTRACT => uploadTraderNegotiationContractPath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getZoneNegotiationFilePath(documentType: String): String = {
    documentType match {
      case constants.File.AWB_PROOF => uploadTraderNegotiationAWBProofPath
      case constants.File.FIAT_PROOF => uploadTraderNegotiationFiatProofPath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getAccountFilePath(documentType: String): String = {
    documentType match {
      case constants.File.PROFILE_PICTURE => uploadAccountProfilePicturePath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def storeFile[T <: Document[T]](name: String, documentType: String, path: String, document: T, masterCreate: T => Future[String]): Future[Boolean] = {
    val getFileNameAndEncodedBase64: Future[(String, Option[Array[Byte]])] = Future {
      utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG |
             constants.File.JPEG_LOWER_CASE | constants.File.JPG_LOWER_CASE | constants.File.PNG_LOWER_CASE
        => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
    }

    def updateAndCreateFile(fileName: String, encodedBase64: Option[Array[Byte]]): Future[String] = masterCreate(document.updateFileName(fileName).updateFile(encodedBase64))

    (for {
      (fileName, encodedBase64) <- getFileNameAndEncodedBase64
      _ <- updateAndCreateFile(fileName, encodedBase64)
    } yield utilities.FileOperations.renameFile(path, name, fileName)
      ).recover {
      case baseException: BaseException => logger.error(baseException.failure.message)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(baseException.failure)
      case e: Exception => logger.error(e.getMessage)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def updateFile[T <: Document[T]](name: String, documentType: String, path: String, oldDocumentFileName: String, document: T, updateOldDocument: T => Future[Int]): Future[Boolean] = {
    val getFileNameAndEncodedBase64: Future[(String, Option[Array[Byte]])] = Future {
      utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG |
             constants.File.JPEG_LOWER_CASE | constants.File.JPG_LOWER_CASE | constants.File.PNG_LOWER_CASE
        => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
    }

    def update(fileName: String, encodedBase64: Option[Array[Byte]]): Future[Int] = updateOldDocument(document.updateFileName(fileName).updateFile(encodedBase64))

    (for {
      (fileName, encodedBase64) <- getFileNameAndEncodedBase64
      _ <- update(fileName, encodedBase64)
    } yield {
      utilities.FileOperations.deleteFile(path, oldDocumentFileName)
      utilities.FileOperations.renameFile(path, name, fileName)
    }).recover {
      case baseException: BaseException => logger.error(baseException.failure.message)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(baseException.failure)
      case e: Exception => logger.error(e.getMessage)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }
}