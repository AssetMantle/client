package utilities

import java.io.{File, FileInputStream, IOException, RandomAccessFile}
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import exceptions.BaseException
import play.api.Logger
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

  def removeSpacesFromName(name: String)(implicit exec: ExecutionContext): String = {
    try {
      name.replaceAll("\\s", "")
    } catch {
      case nullPointerException: NullPointerException => logger.error(constants.Response.NULL_POINTER_EXCEPTION.message, nullPointerException)
        throw new BaseException(constants.Response.NULL_POINTER_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def hashExtractor(hashedName: String)(implicit exec: ExecutionContext): String = {
    try {
      hashedName.split("""\.""")(0)
    }
    catch {
      case nullPointerException: NullPointerException => logger.error(constants.Response.NULL_POINTER_EXCEPTION.message, nullPointerException)
        throw new BaseException(constants.Response.NULL_POINTER_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def renameFile(directory: String, currentName: String, newName: String)(implicit exec: ExecutionContext): Boolean = newFile(directory, currentName).renameTo(newFile(directory, newName))

  def newFile(directoryName: String, fileName: String)(implicit exec: ExecutionContext): File = {
    try {
      new File(directoryName, fileName)
    } catch {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def fileStreamer(file: File, directoryName: String, fileName: String)(implicit exec: ExecutionContext): Source[ByteString, _] = {
    val source: Source[ByteString, _] = FileIO.fromPath(file.toPath)
      .watchTermination()((_, downloadDone) => downloadDone.onComplete {
        case Success(_)
        => deleteFile(directoryName, fileName)
        case Failure(t) => logger.info(t.getMessage, t)
          deleteFile(directoryName, fileName)
      })
    source
  }

  def deleteFile(directoryName: String, name: String)(implicit exec: ExecutionContext): Boolean = {
    try {
      newFile(directoryName, name).delete()
    } catch {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def moveFile(fileName: String, oldPath: String, newPath: String)(implicit exec: ExecutionContext): Boolean = newFile(directoryName = oldPath, fileName = fileName).renameTo(newFile(directoryName = newPath, fileName = fileName))

  def convertToByteArray(file: File)(implicit exec: ExecutionContext): Array[Byte] = {
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
    if (!file.exists()) throw new BaseException(constants.Response.NO_SUCH_FILE_EXCEPTION) else file
  }

}