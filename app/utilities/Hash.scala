package utilities

import java.util.Base64

import org.apache.commons.codec.binary.Hex
import play.api.libs.Codecs.sha1

object Hash {

  def getHash(values: String*): String = Base64.getUrlEncoder.encodeToString(Hex.decodeHex(sha1(values.sorted.mkString("_")).toUpperCase))

}
