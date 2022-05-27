
package utilities

import com.sksamuel.scrimage
import com.sksamuel.scrimage.nio.JpegWriter
import exceptions.BaseException
import play.api.Logger

import java.util.Base64
import javax.imageio.ImageIO
import scala.concurrent.ExecutionContext

object ImageProcess {

  private implicit val module: String = constants.Module.IMAGE_PROCESS

  private implicit val logger: Logger = Logger(this.getClass)

  def convertToThumbnail(name: String, uploadPath: String)(implicit executionContext: ExecutionContext): (String, Option[Array[Byte]]) = {
    try {
      val imageRes = ImageIO.read(FileOperations.newFile(uploadPath, name))
      val writer: JpegWriter = JpegWriter.compression(100)
      val bytes = FileOperations.convertToByteArray(scrimage.ImmutableImage.loader().fromFile(FileOperations.newFile(uploadPath, name)).fit(180, (180 * imageRes.getHeight) / imageRes.getWidth).output(writer, FileOperations.newFile(uploadPath, '~' + name)))
      FileOperations.deleteFile(uploadPath, '~' + name)
      (List(util.hashing.MurmurHash3.stringHash(Base64.getEncoder.encodeToString(bytes)), FileOperations.fileExtensionFromName(name)).mkString("."), Option(Base64.getEncoder.encode(bytes)))
    } catch {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def decodeImageThumbnailData(data: Array[Byte]): String = Base64.getEncoder.encodeToString(Base64.getDecoder.decode(data))

}