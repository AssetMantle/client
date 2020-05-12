package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Order(id: String, orderID: String, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Orders @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ORDER

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val orderTable = TableQuery[OrderTable]

  private def add(order: Order): Future[String] = db.run((orderTable returning orderTable.map(_.id) += order).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateByOrder(order: Order): Future[Int] = db.run(orderTable.filter(_.id === order.id).update(order).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByOrderID(orderID: String, status: String): Future[Int] = db.run(orderTable.filter(_.orderID === orderID).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetByID(id: String): Future[Order] = db.run(orderTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetByOrderID(orderID: String): Future[Order] = db.run(orderTable.filter(_.orderID === orderID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getByOrderIDs(orderIDs: Seq[String]): Future[Seq[Order]] = db.run(orderTable.filter(_.orderID inSet orderIDs).result)

  private def getByOrderIDsAndStatuses(orderIDs: Seq[String], statuses: Seq[String]): Future[Seq[Order]] = db.run(orderTable.filter(_.orderID inSet orderIDs).filter(_.status inSet statuses).result)

  private def getByID(id: String): Future[Option[Order]] = db.run(orderTable.filter(_.id === id).result.headOption)

  private def deleteById(id: String): Future[Int] = db.run(orderTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private[models] class OrderTable(tag: Tag) extends Table[Order](tag, "Order") {

    def * = (id, orderID, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Order.tupled, Order.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def orderID = column[String]("orderID")

    def status = column[String]("status")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(order: Order): Future[String] = add(order)

    def update(order: Order): Future[Int] = updateByOrder(order)

    def markStatusCompletedByBCOrderID(orderID: String): Future[Int] = updateStatusByOrderID(orderID = orderID, status = constants.Status.Order.COMPLETED)

    def markStatusReversedByBCOrderID(orderID: String): Future[Int] = updateStatusByOrderID(orderID = orderID, status = constants.Status.Order.REVERSED)

    def markBuyerExecuteOrderPendingByBCOrderID(orderID: String): Future[Int] = updateStatusByOrderID(orderID = orderID, status = constants.Status.Order.BUYER_EXECUTE_ORDER_PENDING)

    def markSellerExecuteOrderPendingByBCOrderID(orderID: String): Future[Int] = updateStatusByOrderID(orderID = orderID, status = constants.Status.Order.SELLER_EXECUTE_ORDER_PENDING)

    def get(id: String): Future[Option[Order]] = getByID(id)

    def tryGet(id: String): Future[Order] = tryGetByID(id)

    def tryGetOrderByOrderID(orderID: String): Future[Order] = tryGetByOrderID(orderID)

    def getOrdersByOrderIDs(orderIDs: Seq[String]): Future[Seq[Order]] = getByOrderIDs(orderIDs)

    def getIncompleteOrdersByOrderIDs(orderIDs: Seq[String]): Future[Seq[Order]] = getByOrderIDsAndStatuses(orderIDs, Seq(constants.Status.Order.ASSET_AND_FIAT_PENDING, constants.Status.Order.ASSET_AND_FIAT_PENDING, constants.Status.Order.ASSET_SENT_FIAT_PENDING,
      constants.Status.Order.FIAT_SENT_ASSET_PENDING, constants.Status.Order.BUYER_AND_SELLER_EXECUTE_ORDER_PENDING, constants.Status.Order.BUYER_EXECUTE_ORDER_PENDING, constants.Status.Order.SELLER_EXECUTE_ORDER_PENDING))

    def getCompletedOrdersByOrderIDs(orderIDs: Seq[String]): Future[Seq[Order]] = getByOrderIDsAndStatuses(orderIDs, Seq(constants.Status.Order.COMPLETED, constants.Status.Order.REVERSED, constants.Status.Order.TIMED_OUT))

  }

}