package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.GetResponse
import transactions.responses.TransactionResponse.Response
import utilities.PushNotification

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AddOrganization(from: String, to: String, organizationID: String, zoneID: String, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class AddOrganizations @Inject()(actorSystem: ActorSystem, protected val databaseConfigProvider: DatabaseConfigProvider, masterOrganizationKYCs: master.OrganizationKYCs, transactionAddOrganization: transactions.AddOrganization, getResponse: GetResponse, pushNotification: PushNotification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts, blockchainOrganizations: blockchain.Organizations, masterOrganizations: master.Organizations)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_ADD_ORGANIZATION

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val addOrganizationTable = TableQuery[AddOrganizationTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private def add(addOrganization: AddOrganization)(implicit executionContext: ExecutionContext): Future[String] = db.run((addOrganizationTable returning addOrganizationTable.map(_.ticketID) += addOrganization).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(addOrganization: AddOrganization)(implicit executionContext: ExecutionContext): Future[Int] = db.run(addOrganizationTable.insertOrUpdate(addOrganization).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[AddOrganization] = db.run(addOrganizationTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseCodeOnTicketID(ticketID: String, status: Option[Boolean], responseCode: String): Future[Int] = db.run(addOrganizationTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashStatusAndResponseCodeOnTicketID(ticketID: String, txHash: String, status: Option[Boolean], responseCode: String): Future[Int] = db.run(addOrganizationTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status.?, x.responseCode)).update(txHash, status, responseCode).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(addOrganizationTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String) = db.run(addOrganizationTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class AddOrganizationTable(tag: Tag) extends Table[AddOrganization](tag, "AddOrganization") {

    def * = (from, to, organizationID, zoneID, status.?, txHash.?, ticketID, responseCode.?) <> (AddOrganization.tupled, AddOrganization.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def organizationID = column[String]("organizationID")

    def zoneID = column[String]("zoneID")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }

  object Service {

    def create(from: String, to: String, organizationID: String, zoneID: String, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(AddOrganization(from = from, to = to, organizationID = organizationID, zoneID = zoneID, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseCodeOnTicketID(ticketID, txHash, status = Option(true), responseCode), Duration.Inf)

    def markTransactionFailed(ticketID: String, responseCode: String): Int = Await.result(updateStatusAndResponseCodeOnTicketID(ticketID, status = Option(false), responseCode), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def getTransaction(ticketID: String): AddOrganization = Await.result(findByTicketID(ticketID), Duration.Inf)
  }

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, response.TxHash, response.Code)
        val addOrganization = Service.getTransaction(ticketID)
        blockchainOrganizations.Service.create(addOrganization.organizationID, addOrganization.to, dirtyBit = true)
        masterOrganizations.Service.updateStatus(addOrganization.organizationID, status = true)
        masterAccounts.Service.updateUserType(masterOrganizations.Service.getAccountId(addOrganization.organizationID), constants.User.ORGANIZATION)
        val organizationAccountId = masterAccounts.Service.getId(addOrganization.to)
        masterOrganizationKYCs.Service.verifyAll(organizationAccountId)
        blockchainAccounts.Service.markDirty(masterAccounts.Service.getAddress(addOrganization.from))
        pushNotification.sendNotification(organizationAccountId, constants.Notification.SUCCESS, response.TxHash)
        pushNotification.sendNotification(addOrganization.from, constants.Notification.SUCCESS, response.TxHash)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val addOrganization = Service.getTransaction(ticketID)
        pushNotification.sendNotification(masterAccounts.Service.getId(addOrganization.to), constants.Notification.FAILURE, message)
        pushNotification.sendNotification(addOrganization.from, constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start(Service.getTicketIDsOnStatus, transactionAddOrganization.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }
}