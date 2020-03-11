package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import java.sql.Timestamp

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class ChatReceive(chatID: String, toAccountID: String, readAt: Option[Timestamp])

@Singleton
class ChatReceives @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_CHAT_RECEIVE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val chatReceiveTable = TableQuery[ChatReceiveTable]

  private def add(chatReceive: ChatReceive): Future[String] = db.run((chatReceiveTable returning chatReceiveTable.map(_.chatID) += chatReceive).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getAllChatsByRead(chatIDs: Seq[String]): Future[Seq[ChatReceive]] = db.run(chatReceiveTable.filter(_.chatID inSet chatIDs).filter(_.readAt.?.isDefined).result.asTry).map{
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(chatReceive: ChatReceive): Future[Int] = db.run(chatReceiveTable.insertOrUpdate(chatReceive).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateReadTime(chatIDs: Seq[String], toAccountID : String, timestamp: Timestamp): Future[Int] = db.run(chatReceiveTable.filter(_.chatID inSet chatIDs).filter(_.toAccountID === toAccountID).map(_.readAt).update(timestamp).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(chatID: String): Future[Seq[ChatReceive]] = db.run(chatReceiveTable.filter(_.chatID === chatID).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class ChatReceiveTable(tag: Tag) extends Table[ChatReceive](tag, "ChatReceive") {

    def * = (chatID, toAccountID, readAt.?) <> (ChatReceive.tupled, ChatReceive.unapply)

    def chatID = column[String]("chatID", O.PrimaryKey)

    def toAccountID = column[String]("toAccountID", O.PrimaryKey)

    def readAt = column[Timestamp]("readAt")

  }

  object Service {
    def create(chatID: String, toAccountID: String): Future[String] = add(ChatReceive(chatID, toAccountID, None))

    def markRead(chatIDs: Seq[String], toAccountID: String): Future[Int] = updateReadTime(chatIDs, toAccountID, new Timestamp(System.currentTimeMillis))

    def getAllRead(chatIDs: Seq[String]): Future[Seq[ChatReceive]] = {getAllChatsByRead(chatIDs)}
  }


}
