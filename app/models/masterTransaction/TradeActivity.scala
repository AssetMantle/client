package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class TradeActivity(notificationID: String, tradeRoomID: String)

@Singleton
class TradeActivities @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_TRADE_ACTIVITY

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val tradeActivityTable = TableQuery[TradeActivityTable]

  private def add(tradeActivity: TradeActivity): Future[String] = db.run((tradeActivityTable returning tradeActivityTable.map(_.notificationID) += tradeActivity).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(tradeActivity: TradeActivity): Future[Int] = db.run(tradeActivityTable.insertOrUpdate(tradeActivity).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(tradeRoomID: String): Future[Seq[TradeActivity]] = db.run(tradeActivityTable.filter(_.tradeRoomID === tradeRoomID).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(notificationID: String): Future[Int] = db.run(tradeActivityTable.filter(_.notificationID === notificationID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class TradeActivityTable(tag: Tag) extends Table[TradeActivity](tag, "TradeActivity") {

    def * = (notificationID, tradeRoomID) <> (TradeActivity.tupled, TradeActivity.unapply)

    def notificationID = column[String]("notificationID", O.PrimaryKey)

    def tradeRoomID = column[String]("tradeRoomID", O.PrimaryKey)

  }

  object Service {
    def create(notificationID: String, tradeRoomID: String): Future[String] = add(TradeActivity(notificationID,tradeRoomID))

    def getTradeActivity(tradeRoomID: String):Future[Seq[TradeActivity]] = findById(tradeRoomID)
  }

}
