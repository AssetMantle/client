package commonUtilities

import org.slf4j.{Logger, LoggerFactory}
import sun.nio.cs.ISO_8859_1

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

object Secrets {

  private implicit val module: String = commonConstants.Module.COMMON_UTILITIES_SECRETS

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def sha256Hash(value: String): Array[Byte] = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))

  def sha256Hash(value: Array[Byte]): Array[Byte] = MessageDigest.getInstance("SHA-256").digest(value)

  def sha256HashString(value: String): String = java.lang.String.format("%064x", new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))))

  def sha256HashHexString(value: Array[Byte]): String = byteArrayToString(sha256Hash(value))

  def byteArrayToString(value: Array[Byte]): String = value.map("%02x".format(_)).mkString.toUpperCase

  def base64URLDecodeToString(s: String): String = try {
    Base64.getUrlDecoder.decode(s.replace("+", "-").replace("/", "_")).map(_.toChar).mkString
  } catch {
    case exception: Exception => throw new IllegalArgumentException("INVALID_BASE64_ENCODING")
  }

  def base64URLDecode(s: String): Array[Byte] = try {
    Base64.getUrlDecoder.decode(s.replace("+", "-").replace("/", "_"))
  } catch {
    case exception: Exception => throw new IllegalArgumentException("INVALID_BASE64_ENCODING")
  }

  def base64URLEncoder(s: String): String = try {
    Base64.getUrlEncoder.encodeToString(s.getBytes(ISO_8859_1.INSTANCE))
  } catch {
    case exception: Exception => throw new IllegalArgumentException("INVALID_BASE64_ENCODING")
  }

  def base64URLEncoder(s: Array[Byte]): String = try {
    Base64.getUrlEncoder.encodeToString(s)
  } catch {
    case exception: Exception => throw new IllegalArgumentException("INVALID_BASE64_ENCODING")
  }

  def base64Encoder(s: String): Array[Byte] = try {
    Base64.getEncoder.encode(s.getBytes(ISO_8859_1.INSTANCE))
  } catch {
    case exception: Exception => throw new IllegalArgumentException("INVALID_BASE64_ENCODING")
  }

  def base64Encoder(s: Array[Byte]): String = try {
    Base64.getEncoder.encodeToString(s)
  } catch {
    case exception: Exception => throw new IllegalArgumentException("INVALID_BASE64_ENCODING")
  }

  def base64Decoder(s: String): Array[Byte] = try {
    Base64.getDecoder.decode(s)
  } catch {
    case exception: Exception => throw new IllegalArgumentException("INVALID_BASE64_ENCODING")
  }

  def base64MimeEncoder(s: String): Array[Byte] = try {
    Base64.getMimeEncoder.encode(s.getBytes(ISO_8859_1.INSTANCE))
  } catch {
    case exception: Exception => throw new IllegalArgumentException("INVALID_BASE64_ENCODING")
  }

  def base64MimeDecoder(s: String): Array[Byte] = try {
    Base64.getMimeDecoder.decode(s.replace("+", "-").replace("/", "_"))
  } catch {
    case exception: Exception => throw new IllegalArgumentException("INVALID_BASE64_ENCODING")
  }

}