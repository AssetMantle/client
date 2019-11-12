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

  private val uploadOrganizationKYCShareStructurePath = configuration.get[String]("upload.organization.shareStructurePath")

  private val uploadTraderKYCIdentificationPath = configuration.get[String]("upload.trader.identificationPath")

  private val uploadTraderAgreementPath = configuration.get[String]("upload.trader.agreementPath")

  private val uploadTraderAssetContractPath: String = configuration.get[String]("upload.asset.contract")

  private val uploadTraderAssetOBLPath: String = configuration.get[String]("upload.asset.obl")

  private val uploadTraderAssetInvoicePath: String = configuration.get[String]("upload.asset.invoice")

  private val uploadTraderAssetPackingListPath: String = configuration.get[String]("upload.asset.packingList")

  private val uploadTraderAssetCOOPath: String = configuration.get[String]("upload.asset.coo")

  private val uploadTraderAssetCOAPath: String = configuration.get[String]("upload.asset.coa")

  private val uploadTraderAssetOtherPath: String = configuration.get[String]("upload.asset.other")

  private val uploadTraderNegotiationBuyerContractPath: String = configuration.get[String]("upload.negotiation.buyerContract")

  private val uploadTraderNegotiationSellerContractOtherPath: String = configuration.get[String]("upload.negotiation.sellerContract")

  private val uploadTraderNegotiationAWBProofPath: String = configuration.get[String]("upload.negotiation.awbProof")

  private val uploadTraderNegotiationFiatProofPath: String = configuration.get[String]("upload.negotiation.fiatProof")

  def getAccountKYCFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_ACCOUNT_DETAIL => uploadAccountKYCBankAccountDetailPath
      case constants.File.IDENTIFICATION => uploadAccountKYCIdentificationPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getZoneKYCFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_ACCOUNT_DETAIL => uploadZoneKYCBankAccountDetailPath
      case constants.File.IDENTIFICATION => uploadZoneKYCIdentificationPath
      case _ => constants.File.UNKNOWN_TYPE
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
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getTraderKYCFilePath(documentType: String): String = {
    documentType match {
      case constants.File.TRADER_IDENTIFICATION => uploadTraderKYCIdentificationPath
      case constants.File.TRADER_AGREEMENT => uploadTraderAgreementPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getTraderAssetFilePath(documentType: String): String = {
    documentType match {
      case constants.File.CONTRACT => uploadTraderAssetContractPath
      case constants.File.OBL => uploadTraderAssetOBLPath
      case constants.File.INVOICE => uploadTraderAssetInvoicePath
      case constants.File.PACKING_LIST => uploadTraderAssetPackingListPath
      case constants.File.COO => uploadTraderAssetCOOPath
      case constants.File.COA => uploadTraderAssetCOAPath
      case constants.File.OTHER => uploadTraderAssetOtherPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getTraderNegotiationFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BUYER_CONTRACT => uploadTraderNegotiationBuyerContractPath
      case constants.File.SELLER_CONTRACT => uploadTraderNegotiationSellerContractOtherPath
      case constants.File.AWB_PROOF => uploadTraderNegotiationAWBProofPath
      case constants.File.FIAT_PROOF => uploadTraderNegotiationFiatProofPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getZoneNegotiationFilePath(documentType: String): String = {
    documentType match {
      case constants.File.AWB_PROOF => uploadTraderNegotiationAWBProofPath
      case constants.File.FIAT_PROOF => uploadTraderNegotiationFiatProofPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getAccountFilePath(documentType: String): String = {
    documentType match {
      case constants.File.PROFILE_PICTURE => uploadAccountProfilePicturePath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def storeFile[T <: Document[T]](name: String, documentType: String, path: String, document: T, masterCreate: T => Future[String]):Future[Boolean]= {

    try {
      val (fileName, encodedBase64): (String, Option[Array[Byte]]) = utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
      val updateFileName=masterCreate(document.updateFileName(fileName).updateFile(encodedBase64))
      utilities.FileOperations.renameFile(path, name, fileName)
      for{
        _<-updateFileName
      }yield  utilities.FileOperations.renameFile(path, name, fileName)
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(baseException.failure)
      case e: Exception => logger.error(e.getMessage)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def updateFile[T <: Document[T]](name: String, documentType: String, path: String, oldDocumentFileName: String, document: T, updateOldDocument: T => Future[Int]):Future[Unit] = {

    try {
      val (fileName, encodedBase64): (String, Option[Array[Byte]]) = utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
      val updateOldDocumentVal=updateOldDocument(document.updateFileName(fileName).updateFile(encodedBase64))
      for{
        _<-updateOldDocumentVal
      }yield{
        utilities.FileOperations.deleteFile(path, oldDocumentFileName)
        utilities.FileOperations.renameFile(path, name, fileName)
      }
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(baseException.failure)
      case e: Exception => logger.error(e.getMessage)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }



}
