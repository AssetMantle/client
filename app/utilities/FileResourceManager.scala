package utilities

import java.io.File

import akka.NotUsed
import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import org.apache.commons.codec.binary.Base64
import play.api.{Configuration, Logger}
import akka.http.scaladsl.model.{ContentType, HttpCharset, HttpCharsets, MediaTypes}

import scala.concurrent.{ExecutionContext, Future}
import akka.stream.alpakka.s3.{ObjectMetadata, S3Attributes, S3Exception, S3Ext}
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.scaladsl.FileIO
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import akka.stream.Materializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.util.ByteString
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

@Singleton
class FileResourceManager @Inject()(actorSystem: ActorSystem, utilitiesLog: utilities.Log, keyStore: KeyStore)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.FILE_RESOURCE_MANAGER

  private implicit val logger: Logger = Logger(this.getClass)

  implicit val materializer: Materializer = Materializer(actorSystem)

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

  private val s3BucketInUse = configuration.get[Boolean]("s3.useBucket")

  private val tempFilePath: String = rootFilePath + configuration.get[String]("upload.temp")

  private val s3region: String = configuration.get[String]("s3.region")

  private val s3Bucket: String = configuration.get[String]("s3.bucket")

  private val awsCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(keyStore.getPassphrase(constants.KeyStore.AWS_ACCESS_KEY_ID), keyStore.getPassphrase(constants.KeyStore.AWS_SECRET_ACCESS_KEY)))

  private val s3RegionProvider = new AwsRegionProvider {
    override def getRegion: Region = Region.of(s3region)
  }

  private val s3Settings = S3Ext(actorSystem).settings
    .withCredentialsProvider(awsCredentialsProvider)
    .withListBucketApiVersion(ListBucketVersion2)
    .withS3RegionProvider(s3RegionProvider)

  private val s3Attributes = S3Attributes.settings(s3Settings)

  def getAccountKYCFilePath(documentType: String): String = {
    if (s3BucketInUse) {
      tempFilePath
    } else {
      documentType match {
        case constants.File.AccountKYC.IDENTIFICATION => uploadAccountKYCIdentificationPath
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }
    }
  }

  def getZoneKYCFilePath(documentType: String): String = {
    if (s3BucketInUse) {
      tempFilePath
    } else {
      documentType match {
        case constants.File.ZoneKYC.BANK_ACCOUNT_DETAIL => uploadZoneKYCBankAccountDetailPath
        case constants.File.ZoneKYC.IDENTIFICATION => uploadZoneKYCIdentificationPath
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }
    }
  }

  def getOrganizationKYCFilePath(documentType: String): String = {
    if (s3BucketInUse) {
      tempFilePath
    } else {
      documentType match {
        case constants.File.OrganizationKYC.ACRA => uploadOrganizationKYCACRAPath
        case constants.File.OrganizationKYC.BOARD_RESOLUTION => uploadOrganizationKYCBoardResolutionPath
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }
    }
  }

  def getAssetFilePath(documentType: String): String = {
    if (s3BucketInUse) {
      tempFilePath
    } else {
      documentType match {
        case constants.File.Asset.BILL_OF_LADING => uploadAssetBillOfLadingPath
        case constants.File.Asset.COO => uploadAssetCOOPath
        case constants.File.Asset.COA => uploadAssetCOAPath
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }
    }
  }

  def getNegotiationFilePath(documentType: String): String = {
    if (s3BucketInUse) {
      tempFilePath
    } else {
      documentType match {
        case constants.File.Negotiation.INVOICE => uploadNegotiationInvoicePath
        case constants.File.Negotiation.BILL_OF_EXCHANGE => uploadNegotiationBillOfExchangePath
        case constants.File.Negotiation.CONTRACT => uploadNegotiationContractPath
        case constants.File.Negotiation.INSURANCE => uploadNegotiationInsurancePath
        case constants.File.Negotiation.OTHERS => uploadNegotiationOthersPath
        case _ => uploadNegotiationOthersPath
      }
    }
  }

  def getAccountFilePath(documentType: String): String = {
    if (s3BucketInUse) {
      tempFilePath
    } else {
      documentType match {
        case constants.File.Account.PROFILE_PICTURE => uploadAccountProfilePicturePath
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }
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

    def storeFileInS3BucketAndDelete(fileName: String) = {
      if (s3BucketInUse) {
        def charset(): HttpCharset = HttpCharsets.`US-ASCII`

        def getContentType(getCharset: () => HttpCharset) = ContentType(MediaTypes.forExtension(utilities.FileOperations.fileExtensionFromName(fileName)), getCharset)

        val add = FileIO.fromPath(new File(getNegotiationFilePath(document.documentType) + fileName).toPath).runWith(S3.multipartUpload(s3Bucket, fileName, getContentType(charset)).withAttributes(s3Attributes))
        for {
          _ <- add
        } yield utilities.FileOperations.deleteFile(path, fileName)
      } else {
        Future(true)
      }
    }

    (for {
      (fileName, encodedBase64) <- getFileNameAndEncodedBase64
      _ <- updateAndCreateFile(fileName, encodedBase64)
      _ <- Future(utilities.FileOperations.renameFile(path, name, fileName))
      _ <- storeFileInS3BucketAndDelete(fileName)
    } yield {
      utilitiesLog.infoLog(constants.Log.Info.STORE_FILE_EXIT, name, document.documentType, path)
    }
      ).recover {
      case baseException: BaseException => utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.FILE_UPLOAD_ERROR, baseException)
      case s3Exception: S3Exception => utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.S3_EXCEPTION, s3Exception)
      case e: Exception => utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION, e)
    }
  }

  def updateFile[T <: Document[T]](name: String, path: String, oldDocument: T, updateOldDocument: T => Future[Int]): Future[Unit] = {
    val getFileNameAndEncodedBase64: Future[(String, Option[Array[Byte]])] = Future {
      utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG |
             constants.File.JPEG_LOWER_CASE | constants.File.JPG_LOWER_CASE | constants.File.PNG_LOWER_CASE
        => utilities.ImageProcess.convertToThumbnail(name, path)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(path, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
    }

    def update(fileName: String, encodedBase64: Option[Array[Byte]]): Future[Int] = updateOldDocument(oldDocument.updateFileName(fileName).updateFile(encodedBase64).updateStatus(None))

    def storeFileInS3BucketAndDelete(fileName: String) = {
      if (s3BucketInUse) {
        def charset(): HttpCharset = HttpCharsets.`US-ASCII`

        def getContentType(getCharset: () => HttpCharset) = ContentType(MediaTypes.forExtension(utilities.FileOperations.fileExtensionFromName(fileName)), getCharset)

        val add = FileIO.fromPath(new File(getNegotiationFilePath(oldDocument.documentType) + fileName).toPath).runWith(S3.multipartUpload(s3Bucket, fileName, getContentType(charset)).withAttributes(s3Attributes))
        for {
          _ <- add
        } yield {
          S3.deleteObject(s3Bucket, oldDocument.fileName).withAttributes(s3Attributes).runForeach(x => Unit)
          utilities.FileOperations.deleteFile(path, fileName)
        }
      } else {
        Future(true)
      }
    }

    (for {
      (fileName, encodedBase64) <- getFileNameAndEncodedBase64
      _ <- update(fileName, encodedBase64)
      _ <- Future(utilities.FileOperations.renameFile(path, name, fileName))
      _ <- storeFileInS3BucketAndDelete(fileName)
    } yield ()).recover {
      case baseException: BaseException => utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.FILE_UPLOAD_ERROR, baseException)
      case s3Exception: S3Exception => utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.S3_EXCEPTION, s3Exception)
      case e: Exception => utilities.FileOperations.deleteFile(path, name)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION, e)
    }
  }

  def getFile(fileName: String): Future[(Source[ByteString, NotUsed], ObjectMetadata)] = S3.download(s3Bucket, fileName).withAttributes(s3Attributes).runWith(Sink.head).map(_.getOrElse(throw new BaseException(constants.Response.FILE_NOT_FOUND_EXCEPTION)))
    .recover {
      case s3Exception: S3Exception => throw new BaseException(constants.Response.S3_EXCEPTION, s3Exception)
      case e: Exception => throw new BaseException(constants.Response.GENERIC_EXCEPTION, e)
    }
}