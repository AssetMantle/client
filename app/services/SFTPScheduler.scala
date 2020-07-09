package services

import java.io.File
import scala.concurrent.duration._
import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import scala.concurrent.{Await, ExecutionContext, Future}
import akka.Done
import akka.stream.Materializer
import exceptions.BaseException
import models.westernUnion.{SFTPFileTransaction, SFTPFileTransactions}
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.userauth.method.{AuthPassword, AuthPublickey}
import net.schmizz.sshj.userauth.password.PasswordUtils
import utilities.{KeyStore, PGP}
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile
import scala.io.BufferedSource


@Singleton
class SFTPScheduler @Inject()(actorSystem: ActorSystem, sFTPFileTransactions: SFTPFileTransactions, keyStore: KeyStore)(implicit configuration: Configuration, executionContext: ExecutionContext) {
  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.SFTP_SCHEDULER
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")
  private val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)

  private val wuSFTPInitialDelay = configuration.get[Int]("westernUnion.scheduler.initialDelay").seconds
  private val wuSFTPIntervalTime = configuration.get[Int]("westernUnion.scheduler.intervalTime").seconds
  private val basePathSFTPFiles = configuration.get[String]("westernUnion.sftpFileBasePath")
  private val storagePathSFTPFiles = configuration.get[String]("westernUnion.sftpFileStoragePath")

  private val sftpSite = keyStore.getPassphrase(constants.KeyStore.WESTERN_UNION_SFTP_SITE)
  private val sftpPort = configuration.get[Int]("westernUnion.sftpPort")
  private val wuSFTPUsername = keyStore.getPassphrase(constants.KeyStore.WESTERN_UNION_SFTP_USERNAME)
  private val wuSFTPPassword = keyStore.getPassphrase(constants.KeyStore.WESTERN_UNION_SFTP_PASSWORD)
  private val sshPrivateKeyPath = configuration.get[String]("westernUnion.sshPrivateKeyPath")
  private val wuPGPPublicKeyFileLocation = configuration.get[String]("westernUnion.wuPGPPublicKeyPath")
  private val comdexPGPPrivateKeyFileLocation = configuration.get[String]("westernUnion.comdexPGPPrivateKeyPath")
  private val comdexPGPPrivateKeyPassword = keyStore.getPassphrase(constants.KeyStore.COMDEX_PGP_PRIVATE_KEY_PASSWORD)
  private val tempFileName = configuration.get[String]("westernUnion.tempFileName")

  def scheduler: Unit = {
    try {
      val defaultConfig = new DefaultConfig
      val sshClient = new SSHClient(defaultConfig)
      sshClient.addHostKeyVerifier(new PromiscuousVerifier())
      sshClient.connect(sftpSite, sftpPort)
      val keyFile = new PKCS8KeyFile
      keyFile.init(new File(sshPrivateKeyPath))
      sshClient.auth(wuSFTPUsername, new AuthPublickey(keyFile), new AuthPassword(PasswordUtils.createOneOff(wuSFTPPassword.toCharArray())))

      val sftpClient = sshClient.newSFTPClient()
      sftpClient.ls(basePathSFTPFiles).forEach { ftpFile =>
        val newFilePath = storagePathSFTPFiles + ftpFile.getName
        sftpClient.get(ftpFile.getPath, storagePathSFTPFiles)

        def decryptAndReadCSV: Future[BufferedSource] = {
          PGP.decryptFile(newFilePath, storagePathSFTPFiles + tempFileName, wuPGPPublicKeyFileLocation, comdexPGPPrivateKeyFileLocation, comdexPGPPrivateKeyPassword)
          val csvFileContentBuffer = scala.io.Source.fromFile(storagePathSFTPFiles + tempFileName)
          val csvFileContentProcessor = Future.sequence {
            csvFileContentBuffer.getLines.drop(1).map { line =>
              val Array(payerID, invoiceNumber, customerFirstName, customerLastName, customerEmailAddress, settlementDate, clientReceivedAmount, transactionType, productType, transactionReference) = line.split(",").map(_.trim)
              sFTPFileTransactions.Service.create(SFTPFileTransaction(payerID, invoiceNumber, customerFirstName, customerLastName, customerEmailAddress, settlementDate, clientReceivedAmount, transactionType, productType, transactionReference))
            }
          }
          for {
            _ <- csvFileContentProcessor
          } yield csvFileContentBuffer
        }

        def csvBufferCloseAndRemoveSFTPFile(csvFileContentBuffer: BufferedSource): Future[Unit] = {
          Future {
            csvFileContentBuffer.close()
            sftpClient.rm(ftpFile.getPath)
          }
        }

        val complete = for {
          csvFileContentBuffer <- decryptAndReadCSV
          _ <- csvBufferCloseAndRemoveSFTPFile(csvFileContentBuffer)
        } yield {}
        Await.result(complete, Duration.Inf)
      }
      sftpClient.close()
      sshClient.disconnect()
    }
    catch {
      case baseException: BaseException=>
        logger.error(baseException.failure.message, baseException)
        Done
      case e: Exception =>
        logger.error(e.getMessage,e)
        Done
    }
  }

  val runnable = new Runnable {
    override def run(): Unit =
      scheduler
  }

  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = wuSFTPInitialDelay, interval = wuSFTPIntervalTime)(runnable)(schedulerExecutionContext)
}