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

  private val uploadAccountKycBankDetailsPath = configuration.get[String]("upload.account.bankDetailsPath")

  private val uploadAccountKycIdentificationPath = configuration.get[String]("upload.account.identificationPath")

  private val uploadAccountProfilePicturePath = configuration.get[String]("upload.account.profilePicturePath")

  private val uploadZoneKycBankDetailsPath = configuration.get[String]("upload.zone.bankDetailsPath")

  private val uploadZoneKycIdentificationPath = configuration.get[String]("upload.zone.identificationPath")

  private val uploadOrganizationAgreementPath = configuration.get[String]("upload.organization.agreementPath")

  private val uploadOrganizationKYCBankAccountDetailPath = configuration.get[String]("upload.organization.bankAccountDetailPath")

  private val uploadOrganizationKycAdminProfileIdentificationPath = configuration.get[String]("upload.organization.adminProfileIdentificationPath")

  private val uploadOrganizationKycLatestAuditedFinancialReportPath = configuration.get[String]("upload.organization.latestAuditedFinancialReportPath")

  private val uploadOrganizationKYCLastYearAuditedFinancialReportPath = configuration.get[String]("upload.organization.lastYearAuditedFinancialReportPath")

  private val uploadOrganizationKycManagementPath = configuration.get[String]("upload.organization.managementPath")

  private val uploadOrganizationKYCACRAPath = configuration.get[String]("upload.organization.acraPath")

  private val uploadOrganizationKycShareStructurePath = configuration.get[String]("upload.organization.shareStructurePath")

  private val uploadTraderKycIdentificationPath = configuration.get[String]("upload.trader.identificationPath")

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

  def getAccountKycFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_DETAILS => uploadAccountKycBankDetailsPath
      case constants.File.IDENTIFICATION => uploadAccountKycIdentificationPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getZoneKycFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_DETAILS => uploadZoneKycBankDetailsPath
      case constants.File.IDENTIFICATION => uploadZoneKycIdentificationPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getOrganizationKycFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_ACCOUNT_DETAIL => uploadOrganizationKYCBankAccountDetailPath
      case constants.File.ADMIN_PROFILE_IDENTIFICATION => uploadOrganizationKycAdminProfileIdentificationPath
      case constants.File.LATEST_AUDITED_FINANCIAL_REPORT => uploadOrganizationKycLatestAuditedFinancialReportPath
      case constants.File.LAST_YEAR_AUDITED_FINANCIAL_REPORT => uploadOrganizationKYCLastYearAuditedFinancialReportPath
      case constants.File.MANAGEMENT => uploadOrganizationKycManagementPath
      case constants.File.ACRA => uploadOrganizationKYCACRAPath
      case constants.File.SHARE_STRUCTURE => uploadOrganizationKycShareStructurePath
      case constants.File.ORGANIZATION_AGREEMENT => uploadOrganizationAgreementPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getTraderKycFilePath(documentType: String): String = {
    documentType match {
      case constants.File.TRADER_IDENTIFICATION => uploadTraderKycIdentificationPath
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

  def getAccountFilePath(documentType: String): String = {
    documentType match {
      case constants.File.PROFILE_PICTURE => uploadAccountProfilePicturePath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def storeFile[T <: Document[T]](name: String, documentType: String, path: String, document: T, masterCreate: T => Future[String]):Future[Unit] = {
   /* try {
      val (fileName, encodedBase64): (String, Option[Array[Byte]]) = utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
      utilities.FileOperations.renameFile(path, name, fileName)
      masterCreate(document.updateFileName(fileName).updateFile(encodedBase64))


    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(baseException.failure)
      case e: Exception => logger.error(e.getMessage)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }*/

    val convertToThumbnailOrHash=Future{
      utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
    }
    def renameFile(fileName:String)=Future{utilities.FileOperations.renameFile(path, name, fileName)}
    def create(fileName:String,encodedBase64:Option[Array[Byte]])=masterCreate(document.updateFileName(fileName).updateFile(encodedBase64))
    (for{
      (fileName, encodedBase64) <- convertToThumbnailOrHash
      _<- renameFile(fileName)
      _<- create(fileName,encodedBase64)
    }yield {}
      ).recover{
      case baseException: BaseException => logger.error(baseException.failure.message)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(baseException.failure)
      case e: Exception => logger.error(e.getMessage)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def updateFile[T <: Document[T]](name: String, documentType: String, path: String, oldDocumentFileName: String, document: T, updateOldDocument: T => Future[Int]):Future[Unit] = {


     /* val (fileName, encodedBase64): (String, Option[Array[Byte]]) = utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }

      utilities.FileOperations.deleteFile(path, oldDocumentFileName)
      utilities.FileOperations.renameFile(path, name, fileName)
      updateOldDocument(document.updateFileName(fileName).updateFile(encodedBase64))
    }.recover{
      case baseException: BaseException => logger.error(baseException.failure.message)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(baseException.failure)
      case e: Exception => logger.error(e.getMessage)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)*/

    val convertToThumbnailOrHash=Future{
      utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
    }
    def renameFile(fileName:String)=Future{utilities.FileOperations.renameFile(path, name, fileName)}
    def deleteFile(oldDocumentFileName:String)=Future{utilities.FileOperations.deleteFile(path, oldDocumentFileName)}
    def update(fileName:String,encodedBase64:Option[Array[Byte]])=updateOldDocument(document.updateFileName(fileName).updateFile(encodedBase64))
    for{
      (fileName, encodedBase64)<-convertToThumbnailOrHash
      _<- update(fileName,encodedBase64)
      _<- deleteFile(oldDocumentFileName)
      _<- renameFile(oldDocumentFileName)
    }yield{}
  }



}
