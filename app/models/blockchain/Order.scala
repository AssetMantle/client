package models.blockchain

import exceptions.BaseException
import javax.inject.Inject
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

case class Order(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], executed: Boolean)

class Orders @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ORDER

  private[models] val orderTable = TableQuery[OrderTable]

  private def add(order: Order)(implicit executionContext: ExecutionContext): Future[String] = db.run((orderTable returning orderTable.map(_.id) += order).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def insertOrUpdateOrder(order: Order)(implicit executionContext: ExecutionContext): Future[Int] = db.run(orderTable.insertOrUpdate(order).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def updateFiatProofHashById(id: String, fiatProofHash: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(orderTable.filter(_.id === id).map(_.fiatProofHash).update(fiatProofHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateAwbProofHashById(id: String, awbProofHash: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(orderTable.filter(_.id === id).map(_.awbProofHash).update(awbProofHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[Order] = db.run(orderTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(orderTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class OrderTable(tag: Tag) extends Table[Order](tag, "Order_BC") {

    def * = (id, fiatProofHash.?, awbProofHash.?, executed) <> (Order.tupled, Order.unapply)
    
    def id = column[String]("id", O.PrimaryKey)

    def fiatProofHash = column[String]("fiatProofHash")

    def awbProofHash = column[String]("awbProofHash")

    def executed = column[Boolean]("executed")
    
  }

  object Service {

    def addOrder(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], executed: Boolean)(implicit executionContext: ExecutionContext): String = Await.result(add(Order(id = id, fiatProofHash = fiatProofHash, awbProofHash = awbProofHash, executed = executed)),Duration.Inf)

    def insertOrUpdate(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], executed: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(insertOrUpdateOrder(Order(id = id, fiatProofHash = fiatProofHash, awbProofHash = awbProofHash, executed = executed)),Duration.Inf)

    def updateFiatProofHash(id: String, fiatProofHash: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateFiatProofHashById(id, fiatProofHash), Duration.Inf)

    def updateAwbProofHash(id: String, awbProofHash: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateAwbProofHashById(id, awbProofHash), Duration.Inf)


  }

}