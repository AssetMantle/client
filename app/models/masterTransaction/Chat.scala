package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class Chat(id: String, accountID: String)

@Singleton
class Chats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_CHAT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val chatParticipantTable = TableQuery[ChatTable]

  private def add(chat: Chat): Future[String] = db.run((chatParticipantTable returning chatParticipantTable.map(_.id) += chat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def addMultiple(chats: Seq[Chat]): Future[Seq[String]] = db.run((chatParticipantTable returning chatParticipantTable ++= chats).asTry).map {
    case Success(result) => chats.map(_.accountID)
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(chat: Chat): Future[Int] = db.run(chatParticipantTable.insertOrUpdate(chat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getParticipantsByChatIDs(ids: Seq[String]): Future[Seq[Chat]] = db.run(chatParticipantTable.filter(_.id inSet ids).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getParticipantsByChatID(id: String): Future[Seq[Chat]] = db.run(chatParticipantTable.filter(_.id === id).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }

  }

  private def checkUserExists(id: String, accountID: String): Future[Boolean] = db.run(chatParticipantTable.filter(x => x.id === id && x.accountID === accountID).exists.result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private[models] class ChatTable(tag: Tag) extends Table[Chat](tag, "Chat") {

    def * = (id, accountID) <> (Chat.tupled, Chat.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID", O.PrimaryKey)
  }

  object Service {
    def create(id: String, accountID: String): Future[String] = add(Chat(id = id, accountID = accountID))

    def createMultipleWithCommonChatID(id: String, accountIDs: String*): Future[Seq[String]] = addMultiple(accountIDs.map(accountID => Chat(id = id, accountID = accountID)))

    def getParticipants(ids: Seq[String]): Future[Seq[Chat]] = getParticipantsByChatIDs(ids)

    def getParticipants(id: String): Future[Seq[Chat]] = getParticipantsByChatID(id)

    def checkUserInChat(id: String, accountID: String): Future[Boolean] = checkUserExists(id = id, accountID = accountID)

  }

}
