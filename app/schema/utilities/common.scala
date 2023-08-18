package schema.utilities

import java.util.Base64

object common {

  def byteArraysCompare(a: Array[Byte], b: Array[Byte]): Int = {
    if (a sameElements b) return 0
    else if (a == null) return -1
    else if (b == null) return 1
    val length = Math.min(a.length, b.length)
    for (i <- 0 until length) {
      val first = if (a(i) < 0) a(i).toInt + 256 else a(i).toInt
      val second = if (b(i) < 0) b(i).toInt + 256 else b(i).toInt
      val result = first - second
      if (result != 0) {
        return result
      }
    }
    Integer.compare(a.length, b.length);
  }

  def base64URLEncoder(s: Array[Byte]): String = try {
    Base64.getUrlEncoder.encodeToString(s)
  } catch {
    case exception: Exception => throw new IllegalArgumentException("invalid byte array: " + exception.getLocalizedMessage)
  }

}
