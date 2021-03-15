
package utilities

import com.sksamuel.scrimage
import com.sksamuel.scrimage.nio.JpegWriter
import exceptions.BaseException
import javax.imageio.ImageIO
import org.apache.commons.codec.binary.Base64
import org.bouncycastle.its.asn1.Signature
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

object ImageProcess {

  private implicit val module: String = constants.Module.IMAGE_PROCESS

  private implicit val logger: Logger = Logger(this.getClass)

  def convertToThumbnail(name: String, uploadPath: String)(implicit executionContext: ExecutionContext): (String, Option[Array[Byte]]) = {
    try {
      val imageRes = ImageIO.read(FileOperations.newFile(uploadPath, name))
      implicit val writer: JpegWriter = JpegWriter().withCompression(100)
      val bytes = FileOperations.convertToByteArray(scrimage.Image.fromFile(FileOperations.newFile(uploadPath, name)).fit(180, (180 * imageRes.getHeight) / imageRes.getWidth).output(FileOperations.newFile(uploadPath, '~' + name)))
      FileOperations.deleteFile(uploadPath, '~' + name)
      (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(bytes)), FileOperations.fileExtensionFromName(name)).mkString("."), Option(Base64.encodeBase64(bytes)))
    } catch {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

  def decodeImageThumbnailData(data: Array[Byte]): String = Base64.encodeBase64String(Base64.decodeBase64(data))

}