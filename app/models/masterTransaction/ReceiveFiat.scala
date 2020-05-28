package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.Logger
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ReceiveFiat(id: String, traderID: String, orderID: String, amount: Int, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class ReceiveFiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_RECEIVE_FIAT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val receiveFiatTable = TableQuery[ReceiveFiatTable]

  private def add(receiveFiat: ReceiveFiat): Future[String] = db.run((receiveFiatTable returning receiveFiatTable.map(_.id) += receiveFiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByID(id: String): Future[ReceiveFiat] = db.run(receiveFiatTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getByTraderIDsAndStatuses(traderIDs: Seq[String], status: Seq[String]): Future[Seq[ReceiveFiat]] = db.run(receiveFiatTable.filter(_.traderID inSet traderIDs).filter(_.status inSet status).result)

  private def getByTraderIDAndStatuses(traderID: String, status: Seq[String]): Future[Seq[ReceiveFiat]] = db.run(receiveFiatTable.filter(_.traderID === traderID).filter(_.status inSet status).result)

  private def updateStatusByID(id: String, status: String): Future[Int] = db.run(receiveFiatTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByID(id: String): Future[Int] = db.run(receiveFiatTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class ReceiveFiatTable(tag: Tag) extends Table[ReceiveFiat](tag, "ReceiveFiat") {

    def * = (id, traderID, orderID, amount, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (ReceiveFiat.tupled, ReceiveFiat.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def traderID = column[String]("traderID")

    def orderID = column[String]("orderID")

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
    def create(traderID: String, orderID: String, amount: Int, status: String): Future[String] = add(ReceiveFiat(utilities.IDGenerator.requestID(), traderID, orderID, amount, status))

    def get(traderID: String): Future[Seq[ReceiveFiat]] = getByTraderIDAndStatuses(traderID, Seq(constants.Status.ReceiveFiat.ORDER_COMPLETION_FIAT, constants.Status.ReceiveFiat.ORDER_REVERSED_FIAT))

    def get(traderIDs: Seq[String]): Future[Seq[ReceiveFiat]] = getByTraderIDsAndStatuses(traderIDs, Seq(constants.Status.ReceiveFiat.ORDER_COMPLETION_FIAT, constants.Status.ReceiveFiat.ORDER_REVERSED_FIAT))
  }

}

