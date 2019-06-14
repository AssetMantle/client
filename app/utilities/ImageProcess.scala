
package utilities

import com.sksamuel.scrimage
import com.sksamuel.scrimage.nio.JpegWriter
import exceptions.BaseException
import javax.imageio.ImageIO
import javax.inject.Inject
import org.apache.commons.codec.binary.Base64
import play.api.Logger

class ImageProcess @Inject()(fileResourceManager: FileResourceManager) {

  private implicit val module: String = constants.Module.IMAGE_PROCESS

  private implicit val logger: Logger = Logger(this.getClass)

  def convertToThumbnail(name: String, uploadPath: String): (String, Array[Byte]) = {
    try {
      val imageRes = ImageIO.read(fileResourceManager.newFile(uploadPath, name))
      implicit val writer: JpegWriter = JpegWriter().withCompression(100)
      val bytes = fileResourceManager.convertToByteArray(scrimage.Image.fromFile(fileResourceManager.newFile(uploadPath, name)).fit(180, (180 * imageRes.getHeight) / imageRes.getWidth).output(fileResourceManager.newFile(uploadPath, '~' + name)))
      fileResourceManager.deleteFile(uploadPath, '~' + name)
      (List(util.hashing.MurmurHash3.stringHash(Base64.encodeBase64String(bytes)), fileResourceManager.fileExtensionFromName(name)).mkString("."), Base64.encodeBase64(bytes))
    } catch {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case e: Exception => logger.error(constants.Response.GENERIC_EXCEPTION.message, e)
        throw new BaseException(constants.Response.GENERIC_EXCEPTION)
    }
  }

}