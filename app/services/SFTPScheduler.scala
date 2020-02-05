package services

import java.io.File
import java.net.InetAddress

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.stream.alpakka.ftp.scaladsl.{Sftp, SftpApi}
import javax.inject.{Inject, Singleton}
import net.schmizz.sshj.{DefaultConfig, SSHClient}
import play.api.{Configuration, Logger}

import scala.concurrent.{Await, ExecutionContext, Future}
import java.security.Security
import java.nio.file.{Files, Paths}
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.alpakka.ftp.{FtpCredentials, FtpFile, SftpIdentity, SftpSettings}
//import akka.stream.scaladsl.JavaFlowSupport.Source
import akka.stream.scaladsl.{Compression, FileIO, RunnableGraph}
import akka.util.ByteString
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.userauth.method.AuthMethod

@Singleton
class SFTPScheduler @Inject()(actorSystem: ActorSystem)(implicit configuration: Configuration) {
  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.SFTP_SCHEDULER
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  private val wuSFTPInitialDelay = configuration.get[Int]("westernUnion.scheduler.initialDelay").seconds
  private val wuSFTPIntervalTime = configuration.get[Int]("westernUnion.scheduler.intervalTime").seconds
  private val basePathSFTPFiles = configuration.get[String]("westernUnion.sftpFileBasePath")
  private val storagePathSFTPFiles = configuration.get[String]("westernUnion.sftpFileStoragePath")


  implicit val system = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val sftpSite = configuration.get[String]("westernUnion.sftpSite")
  private val sftpPort = configuration.get[Int]("westernUnion.sftpPort")
  private val wuSFTPUsername = configuration.get[String]("westernUnion.sftpUsername")
  private val wuSFTPPassword = configuration.get[String]("westernUnion.sftpPassword")
  private val sshPrivateKeyPath = configuration.get[String]("westernUnion.sshPrivateKeyPath")
  private val wuPGPPublicKeyFileLocation = configuration.get[String]("westernUnion.wuPGPPublicKeyPath")
  private val comdexPGPPrivateKeyFileLocation = configuration.get[String]("westernUnion.comdexPGPPrivateKeyPath")
  private val comdexPGPPrivateKeyPassword = configuration.get[String]("westernUnion.comdexPGPPrivateKeyPassword")

  def testFunc(ftpFile: FtpFile): String = {
    println(ftpFile.name)
    new File(storagePathSFTPFiles + ftpFile.name)
    storagePathSFTPFiles + ftpFile.name
  }


  def scheduler = {
    val sftpSettings = SftpSettings
      .create(InetAddress.getByName(sftpSite))
      .withPort(sftpPort)
      .withCredentials(FtpCredentials.create(wuSFTPUsername, wuSFTPPassword))
      .withStrictHostKeyChecking(false)
      .withSftpIdentity(SftpIdentity.createRawSftpIdentity(Files.readAllBytes(Paths.get(sshPrivateKeyPath))))


    val sshClient = new SSHClient(new DefaultConfig)
    sshClient.addHostKeyVerifier(new PromiscuousVerifier())
    val sftp = Sftp(sshClient)

    Sftp
      .ls(basePathSFTPFiles, sftpSettings)
      .flatMapConcat(ftpFile => Sftp.fromPath(ftpFile.path, sftpSettings).map((_, ftpFile)))
      .runForeach { ftpFile =>
        val newFilePath = storagePathSFTPFiles + ftpFile._2.name;
        val fileCreate = new File(newFilePath);
        fileCreate.createNewFile();
        Source
          .single(ftpFile._1)
          .runWith(FileIO.toPath(fileCreate.toPath));

        services.PGP.decryptFile(newFilePath, storagePathSFTPFiles + "tmp.csv", wuPGPPublicKeyFileLocation, comdexPGPPrivateKeyFileLocation, comdexPGPPrivateKeyPassword)

      }

  }

  actorSystem.scheduler.schedule(initialDelay = wuSFTPInitialDelay, interval = wuSFTPIntervalTime) {
    println("scheduler running")
    scheduler
    println("returned something")
  }(schedulerExecutionContext)
}