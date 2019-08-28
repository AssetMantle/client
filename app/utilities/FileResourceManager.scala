package utilities

import exceptions.BaseException
import org.apache.commons.codec.binary.Base64
import javax.inject.Inject
import play.api.{Configuration, Logger}
import models.master.Document

import scala.concurrent.ExecutionContext

class FileResourceManager @Inject()()(implicit exec: ExecutionContext, configuration: Configuration){

  private implicit val module: String = constants.Module.FILE_RESOURCE_MANAGER

  private val logger: Logger = Logger(this.getClass)

  private val uploadAccountKycBankDetailsPath = configuration.get[String]("upload.account.bankDetailsPath")

  private val uploadAccountKycIdentificationPath = configuration.get[String]("upload.account.identificationPath")

  private val uploadAccountProfilePicturePath = configuration.get[String]("upload.account.profilePicturePath")

  private val uploadZoneKycBankDetailsPath = configuration.get[String]("upload.zone.bankDetailsPath")

  private val uploadZoneKycIdentificationPath = configuration.get[String]("upload.zone.identificationPath")

  private val uploadOrganizationKycBankDetailsPath = configuration.get[String]("upload.organization.bankDetailsPath")

  private val uploadOrganizationKycIdentificationPath = configuration.get[String]("upload.organization.identificationPath")

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
      case constants.File.BANK_DETAILS => uploadOrganizationKycBankDetailsPath
      case constants.File.IDENTIFICATION => uploadOrganizationKycIdentificationPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getTraderKycFilePath(documentType: String): String = {
    documentType match {
      case constants.File.IDENTIFICATION => uploadTraderKycIdentificationPath
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
      val (fileName, encodedBase64) = utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), null)
      }
      masterCreate(document.updateFileName(fileName).updateFile(Option(encodedBase64)))
      utilities.FileOperations.renameFile(path, name, fileName)
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message)
        throw new BaseException(baseException.failure)
      case e: Exception => logger.error(e.getMessage)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def updateFile[T <: Document[T]](name: String, documentType: String, path: String, oldDocumentFileName: String, document: T, updateOldDocument: T => Int): Unit = {
    try {
      val (fileName, encodedBase64) = utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), null)
      }
      updateOldDocument(document.updateFileName(fileName).updateFile(Option(encodedBase64)))
      utilities.FileOperations.deleteFile(path, oldDocumentFileName)
      utilities.FileOperations.renameFile(path, name, fileName)
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message)
        throw new BaseException(baseException.failure)
      case e: Exception => logger.error(e.getMessage)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }
}
