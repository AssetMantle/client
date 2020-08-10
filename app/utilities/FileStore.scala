package utilities

import java.io.{FileNotFoundException, IOException, RandomAccessFile}
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Document
import org.apache.commons.codec.binary.Base64
import play.api.{Configuration, Logger}
import views.companion.master.FileUpload.FileUploadInfo
import play.api.http.DefaultFileMimeTypes

import scala.concurrent.{ExecutionContext, Future}
import fly.play.s3.{BucketFile, BucketFilePart, BucketFilePartUploadTicket, BucketFileUploadTicket, S3}
import play.api.libs.ws.WSClient

@Singleton
class FileStore @Inject()(wsClient: WSClient, defaultFileMimeTypes: DefaultFileMimeTypes)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = "FILE_STORE"

  private val uploadedParts: ConcurrentMap[String, Set[FileUploadInfo]] = new ConcurrentHashMap(8, 0.9f, 1)

  private val s3IntiateUploadTicket: ConcurrentMap[String, BucketFileUploadTicket] = new ConcurrentHashMap(8, 0.9f, 1)

  private val uploadPartsTicket: ConcurrentMap[String, Set[BucketFilePartUploadTicket]] = new ConcurrentHashMap(8, 0.9f, 1)

  private implicit val logger: Logger = Logger(this.getClass)

  def savePartialFile(filePart: Array[Byte], fileInfo: FileUploadInfo, uploadPath: String) {
    try {
      try {
        println("max number of chunks--"+math.ceil(fileInfo.resumableTotalSize.toDouble / fileInfo.resumableChunkSize.toDouble).toInt)
        println("s3IntiateUploadTicket-----" + s3IntiateUploadTicket.get(fileInfo.resumableFilename).toString)
        println("uploadPartsTicket------" + uploadPartsTicket.get(fileInfo.resumableFilename).toString)
      } catch {
        case e: Exception => logger.error(e.getMessage+"asdzxc")
      }
      val s3 = S3.fromConfiguration(wsClient, configuration)
      val bucket = s3.getBucket("testing-pers")

      if (fileInfo.resumableChunkNumber == 1) {
        bucket.initiateMultipartUpload(BucketFile(fileInfo.resumableFilename, defaultFileMimeTypes.forFileName(fileInfo.resumableFilename).getOrElse(""))).map { ticket =>
          println(" upload intiated-----" + ticket.toString)
          s3IntiateUploadTicket.put(fileInfo.resumableFilename, ticket)
          bucket.uploadPart(ticket, BucketFilePart(fileInfo.resumableChunkNumber, filePart)).map { partUploadTicket =>
            println("first part uploaded--" + partUploadTicket)
            uploadPartsTicket.put(fileInfo.resumableFilename, Set(partUploadTicket))
            bucket.completeMultipartUpload(s3IntiateUploadTicket.get(fileInfo.resumableFilename), uploadPartsTicket.get(fileInfo.resumableFilename).toSeq).map{case x=>
              println("file uploaded")
            }.recover{
              case e:Exception=>logger.error(e.getMessage,e)
            }
          }
        }
      } else if (fileInfo.resumableChunkNumber == math.ceil(fileInfo.resumableTotalSize.toDouble / fileInfo.resumableChunkSize.toDouble).toInt) {
        while (!s3IntiateUploadTicket.containsKey(fileInfo.resumableFilename)) {
          println("s3IntiateUploadTicket done not contain Key yet222---"+fileInfo.resumableChunkNumber)
          Thread.sleep(100)
        }
        bucket.uploadPart(s3IntiateUploadTicket.get(fileInfo.resumableFilename), BucketFilePart(fileInfo.resumableChunkNumber, filePart)).map { partUploadTicket =>
          println("part Uploading--" + fileInfo.resumableChunkNumber)
          while (!uploadPartsTicket.containsKey(fileInfo.resumableFilename)) {
            println("uploadPartsTicket done not contain Key yet2222---"+fileInfo.resumableChunkNumber)
            Thread.sleep(100)
          }
          val partUploadTickets = uploadPartsTicket.get(fileInfo.resumableFilename)
          uploadPartsTicket.put(fileInfo.resumableFilename, partUploadTickets + partUploadTicket)
          println("beforeComplete---"+uploadPartsTicket.get(fileInfo.resumableFilename).toSeq.toString())
          while(uploadPartsTicket.get(fileInfo.resumableFilename).toSeq.length !=  math.ceil(fileInfo.resumableTotalSize.toDouble / fileInfo.resumableChunkSize.toDouble).toInt){
            Thread.sleep(100)
          }
            println("almostComplete---"+uploadPartsTicket.get(fileInfo.resumableFilename).toSeq.toString())
            println("almostComplete22---"+s3IntiateUploadTicket.get(fileInfo.resumableFilename))
            println("completing upload---")
            bucket.completeMultipartUpload(s3IntiateUploadTicket.get(fileInfo.resumableFilename), uploadPartsTicket.get(fileInfo.resumableFilename).toSeq).map{case x=>
            println("file uploaded")
            }.recover{
              case e:Exception=>logger.error(e.getMessage,e)
            }
        }
      } else {
        while (!s3IntiateUploadTicket.containsKey(fileInfo.resumableFilename)) {
          println("s3IntiateUploadTicket done not contain Key yet----"+fileInfo.resumableChunkNumber)
          Thread.sleep(100)
        }
        bucket.uploadPart(s3IntiateUploadTicket.get(fileInfo.resumableFilename), BucketFilePart(fileInfo.resumableChunkNumber, filePart)).map { partUploadTicket =>
          println("part Uploading--" + fileInfo.resumableChunkNumber)
          while (!uploadPartsTicket.containsKey(fileInfo.resumableFilename)) {
            println("uploadPartsTicket done not contain Key yet----"+fileInfo.resumableChunkNumber)
            Thread.sleep(100)
          }
          val partUploadTickets = uploadPartsTicket.get(fileInfo.resumableFilename)
          uploadPartsTicket.put(fileInfo.resumableFilename, partUploadTickets + partUploadTicket)
        }
      }

      /*if (s3IntiateUploadTicket.containsKey(fileInfo.resumableFilename)) {
        println("s3IntiateUploadTicket.containsKey")
        println(s3IntiateUploadTicket.get(fileInfo.resumableFilename).toString)
        while (!uploadPartsTicket.containsKey(fileInfo.resumableFilename)) {
          println("uploadPartsTicket done not contain Key yet")
          Thread.sleep(100)
        }
        bucket.uploadPart(s3IntiateUploadTicket.get(fileInfo.resumableFilename), BucketFilePart(fileInfo.resumableChunkNumber, filePart)).map { partUploadTicket =>
          println("part Uploading--"+fileInfo.resumableChunkNumber)
          val partUploadTickets = uploadPartsTicket.get(fileInfo.resumableFilename)
          uploadPartsTicket.put(fileInfo.resumableFilename, partUploadTickets + partUploadTicket)
        }
        if (fileInfo.resumableChunkNumber == math.ceil(fileInfo.resumableTotalSize.toDouble / fileInfo.resumableChunkSize.toDouble).toInt) {
          println("completing Upload--" + fileInfo.resumableChunkNumber)
          bucket.completeMultipartUpload(s3IntiateUploadTicket.get(fileInfo.resumableFilename), uploadPartsTicket.get(fileInfo.resumableFilename).toSeq)
        }
        /*if (uploadPartsTicket.containsKey(fileInfo.resumableFilename)) {
          val partsUploaded = uploadPartsTicket.get(fileInfo.resumableFilename)
          uploadPartsTicket.put(fileInfo.resumableFilename, partsUploaded + "ticket")
        } else {
          uploadPartsTicket.put(fileInfo.resumableFilename, Set("tickets"))
        }*/
      } else {
        println("intiating upload")
        bucket.initiateMultipartUpload(BucketFile(fileInfo.resumableFilename, defaultFileMimeTypes.forFileName(fileInfo.resumableFilename).getOrElse(""))).map { ticket =>
          println(" upload intiated-----"+ticket.toString)
          s3IntiateUploadTicket.put(fileInfo.resumableFilename, ticket)
          bucket.uploadPart(ticket, BucketFilePart(fileInfo.resumableChunkNumber, filePart)).map { partUploadTicket =>
            println("first part uploaded--"+partUploadTicket)
            uploadPartsTicket.put(fileInfo.resumableFilename, Set(partUploadTicket))
          }
        }
      }*/

      // bucket.com
      /* s3.
       bucket.initiateMultipartUpload(BucketFile("name",))
       bucket.uploadPart()
       bucket + (BucketFile())
       bucket.*/
      /*val fullFileName = uploadPath + fileInfo.resumableFilename
      val partialFile = new RandomAccessFile(fullFileName, "rw")
      try {
        partialFile.seek((fileInfo.resumableChunkNumber - 1) * fileInfo.resumableChunkSize.toLong)
        partialFile.write(filePart, 0, filePart.length)
      } catch {
        case baseException: BaseException => throw baseException
        case ioException: IOException => logger.error(ioException.getMessage)
          throw new BaseException(constants.Response.I_O_EXCEPTION)
        case e: Exception => logger.error(e.getMessage)
          throw new BaseException(constants.Response.GENERIC_EXCEPTION)
      } finally {
        partialFile.close()
      }
      if (uploadedParts.containsKey(fullFileName)) {
        val partsUploaded = uploadedParts.get(fullFileName)
        uploadedParts.put(fullFileName, partsUploaded + fileInfo)
      } else {
        uploadedParts.put(fullFileName, Set(fileInfo))
      }*/
     // println("filePartAdded-------" + fileInfo)
    } catch {
      case baseException: BaseException => throw baseException
      case illegalArgumentException: IllegalArgumentException => logger.error(illegalArgumentException.getMessage)
        throw new BaseException(constants.Response.FILE_ILLEGAL_ARGUMENT_EXCEPTION)
      case fileNotFoundException: FileNotFoundException => logger.error(fileNotFoundException.getMessage)
        throw new BaseException(constants.Response.FILE_NOT_FOUND_EXCEPTION)
      case securityException: SecurityException => logger.error(securityException.getMessage)
        throw new BaseException(constants.Response.FILE_SECURITY_EXCEPTION)
      case ioException: IOException => logger.error(ioException.getMessage)
        throw new BaseException(constants.Response.I_O_EXCEPTION)
      case nullPointerException: NullPointerException => logger.error(nullPointerException.getMessage)
        throw new BaseException(constants.Response.NULL_POINTER_EXCEPTION)
      case classCastException: ClassCastException => logger.error(classCastException.getMessage)
        throw new BaseException(constants.Response.CLASS_CAST_EXCEPTION)
      case unsupportedOperationException: UnsupportedOperationException => logger.error(unsupportedOperationException.getMessage)
        throw new BaseException(constants.Response.FILE_UNSUPPORTED_OPERATION_EXCEPTION)
      case e: Exception => logger.error(e.getMessage)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

}