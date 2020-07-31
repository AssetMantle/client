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

case class RedeemFiatRequest(id: String, traderID: String, ticketID: String, amount: MicroNumber, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class RedeemFiatRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  def serialize(redeemFiatRequest: RedeemFiatRequest): RedeemFiatRequestSerialized = RedeemFiatRequestSerialized(id = redeemFiatRequest.id, traderID = redeemFiatRequest.traderID, ticketID = redeemFiatRequest.ticketID, amount = redeemFiatRequest.amount.toMicroString, status = redeemFiatRequest.status, createdBy = redeemFiatRequest.createdBy, createdOn = redeemFiatRequest.createdOn, createdOnTimeZone = redeemFiatRequest.createdOnTimeZone, updatedBy = redeemFiatRequest.updatedBy, updatedOn = redeemFiatRequest.updatedOn, updatedOnTimeZone = redeemFiatRequest.updatedOnTimeZone)

  case class RedeemFiatRequestSerialized(id: String, traderID: String, ticketID: String, amount: String, status: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: RedeemFiatRequest = RedeemFiatRequest(id = id, traderID = traderID, ticketID = ticketID, amount = new MicroNumber(BigInt(amount)), status = status, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_REDEEM_FIAT_REQUEST

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val redeemFiatRequestTable = TableQuery[RedeemFiatRequestTable]

  private def add(redeemFiatRequestSerialized: RedeemFiatRequestSerialized): Future[String] = db.run((redeemFiatRequestTable returning redeemFiatRequestTable.map(_.id) += redeemFiatRequestSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByID(id: String): Future[RedeemFiatRequestSerialized] = db.run(redeemFiatRequestTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByTraderIDsAndStatus(traderIDs: Seq[String], status: String): Future[Seq[RedeemFiatRequestSerialized]] = db.run(redeemFiatRequestTable.filter(_.traderID inSet traderIDs).filter(_.status === status).sortBy(x=>x.updatedOn.ifNull(x.createdOn).desc).result)

  private def getByTraderIDAndStatus(traderID: String, status: String): Future[Seq[RedeemFiatRequestSerialized]] = db.run(redeemFiatRequestTable.filter(_.traderID === traderID).filter(_.status === status).sortBy(x=>x.updatedOn.ifNull(x.createdOn).desc).result)

  private def update(redeemFiatRequestSerialized: RedeemFiatRequestSerialized): Future[Int] = db.run(redeemFiatRequestTable.update(redeemFiatRequestSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateStatusByTicketIDAndStatus(ticketID: String, statusPrecondition: String, status: String): Future[Int] = db.run(redeemFiatRequestTable.filter(_.ticketID === ticketID).filter(_.status === statusPrecondition).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByIDAndStatus(id: String, statusPrecondition: String, status: String): Future[Int] = db.run(redeemFiatRequestTable.filter(_.id === id).filter(_.status === statusPrecondition).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByID(id: String): Future[Int] = db.run(redeemFiatRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class RedeemFiatRequestTable(tag: Tag) extends Table[RedeemFiatRequestSerialized](tag, "RedeemFiatRequest") {

    def * = (id, traderID, ticketID, amount, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (RedeemFiatRequestSerialized.tupled, RedeemFiatRequestSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def traderID = column[String]("traderID")

    def ticketID = column[String]("ticketID", O.Unique)

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
    def create(traderID: String, ticketID: String, amount: MicroNumber): Future[String] = add(serialize(RedeemFiatRequest(id = utilities.IDGenerator.requestID(), traderID, ticketID, amount, status = constants.Status.RedeemFiat.AWAITING_BLOCKCHAIN_RESPONSE)))

    def getPendingRedeemFiatRequests(traderIDs: Seq[String]): Future[Seq[RedeemFiatRequest]] = getByTraderIDsAndStatus(traderIDs, constants.Status.RedeemFiat.BLOCKCHAIN_SUCCESS).map(_.map(_.deserialize))

    def getCompleteRedeemFiatRequests(traderIDs: Seq[String]): Future[Seq[RedeemFiatRequest]] = getByTraderIDsAndStatus(traderIDs, constants.Status.RedeemFiat.REDEEMED).map(_.map(_.deserialize))

    def getFailedRedeemFiatRequests(traderIDs: Seq[String]): Future[Seq[RedeemFiatRequest]] = getByTraderIDsAndStatus(traderIDs, constants.Status.RedeemFiat.BLOCKCHAIN_FAILURE).map(_.map(_.deserialize))

    def getPendingRedeemFiatRequests(traderID: String): Future[Seq[RedeemFiatRequest]] = getByTraderIDAndStatus(traderID, constants.Status.RedeemFiat.BLOCKCHAIN_SUCCESS).map(_.map(_.deserialize))

    def getCompleteRedeemFiatRequests(traderID: String): Future[Seq[RedeemFiatRequest]] = getByTraderIDAndStatus(traderID, constants.Status.RedeemFiat.REDEEMED).map(_.map(_.deserialize))

    def getFailedRedeemFiatRequests(traderID: String): Future[Seq[RedeemFiatRequest]] = getByTraderIDAndStatus(traderID, constants.Status.RedeemFiat.BLOCKCHAIN_FAILURE).map(_.map(_.deserialize))

    def markBlockchainSuccess(ticketID: String): Future[Int] = updateStatusByTicketIDAndStatus(ticketID, constants.Status.RedeemFiat.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.RedeemFiat.BLOCKCHAIN_SUCCESS)

    def markBlockchainFailure(ticketID: String): Future[Int] = updateStatusByTicketIDAndStatus(ticketID, constants.Status.RedeemFiat.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.RedeemFiat.BLOCKCHAIN_FAILURE)

    def markRedeemed(id: String): Future[Int] = updateStatusByIDAndStatus(id, constants.Status.RedeemFiat.BLOCKCHAIN_SUCCESS, constants.Status.RedeemFiat.REDEEMED)

  }

}

