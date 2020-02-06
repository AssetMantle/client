package services

import java.io.File
import java.net.InetAddress
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.stream.alpakka.ftp.scaladsl.Sftp
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import scala.concurrent.{Await, ExecutionContext, Future}
import java.nio.file.{Files, Paths}
import scala.io
import akka.Done
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.alpakka.ftp.{FtpCredentials, SftpIdentity, SftpSettings}
import akka.stream.scaladsl.FileIO
import exceptions.BaseException
import models.masterTransaction.{WUSFTPFileTransaction, WUSFTPFileTransactions}
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import utilities.PGP
import scala.io.BufferedSource


@Singleton
class SFTPScheduler @Inject()(actorSystem: ActorSystem, wuSFTPFileTransactions: WUSFTPFileTransactions)(implicit configuration: Configuration, executionContext: ExecutionContext) {
  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.SFTP_SCHEDULER
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")
  implicit val system = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val wuSFTPInitialDelay = configuration.get[Int]("westernUnion.scheduler.initialDelay").seconds
  private val wuSFTPIntervalTime = configuration.get[Int]("westernUnion.scheduler.intervalTime").seconds
  private val basePathSFTPFiles = configuration.get[String]("westernUnion.sftpFileBasePath")
  private val storagePathSFTPFiles = configuration.get[String]("westernUnion.sftpFileStoragePath")

  private val sftpSite = configuration.get[String]("westernUnion.sftpSite")
  private val sftpPort = configuration.get[Int]("westernUnion.sftpPort")
  private val wuSFTPUsername = configuration.get[String]("westernUnion.sftpUsername")
  private val wuSFTPPassword = configuration.get[String]("westernUnion.sftpPassword")
  private val sshPrivateKeyPath = configuration.get[String]("westernUnion.sshPrivateKeyPath")
  private val wuPGPPublicKeyFileLocation = configuration.get[String]("westernUnion.wuPGPPublicKeyPath")
  private val comdexPGPPrivateKeyFileLocation = configuration.get[String]("westernUnion.comdexPGPPrivateKeyPath")
  private val comdexPGPPrivateKeyPassword = configuration.get[String]("westernUnion.comdexPGPPrivateKeyPassword")
  private val tempFileName = configuration.get[String]("westernUnion.tempFileName")

  def scheduler: Done = {
    try {
      val sftpSettings = SftpSettings
        .create(InetAddress.getByName(sftpSite))
        .withPort(sftpPort)
        .withCredentials(FtpCredentials.create(wuSFTPUsername, wuSFTPPassword))
        .withStrictHostKeyChecking(false)
        .withSftpIdentity(SftpIdentity.createRawSftpIdentity(Files.readAllBytes(Paths.get(sshPrivateKeyPath))))

      val sshClient = new SSHClient(new DefaultConfig)
      sshClient.addHostKeyVerifier(new PromiscuousVerifier())
      val _ = Sftp(sshClient)

      val sftpProcess = Sftp
        .ls(basePathSFTPFiles, sftpSettings)
        .flatMapConcat(ftpFile => Sftp.fromPath(ftpFile.path, sftpSettings).map((_, ftpFile)))
        .runForeach { ftpFile =>
          val newFilePath = storagePathSFTPFiles + ftpFile._2.name
          val fileCreate = new File(newFilePath)
          fileCreate.createNewFile()
          val writeEncryptedData = Source.single(ftpFile._1).runWith(FileIO.toPath(fileCreate.toPath))

          def decryptAndReadCSV: Future[BufferedSource] = {
            PGP.decryptFile(newFilePath, storagePathSFTPFiles + tempFileName, wuPGPPublicKeyFileLocation, comdexPGPPrivateKeyFileLocation, comdexPGPPrivateKeyPassword)
            val csvFileContentBuffer = io.Source.fromFile(storagePathSFTPFiles + tempFileName)
            val csvFileContentProcessor = Future.sequence {
              csvFileContentBuffer.getLines.drop(1).map { line =>
                val Array(payerID, invoiceNumber, customerFirstName, customerLastName, customerEmailAddress, settlementDate, clientReceivedAmount, transactionType, productType, transactionReference) = line.split(",").map(_.trim)
                wuSFTPFileTransactions.Service.create(WUSFTPFileTransaction(payerID, invoiceNumber, customerFirstName, customerLastName, customerEmailAddress, settlementDate, clientReceivedAmount, transactionType, productType, transactionReference))
              }
            }
            for {
              _ <- csvFileContentProcessor
            } yield csvFileContentBuffer
          }

          def csvBufferCloseAndRemoveSFTPFile(csvFileContentBuffer: BufferedSource): Future[IOResult] = {
            csvFileContentBuffer.close()
            Source.single(ftpFile._2).runWith(Sftp.remove(sftpSettings))
          }

          val complete=for {
            _ <- writeEncryptedData
            csvFileContentBuffer <- decryptAndReadCSV
            _ <- csvBufferCloseAndRemoveSFTPFile(csvFileContentBuffer)
          } yield {}
          Await.result(complete,Duration.Inf)
        }

      Await.result(sftpProcess, Duration.Inf)
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

  actorSystem.scheduler.schedule(initialDelay = wuSFTPInitialDelay, interval = wuSFTPIntervalTime) {
    scheduler
  }(schedulerExecutionContext)
}