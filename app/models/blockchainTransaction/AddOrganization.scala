package models.blockchainTransaction

import exceptions.BaseException
import javax.inject.Inject
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import akka.actor.ActorSystem
import play.api.libs.ws.WSClient
import transactions.GetResponse

import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AddOrganization(from: String, to: String, organizationID: String, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class AddOrganizations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, transactionAddOrganization: transactions.AddOrganization, getResponse: GetResponse, actorSystem: ActorSystem)(implicit  wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_ADD_ORGANIZATION

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val addOrganizationTable = TableQuery[AddOrganizationTable]

  private def add(addOrganization: AddOrganization)(implicit executionContext: ExecutionContext): Future[String] = db.run((addOrganizationTable returning addOrganizationTable.map(_.ticketID) += addOrganization).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def update(addOrganization: AddOrganization)(implicit executionContext: ExecutionContext): Future[Int] = db.run(addOrganizationTable.insertOrUpdate(addOrganization).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[AddOrganization] = db.run(addOrganizationTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String])(implicit executionContext: ExecutionContext) = db.run(addOrganizationTable.filter(_.ticketID === ticketID).map(_.txHash.?).update(txHash))

  private def updateResponseCodeOnTicketID(ticketID: String, responseCode: String)(implicit executionContext: ExecutionContext) = db.run(addOrganizationTable.filter(_.ticketID === ticketID).map(_.responseCode.?).update(Option(responseCode)))

  private def updateStatusOnTicketID(ticketID: String, status: Boolean)(implicit executionContext: ExecutionContext) = db.run(addOrganizationTable.filter(_.ticketID === ticketID).map(_.status.?).update(Option(status)))

  private def getTicketIDsWithEmptyTxHash()(implicit executionContext: ExecutionContext):Future[Seq[String]] = db.run(addOrganizationTable.filter(_.txHash.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String) = db.run(addOrganizationTable.filter(_.ticketID === ticketID).delete)

  private[models] class AddOrganizationTable(tag: Tag) extends Table[AddOrganization](tag, "AddOrganization") {

    def * = (from, to, organizationID, status.?, txHash.?, ticketID, responseCode.?) <> (AddOrganization.tupled, AddOrganization.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def organizationID = column[String]("organizationID")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }

  if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
    actorSystem.scheduler.schedule(initialDelay = configuration.get[Int]("blockchain.kafka.ticketIterator.initialDelay").seconds, interval = configuration.get[Int]("blockchain.kafka.ticketIterator.interval").second) {
      utilities.TicketIterator.start(Service.geTicketIDsWithEmptyTxHash, transactionAddOrganization.Service.getTxHashFromWSResponse, Service.updateTxHash)
    }
  }

  object Service {

    def addOrganization(from: String, to: String, organizationID: String, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(AddOrganization(from = from, to = to, organizationID = organizationID, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def addOrganizationKafka(from: String, to: String, organizationID: String, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(AddOrganization(from = from, to = to, organizationID = organizationID, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def updateTxHash(ticketID: String, txHash: String) (implicit executionContext: ExecutionContext): Int = Await.result(updateTxHashOnTicketID(ticketID, Option(txHash)),Duration.Inf)

    def updateResponseCode(ticketID: String, responseCode: String) (implicit executionContext: ExecutionContext): Int = Await.result(updateResponseCodeOnTicketID(ticketID, responseCode), Duration.Inf)

    def updateStatus(ticketID: String, status: Boolean) (implicit executionContext: ExecutionContext): Int = Await.result(updateStatusOnTicketID(ticketID, status), Duration.Inf)

    def geTicketIDsWithEmptyTxHash()(implicit executionContext: ExecutionContext): Seq[String] = Await.result(getTicketIDsWithEmptyTxHash(), Duration.Inf)

  }
}