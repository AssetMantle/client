package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.joda.time.{DateTime, DateTimeZone}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class Notification(accountID: String, notificationTitle: String, notificationMessage: String, time: Long, read: Boolean, id: String)

@Singleton
class Notifications @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_NOTIFICATION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val notificationTable = TableQuery[NotificationTable]

  private def add(notification: Notification): Future[String] = db.run((notificationTable returning notificationTable.map(_.accountID) += notification).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findNotificationsByAccountId(accountID: String, offset: Int, limit: Int): Future[Seq[Notification]] = db.run(notificationTable.filter(_.accountID === accountID).sortBy(_.time.desc).drop(offset).take(limit).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findNumberOfReadOnStatusByAccountId(accountID: String, status: Boolean): Future[Int] = db.run(notificationTable.filter(_.accountID === accountID).filter(_.read === status).length.result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateReadById(id: String, status: Boolean): Future[Int] = db.run(notificationTable.filter(_.id === id).map(_.read).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(accountID: String) = db.run(notificationTable.filter(_.accountID === accountID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class NotificationTable(tag: Tag) extends Table[Notification](tag, "Notification") {

    def * = (accountID, notificationTitle, notificationMessage, time, read, id) <> (Notification.tupled, Notification.unapply)

    def accountID = column[String]("accountID")

    def notificationTitle = column[String]("notificationTitle")

    def notificationMessage = column[String]("notificationMessage")

    def read = column[Boolean]("read")

    def time = column[Long]("time")

    def id = column[String]("id", O.PrimaryKey)

  }

  object Service {

    def create(accountID: String, notificationTitle: String, notificationMessage: String): String = Await.result(add(Notification(accountID, notificationTitle, notificationMessage, DateTime.now(DateTimeZone.UTC).getMillis, false, Random.nextString(32))), Duration.Inf)

    def get(accountID: String, offset: Int, limit: Int): Seq[Notification] = Await.result(findNotificationsByAccountId(accountID, offset, limit), Duration.Inf)

    def markAsRead(id: String): Int = Await.result(updateReadById(id, status = true), Duration.Inf)

    def getNumberOfUnread(accountID: String): Int = Await.result(findNumberOfReadOnStatusByAccountId(accountID, status = false), Duration.Inf)

  }

}