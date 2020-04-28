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

  private val uploadAccountKYCIdentificationPath = configuration.get[String]("upload.account.identificationPath")

  private val uploadAccountProfilePicturePath = configuration.get[String]("upload.account.profilePicturePath")

  private val uploadZoneKYCBankAccountDetailPath = configuration.get[String]("upload.zone.bankAccountDetailPath")

  private val uploadZoneKYCIdentificationPath = configuration.get[String]("upload.zone.identificationPath")

  private val uploadOrganizationKYCACRAPath = configuration.get[String]("upload.organization.acraPath")

  private val uploadOrganizationKYCIncorporationDocumentPath = configuration.get[String]("upload.organization.incorporationDocument")

  private val uploadOrganizationWorldCheck = configuration.get[String]("upload.backgroundCheck.organizationWorldCheck")

  private val uploadTraderEmploymentProofPath = configuration.get[String]("upload.trader.employmentProofPath")

  private val uploadTraderWorldCheck = configuration.get[String]("upload.backgroundCheck.traderWorldCheck")

  private val uploadAssetBillOfLadingPath: String = configuration.get[String]("upload.asset.billOfLading")

  private val uploadAssetCOOPath: String = configuration.get[String]("upload.asset.coo")

  private val uploadAssetCOAPath: String = configuration.get[String]("upload.asset.coa")

  private val uploadNegotiationInvoicePath: String = configuration.get[String]("upload.negotiation.invoice")

  private val uploadNegotiationBillOfExchangePath: String = configuration.get[String]("upload.negotiation.billOfExchange")

  private val uploadNegotiationContractPath: String = configuration.get[String]("upload.negotiation.contract")

  private val uploadNegotiationInsurancePath: String = configuration.get[String]("upload.negotiation.insurance")

  private val uploadNegotiationOthersPath: String = configuration.get[String]("upload.negotiation.others")


  def getAccountKYCFilePath(documentType: String): String = {
    documentType match {
      case constants.File.AccountKYC.IDENTIFICATION => uploadAccountKYCIdentificationPath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getBackgroundCheckFilePath(documentType: String): String = {
    documentType match {
      case constants.File.WorldCheck.TRADER_WORLD_CHECK => uploadTraderWorldCheck
      case constants.File.WorldCheck.ORGANIZATION_WORLD_CHECK => uploadOrganizationWorldCheck
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
      case constants.File.OrganizationKYC.INCORPORATION_DOCUMENT => uploadOrganizationKYCIncorporationDocumentPath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getTraderKYCFilePath(documentType: String): String = {
    documentType match {
      case constants.File.TraderKYC.EMPLOYMENT_PROOF => uploadTraderEmploymentProofPath
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
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def getAccountFilePath(documentType: String): String = {
    documentType match {
      case constants.File.Account.PROFILE_PICTURE => uploadAccountProfilePicturePath
      case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
    }
  }

  def storeFile[T <: Document[T]](name: String, path: String, document: T, masterCreate: T => Future[String]): Future[Boolean] = {
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

  def updateFile[T <: Document[T]](name: String, path: String, oldDocument: T, updateOldDocument: T => Future[Int]): Future[Boolean] = {
    val getFileNameAndEncodedBase64: Future[(String, Option[Array[Byte]])] = Future {
      utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG |
             constants.File.JPEG_LOWER_CASE | constants.File.JPG_LOWER_CASE | constants.File.PNG_LOWER_CASE
        => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
    }

    def update(fileName: String, encodedBase64: Option[Array[Byte]]): Future[Int] = updateOldDocument(oldDocument.updateFileName(fileName).updateFile(encodedBase64))

    (for {
      (fileName, encodedBase64) <- getFileNameAndEncodedBase64
      _ <- update(fileName, encodedBase64)
    } yield {
      utilities.FileOperations.deleteFile(path, oldDocument.fileName)
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