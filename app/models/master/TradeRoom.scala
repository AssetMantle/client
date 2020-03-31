package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class TradeRoom(id: String, salesQuoteID: String, buyerAccountID: String, sellerAccountID: String, financierAccountID: Option[String], chatID: String, status: String)

@Singleton
class TradeRooms @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRADE_ROOM

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

  private def getIDBySalesQuoteID(salesQuoteID: String) = db.run(tradeRoomTable.filter(_.salesQuoteID === salesQuoteID).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTradeListByAccountID(accountID: String) = db.run(tradeRoomTable.filter(x => x.buyerAccountID === accountID || x.sellerAccountID === accountID).result)

  private def checkTraderByAccountIDAndID(id: String, accountID: String): Future[Boolean] = db.run(tradeRoomTable.filter(_.id===id).filter(x => x.buyerAccountID === accountID || x.sellerAccountID === accountID).exists.result)

  private[models] class TradeRoomTable(tag: Tag) extends Table[TradeRoom](tag, "TradeRoom") {

    def * = (id, salesQuoteID, buyerAccountID, sellerAccountID, financierAccountID.?, chatID, status) <> (TradeRoom.tupled, TradeRoom.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def salesQuoteID = column[String]("salesQuoteID")

    def buyerAccountID = column[String]("buyerAccountID")

    def sellerAccountID = column[String]("sellerAccountID")

    def financierAccountID = column[String]("financierAccountID")

    def chatID = column[String]("chatID")

    def status = column[String]("status")

  }

  object Service {

    def create(salesQuoteID: String, buyerAccountID: String, sellerAccountID: String, financierAccountID: Option[String], status: String): Future[String] = add(TradeRoom(id = utilities.IDGenerator.requestID, salesQuoteID = salesQuoteID, buyerAccountID = buyerAccountID, sellerAccountID = sellerAccountID, financierAccountID = financierAccountID, chatID = utilities.IDGenerator.requestID, status = status))

    def get(id: String) = findById(id)

    def getID(salesQuoteID: String) = getIDBySalesQuoteID(salesQuoteID)

    def tradeListByAccountID(accountID: String) = getTradeListByAccountID(accountID)

    def checkTraderInTradeRoom(id: String, accountID: String) = checkTraderByAccountIDAndID(id, accountID)
  }

}
