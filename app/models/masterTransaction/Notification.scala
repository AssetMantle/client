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

  private def findNotificationsByAccountId(accountID: String, offset: Int, limit: Int): Future[Seq[Notification]] = db.run(notificationTable.filter(_.accountID === accountID).sortBy(_.time.desc).drop(offset).take(limit).result)

  private def findNumberOfUnreadByAccountId(accountID: String): Future[Int] = db.run(notificationTable.filter(_.accountID === accountID).filter(_.read === false).length.result)

  private def markReadById(id: String): Future[Int] = db.run(notificationTable.filter(_.id === id).map(_.read).update(true))

  private def checkById(accountID: String): Future[Boolean] = db.run(notificationTable.filter(_.accountID === accountID).exists.result)

  private def deleteById(accountID: String) = db.run(notificationTable.filter(_.accountID === accountID).delete)

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

    def markAsRead(id: String): Int = Await.result(markReadById(id), Duration.Inf)

    def getNumberOfUnread(accountID: String): Int = Await.result(findNumberOfUnreadByAccountId(accountID), Duration.Inf)

  }

}