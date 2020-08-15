package utilities

import java.util.Base64

import org.apache.commons.codec.binary.Hex
import play.api.libs.Codecs.sha1

object Hash {

  //apache-codec Base64 encoder was using +/ instead of -_ which is output in BC. So use java.util.Base64
  def getHash(values: String*): String = Base64.getUrlEncoder.encodeToString(Hex.decodeHex(sha1(values.sorted.mkString("_"))))

}
