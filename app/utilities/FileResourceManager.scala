package utilities

import exceptions.BaseException
import org.apache.commons.codec.binary.Base64
import javax.inject.Inject
import models.Trait.Document
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

class FileResourceManager @Inject()()(implicit exec: ExecutionContext, configuration: Configuration){

  private implicit val module: String = constants.Module.FILE_RESOURCE_MANAGER

  private val logger: Logger = Logger(this.getClass)

  private val uploadAccountKycBankDetailsPath = configuration.get[String]("upload.account.bankDetailsPath")

  private val uploadAccountKycIdentificationPath = configuration.get[String]("upload.account.identificationPath")

  private val uploadAccountProfilePicturePath = configuration.get[String]("upload.account.profilePicturePath")

  private val uploadZoneKycBankDetailsPath = configuration.get[String]("upload.zone.bankDetailsPath")

  private val uploadZoneKycIdentificationPath = configuration.get[String]("upload.zone.identificationPath")

  private val uploadOrganizationKYCBankAccountDetailPath = configuration.get[String]("upload.organization.bankAccountDetailPath")

  private val uploadOrganizationKycAdminProfileIdentificationPath = configuration.get[String]("upload.organization.adminProfileIdentificationPath")

  private val uploadOrganizationKycLatestAuditedFinancialReportPath = configuration.get[String]("upload.organization.latestAuditedFinancialReportPath")

  private val uploadOrganizationKYCLastYearAuditedFinancialReportPath = configuration.get[String]("upload.organization.lastYearAuditedFinancialReportPath")

  private val uploadOrganizationKycManagementPath = configuration.get[String]("upload.organization.managementPath")

  private val uploadOrganizationKYCACRAPath = configuration.get[String]("upload.organization.acraPath")

  private val uploadOrganizationKycShareStructurePath = configuration.get[String]("upload.organization.shareStructurePath")

  private val uploadTraderKycIdentificationPath = configuration.get[String]("upload.trader.identificationPath")

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
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getTraderKycFilePath(documentType: String): String = {
    documentType match {
      case constants.File.TRADER_IDENTIFICATION => uploadTraderKycIdentificationPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getAccountFilePath(documentType: String): String = {
    documentType match {
      case constants.File.PROFILE_PICTURE => uploadAccountProfilePicturePath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def storeFile[T <: Document[T]](name: String, documentType: String, path: String, document: T, masterCreate: T => String): Unit = {
    try {
      val (fileName, encodedBase64): (String, Option[Array[Byte]]) = utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
      masterCreate(document.updateFileName(fileName).updateFile(encodedBase64))
      utilities.FileOperations.renameFile(path, name, fileName)
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(baseException.failure)
      case e: Exception => logger.error(e.getMessage)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def updateFile[T <: Document[T]](name: String, documentType: String, path: String, oldDocumentFileName: String, document: T, updateOldDocument: T => Int): Unit = {
    try {
      val (fileName, encodedBase64): (String, Option[Array[Byte]]) = utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
      updateOldDocument(document.updateFileName(fileName).updateFile(encodedBase64))
      utilities.FileOperations.deleteFile(path, oldDocumentFileName)
      utilities.FileOperations.renameFile(path, name, fileName)
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
