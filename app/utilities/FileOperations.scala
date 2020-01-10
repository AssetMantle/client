package utilities

import java.io.{File, FileInputStream, IOException, RandomAccessFile}
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import exceptions.BaseException
import models.Trait.Document
import models.common.Serializable._
import play.api.Logger
import play.api.libs.json.Json
import views.companion.master.FileUpload.FileUploadInfo

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object FileOperations {

  private implicit val module: String = constants.Module.FILE_OPERATIONS

  private val uploadedParts: ConcurrentMap[String, Set[FileUploadInfo]] = new ConcurrentHashMap(8, 0.9f, 1)

  private val logger: Logger = Logger(this.getClass)

  def savePartialFile(filePart: Array[Byte], fileInfo: FileUploadInfo, uploadPath: String) {
    try {
      val fullFileName = uploadPath + fileInfo.resumableFilename
      val partialFile = new RandomAccessFile(fullFileName, "rw")
      try {
        partialFile.seek((fileInfo.resumableChunkNumber - 1) * fileInfo.resumableChunkSize.toLong)
        partialFile.write(filePart, 0, filePart.length)
      } catch {
        case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
          throw new BaseException(constants.Response.GENERIC_EXCEPTION)
      } finally {
        partialFile.close()
      }
      if (uploadedParts.containsKey(fullFileName)) {
        val partsUploaded = uploadedParts.get(fullFileName)
        uploadedParts.put(fullFileName, partsUploaded + fileInfo)
      } else {
        uploadedParts.put(fullFileName, Set(fileInfo))
      }
    } catch {
      case _: BaseException => throw new BaseException(constants.Response.GENERIC_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def fileExtensionFromName(fileName: String): String = {
    if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) fileName.substring(fileName.lastIndexOf(".") + 1)
    else ""
  }

  def renameFile(directory: String, currentName: String, newName: String)(implicit executionContext: ExecutionContext): Boolean = newFile(directory, currentName).renameTo(newFile(directory, newName))

  def fileStreamer(file: File, directoryName: String, fileName: String)(implicit executionContext: ExecutionContext): Source[ByteString, _] = {
    val source: Source[ByteString, _] = FileIO.fromPath(file.toPath)
      .watchTermination()((_, downloadDone) => downloadDone.onComplete {
        case Success(_)
        => deleteFile(directoryName, fileName)
        case Failure(t) => logger.info(t.getMessage, t)
          deleteFile(directoryName, fileName)
      })
    source
  }

  def deleteFile(directoryName: String, name: String)(implicit executionContext: ExecutionContext): Boolean = {
    try {
      newFile(directoryName, name).delete()
    } catch {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def moveFile(fileName: String, oldPath: String, newPath: String)(implicit executionContext: ExecutionContext): Boolean = newFile(directoryName = oldPath, fileName = fileName).renameTo(newFile(directoryName = newPath, fileName = fileName))

  def newFile(directoryName: String, fileName: String)(implicit executionContext: ExecutionContext): File = {
    try {
      new File(directoryName, fileName)
    } catch {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def convertToByteArray(file: File)(implicit executionContext: ExecutionContext): Array[Byte] = {
    try {
      val fileInputStreamReader = new FileInputStream(file)
      val bytes = new Array[Byte](file.length.asInstanceOf[Int])
      fileInputStreamReader.read(bytes)
      fileInputStreamReader.close()
      bytes
    } catch {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case ioException: IOException => logger.info(constants.Response.I_O_EXCEPTION.message, ioException)
        throw new BaseException(constants.Response.I_O_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def fetchFile(path: String, fileName: String): File = {
    val file = new java.io.File(path + fileName)
    if (!file.exists) throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION) else file
  }

  def combinedHash(documents: Seq[Document[_]])(implicit executionContext: ExecutionContext): String = {
    Json.toJson(documents.map { doc =>
      DocumentBlockchainDetails(doc.documentType, hashExtractor(doc.fileName))
    }).toString()
  }

  def hashExtractor(hashedName: String)(implicit executionContext: ExecutionContext): String = hashedName.split("""\.""")(0)
}