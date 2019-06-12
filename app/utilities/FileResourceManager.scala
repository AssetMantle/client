package utilities

import java.io.{File, FileInputStream, IOException}

import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import exceptions.BaseException
import play.api.Logger

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


object FileResourceManager {
  private implicit val module: String = constants.Module.FILE_RESOURCE_MANAGER
  private val logger: Logger = Logger(this.getClass)

  def fileExtensionFromName(fileName: String): String = {
    if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) fileName.substring(fileName.lastIndexOf(".") + 1)
    else ""
  }

  def removeSpacesFromName(name: String): String = {
    try {
      name.replaceAll("\\s", "")
    } catch {
      case nullPointerException: NullPointerException => logger.error(constants.Response.NULL_POINTER_EXCEPTION.message, nullPointerException)
        throw new BaseException(constants.Response.NULL_POINTER_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def hashExtractor(hashedName: String): String = {
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

  def renameFile(directory: String, currentName: String, newName: String): Boolean = newFile(directory, currentName).renameTo(newFile(directory, newName))

  def newFile(directoryName: String, fileName: String): File = {
    try {
      new File(directoryName, fileName)
    } catch {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

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

  def deleteFile(directoryName: String, name: String): Boolean = {
    try {
      newFile(directoryName, name).delete()
    } catch {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def convertFileToBase64(file: File): Array[Byte] = {
    try {
      val fileInputStreamReader = new FileInputStream(file)
      val bytes = new Array[Byte](file.length.asInstanceOf[Int])
      fileInputStreamReader.read(bytes)
      fileInputStreamReader.close()
      java.util.Base64.getEncoder.encode(bytes)
    } catch {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case ioException: IOException => logger.info(constants.Response.I_O_EXCEPTION.message, ioException)
        throw new BaseException(constants.Response.I_O_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def convertToByteArray(file: File): Array[Byte] = {
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

}
