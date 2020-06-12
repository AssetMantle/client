package services

import java.io.{FileInputStream, FileNotFoundException, FileOutputStream}
import java.security.KeyStore

import exceptions.BaseException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}

@Singleton
class KeyStore @Inject()(configuration: Configuration) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.SERVICES_KEY_STORE

  private val keyStoreLocation = configuration.get[String]("keyStore.filePath")

  private val keyStorePassword = configuration.get[String]("keyStore.password")

  private val keyStoreType = "PKCS12"

  def getPassphrase(alias: String): String = try {
    val ks = KeyStore.getInstance(keyStoreType)
    val fis = new FileInputStream(keyStoreLocation)
    ks.load(fis, keyStorePassword.toCharArray)
    val keyStorePasswordProtection = new KeyStore.PasswordProtection(keyStorePassword.toCharArray)
    val fIn = new FileInputStream(keyStoreLocation)
    ks.load(fIn, keyStorePassword.toCharArray)
    val factory = SecretKeyFactory.getInstance("PBE")
    val secretKeyEntry = {
      try ks.getEntry(alias, keyStorePasswordProtection).asInstanceOf[KeyStore.SecretKeyEntry]
      finally keyStorePasswordProtection.destroy()
    }
    val keySpec = factory.getKeySpec(secretKeyEntry.getSecretKey, classOf[PBEKeySpec]).asInstanceOf[PBEKeySpec]
    val password = keySpec.getPassword
    new String(password)
  } catch {
    case exception: Exception => logger.error(exception.getMessage)
      throw new BaseException(constants.Response.KEY_STORE_ERROR)
  }

  def setPassphrase(alias: String, aliasValue: String): Unit = try {
    val factory = SecretKeyFactory.getInstance("PBE")
    val generatedSecret = factory.generateSecret(new PBEKeySpec(aliasValue.toCharArray))
    val fis = new FileInputStream(keyStoreLocation)
    val ks = KeyStore.getInstance(keyStoreType)
    ks.load(fis, keyStorePassword.toCharArray)
    val keyStorePasswordProtection = new KeyStore.PasswordProtection(keyStorePassword.toCharArray)
    try ks.setEntry(alias, new KeyStore.SecretKeyEntry(generatedSecret), keyStorePasswordProtection)
    finally keyStorePasswordProtection.destroy()
    val fos = new FileOutputStream(keyStoreLocation)
    ks.store(fos, keyStorePassword.toCharArray)
  } catch {
    case exception: Exception => logger.error(exception.getMessage)
      throw new BaseException(constants.Response.KEY_STORE_ERROR)
  }

}
