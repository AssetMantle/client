package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.HistoryLogged
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class OrderHistory(id: String, orderID: String, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None, deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) extends HistoryLogged

@Singleton
class OrderHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORDER_HISTORY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val orderHistoryTable = TableQuery[OrderHistoryTable]

  private def tryGetByID(id: String): Future[OrderHistory] = db.run(orderHistoryTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getByID(id: String): Future[Option[OrderHistory]] = db.run(orderHistoryTable.filter(_.id === id).result.headOption)

  private[models] class OrderHistoryTable(tag: Tag) extends Table[OrderHistory](tag, "Order_History") {

    def * = (id, orderID, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?, deletedBy, deletedOn, deletedOnTimeZone) <> (OrderHistory.tupled, OrderHistory.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def orderID = column[String]("orderID")

    def status = column[String]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

    def deletedBy = column[String]("deletedBy")

    def deletedOn = column[Timestamp]("deletedOn")

    def deletedOnTimeZone = column[String]("deletedOnTimeZone")

  }

  object Service {

    def get(id: String): Future[Option[OrderHistory]] = getByID(id)

    def tryGet(id: String): Future[OrderHistory] = tryGetByID(id)

  }

}