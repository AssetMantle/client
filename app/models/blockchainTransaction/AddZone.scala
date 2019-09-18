package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AddZone(from: String, to: String, zoneID: String, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[AddZone] {
  def mutateTicketID(newTicketID: String): AddZone = AddZone(from = from, to = to, zoneID = zoneID, gas = gas, status = status, txHash = txHash, ticketID = newTicketID, mode = mode, code = code)
}

@Singleton
class AddZones @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, masterZoneKYCs: master.ZoneKYCs, transactionAddZone: transactions.AddZone, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts, blockchainZones: models.blockchain.Zones, masterZones: master.Zones)(implicit configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_ADD_ZONE

  private implicit val logger: Logger = Logger(this.getClass)

  private val schedulerExecutionContext:ExecutionContext= actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val addZoneTable = TableQuery[AddZoneTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(addZone: AddZone): Future[String] = db.run((addZoneTable returning addZoneTable.map(_.ticketID) += addZone).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(addZone: AddZone): Future[Int] = db.run(addZoneTable.insertOrUpdate(addZone).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[AddZone] = db.run(addZoneTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(addZoneTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(addZoneTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(addZoneTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(addZoneTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(addZoneTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(addZoneTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update(txHash, status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String): Future[Int] = db.run(addZoneTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class AddZoneTable(tag: Tag) extends Table[AddZone](tag, "AddZone") {

    def * = (from, to, zoneID, gas, status.?, txHash.?, ticketID, mode, code.?) <> (AddZone.tupled, AddZone.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def zoneID = column[String]("zoneID")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(addZone: AddZone): String = Await.result(add(AddZone(from = addZone.from, to = addZone.to, zoneID = addZone.zoneID, gas = addZone.gas, status = addZone.status, txHash = addZone.txHash, ticketID = addZone.ticketID, mode = addZone.mode, code = addZone.code)), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String): Int = Await.result(updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true)), Duration.Inf)

    def markTransactionFailed(ticketID: String, code: String): Int = Await.result(updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def getTransaction(ticketID: String): AddZone = Await.result(findByTicketID(ticketID), Duration.Inf)

    def getTransactionHash(ticketID: String): Option[String] = Await.result(findTransactionHashByTicketID(ticketID), Duration.Inf)

    def getMode(ticketID: String): String = Await.result(findModeByTicketID(ticketID), Duration.Inf)

    def updateTransactionHash(ticketID: String, txHash: String): Int = Await.result(updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash)), Duration.Inf)

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
        val addZone = Service.getTransaction(ticketID)
        blockchainZones.Service.create(addZone.zoneID, addZone.to, dirtyBit = true)
        masterZones.Service.verifyZone(addZone.zoneID)
        masterAccounts.Service.updateUserTypeOnAddress(addZone.to, constants.User.ZONE)
        val zoneAccountId = masterAccounts.Service.getId(addZone.to)
        masterZoneKYCs.Service.verifyAll(zoneAccountId)
        blockchainAccounts.Service.markDirty(addZone.from)
        utilitiesNotification.send(zoneAccountId, constants.Notification.SUCCESS, blockResponse.txhash)
        utilitiesNotification.send(masterAccounts.Service.getId(addZone.from), constants.Notification.SUCCESS, blockResponse.txhash)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val addZone = Service.getTransaction(ticketID)
        utilitiesNotification.send(masterAccounts.Service.getId(addZone.to), constants.Notification.FAILURE, message)
        utilitiesNotification.send(masterAccounts.Service.getId(addZone.from), constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure)
    }(schedulerExecutionContext)
  }
}