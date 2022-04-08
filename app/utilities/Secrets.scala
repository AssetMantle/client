package utilities

import exceptions.BaseException
import org.apache.commons.codec.binary.Hex
import play.api.Logger
import play.api.libs.Codecs.sha1
import sun.nio.cs.ISO_8859_1

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util
import java.util.Base64
import javax.crypto.spec.{PBEKeySpec, SecretKeySpec}
import javax.crypto.{Cipher, SecretKeyFactory}

object Secrets {

  private implicit val module: String = constants.Module.UTILITIES_SECRETS

  private implicit val logger: Logger = Logger(this.getClass)

  def encrypt(data: Array[Byte], secret: String): Array[Byte] = {
    val key = MessageDigest.getInstance("SHA-256").digest(secret.getBytes())
    val aesKey = new SecretKeySpec(util.Arrays.copyOf(key, 16), "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, aesKey)
    cipher.doFinal(data)
  }

  def decrypt(encryptedString: Array[Byte], secret: String): Array[Byte] = {
    val key = MessageDigest.getInstance("SHA-256").digest(secret.getBytes())
    val aesKey = new SecretKeySpec(util.Arrays.copyOf(key, 16), "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, aesKey)
    cipher.doFinal(encryptedString)
  }

  def pbkdf2(password: String, salt: Array[Byte], iterations: Int): Array[Byte] = {
    val keySpec = new PBEKeySpec(password.toCharArray, salt, iterations, 256)
    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    keyFactory.generateSecret(keySpec).getEncoded
  }

  def hashPassword(password: String, salt: Array[Byte], pepper: Array[Byte], iterations: Int = constants.Security.DefaultIterations): String = {
    Base64.getEncoder.encodeToString(pbkdf2(password, salt ++ pepper, iterations))
  }

  def verifyPassword(password: String, passwordHash: String, salt: Array[Byte], pepper: Array[Byte], iterations: Int = constants.Security.DefaultIterations): Boolean = {
    pbkdf2(password, salt ++ pepper, iterations).sameElements(passwordHash)
  }

  //apache-codec Base64 encoder was using +/ instead of -_ which is output in BC. So use java.util.Base64
  def getBlockchainHash(values: String*): String = Base64.getUrlEncoder.encodeToString(Hex.decodeHex(sha1(values.sorted.mkString(constants.Blockchain.ToHashSeparator))))

  def base64URLDecoder(s: String): String = try {
    Base64.getUrlDecoder.decode(s.replace("+", "-").replace("/", "_")).map(_.toChar).mkString
  } catch {
    case exception: Exception => logger.error(s"$s : ${exception.getLocalizedMessage}")
      throw new BaseException(constants.Response.INVALID_BASE64_ENCODING, exception)
  }

  def base64URLEncoder(s: String): String = try {
    Base64.getUrlEncoder.encodeToString(s.getBytes(ISO_8859_1.INSTANCE))
  } catch {
    case exception: Exception => logger.error(s"$s : ${exception.getLocalizedMessage}")
      throw new BaseException(constants.Response.INVALID_BASE64_ENCODING, exception)
  }

  def base64Encoder(s: String): Array[Byte] = try {
    Base64.getEncoder.encode(s.getBytes(ISO_8859_1.INSTANCE))
  } catch {
    case exception: Exception => logger.error(s"$s : ${exception.getLocalizedMessage}")
      throw new BaseException(constants.Response.INVALID_BASE64_ENCODING, exception)
  }

  def base64Decoder(s: String): Array[Byte] = try {
    Base64.getDecoder.decode(s)
  } catch {
    case exception: Exception => logger.error(s"$s : ${exception.getLocalizedMessage}")
      throw new BaseException(constants.Response.INVALID_BASE64_ENCODING, exception)
  }

  def base64MimeEncoder(s: String): Array[Byte] = try {
    Base64.getMimeEncoder.encode(s.getBytes(ISO_8859_1.INSTANCE))
  } catch {
    case exception: Exception => logger.error(s"$s : ${exception.getLocalizedMessage}")
      throw new BaseException(constants.Response.INVALID_BASE64_ENCODING, exception)
  }

  def base64MimeDecoder(s: String): Array[Byte] = try {
    Base64.getMimeDecoder.decode(s.replace("+", "-").replace("/", "_"))
  } catch {
    case exception: Exception => logger.error(s"$s : ${exception.getLocalizedMessage}")
      throw new BaseException(constants.Response.INVALID_BASE64_ENCODING, exception)
  }

  def sha256Hash(value: String): Array[Byte] = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))

}