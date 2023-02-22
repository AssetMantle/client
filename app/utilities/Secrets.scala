package utilities

import exceptions.BaseException
import play.api.Logger
import sun.nio.cs.ISO_8859_1

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.{MessageDigest, SecureRandom}
import java.util
import java.util.Base64
import javax.crypto.spec.{PBEKeySpec, SecretKeySpec}
import javax.crypto.{Cipher, SecretKeyFactory}

object Secrets {

  private implicit val module: String = constants.Module.UTILITIES_SECRETS

  private implicit val logger: Logger = Logger(this.getClass)

  def encryptData(data: Array[Byte], secret: String): Array[Byte] = {
    val key = MessageDigest.getInstance("SHA-256").digest(secret.getBytes())
    val aesKey = new SecretKeySpec(util.Arrays.copyOf(key, 16), "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, aesKey)
    cipher.doFinal(data)
  }

  def decryptData(encryptedData: Array[Byte], secret: String): Array[Byte] = {
    val key = MessageDigest.getInstance("SHA-256").digest(secret.getBytes())
    val aesKey = new SecretKeySpec(util.Arrays.copyOf(key, 16), "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, aesKey)
    cipher.doFinal(encryptedData)
  }

  def pbkdf2(password: String, salt: Array[Byte], iterations: Int): Array[Byte] = {
    val keySpec = new PBEKeySpec(password.toCharArray, salt, iterations, 256)
    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    keyFactory.generateSecret(keySpec).getEncoded
  }

  def getNewSalt: Array[Byte] = {
    val salt = new Array[Byte](16)
    val random = new SecureRandom()
    random.nextBytes(salt)
    salt
  }

  def hashPassword(password: String, salt: Array[Byte], pepper: Array[Byte], iterations: Int = constants.Security.DefaultIterations): String = {
    Base64.getEncoder.encodeToString(pbkdf2(password, salt ++ pepper, iterations))
  }

  def verifyPassword(password: String, passwordHash: String, salt: Array[Byte], pepper: Array[Byte], iterations: Int = constants.Security.DefaultIterations): Boolean = {
    pbkdf2(password, salt ++ pepper, iterations).sameElements(passwordHash)
  }

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

  def base64URLEncoder(s: Array[Byte]): String = try {
    Base64.getUrlEncoder.encodeToString(s)
  } catch {
    case exception: Exception => logger.error(s"${s.mkString("Array(", ", ", ")")} : ${exception.getLocalizedMessage}")
      throw new BaseException(constants.Response.INVALID_BASE64_ENCODING, exception)
  }

  def base64Encoder(s: String): Array[Byte] = try {
    Base64.getEncoder.encode(s.getBytes(ISO_8859_1.INSTANCE))
  } catch {
    case exception: Exception => logger.error(s"$s : ${exception.getLocalizedMessage}")
      throw new BaseException(constants.Response.INVALID_BASE64_ENCODING, exception)
  }

  def base64Encoder(s: Array[Byte]): String = try {
    Base64.getEncoder.encodeToString(s)
  } catch {
    case exception: Exception => logger.error(s"${s.mkString("Array(", ", ", ")")} : ${exception.getLocalizedMessage}")
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

  def sha256Hash(value: Array[Byte]): Array[Byte] = MessageDigest.getInstance("SHA-256").digest(value)

  def sha256HashString(value: Array[Byte]): String = java.lang.String.format("%064x", new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(value)))

  def sha256HashString(value: String): String = java.lang.String.format("%064x", new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))))

  def sha256HashHexString(value: Array[Byte]): String = byteArrayToString(sha256Hash(value))

  def byteArrayToString(value: Array[Byte]): String = value.map("%02x".format(_)).mkString.toUpperCase

  def base64URLDecodeToString(s: String): String = try {
    Base64.getUrlDecoder.decode(s.replace("+", "-").replace("/", "_")).map(_.toChar).mkString
  } catch {
    case exception: Exception => constants.Response.INVALID_BASE64_ENCODING.throwBaseException(exception)
  }

  def base64URLDecode(s: String): Array[Byte] = try {
    Base64.getUrlDecoder.decode(s.replace("+", "-").replace("/", "_"))
  } catch {
    case exception: Exception => constants.Response.INVALID_BASE64_ENCODING.throwBaseException(exception)
  }
}