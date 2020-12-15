package models.blockchainTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.Trait.Logged
import models.common.Serializable.{MetaProperties, Properties}
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class IdentityIssue(from: String, to: String, fromID: String, classificationID: String, immutableMetaProperties: MetaProperties, immutableProperties: Properties, mutableMetaProperties: MetaProperties, mutableProperties: Properties, gas: MicroNumber, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends BaseTransaction[IdentityIssue] with Logged {
  def mutateTicketID(newTicketID: String): IdentityIssue = IdentityIssue(from = from, fromID = fromID, to = to, classificationID = classificationID, immutableMetaProperties = immutableMetaProperties, immutableProperties = immutableProperties, mutableMetaProperties = mutableMetaProperties, mutableProperties = mutableProperties, gas = gas, status = status, txHash = txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class IdentityIssues @Inject()(
                                transaction: utilities.Transaction,
                                protected val databaseConfigProvider: DatabaseConfigProvider,
                                utilitiesNotification: utilities.Notification,
                                masterAccounts: master.Accounts,
                                blockchainAccounts: blockchain.Accounts
                              )(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  case class IdentityIssueSerialized(from: String, to: String, fromID: String, classificationID: String, immutableMetaProperties: String, immutableProperties: String, mutableMetaProperties: String, mutableProperties: String, gas: String, status: Option[Boolean], txHash: Option[String], ticketID: String, mode: String, code: Option[String], createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: IdentityIssue = IdentityIssue(from = from, fromID = fromID, to = to, classificationID = classificationID, immutableMetaProperties = utilities.JSON.convertJsonStringToObject[MetaProperties](immutableMetaProperties), immutableProperties = utilities.JSON.convertJsonStringToObject[Properties](immutableProperties), mutableMetaProperties = utilities.JSON.convertJsonStringToObject[MetaProperties](mutableMetaProperties), mutableProperties = utilities.JSON.convertJsonStringToObject[Properties](mutableProperties), gas = new MicroNumber(BigInt(gas)), status = status, txHash = txHash, ticketID = ticketID, mode = mode, code = code, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedOn = updatedOn, updatedBy = updatedBy, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(identityIssue: IdentityIssue): IdentityIssueSerialized = IdentityIssueSerialized(from = identityIssue.from, to = identityIssue.to, fromID = identityIssue.fromID, classificationID = identityIssue.classificationID, immutableMetaProperties = Json.toJson(identityIssue.immutableMetaProperties).toString, immutableProperties = Json.toJson(identityIssue.immutableProperties).toString, mutableMetaProperties = Json.toJson(identityIssue.mutableMetaProperties).toString, mutableProperties = Json.toJson(identityIssue.mutableProperties).toString, gas = identityIssue.gas.toMicroString, status = identityIssue.status, txHash = identityIssue.txHash, ticketID = identityIssue.ticketID, mode = identityIssue.mode, code = identityIssue.code, createdBy = identityIssue.createdBy, createdOn = identityIssue.createdOn, createdOnTimeZone = identityIssue.createdOnTimeZone, updatedBy = identityIssue.updatedBy, updatedOn = identityIssue.updatedOn, updatedOnTimeZone = identityIssue.updatedOnTimeZone)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private implicit val logger: Logger = Logger(this.getClass)
  val db = databaseConfig.db

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_IDENTITY_ISSUE

  private val schedulerExecutionContext: ExecutionContext = actors.Service.actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  import databaseConfig.profile.api._

  private[models] val identityIssueTable = TableQuery[IdentityIssueTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").second

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(identityIssue: IdentityIssue): Future[String] = db.run((identityIssueTable returning identityIssueTable.map(_.ticketID) += serialize(identityIssue)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(identityIssueTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(identityIssueTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByTicketID(ticketID: String, status: Option[Boolean]): Future[Int] = db.run(identityIssueTable.filter(_.ticketID === ticketID).map(_.status.?).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findByTicketID(ticketID: String): Future[IdentityIssueSerialized] = db.run(identityIssueTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(identityIssueTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(identityIssueTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(identityIssueTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(identityIssueTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class IdentityIssueTable(tag: Tag) extends Table[IdentityIssueSerialized](tag, "IdentityIssue") {

    def * = (from, to, fromID, classificationID, immutableMetaProperties, immutableProperties, mutableMetaProperties, mutableProperties, gas, status.?, txHash.?, ticketID, mode, code.?, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (IdentityIssueSerialized.tupled, IdentityIssueSerialized.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def fromID = column[String]("fromID")

    def classificationID = column[String]("classificationID")

    def immutableMetaProperties = column[String]("immutableMetaProperties")

    def immutableProperties = column[String]("immutableProperties")

    def mutableMetaProperties = column[String]("mutableMetaProperties")

    def mutableProperties = column[String]("mutableProperties")

    def gas = column[String]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(identityIssue: IdentityIssue): Future[String] = add(identityIssue)

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def resetTransactionStatus(ticketID: String): Future[Int] = updateStatusByTicketID(ticketID, status = null)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[IdentityIssue] = findByTicketID(ticketID).map(_.deserialize)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, txHash: String): Future[Unit] = {
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, txHash)

      (for {
        _ <- markTransactionSuccessful
      } yield {}).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {
      val markTransactionFailed = Service.markTransactionFailed(ticketID, message)

      (for {
        _ <- markTransactionFailed
      } yield {}).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  private val txRunnable = new Runnable {
    def run(): Unit = Await.result(transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure), Duration.Inf)
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actors.Service.actorSystem.scheduler.scheduleAtFixedRate(initialDelay = schedulerInitialDelay, interval = schedulerInterval)(txRunnable)(schedulerExecutionContext)
  }
}