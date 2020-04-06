package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class ChatParticipant(accountID: String, chatID: String)

@Singleton
class ChatParticipants @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_CHAT_PARTICIPANT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val chatParticipantTable = TableQuery[ChatParticipantTable]

  private def add(chatParticipant: ChatParticipant): Future[String] = db.run((chatParticipantTable returning chatParticipantTable.map(_.chatID) += chatParticipant).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(chatParticipant: ChatParticipant): Future[Int] = db.run(chatParticipantTable.insertOrUpdate(chatParticipant).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getParticipantsByChatIDs(chatIDs: Seq[String]): Future[Seq[ChatParticipant]] = db.run(chatParticipantTable.filter(_.chatID inSet chatIDs).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getParticipantsByChatID(chatID: String): Future[Seq[ChatParticipant]] = db.run(chatParticipantTable.filter(_.chatID === chatID).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }

  }

  private def checkUserExists(accountID:String, chatID: String): Future[Boolean] = db.run(chatParticipantTable.filter(x => x.chatID === chatID && x.accountID===accountID).exists.result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private[models] class ChatParticipantTable(tag: Tag) extends Table[ChatParticipant](tag, "ChatParticipant") {

    def * = (accountID, chatID) <> (ChatParticipant.tupled, ChatParticipant.unapply)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def chatID = column[String]("chatID", O.PrimaryKey)
  }

  object Service {
    def create(accountID: String, chatID: String): Future[String] = add(ChatParticipant(accountID = accountID, chatID = chatID))

    def getParticipants(chatIDs: Seq[String]): Future[Seq[ChatParticipant]] = getParticipantsByChatIDs(chatIDs)

    def getParticipants(chatID: String): Future[Seq[ChatParticipant]] = getParticipantsByChatID(chatID)

    def checkUserInChat(accountID: String, chatID: String): Future[Boolean] = checkUserExists(accountID, chatID)

  }

}
