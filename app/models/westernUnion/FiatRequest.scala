package models.westernUnion

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class FiatRequest(id: String, traderID: String, transactionAmount: Int, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class FiatRequests @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.WESTERN_UNION_FIAT_REQUEST

  import databaseConfig.profile.api._

  private[models] val fiatRequestTable = TableQuery[FiatRequestTable]

  private def add(fiatRequest: FiatRequest): Future[String] = db.run((fiatRequestTable returning fiatRequestTable.map(_.id) += fiatRequest).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByID(id: String): Future[FiatRequest] = db.run(fiatRequestTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findAllByID(traderID: String): Future[Seq[FiatRequest]] = db.run(fiatRequestTable.filter(_.traderID === traderID).result)

  private def findAllByTraderIDs(traderIDs: Seq[String]): Future[Seq[FiatRequest]] = db.run(fiatRequestTable.filter(_.traderID inSet traderIDs).result)

  private def updateStatusByID(id: String, status: String): Future[Int] = db.run(fiatRequestTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => if (result > 0) {
      result
    } else {
      throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, new NoSuchElementException("ID NOT FOUND, NO ROW UPDATED FOR TRANSACTION ID = " + id))
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def deleteByID(id: String): Future[Int] = db.run(fiatRequestTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getStatusByID(id: String): Future[String] = db.run(fiatRequestTable.filter(_.id === id).map(_.status).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }


  private[models] class FiatRequestTable(tag: Tag) extends Table[FiatRequest](tag, "FiatRequest") {

    def * = (id, traderID, transactionAmount, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (FiatRequest.tupled, FiatRequest.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def traderID = column[String]("traderID")

    def transactionAmount = column[Int]("transactionAmount")

    def status = column[String]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(traderID: String, transactionAmount: Int): Future[String] = add(FiatRequest(id = utilities.IDGenerator.requestID(length = 30), traderID = traderID, transactionAmount = transactionAmount, status = constants.Status.IssueFiat.REQUEST_INITIATED))

    def tryGetByID(id: String): Future[FiatRequest] = findByID(id)

    def getStatus(id: String): Future[String] = getStatusByID(id)

    def getAll(traderID: String) = findAllByID(traderID)

    def getAllByTraderIDs(traderIDs: Seq[String]) = findAllByTraderIDs(traderIDs)

    def markRTCBReceived(id: String, amountRequested: Int, totalRTCBAmount: Int): Future[Int] = {
      if (amountRequested == totalRTCBAmount) {
        updateStatusByID(id, constants.Status.IssueFiat.FULLY_PAID)
      } else if (amountRequested < totalRTCBAmount) {
        updateStatusByID(id, constants.Status.IssueFiat.OVER_PAID)
      } else {
        updateStatusByID(id, constants.Status.IssueFiat.PARTIALLY_PAID)
      }
    }
  }

}
