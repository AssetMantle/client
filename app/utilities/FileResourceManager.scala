package utilities

import java.io.{File, FileInputStream, IOException}

import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import exceptions.BaseException
import javax.inject.Inject
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class FileResourceManager @Inject()()(implicit exec: ExecutionContext, configuration: Configuration){

  private implicit val module: String = constants.Module.FILE_RESOURCE_MANAGER

  private val logger: Logger = Logger(this.getClass)

  private val uploadAccountKycBankDetailsPath = configuration.get[String]("upload.accountKYCsBankDetailsPath")

  private val uploadAccountKycIdentificationPath = configuration.get[String]("upload.accountKYCsIdentificationPath")

  private val uploadZoneKycBankDetailsPath = configuration.get[String]("upload.zoneKYCsBankDetailsPath")

  private val uploadZoneKycIdentificationPath = configuration.get[String]("upload.zoneKYCsIdentificationPath")

  private val uploadOrganizationKycBankDetailsPath = configuration.get[String]("upload.organizationKYCsBankDetailsPath")

  private val uploadOrganizationKycIdentificationPath = configuration.get[String]("upload.organizationKYCsIdentificationPath")

  def getAccountFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_DETAILS => uploadAccountKycBankDetailsPath
      case constants.File.IDENTIFICATION => uploadAccountKycIdentificationPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getZoneFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_DETAILS => uploadZoneKycBankDetailsPath
      case constants.File.IDENTIFICATION => uploadZoneKycIdentificationPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

  def getOrganizationFilePath(documentType: String): String = {
    documentType match {
      case constants.File.BANK_DETAILS => uploadOrganizationKycBankDetailsPath
      case constants.File.IDENTIFICATION => uploadOrganizationKycIdentificationPath
      case _ => constants.File.UNKNOWN_TYPE
    }
  }

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

  def fileStreamer(file: File, directoryName: String, fileName: String): Source[ByteString, _] = {
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

  def moveFile(fileName: String, oldPath: String, newPath: String): Boolean = newFile(directoryName = oldPath, fileName = fileName).renameTo(newFile(directoryName = newPath, fileName = fileName))

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
