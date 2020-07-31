package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendFiatRequest(id: String, traderID: String, ticketID: String, negotiationID: String, amount: MicroNumber, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class SendFiatRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  def serialize(sendFiatRequest: SendFiatRequest): SendFiatRequestSerialized = SendFiatRequestSerialized(id = sendFiatRequest.id, traderID = sendFiatRequest.traderID, ticketID = sendFiatRequest.ticketID, negotiationID = sendFiatRequest.negotiationID, amount = sendFiatRequest.amount.toMicroString, status = sendFiatRequest.status, createdBy = sendFiatRequest.createdBy, createdOn = sendFiatRequest.createdOn, createdOnTimeZone = sendFiatRequest.createdOnTimeZone, updatedBy = sendFiatRequest.updatedBy, updatedOn = sendFiatRequest.updatedOn, updatedOnTimeZone = sendFiatRequest.updatedOnTimeZone)

  case class SendFiatRequestSerialized(id: String, traderID: String, ticketID: String, negotiationID: String, amount: String, status: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: SendFiatRequest = SendFiatRequest(id = id, traderID = traderID, ticketID = ticketID, negotiationID = negotiationID, amount = new MicroNumber(BigInt(amount)), status = status, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_SEND_FIAT_REQUEST

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val sendFiatRequestTable = TableQuery[SendFiatRequestTable]

  private def add(sendFiatRequest: SendFiatRequestSerialized): Future[String] = db.run((sendFiatRequestTable returning sendFiatRequestTable.map(_.id) += sendFiatRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByID(id: String): Future[SendFiatRequestSerialized] = db.run(sendFiatRequestTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByTraderIDsAndStatus(traderIDs: Seq[String], status: String): Future[Seq[SendFiatRequestSerialized]] = db.run(sendFiatRequestTable.filter(_.traderID inSet traderIDs).filter(_.status === status).sortBy(x => x.updatedOn.ifNull(x.createdOn).desc).result)

  private def getByTraderIDAndStatus(traderID: String, status: String): Future[Seq[SendFiatRequestSerialized]] = db.run(sendFiatRequestTable.filter(_.traderID === traderID).filter(_.status === status).result)

  private def getAmountsByNegotiationIDAndStatuses(negotiationID: String, statuses: Seq[String]): Future[Seq[String]] = db.run(sendFiatRequestTable.filter(_.negotiationID === negotiationID).filter(_.status inSet statuses).map(_.amount).result)

  private def getByNegotiationIDsAndStatuses(negotiationIDs: Seq[String], statuses: Seq[String]): Future[Seq[SendFiatRequestSerialized]] = db.run(sendFiatRequestTable.filter(_.negotiationID inSet negotiationIDs).filter(_.status inSet statuses).result)

  private def update(sendFiatRequest: SendFiatRequestSerialized): Future[Int] = db.run(sendFiatRequestTable.update(sendFiatRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusByTicketIDAndStatus(ticketID: String, statusPrecondition: String, status: String): Future[Int] = db.run(sendFiatRequestTable.filter(_.ticketID === ticketID).filter(_.status === statusPrecondition).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByIDAndStatus(id: String, statusPrecondition: String, status: String): Future[Int] = db.run(sendFiatRequestTable.filter(_.id === id).filter(_.status === statusPrecondition).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByID(id: String): Future[Int] = db.run(sendFiatRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class SendFiatRequestTable(tag: Tag) extends Table[SendFiatRequestSerialized](tag, "SendFiatRequest") {

    def * = (id, traderID, ticketID, negotiationID, amount, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (SendFiatRequestSerialized.tupled, SendFiatRequestSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def traderID = column[String]("traderID")

    def ticketID = column[String]("ticketID", O.Unique)

    def negotiationID = column[String]("negotiationID")

    def amount = column[String]("amount")

    def status = column[String]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(traderID: String, ticketID: String, negotiationID: String, amount: MicroNumber): Future[String] = add(serialize(SendFiatRequest(id = utilities.IDGenerator.requestID(), traderID, ticketID, negotiationID, amount, status = constants.Status.SendFiat.AWAITING_BLOCKCHAIN_RESPONSE)))

    def getFiatsInOrder(negotiationID: String): Future[MicroNumber] = getAmountsByNegotiationIDAndStatuses(negotiationID, Seq(constants.Status.SendFiat.BLOCKCHAIN_SUCCESS, constants.Status.SendFiat.SENT)).map(y => new MicroNumber(y.map(x => BigInt(x)).sum))

    def getFiatRequestsInOrders(negotiationIDs: Seq[String]): Future[Seq[SendFiatRequest]] = getByNegotiationIDsAndStatuses(negotiationIDs, Seq(constants.Status.SendFiat.BLOCKCHAIN_SUCCESS, constants.Status.SendFiat.SENT)).map(_.map(_.deserialize))

    def getPendingSendFiatRequests(traderIDs: Seq[String]): Future[Seq[SendFiatRequest]] = getByTraderIDsAndStatus(traderIDs, constants.Status.SendFiat.BLOCKCHAIN_SUCCESS).map(_.map(_.deserialize))

    def getCompleteSendFiatRequests(traderIDs: Seq[String]): Future[Seq[SendFiatRequest]] = getByTraderIDsAndStatus(traderIDs, constants.Status.SendFiat.SENT).map(_.map(_.deserialize))

    def getFailedSendFiatRequests(traderIDs: Seq[String]): Future[Seq[SendFiatRequest]] = getByTraderIDsAndStatus(traderIDs, constants.Status.SendFiat.BLOCKCHAIN_FAILURE).map(_.map(_.deserialize))

    def getPendingSendFiatRequests(traderID: String): Future[Seq[SendFiatRequest]] = getByTraderIDAndStatus(traderID, constants.Status.SendFiat.BLOCKCHAIN_SUCCESS).map(_.map(_.deserialize))

    def getCompleteSendFiatRequests(traderID: String): Future[Seq[SendFiatRequest]] = getByTraderIDAndStatus(traderID, constants.Status.SendFiat.SENT).map(_.map(_.deserialize))

    def getFailedSendFiatRequests(traderID: String): Future[Seq[SendFiatRequest]] = getByTraderIDAndStatus(traderID, constants.Status.SendFiat.BLOCKCHAIN_FAILURE).map(_.map(_.deserialize))

    def markBlockchainSuccess(ticketID: String): Future[Int] = updateStatusByTicketIDAndStatus(ticketID, constants.Status.SendFiat.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.SendFiat.BLOCKCHAIN_SUCCESS)

    def markBlockchainFailure(ticketID: String): Future[Int] = updateStatusByTicketIDAndStatus(ticketID, constants.Status.SendFiat.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.SendFiat.BLOCKCHAIN_FAILURE)

    def markSent(id: String): Future[Int] = updateStatusByIDAndStatus(id, constants.Status.SendFiat.BLOCKCHAIN_SUCCESS, constants.Status.SendFiat.SENT)

  }

}

