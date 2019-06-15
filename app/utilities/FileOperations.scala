package utilities

import java.io.RandomAccessFile
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import exceptions.BaseException
import org.apache.commons.codec.binary.Base64
import play.api.Logger
import views.companion.master.FileUpload.FileUploadInfo

object FileOperations {

  private implicit val module: String = constants.Module.FILE_OPERATIONS

  private val uploadedParts: ConcurrentMap[String, Set[FileUploadInfo]] = new ConcurrentHashMap(8, 0.9f, 1)

  private val logger: Logger = Logger(this.getClass)

  def savePartialFile(filePart: Array[Byte], fileInfo: FileUploadInfo, uploadPath: String) {
    try {
      val fullFileName = uploadPath + fileInfo.resumableFilename
      val partialFile = new RandomAccessFile(fullFileName, "rw")
      val offset = (fileInfo.resumableChunkNumber - 1) * fileInfo.resumableChunkSize.toLong

      try {
        partialFile.seek(offset)
        partialFile.write(filePart, 0, filePart.length)
      }
      finally {
        partialFile.close()
      }

      if (uploadedParts.containsKey(fullFileName)) {
        val partsUploaded = uploadedParts.get(fullFileName)
        uploadedParts.put(fullFileName, partsUploaded + fileInfo)
      } else {
        uploadedParts.put(fullFileName, Set(fileInfo))
      }
    } catch {
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def fileExtensionFromName(fileName: String): String = {
    if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) fileName.substring(fileName.lastIndexOf(".") + 1)
    else ""
  }

  def decodeImageThumbnailData(data: Array[Byte]): String = Base64.encodeBase64String(Base64.decodeBase64(data))
}