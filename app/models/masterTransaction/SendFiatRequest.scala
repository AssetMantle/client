package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Node
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendFiatRequest(id: String, traderID: String, ticketID: String, negotiationID: String, amount: Int, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged[SendFiatRequest] {

  def createLog()(implicit node: Node): SendFiatRequest = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimeZone = Option(node.timeZone))

  def updateLog()(implicit node: Node): SendFiatRequest = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

@Singleton
class SendFiatRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_SEND_FIAT_REQUEST

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  private[models] val sendFiatTable = TableQuery[SendFiatTable]

  private def add(sendFiat: SendFiatRequest): Future[String] = db.run((sendFiatTable returning sendFiatTable.map(_.id) += sendFiat.createLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[SendFiatRequest] = db.run(sendFiatTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getByTraderIDsAndStatus(traderIDs: Seq[String], status: String): Future[Seq[SendFiatRequest]] = db.run(sendFiatTable.filter(_.traderID inSet traderIDs).filter(_.status === status).result)

  private def getByTraderIDAndStatus(traderID: String, status: String): Future[Seq[SendFiatRequest]] = db.run(sendFiatTable.filter(_.traderID === traderID).filter(_.status === status).result)

  private def update(sendFiat: SendFiatRequest): Future[Int] = db.run(sendFiatTable.update(sendFiat.updateLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateStatusByTicketIDAndStatus(ticketID: String, statusPrecondition: String, status: String): Future[Int] = db.run(sendFiatTable.filter(_.ticketID === ticketID).filter(_.status === statusPrecondition).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByIDAndStatus(id: String, statusPrecondition: String, status: String): Future[Int] = db.run(sendFiatTable.filter(_.id === id).filter(_.status === statusPrecondition).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByID(id: String): Future[Int] = db.run(sendFiatTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SendFiatTable(tag: Tag) extends Table[SendFiatRequest](tag, "SendFiatRequest") {

    def * = (id, traderID, ticketID, negotiationID, amount, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (SendFiatRequest.tupled, SendFiatRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def traderID = column[String]("traderID")

    def ticketID = column[String]("ticketID", O.Unique)

    def negotiationID = column[String]("negotiationID")

    def amount = column[Int]("amount")

    def status = column[String]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(traderID: String, ticketID: String, negotiationID: String, amount: Int): Future[String] = add(SendFiatRequest(id = utilities.IDGenerator.requestID(), traderID, ticketID, negotiationID, amount, status = constants.Status.SendFiat.AWAITING_BLOCKCHAIN_RESPONSE))

    def getPendingSendFiatRequests(traderIDs: Seq[String]): Future[Seq[SendFiatRequest]] = getByTraderIDsAndStatus(traderIDs, constants.Status.SendFiat.BLOCKCHAIN_SUCCESS)

    def getCompleteSendFiatRequests(traderIDs: Seq[String]): Future[Seq[SendFiatRequest]] = getByTraderIDsAndStatus(traderIDs, constants.Status.SendFiat.SENT)

    def getFailedSendFiatRequests(traderIDs: Seq[String]): Future[Seq[SendFiatRequest]] = getByTraderIDsAndStatus(traderIDs, constants.Status.SendFiat.BLOCKCHAIN_FAILURE)

    def getPendingSendFiatRequests(traderID: String): Future[Seq[SendFiatRequest]] = getByTraderIDAndStatus(traderID, constants.Status.SendFiat.BLOCKCHAIN_SUCCESS)

    def getCompleteSendFiatRequests(traderID: String): Future[Seq[SendFiatRequest]] = getByTraderIDAndStatus(traderID, constants.Status.SendFiat.SENT)

    def getFailedSendFiatRequests(traderID: String): Future[Seq[SendFiatRequest]] = getByTraderIDAndStatus(traderID, constants.Status.SendFiat.BLOCKCHAIN_FAILURE)

    def markBlockchainSuccess(ticketID: String): Future[Int] = updateStatusByTicketIDAndStatus(ticketID, constants.Status.SendFiat.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.SendFiat.BLOCKCHAIN_SUCCESS)

    def markBlockchainFailure(ticketID: String): Future[Int] = updateStatusByTicketIDAndStatus(ticketID, constants.Status.SendFiat.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.SendFiat.BLOCKCHAIN_FAILURE)

    def markSent(id: String): Future[Int] = updateStatusByIDAndStatus(id, constants.Status.SendFiat.BLOCKCHAIN_SUCCESS, constants.Status.SendFiat.SENT)

  }

}

