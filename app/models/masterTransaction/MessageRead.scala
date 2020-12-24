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

case class MessageRead(messageID: String, accountID: String, read: Boolean = false, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class MessageReads @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_MESSAGE_READ

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val messageReadTable = TableQuery[MessageReadTable]

  private def add(messageRead: MessageRead): Future[String] = db.run((messageReadTable returning messageReadTable.map(_.messageID) += messageRead).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getAllChatsByRead(messageIDs: Seq[String]): Future[Seq[MessageRead]] = db.run(messageReadTable.filter(x => x.messageID.inSet(messageIDs) && x.read).result)

  private def updateReadByMessageIDsAndToAccountID(messageIDs: Seq[String], toAccountID: String, read: Boolean): Future[Int] = db.run(messageReadTable.filter(x => x.messageID.inSet(messageIDs) && x.accountID === toAccountID).map(_.read).update(read).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findById(messageID: String): Future[Seq[MessageRead]] = db.run(messageReadTable.filter(_.messageID === messageID).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class MessageReadTable(tag: Tag) extends Table[MessageRead](tag, "MessageRead") {

    def * = (messageID, accountID, read, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (MessageRead.tupled, MessageRead.unapply)

    def messageID = column[String]("messageID", O.PrimaryKey)

    def accountID = column[String]("accountID", O.PrimaryKey)

    def read = column[Boolean]("read")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {
    def create(messageID: String, toAccountID: String): Future[String] = add(MessageRead(messageID, toAccountID))

    def markRead(messageIDs: Seq[String], toAccountID: String): Future[Int] = updateReadByMessageIDsAndToAccountID(messageIDs = messageIDs, toAccountID = toAccountID, read = true)

    def getAllRead(messageIDs: Seq[String]): Future[Seq[MessageRead]] = getAllChatsByRead(messageIDs)
  }

}
