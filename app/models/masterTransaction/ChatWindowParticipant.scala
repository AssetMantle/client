package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class ChatWindowParticipant(accountID: String, chatWindowID: String)

@Singleton
class ChatWindowParticipants @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_CHAT_WINDOW_PARTICIPANT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val chatWindowParticipantTable = TableQuery[ChatWindowParticipantTable]

  private def add(chatWindowParticipant: ChatWindowParticipant): Future[String] = db.run((chatWindowParticipantTable returning chatWindowParticipantTable.map(_.chatWindowID) += chatWindowParticipant).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(chatWindowParticipant: ChatWindowParticipant): Future[Int] = db.run(chatWindowParticipantTable.insertOrUpdate(chatWindowParticipant).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getParticipantsByChatWindows(chatWindowIDs: Seq[String]): Future[Seq[ChatWindowParticipant]] = db.run(chatWindowParticipantTable.filter(_.chatWindowID inSet chatWindowIDs).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getParticipantsByChatWindow(chatWindowID: String): Future[Seq[ChatWindowParticipant]] = db.run(chatWindowParticipantTable.filter(_.chatWindowID === chatWindowID).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }

  }

  private def checkUserExists(accountID:String, chatWindowID: String): Future[Boolean] = db.run(chatWindowParticipantTable.filter(x => x.chatWindowID === chatWindowID && x.accountID===accountID).exists.result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private[models] class ChatWindowParticipantTable(tag: Tag) extends Table[ChatWindowParticipant](tag, "ChatWindowParticipant") {

    def * = (accountID, chatWindowID) <> (ChatWindowParticipant.tupled, ChatWindowParticipant.unapply)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def chatWindowID = column[String]("chatWindowID", O.PrimaryKey)
  }

  object Service {
    def create(accountID: String, chatWindowID: String): Future[String] = add(ChatWindowParticipant(accountID = accountID, chatWindowID = chatWindowID))

    def getParticipants(chatWindowIDs: Seq[String]): Future[Seq[ChatWindowParticipant]] = getParticipantsByChatWindows(chatWindowIDs)

    def getParticipants(chatWindowIDs: String): Future[Seq[ChatWindowParticipant]] = getParticipantsByChatWindow(chatWindowIDs)

    def checkUserInChatWindow(accountID: String, chatWindowID: String): Future[Boolean] = checkUserExists(accountID, chatWindowID)

  }

}
