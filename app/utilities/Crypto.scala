package utilities

import java.security.MessageDigest
import java.util

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object Crypto {

  def encrypt(str: Array[Byte], secret: String) = {
    val key = MessageDigest.getInstance("SHA-256").digest(secret.getBytes())
    val aesKey = new SecretKeySpec(util.Arrays.copyOf(key, 16), "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, aesKey)
    cipher.doFinal(str)
  }

  def decrypt(encryptedString: Array[Byte], secret: String) = {
    val key = MessageDigest.getInstance("SHA-256").digest(secret.getBytes())
    val aesKey = new SecretKeySpec(util.Arrays.copyOf(key, 16), "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, aesKey)
    cipher.doFinal(encryptedString)
  }
}