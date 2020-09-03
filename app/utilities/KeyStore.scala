package utilities

import java.io.{FileInputStream, FileOutputStream}
import java.security.KeyStore

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder

import scala.concurrent.blocking
import exceptions.BaseException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import java.util.WeakHashMap

import com.amazonaws.auth.{AWSCredentials, AWSCredentialsProvider, AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.secretsmanager.model.{CreateSecretRequest, GetSecretValueRequest}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, AwsCredentials, StaticCredentialsProvider}

@Singleton
class KeyStore @Inject()(configuration: Configuration) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.UTILITIES_KEY_STORE

  private val keyStoreLocation = configuration.get[String]("keyStore.filePath")

  private val keyStorePassword = configuration.get[String]("keyStore.password")

  private val keyStoreType = "JCEKS"

  private val secretKeyFactoryAlgorithm = "PBEWithHmacSHA512AndAES_256"
  //private val awsBasicCredentials = AwsBasicCredentials.create(getPassphrase(constants.KeyStore.AWS_ACCESS_KEY_ID), getPassphrase(constants.KeyStore.AWS_SECRET_ACCESS_KEY))

 /* private val awsCreds= new BasicAWSCredentials("AKIAYHKHYDI475LOOMUF","rzcK6xQ31zVqB2xXUME0070pvmw97wjllcfs9XJt")
  val endpoint="secretsmanager.eu-central-1.amazonaws.com"
  val region="eu-central-1"

  val clientBuilder=AWSSecretsManagerClientBuilder.standard()
  clientBuilder.setCredentials(new AWSStaticCredentialsProvider(awsCreds))
  clientBuilder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint,region))
  val client=clientBuilder.build()*/

 /* def getAWSSecret={

    val secret="zonePassword"

    val createSecretRequest=(new CreateSecretRequest())
    createSecretRequest.setName("zonePassword123")
    createSecretRequest.setSecretString("1234567890")
   // val getSecretValueRequest=new GetSecretValueRequest().withSecretId(secret)
    try{
      client.createSecret(createSecretRequest)
      /*println(System.currentTimeMillis())
      val secretResult=client.getSecretValue(getSecretValueRequest)
      println(System.currentTimeMillis())*/
     /* if(secretResult == null) throw new BaseException(constants.Response.BILL_OF_LADING_REQUIRED)
      else {
        if(secretResult.getSecretString != null){

          println("secret String -------"+secretResult.getSecretString)
        }else{
          val binaryData=secretResult.getSecretBinary
          println("binary Data--"+binaryData)
        }
      }*/
    }catch {
      case exception: Exception=>logger.error(exception.getMessage,exception)
    }
  }*/

  def getPassphrase(alias: String): String = try {
    val ks = KeyStore.getInstance(keyStoreType)
    ks.load(new FileInputStream(keyStoreLocation), keyStorePassword.toCharArray)
    val keyStorePasswordProtection = new KeyStore.PasswordProtection(keyStorePassword.toCharArray)
    val secretKeyEntry = {
      try ks.getEntry(alias, keyStorePasswordProtection).asInstanceOf[KeyStore.SecretKeyEntry]
      finally keyStorePasswordProtection.destroy()
    }
    new String(SecretKeyFactory.getInstance(secretKeyFactoryAlgorithm).getKeySpec(secretKeyEntry.getSecretKey, classOf[PBEKeySpec]).asInstanceOf[PBEKeySpec].getPassword)

  /*  val secretResult=client.getSecretValue(new GetSecretValueRequest().withSecretId(alias))
    if(secretResult == null) throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    else {
      if (secretResult.getSecretString != null) {
        if(secretResult.getSecretString.contains(":")) secretResult.getSecretString.split(":")(1).replaceAll("\"","").replace("}","")
        else secretResult.getSecretString
      } else throw new BaseException(constants.Response.FAILURE)
    }*/
  } catch {
    case exception: Exception => logger.error(exception.getMessage)
      throw new BaseException(constants.Response.FAILURE)
  }

  def setPassphrase(alias: String, aliasValue: String): Unit = /*blocking(this.synchronized(*/ try {
      val generatedSecret = SecretKeyFactory.getInstance(secretKeyFactoryAlgorithm).generateSecret(new PBEKeySpec(aliasValue.toCharArray))
      val ks = KeyStore.getInstance(keyStoreType)
      ks.load(new FileInputStream(keyStoreLocation), keyStorePassword.toCharArray)
      val keyStorePasswordProtection = new KeyStore.PasswordProtection(keyStorePassword.toCharArray)
      try ks.setEntry(alias, new KeyStore.SecretKeyEntry(generatedSecret), keyStorePasswordProtection)
      finally keyStorePasswordProtection.destroy()
      ks.store(new FileOutputStream(keyStoreLocation), keyStorePassword.toCharArray)

   /* val createSecretRequest=(new CreateSecretRequest())
    createSecretRequest.setName(alias)
    createSecretRequest.setSecretString(aliasValue)
    client.createSecret(createSecretRequest)*/
    } catch {
      case exception: Exception => logger.error(exception.getMessage)
        throw new BaseException(constants.Response.FAILURE)
    }
  //))

  //https://stackoverflow.com/questions/6243446/how-to-store-a-simple-key-string-inside-java-keystore
}
