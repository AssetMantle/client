package utilities

import java.util.Base64

import exceptions.BaseException
import org.apache.commons.codec.binary.Hex
import play.api.Logger
import play.api.libs.Codecs.sha1

object Hash {
  private implicit val module: String = constants.Module.UTILITIES_HASH

  private implicit val logger: Logger = Logger(this.getClass)

  //apache-codec Base64 encoder was using +/ instead of -_ which is output in BC. So use java.util.Base64
  def getHash(values: String*): String = Base64.getUrlEncoder.encodeToString(Hex.decodeHex(sha1(values.sorted.mkString(constants.Blockchain.ToHashSeparator))))

  def base64URLDecoder(s: String): String = try {
    Base64.getUrlDecoder.decode(s.replace("+", "-").replace("/", "_")).map(_.toChar).mkString
  } catch {
    case exception: Exception => logger.error(s"${s} : ${exception.getLocalizedMessage}")
      throw new BaseException(constants.Response.INVALID_BASE64_ENCODING, exception)
  }

}
