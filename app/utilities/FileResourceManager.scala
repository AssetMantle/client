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
import akka.stream.alpakka.s3.{ForwardProxy, ObjectMetadata, S3Attributes, S3Exception, S3Ext}
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

  private val uploadAccountKYCIdentificationPath = configuration.get[String]("upload.account.identificationPath")

  private val uploadAccountProfilePicturePath = configuration.get[String]("upload.account.profilePicturePath")

  private val uploadZoneKYCBankAccountDetailPath = configuration.get[String]("upload.zone.bankAccountDetailPath")

  private val uploadZoneKYCIdentificationPath = configuration.get[String]("upload.zone.identificationPath")

  private val uploadOrganizationKYCACRAPath = configuration.get[String]("upload.organization.acraPath")

  private val uploadOrganizationKYCBoardResolutionPath = configuration.get[String]("upload.organization.boardResolutionPath")

  private val uploadAssetBillOfLadingPath: String = configuration.get[String]("upload.asset.billOfLading")

  private val uploadAssetCOOPath: String = configuration.get[String]("upload.asset.coo")

  private val uploadAssetCOAPath: String = configuration.get[String]("upload.asset.coa")

  private val uploadNegotiationInvoicePath: String = configuration.get[String]("upload.negotiation.invoice")

  private val uploadNegotiationBillOfExchangePath: String = configuration.get[String]("upload.negotiation.billOfExchange")

  private val uploadNegotiationContractPath: String = configuration.get[String]("upload.negotiation.contract")

  private val uploadNegotiationInsurancePath: String = configuration.get[String]("upload.negotiation.insurance")

  private val uploadNegotiationOthersPath: String = configuration.get[String]("upload.negotiation.others")

  private val s3BucketInUse = configuration.get[Boolean]("s3.useBucket")

  private val tempFilePath: String = rootFilePath + configuration.get[String]("upload.temp")

  private val s3region: String = configuration.get[String]("s3.region")

  private val s3Bucket: String = configuration.get[String]("s3.bucket")

  private val awsCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create("AKIAYHKHYDI475LOOMUF", "rzcK6xQ31zVqB2xXUME0070pvmw97wjllcfs9XJt"))

  private val s3RegionProvider = new AwsRegionProvider {
    override def getRegion: Region = Region.of(s3region)
  }

  private val s3Settings = S3Ext(actorSystem).settings
    .withCredentialsProvider(awsCredentialsProvider)
    .withListBucketApiVersion(ListBucketVersion2)
    .withS3RegionProvider(s3RegionProvider)

  private val s3Attributes = S3Attributes.settings(s3Settings)

  def getAccountKYCFilePath(documentType: String, tempPathRequired: Boolean = false): String = {
    if (s3BucketInUse && tempPathRequired) {
      tempFilePath
    } else {
      val basePath = documentType match {
        case constants.File.AccountKYC.IDENTIFICATION => uploadAccountKYCIdentificationPath
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }
      if (s3BucketInUse) basePath else rootFilePath + basePath
    }
  }

  def getZoneKYCFilePath(documentType: String, tempPathRequired: Boolean = false): String = {
    if (s3BucketInUse && tempPathRequired) {
      tempFilePath
    } else {
      val basePath = documentType match {
        case constants.File.ZoneKYC.BANK_ACCOUNT_DETAIL => uploadZoneKYCBankAccountDetailPath
        case constants.File.ZoneKYC.IDENTIFICATION => uploadZoneKYCIdentificationPath
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }
      if (s3BucketInUse) basePath else rootFilePath + basePath
    }
  }

  def getOrganizationKYCFilePath(documentType: String, tempPathRequired: Boolean = false): String = {
    if (s3BucketInUse && tempPathRequired) {
      tempFilePath
    } else {
      val basePath = documentType match {
        case constants.File.OrganizationKYC.ACRA => uploadOrganizationKYCACRAPath
        case constants.File.OrganizationKYC.BOARD_RESOLUTION => uploadOrganizationKYCBoardResolutionPath
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }
      if (s3BucketInUse) basePath else rootFilePath + basePath
    }
  }

  def getAssetFilePath(documentType: String, tempPathRequired: Boolean = false): String = {
    if (s3BucketInUse && tempPathRequired) {
      tempFilePath
    } else {
      val basePath = documentType match {
        case constants.File.Asset.BILL_OF_LADING => uploadAssetBillOfLadingPath
        case constants.File.Asset.COO => uploadAssetCOOPath
        case constants.File.Asset.COA => uploadAssetCOAPath
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }
      if (s3BucketInUse) basePath else rootFilePath + basePath
    }
  }

  def getNegotiationFilePath(documentType: String, tempPathRequired: Boolean = false): String = {
    if (s3BucketInUse && tempPathRequired) {
      tempFilePath
    } else {
      val basePath = documentType match {
        case constants.File.Negotiation.INVOICE => uploadNegotiationInvoicePath
        case constants.File.Negotiation.BILL_OF_EXCHANGE => uploadNegotiationBillOfExchangePath
        case constants.File.Negotiation.CONTRACT => uploadNegotiationContractPath
        case constants.File.Negotiation.INSURANCE => uploadNegotiationInsurancePath
        case constants.File.Negotiation.OTHERS => uploadNegotiationOthersPath
        case _ => uploadNegotiationOthersPath
      }
      if (s3BucketInUse) basePath else rootFilePath + basePath
    }
  }

  def getAccountFilePath(documentType: String, tempPathRequired: Boolean = false): String = {
    if (s3BucketInUse && tempPathRequired) {
      tempFilePath
    } else {
      val basePath = documentType match {
        case constants.File.Account.PROFILE_PICTURE => uploadAccountProfilePicturePath
        case _ => throw new BaseException(constants.Response.NO_SUCH_DOCUMENT_TYPE_EXCEPTION)
      }
      if (s3BucketInUse) basePath else rootFilePath + basePath
    }
  }

  def storeFile[T <: Document[T]](name: String, path: String, document: T, masterCreate: T => Future[String]): Future[Unit] = {
    utilitiesLog.infoLog(constants.Log.Info.STORE_FILE_ENTRY, name, document.documentType, path)
    val currentFilePath = if (s3BucketInUse) tempFilePath else path
    val getFileNameAndEncodedBase64: Future[(String, Option[Array[Byte]])] = Future {
      utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG |
             constants.File.JPEG_LOWER_CASE | constants.File.JPG_LOWER_CASE | constants.File.PNG_LOWER_CASE
        => utilities.ImageProcess.convertToThumbnail(name, currentFilePath)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(currentFilePath, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
    }

    def updateAndCreateFile(fileName: String, encodedBase64: Option[Array[Byte]]): Future[String] = masterCreate(document.updateFileName(fileName).updateFile(encodedBase64))

    def storeFileInS3BucketAndDelete(fileName: String) = {
      if (s3BucketInUse) {
        def charset(): HttpCharset = HttpCharsets.`US-ASCII`

        def getContentType(getCharset: () => HttpCharset) = ContentType(MediaTypes.forExtension(utilities.FileOperations.fileExtensionFromName(fileName)), getCharset)

        //val add = FileIO.fromPath(new File(currentFilePath + fileName).toPath).runWith(S3.multipartUpload(s3Bucket, path + fileName, getContentType(charset)).withAttributes(s3Attributes))
        for {
          _ <- Future(0)
        } yield utilities.FileOperations.deleteFile(currentFilePath, fileName)
      } else {
        Future(true)
      }
    }

    (for {
      (fileName, encodedBase64) <- getFileNameAndEncodedBase64
      _ <- updateAndCreateFile(fileName, encodedBase64)
      _ <- Future(utilities.FileOperations.renameFile(currentFilePath, name, fileName))
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
    val currentFilePath = if (s3BucketInUse) tempFilePath else path
    val getFileNameAndEncodedBase64: Future[(String, Option[Array[Byte]])] = Future {
      utilities.FileOperations.fileExtensionFromName(name) match {
        case constants.File.JPEG | constants.File.JPG | constants.File.PNG |
             constants.File.JPEG_LOWER_CASE | constants.File.JPG_LOWER_CASE | constants.File.PNG_LOWER_CASE
        => utilities.ImageProcess.convertToThumbnail(name, currentFilePath)
        case _ => (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(utilities.FileOperations.convertToByteArray(utilities.FileOperations.newFile(currentFilePath, name)))).toString, utilities.FileOperations.fileExtensionFromName(name)).mkString("."), None)
      }
    }

    def update(fileName: String, encodedBase64: Option[Array[Byte]]): Future[Int] = updateOldDocument(oldDocument.updateFileName(fileName).updateFile(encodedBase64).updateStatus(None))

    def storeFileInS3BucketAndDelete(fileName: String) = {
      if (s3BucketInUse) {
        def charset(): HttpCharset = HttpCharsets.`US-ASCII`

        def getContentType(getCharset: () => HttpCharset) = ContentType(MediaTypes.forExtension(utilities.FileOperations.fileExtensionFromName(fileName)), getCharset)

        //val add = FileIO.fromPath(new File(currentFilePath + fileName).toPath).runWith(S3.multipartUpload(s3Bucket, path + fileName, getContentType(charset)).withAttributes(s3Attributes))
        for {
          _ <- Future(0)
        } yield {
          //S3.deleteObject(s3Bucket, path + oldDocument.fileName).withAttributes(s3Attributes).runForeach(x => Unit)
          utilities.FileOperations.deleteFile(currentFilePath, fileName)
        }
      } else {
        Future(true)
      }
    }

    (for {
      (fileName, encodedBase64) <- getFileNameAndEncodedBase64
      _ <- update(fileName, encodedBase64)
      _ <- Future(utilities.FileOperations.renameFile(currentFilePath, name, fileName))
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

  def getFile(fileName: String, path: String) = {
    S3.download(s3Bucket, path + fileName).withAttributes(s3Attributes).runWith(Sink.head).map(_.getOrElse(throw new BaseException(constants.Response.FILE_NOT_FOUND_EXCEPTION)))
      .recover {
        case baseException: BaseException => throw baseException
        case s3Exception: S3Exception => throw new BaseException(constants.Response.S3_EXCEPTION, s3Exception)
        case e: Exception => throw new BaseException(constants.Response.GENERIC_EXCEPTION, e)
      }
  }
}