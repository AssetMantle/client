package models.masterTransaction

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

case class Chat(id: String, accountID: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Chats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_CHAT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val chatTable = TableQuery[ChatTable]

  private def add(chat: Chat): Future[String] = db.run((chatTable returning chatTable.map(_.id) += chat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def addMultiple(chats: Seq[Chat]): Future[Seq[String]] = db.run((chatTable returning chatTable ++= chats).asTry).map {
    case Success(result) => chats.map(_.accountID)
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def upsert(chat: Chat): Future[Int] = db.run(chatTable.insertOrUpdate(chat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getParticipantsByChatIDs(ids: Seq[String]): Future[Seq[Chat]] = db.run(chatTable.filter(_.id inSet ids).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getParticipantsByChatID(id: String): Future[Seq[Chat]] = db.run(chatTable.filter(_.id === id).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def checkUserExists(id: String, accountID: String): Future[Boolean] = db.run(chatTable.filter(x => x.id === id && x.accountID === accountID).exists.result)

  private def deleteByIDAndAccountID(chatID: String, accountID: String): Future[Int] = db.run(chatTable.filter(x => x.id === chatID && x.accountID === accountID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class ChatTable(tag: Tag) extends Table[Chat](tag, "Chat") {

    def * = (id, accountID, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Chat.tupled, Chat.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {
    def create(accountID: String): Future[String] = add(Chat(id = utilities.IDGenerator.hexadecimal, accountID = accountID))

    def createGroupChat(accountIDs: String*): Future[String] = {
      val chatID = utilities.IDGenerator.hexadecimal
      for {
        _ <- addMultiple(accountIDs.map(accountID => Chat(id = chatID, accountID = accountID)))
      } yield chatID
    }

    def addToGroup(chatID: String, accountID: String): Future[String] = add(Chat(id = chatID, accountID = accountID))

    def addMultipleToGroup(chatID: String, accountIDs: String*): Future[Seq[String]] = addMultiple(accountIDs.map(accountID => Chat(id = chatID, accountID = accountID)))

    def delete(chatID: String, accountID: String): Future[Int] = deleteByIDAndAccountID(chatID = chatID, accountID = accountID)

    def getAllChats(id: String): Future[Seq[Chat]] = getParticipantsByChatID(id)

    def checkUserInChat(id: String, accountID: String): Future[Boolean] = checkUserExists(id = id, accountID = accountID)

  }

}
