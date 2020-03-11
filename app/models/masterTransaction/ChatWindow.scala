package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class ChatWindow(id: String, tradeRoomID: String)

@Singleton
class ChatWindows @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_CHAT_WINDOW

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val chatWindowTable = TableQuery[ChatWindowTable]

  private def add(chatWindow: ChatWindow): Future[String] = db.run((chatWindowTable returning chatWindowTable.map(_.id) += chatWindow).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(chatWindow: ChatWindow): Future[Int] = db.run(chatWindowTable.insertOrUpdate(chatWindow).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[ChatWindow] = db.run(chatWindowTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findTradeRoomIDByID(id: String): Future[String] = db.run(chatWindowTable.filter(_.id === id).map(_.tradeRoomID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getAllChatWindowsByTradeRoomID(tradeRoomID: String): Future[Seq[ChatWindow]] = db.run(chatWindowTable.filter(_.tradeRoomID === tradeRoomID).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def deleteById(id: String): Future[Int] = db.run(chatWindowTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ChatWindowTable(tag: Tag) extends Table[ChatWindow](tag, "ChatWindow") {

    def * = (id, tradeRoomID) <> (ChatWindow.tupled, ChatWindow.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def tradeRoomID = column[String]("tradeRoomID")
  }

  object Service {
    def create(tradeRoomID: String): Future[String] = {add(ChatWindow(id = utilities.IDGenerator.hexadecimal, tradeRoomID = tradeRoomID))}

    def getTradeRoomID(id: String): Future[String] = findTradeRoomIDByID(id)

    def getAllChatWindows(tradeRoomID: String): Future[Seq[ChatWindow]] = getAllChatWindowsByTradeRoomID(tradeRoomID)
  }

}
