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

case class MessageReceive(messageID: String, accountID: String, readAt: Option[Timestamp])

@Singleton
class MessageReceives @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_MESSAGE_RECEIVE

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val messageReceiveTable = TableQuery[MessageReceiveTable]

  private def add(messageReceive: MessageReceive): Future[String] = db.run((messageReceiveTable returning messageReceiveTable.map(_.messageID) += messageReceive).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getAllChatsByRead(messageIDs: Seq[String]): Future[Seq[MessageReceive]] = db.run(messageReceiveTable.filter(_.messageID inSet messageIDs).filter(_.readAt.?.isDefined).result.asTry).map{
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(messageReceive: MessageReceive): Future[Int] = db.run(messageReceiveTable.insertOrUpdate(messageReceive).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateReadTime(messageIDs: Seq[String], toAccountID : String, timestamp: Timestamp): Future[Int] = db.run(messageReceiveTable.filter(_.messageID inSet messageIDs).filter(_.accountID === toAccountID).map(_.readAt).update(timestamp).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(messageID: String): Future[Seq[MessageReceive]] = db.run(messageReceiveTable.filter(_.messageID === messageID).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class MessageReceiveTable(tag: Tag) extends Table[MessageReceive](tag, "MessageReceive") {

    def * = (messageID, accountID, readAt.?) <> (MessageReceive.tupled, MessageReceive.unapply)

    def messageID = column[String]("messageID", O.PrimaryKey)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def readAt = column[Timestamp]("readAt")

  }

  object Service {
    def create(messageID: String, toAccountID: String): Future[String] = add(MessageReceive(messageID, toAccountID, None))

    def markRead(messageIDs: Seq[String], toAccountID: String): Future[Int] = updateReadTime(messageIDs, toAccountID, new Timestamp(System.currentTimeMillis))

    def getAllRead(messageIDs: Seq[String]): Future[Seq[MessageReceive]] = {getAllChatsByRead(messageIDs)}
  }


}
