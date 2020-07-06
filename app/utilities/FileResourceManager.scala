package utilities

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import org.apache.commons.codec.binary.Base64
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileResourceManager @Inject()(utilitiesLog: utilities.Log)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.FILE_RESOURCE_MANAGER

  private implicit val logger: Logger = Logger(this.getClass)

  private val rootFilePath = configuration.get[String]("upload.rootFilePath")

  private val uploadAccountKYCIdentificationPath = rootFilePath + configuration.get[String]("upload.account.identificationPath")

  private val uploadAccountProfilePicturePath = rootFilePath + configuration.get[String]("upload.account.profilePicturePath")

  private val uploadZoneKYCBankAccountDetailPath = rootFilePath + configuration.get[String]("upload.zone.bankAccountDetailPath")

  private val uploadZoneKYCIdentificationPath = rootFilePath + configuration.get[String]("upload.zone.identificationPath")

  private val uploadOrganizationKYCACRAPath = rootFilePath + configuration.get[String]("upload.organization.acraPath")

  private val uploadOrganizationKYCBoardResolutionPath = rootFilePath + configuration.get[String]("upload.organization.boardResolutionPath")

  private val uploadAssetBillOfLadingPath: String = rootFilePath + configuration.get[String]("upload.asset.billOfLading")

  private val uploadAssetCOOPath: String = rootFilePath + configuration.get[String]("upload.asset.coo")

  private val uploadAssetCOAPath: String = rootFilePath + configuration.get[String]("upload.asset.coa")

  private val uploadNegotiationInvoicePath: String = rootFilePath + configuration.get[String]("upload.negotiation.invoice")

  private val uploadNegotiationBillOfExchangePath: String = rootFilePath + configuration.get[String]("upload.negotiation.billOfExchange")

  private val uploadNegotiationContractPath: String = rootFilePath + configuration.get[String]("upload.negotiation.contract")

  private val uploadNegotiationInsurancePath: String = rootFilePath + configuration.get[String]("upload.negotiation.insurance")

  private val uploadNegotiationOthersPath: String = rootFilePath + configuration.get[String]("upload.negotiation.others")


  def getAccountKYCFilePath(documentType: String): String = {
    documentType match {
      case constants.File.AccountKYC.IDENTIFICATION => uploadAccountKYCIdentificationPath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getZoneKYCFilePath(documentType: String): String = {
    documentType match {
      case constants.File.ZoneKYC.BANK_ACCOUNT_DETAIL => uploadZoneKYCBankAccountDetailPath
      case constants.File.ZoneKYC.IDENTIFICATION => uploadZoneKYCIdentificationPath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getOrganizationKYCFilePath(documentType: String): String = {
    documentType match {
      case constants.File.OrganizationKYC.ACRA => uploadOrganizationKYCACRAPath
      case constants.File.OrganizationKYC.BOARD_RESOLUTION => uploadOrganizationKYCBoardResolutionPath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getAssetFilePath(documentType: String): String = {
    documentType match {
      case constants.File.Asset.BILL_OF_LADING => uploadAssetBillOfLadingPath
      case constants.File.Asset.COO => uploadAssetCOOPath
      case constants.File.Asset.COA => uploadAssetCOAPath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getNegotiationFilePath(documentType: String): String = {
    documentType match {
      case constants.File.Negotiation.INVOICE => uploadNegotiationInvoicePath
      case constants.File.Negotiation.BILL_OF_EXCHANGE => uploadNegotiationBillOfExchangePath
      case constants.File.Negotiation.CONTRACT => uploadNegotiationContractPath
      case constants.File.Negotiation.INSURANCE => uploadNegotiationInsurancePath
      case constants.File.Negotiation.OTHERS => uploadNegotiationOthersPath
      case _ => uploadNegotiationOthersPath
    }
  }

  def getAccountFilePath(documentType: String): String = {
    documentType match {
      case constants.File.Account.PROFILE_PICTURE => uploadAccountProfilePicturePath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def storeFile[T <: Document[T]](name: String, path: String, document: T, masterCreate: T => Future[String]): Future[Unit] = {
    utilitiesLog.infoLog(constants.Log.Info.STORE_FILE_ENTRY, name, document.documentType, path)
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
    } yield {
      utilities.FileOperations.renameFile(path, name, fileName)
      utilitiesLog.infoLog(constants.Log.Info.STORE_FILE_EXIT, name, document.documentType, path)
    }
      ).recover {
      case baseException: BaseException => logger.error(baseException.failure.message)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.FILE_UPLOAD_ERROR)
      case e: Exception => logger.error(e.getMessage)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def updateFile[T <: Document[T]](name: String, path: String, oldDocument: T, updateOldDocument: T => Future[Int]): Future[Boolean] = {
    val getFileNameAndEncodedBase64: Future[(String, Option[Array[Byte]])] = Future {
      utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG |
             constants.File.JPEG_LOWER_CASE | constants.File.JPG_LOWER_CASE | constants.File.PNG_LOWER_CASE
        => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
    }

    def update(fileName: String, encodedBase64: Option[Array[Byte]]): Future[Int] = updateOldDocument(oldDocument.updateFileName(fileName).updateFile(encodedBase64).updateStatus(None))

    (for {
      (fileName, encodedBase64) <- getFileNameAndEncodedBase64
      _ <- update(fileName, encodedBase64)
    } yield {
      utilities.FileOperations.deleteFile(path, oldDocument.fileName)
      utilities.FileOperations.renameFile(path, name, fileName)
    }).recover {
      case baseException: BaseException => logger.error(baseException.failure.message)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.FILE_UPLOAD_ERROR)
      case e: Exception => logger.error(e.getMessage)
        utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }
}