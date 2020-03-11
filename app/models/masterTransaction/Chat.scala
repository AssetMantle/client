package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import java.sql.Timestamp

import play.api.libs.json.{Json, OWrites, Reads}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Chat(id: String, fromAccountID: String, chatWindowID: String, message: String, replyToID: Option[String], createdAt: Timestamp)

@Singleton
class Chats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_CHAT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val chatTable = TableQuery[ChatTable]

  private def add(chat: Chat): Future[String] = db.run((chatTable returning chatTable.map(_.id) += chat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(chat: Chat): Future[Int] = db.run(chatTable.insertOrUpdate(chat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Chat] = db.run(chatTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findAllChatIDsByChatWindowID(chatWindowID: String): Future[Seq[String]] = db.run(chatTable.filter(_.chatWindowID === chatWindowID).sortBy(_.createdAt.desc).map(_.id).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }


  private def findChatsByChatWindowID(chatWindowID: String, offset: Int, limit: Int): Future[Seq[Chat]] = db.run(chatTable.filter(_.chatWindowID === chatWindowID).sortBy(_.createdAt.desc).drop(offset).take(limit).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findChatByChatWindowID(chatWindowID: String, chatID: String): Future[Chat] = db.run(chatTable.filter(x => x.id === chatID && x.chatWindowID === chatWindowID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String): Future[Int] = db.run(chatTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ChatTable(tag: Tag) extends Table[Chat](tag, "Chat") {

    def * = (id, fromAccountID, chatWindowID, message, replyToID.?, createdAt) <> (Chat.tupled, Chat.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def fromAccountID = column[String]("fromAccountID")

    def chatWindowID = column[String]("chatWindowID")

    def message = column[String]("message")

    def replyToID = column[String]("replyToID")

    def createdAt = column[Timestamp]("createdAt")

  }

  object Service {
    def create(fromAccountID: String, chatWindowID: String, message: String, replyToID: Option[String]): Future[Chat] = {
      val chat = Chat(utilities.IDGenerator.hexadecimal, fromAccountID, chatWindowID, message, replyToID, new Timestamp(System.currentTimeMillis))
      for {
        _ <- add(chat)
      } yield chat
    }

    def get(chatWindowID: String, offset: Int, limit: Int): Future[Seq[Chat]] = findChatsByChatWindowID(chatWindowID = chatWindowID, offset = offset, limit = limit)

    def get(chatWindowID: String, chatID: String): Future[Chat] = findChatByChatWindowID(chatWindowID, chatID)

    def getChatIDs(chatWindowID: String): Future[Seq[String]] = findAllChatIDsByChatWindowID(chatWindowID)

  }

}
