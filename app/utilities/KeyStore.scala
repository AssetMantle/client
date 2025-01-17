package utilities

import play.api.{Configuration, Logger}

import java.io.{FileInputStream, FileOutputStream}
import java.security.KeyStore
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.{Inject, Singleton}
import scala.concurrent.blocking

@Singleton
class KeyStore @Inject()(configuration: Configuration) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.UTILITIES_KEY_STORE

  private val keyStoreLocation = configuration.get[String]("keyStore.filePath")

  private val keyStorePassword = configuration.get[String]("keyStore.password")

  private val keyStoreType = "JCEKS"

  private val secretKeyFactoryAlgorithm = "PBEWithHmacSHA512AndAES_256"

  def getPassphrase(alias: String): String = try {
    val ks = KeyStore.getInstance(keyStoreType)
    ks.load(new FileInputStream(keyStoreLocation), keyStorePassword.toCharArray)
    val keyStorePasswordProtection = new KeyStore.PasswordProtection(keyStorePassword.toCharArray)
    val secretKeyEntry = {
      try ks.getEntry(alias, keyStorePasswordProtection).asInstanceOf[KeyStore.SecretKeyEntry]
      finally keyStorePasswordProtection.destroy()
    }
    new String(SecretKeyFactory.getInstance(secretKeyFactoryAlgorithm).getKeySpec(secretKeyEntry.getSecretKey, classOf[PBEKeySpec]).asInstanceOf[PBEKeySpec].getPassword)
  } catch {
    case exception: Exception => logger.error(exception.getMessage)
      constants.Response.KEY_STORE_ERROR.throwBaseException(exception)
  }

  def setPassphrase(alias: String, aliasValue: String): Unit = blocking(this.synchronized(
    try {
      val generatedSecret = SecretKeyFactory.getInstance(secretKeyFactoryAlgorithm).generateSecret(new PBEKeySpec(aliasValue.toCharArray))
      val ks = KeyStore.getInstance(keyStoreType)
      ks.load(new FileInputStream(keyStoreLocation), keyStorePassword.toCharArray)
      val keyStorePasswordProtection = new KeyStore.PasswordProtection(keyStorePassword.toCharArray)
      try ks.setEntry(alias, new KeyStore.SecretKeyEntry(generatedSecret), keyStorePasswordProtection)
      finally keyStorePasswordProtection.destroy()
      ks.store(new FileOutputStream(keyStoreLocation), keyStorePassword.toCharArray)
    } catch {
      case exception: Exception => logger.error(exception.getMessage)
        constants.Response.KEY_STORE_ERROR.throwBaseException(exception)
    }
  ))

  //https://stackoverflow.com/questions/6243446/how-to-store-a-simple-key-string-inside-java-keystore
}
