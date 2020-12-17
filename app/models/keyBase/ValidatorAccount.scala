package models.keyBase

import java.net.ConnectException
import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.GetValidatorKeyBaseAccount
import queries.responses.ValidatorKeyBaseAccountResponse.{Response => ValidatorKeyBaseAccountResponse}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ValidatorAccount(address: String, identity: Option[String], username: Option[String], picture: Option[Array[Byte]], createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class ValidatorAccounts @Inject()(
                                   wsClient: WSClient,
                                   getValidatorKeyBaseAccount: GetValidatorKeyBaseAccount,
                                   protected val databaseConfigProvider: DatabaseConfigProvider,
                                   configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ACCOUNT_FILE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private val keyBaseAccountUpdateRate = configuration.get[Int]("blockchain.validator.keyBaseAccount.updateRate")

  private val keyBaseAccountInitialDelay = configuration.get[Int]("blockchain.validator.keyBaseAccount.initialDelay")

  private val schedulerExecutionContext: ExecutionContext = actors.Service.actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private[models] val validatorAccountTable = TableQuery[ValidatorAccountTable]

  private def add(validatorKeyBase: ValidatorAccount): Future[String] = db.run((validatorAccountTable returning validatorAccountTable.map(_.address) += validatorKeyBase).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.DOCUMENT_UPLOAD_FAILED, psqlException)
    }
  }

  private def update(validatorKeyBase: ValidatorAccount): Future[Int] = db.run(validatorAccountTable.filter(_.address === validatorKeyBase.address).update(validatorKeyBase).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.DOCUMENT_UPDATE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.DOCUMENT_UPDATE_FAILED, noSuchElementException)
    }
  }

  private def upsert(validatorKeyBase: ValidatorAccount): Future[Int] = db.run(validatorAccountTable.insertOrUpdate(validatorKeyBase).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.DOCUMENT_UPDATE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.DOCUMENT_UPDATE_FAILED, noSuchElementException)
    }
  }

  private def tryGetByAddress(address: String): Future[ValidatorAccount] = db.run(validatorAccountTable.filter(_.address === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.DOCUMENT_NOT_FOUND, noSuchElementException)
    }
  }

  private def findAll: Future[Seq[ValidatorAccount]] = db.run(validatorAccountTable.result)

  private[models] class ValidatorAccountTable(tag: Tag) extends Table[ValidatorAccount](tag, "ValidatorAccount") {

    def * = (address, identity.?, username.?, picture.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ValidatorAccount.tupled, ValidatorAccount.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def identity = column[String]("identity")

    def username = column[String]("username")

    def picture = column[Array[Byte]]("picture")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(validatorKAccount: ValidatorAccount): Future[String] = add(validatorKAccount)

    def insertOrUpdate(validatorKAccount: ValidatorAccount): Future[Int] = upsert(validatorKAccount)

    def tryGet(address: String): Future[ValidatorAccount] = tryGetByAddress(address)

    def getAll: Future[Seq[ValidatorAccount]] = findAll
  }

  object Utility {

    def insertOrUpdateKeyBaseAccount(validatorAddress: String, identity: Option[String]): Future[Unit] = {
      val validatorAccount = if (identity.isDefined) {
        val keyBaseResponse = getValidatorKeyBaseAccount.Service.get(identity.get)

        def getImageByteArray(keyBaseResponse: ValidatorKeyBaseAccountResponse): Future[Option[Array[Byte]]] = {
          keyBaseResponse.them.headOption.fold[Future[Option[Array[Byte]]]](Future(None)) { them =>
            val imageURL: String = them.pictures.fold("")(x => x.primary.url)
            val imageResponse: Future[Option[Array[Byte]]] = if (imageURL == "") Future(None) else wsClient.url(imageURL).get.map(x => Option(x.body[Array[Byte]]))
            (for {
              imageResponse <- imageResponse
            } yield imageResponse
              ).recover {
              case connectException: ConnectException => throw new BaseException(constants.Response.CONNECT_EXCEPTION, connectException)
            }
          }
        }

        (for {
          keyBaseResponse <- keyBaseResponse
          image <- getImageByteArray(keyBaseResponse)
        } yield ValidatorAccount(address = validatorAddress, identity = identity, username = keyBaseResponse.them.headOption.fold[Option[String]](None)(x => Option(x.basics.username)), picture = image)
          ).recover {
          case baseException: BaseException => throw baseException
        }
      } else Future(ValidatorAccount(address = validatorAddress, identity = None, username = None, picture = None))

      (for {
        validatorAccount <- validatorAccount
        _ <- Service.insertOrUpdate(validatorAccount)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def scheduleUpdates(): Future[Unit] = {
      val allValidatorAccounts = Service.getAll

      def updateAll(validatorAccounts: Seq[ValidatorAccount]) = Future.traverse(validatorAccounts)(validatorAccount => insertOrUpdateKeyBaseAccount(validatorAddress = validatorAccount.address, identity = validatorAccount.identity))

      (for {
        allValidatorAccounts <- allValidatorAccounts
        _ <- updateAll(allValidatorAccounts)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.KEY_BASE_ACCOUNTS_UPDATE_FAILED.logMessage)
      }
    }

  }

  private val runnable = new Runnable {
    def run(): Unit = Utility.scheduleUpdates()
  }

  actors.Service.actorSystem.scheduler.scheduleAtFixedRate(keyBaseAccountInitialDelay.milliseconds, keyBaseAccountUpdateRate.days)(runnable)(schedulerExecutionContext)

}