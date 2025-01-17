package models.keyBase

import exceptions.BaseException
import models.traits.Logging
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.keyBase.GetValidatorKeyBaseAccount
import queries.responses.keyBase.ValidatorKeyBaseAccountResponse.{Response => ValidatorKeyBaseAccountResponse}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ValidatorAccount(address: String, identity: String, username: Option[String], pictureURL: Option[String], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging

@Singleton
class ValidatorAccounts @Inject()(
                                   wsClient: WSClient,
                                   getValidatorKeyBaseAccount: GetValidatorKeyBaseAccount,
                                   utilitiesOperations: utilities.Operations,
                                   protected val databaseConfigProvider: DatabaseConfigProvider,
                                   configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.KEY_BASE_VALIDATOR_ACCOUNT

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
      case psqlException: PSQLException => throw new BaseException(constants.Response.KEY_BASE_ACCOUNT_INSERT_FAILED, psqlException)
    }
  }

  private def update(validatorKeyBase: ValidatorAccount): Future[Int] = db.run(validatorAccountTable.filter(_.address === validatorKeyBase.address).update(validatorKeyBase).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.KEY_BASE_ACCOUNT_UPDATE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.DOCUMENT_UPDATE_FAILED, noSuchElementException)
    }
  }

  private def upsert(validatorKeyBase: ValidatorAccount): Future[Int] = db.run(validatorAccountTable.insertOrUpdate(validatorKeyBase).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.KEY_BASE_ACCOUNT_UPSERT_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.KEY_BASE_ACCOUNT_UPSERT_FAILED, noSuchElementException)
    }
  }

  private def tryGetByAddress(address: String): Future[ValidatorAccount] = db.run(validatorAccountTable.filter(_.address === address).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.KEY_BASE_ACCOUNT_NOT_FOUND, noSuchElementException)
    }
  }

  private def getListByAddress(addresses: Seq[String]): Future[Seq[ValidatorAccount]] = db.run(validatorAccountTable.filter(_.address.inSet(addresses)).result)

  private def getByAddress(address: String): Future[Option[ValidatorAccount]] = db.run(validatorAccountTable.filter(_.address === address).result.headOption)

  private def findAll: Future[Seq[ValidatorAccount]] = db.run(validatorAccountTable.result)

  private def deleteByAddress(operatorAddress: String): Future[Int] = db.run(validatorAccountTable.filter(_.address === operatorAddress).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      // Possibility that key base account doesn't exist
      case _: NoSuchElementException => 0
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private[models] class ValidatorAccountTable(tag: Tag) extends Table[ValidatorAccount](tag, "ValidatorAccount") {

    def * = (address, identity, username.?, pictureURL.?, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (ValidatorAccount.tupled, ValidatorAccount.unapply)

    def address = column[String]("address", O.PrimaryKey)

    def identity = column[String]("identity")

    def username = column[String]("username")

    def pictureURL = column[String]("pictureURL")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

  }

  object Service {

    def create(validatorAccount: ValidatorAccount): Future[String] = add(validatorAccount)

    def insertOrUpdate(validatorAccount: ValidatorAccount): Future[Int] = upsert(validatorAccount)

    def tryGet(address: String): Future[ValidatorAccount] = tryGetByAddress(address)

    def get(addresses: Seq[String]): Future[Seq[ValidatorAccount]] = getListByAddress(addresses)

    def get(address: String): Future[Option[ValidatorAccount]] = getByAddress(address)

    def getAll: Future[Seq[ValidatorAccount]] = findAll

    def delete(operatorAddress: String): Future[Int] = deleteByAddress(operatorAddress)

  }

  object Utility {

    def insertOrUpdateKeyBaseAccount(validatorAddress: String, identity: String): Future[Unit] = if (identity != "" || identity != "[do-not-modify]") {
      val validatorAccount = if (identity != "") {
        val keyBaseResponse = getValidatorKeyBaseAccount.Service.get(identity)

        def getImageURL(keyBaseResponse: ValidatorKeyBaseAccountResponse): Option[String] = {
          keyBaseResponse.them.headOption.fold[Option[String]](None) { them =>
            Some(them.pictures.fold("")(x => x.primary.url))
          }

        }

        for {
          keyBaseResponse <- keyBaseResponse
        } yield ValidatorAccount(address = validatorAddress, identity = identity, username = keyBaseResponse.them.headOption.fold[Option[String]](None)(x => Option(x.basics.username)), pictureURL = getImageURL(keyBaseResponse))
      } else Future(ValidatorAccount(address = validatorAddress, identity = "", username = None, pictureURL = None))

      for {
        validatorAccount <- validatorAccount
        _ <- Service.insertOrUpdate(validatorAccount)
      } yield ()
    } else Future()

    def scheduleUpdates(): Future[Unit] = {
      val allValidatorAccounts = Service.getAll

      def updateAll(validatorAccounts: Seq[ValidatorAccount]) = utilitiesOperations.traverse(validatorAccounts)(validatorAccount => insertOrUpdateKeyBaseAccount(validatorAddress = validatorAccount.address, identity = validatorAccount.identity))

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