package utilities

import java.io.{File, FileInputStream, FileNotFoundException, IOException, RandomAccessFile}
import java.nio.file.InvalidPathException
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import exceptions.BaseException
import models.Trait.Document
import play.api.Logger
import views.companion.master.FileUpload.FileUploadInfo

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object FileOperations {

  private implicit val module: String = constants.Module.FILE_OPERATIONS

  private val uploadedParts: ConcurrentMap[String, Set[FileUploadInfo]] = new ConcurrentHashMap(8, 0.9f, 1)

  private implicit val logger: Logger = Logger(this.getClass)

  def savePartialFile(filePart: Array[Byte], fileInfo: FileUploadInfo, uploadPath: String) {
    try {
      val fullFileName = uploadPath + fileInfo.resumableFilename
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
      }
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

  def fileExtensionFromName(fileName: String): String = {
    if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) fileName.substring(fileName.lastIndexOf(".") + 1)
    else ""
  }

  def renameFile(directory: String, currentName: String, newName: String)(implicit executionContext: ExecutionContext): Boolean =
    try {
      newFile(directory, currentName).renameTo(newFile(directory, newName))
    } catch {
      case baseException: BaseException => throw baseException
      case securityException: SecurityException => logger.error(securityException.getMessage)
        throw new BaseException(constants.Response.FILE_SECURITY_EXCEPTION)
      case nullPointerException: NullPointerException => logger.error(nullPointerException.getMessage)
        throw new BaseException(constants.Response.NULL_POINTER_EXCEPTION)
      case ioException: IOException => logger.error(ioException.getMessage)
        throw new BaseException(constants.Response.I_O_EXCEPTION)
      case e: Exception => logger.error(e.getMessage)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }

  def fileStreamer(file: File, directoryName: String, fileName: String)(implicit executionContext: ExecutionContext): Source[ByteString, _] =
    try {
      val source: Source[ByteString, _] = FileIO.fromPath(file.toPath)
        .watchTermination()((_, downloadDone) => downloadDone.onComplete {
          case Success(_) => deleteFile(directoryName, fileName)
          case Failure(t) => logger.error(t.getMessage, t)
            deleteFile(directoryName, fileName)
        })
      source
    } catch {
      case baseException: BaseException => throw baseException
      case invalidPathException: InvalidPathException => logger.error(invalidPathException.getMessage)
        throw new BaseException(constants.Response.INVALID_FILE_PATH_EXCEPTION)
      case securityException: SecurityException => logger.error(securityException.getMessage)
        throw new BaseException(constants.Response.FILE_SECURITY_EXCEPTION)
      case nullPointerException: NullPointerException => logger.error(nullPointerException.getMessage)
        throw new BaseException(constants.Response.NULL_POINTER_EXCEPTION)
      case ioException: IOException => logger.error(ioException.getMessage)
        throw new BaseException(constants.Response.I_O_EXCEPTION)
      case e: Exception => logger.error(e.getMessage)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }

  def deleteFile(directoryName: String, name: String)(implicit executionContext: ExecutionContext): Boolean = {
    try {
      newFile(directoryName, name).delete()
    } catch {
      case baseException: BaseException => throw baseException
      case ioException: IOException => logger.error(ioException.getMessage)
        throw new BaseException(constants.Response.I_O_EXCEPTION)
      case securityException: SecurityException => logger.error(securityException.getMessage)
        throw new BaseException(constants.Response.FILE_SECURITY_EXCEPTION)
      case e: Exception => logger.error(e.getMessage)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def moveFile(fileName: String, oldPath: String, newPath: String)(implicit executionContext: ExecutionContext): Boolean =
    try {
      newFile(directoryName = oldPath, fileName = fileName).renameTo(newFile(directoryName = newPath, fileName = fileName))
    } catch {
      case baseException: BaseException => throw baseException
      case ioException: IOException => logger.error(ioException.getMessage)
        throw new BaseException(constants.Response.I_O_EXCEPTION)
      case securityException: SecurityException => logger.error(securityException.getMessage)
        throw new BaseException(constants.Response.FILE_SECURITY_EXCEPTION)
      case e: Exception => logger.error(e.getMessage)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }

  def newFile(directoryName: String, fileName: String)(implicit executionContext: ExecutionContext): File = {
    try {
      new   File(directoryName, fileName)
    } catch {
      case baseException: BaseException => throw baseException
      case nullPointerException: NullPointerException => logger.error(nullPointerException.getMessage)
        throw new BaseException(constants.Response.NULL_POINTER_EXCEPTION)
      case e: Exception => logger.error(e.getMessage)
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
      case baseException: BaseException => throw baseException
      case fileNotFoundException: FileNotFoundException => logger.error(fileNotFoundException.getMessage)
        throw new BaseException(constants.Response.FILE_NOT_FOUND_EXCEPTION)
      case securityException: SecurityException => logger.error(securityException.getMessage)
        throw new BaseException(constants.Response.FILE_SECURITY_EXCEPTION)
      case ioException: IOException => logger.info(constants.Response.I_O_EXCEPTION.message, ioException)
        throw new BaseException(constants.Response.I_O_EXCEPTION)
      case e: Exception => logger.error(e.getMessage)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def fetchFile(path: String, fileName: String): File = {
    val file = new java.io.File(path + fileName)
    if (!file.exists) throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION) else file
  }

  def getFileNameWithoutExtension(fileName: String): String = fileName.split("""\.""")(0)

  def getDocumentsHash(documents: Document[_]*): String = utilities.String.sha256Sum(documents.map(document => Seq(document.documentType, getFileNameWithoutExtension(document.fileName)).mkString(".")).mkString(""))
}