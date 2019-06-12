
package utilities

import com.sksamuel.scrimage
import com.sksamuel.scrimage.nio.JpegWriter
import exceptions.BaseException
import javax.imageio.ImageIO
import org.apache.commons.codec.binary.Base64
import play.api.Logger

object ImageUploadProcess {

  private implicit val module: String = constants.Module.IMAGE_UPLOAD_PROCESS

  private implicit val logger: Logger = Logger(this.getClass)

  def convertToThumbnail(name: String, uploadPath: String): (String, Array[Byte]) = {
    try {
      val imageRes = ImageIO.read(FileResourceManager.newFile(uploadPath, name))
      implicit val writer: JpegWriter = JpegWriter().withCompression(100)
      val bytes = FileResourceManager.convertToByteArray(scrimage.Image.fromFile(FileResourceManager.newFile(uploadPath, name)).fit(180, (180 * imageRes.getHeight) / imageRes.getWidth).output(FileResourceManager.newFile(uploadPath, '~' + name)))
      FileResourceManager.deleteFile(uploadPath, '~' + name)
      (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(bytes)), FileResourceManager.fileExtensionFromName(name)).mkString("."), Base64.encodeBase64(bytes))
    } catch {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }


}