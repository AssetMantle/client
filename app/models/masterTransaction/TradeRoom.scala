package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class TradeRoom(id: String, salesQuoteID: String)

@Singleton
class TradeRooms @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_TRADE_ROOM

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val tradeRoomTable = TableQuery[TradeRoomTable]

  private def add(tradeRoom: TradeRoom): Future[String] = db.run((tradeRoomTable returning tradeRoomTable.map(_.id) += tradeRoom).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(tradeRoom: TradeRoom): Future[Int] = db.run(tradeRoomTable.insertOrUpdate(tradeRoom).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[TradeRoom] = db.run(tradeRoomTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String): Future[Int] = db.run(tradeRoomTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class TradeRoomTable(tag: Tag) extends Table[TradeRoom](tag, "TradeRoom") {

    def * = (id, salesQuoteID) <> (TradeRoom.tupled, TradeRoom.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def salesQuoteID = column[String]("salesQuoteID")
  }

  object Service {
    def create(salesQuoteID: String): Future[String] = {add(TradeRoom(id = utilities.IDGenerator.hexadecimal, salesQuoteID = salesQuoteID))}

  //    def get()
  }

}
