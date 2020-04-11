package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Database
import models.common.Serializable.ActivityMessage
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Notification(id: String, accountID: String, title: String, message: ActivityMessage, read: Boolean, createdOn: Timestamp, createdBy: String, updatedOn: Option[Timestamp] = None, updatedBy: Option[String] = None, timezone: String) extends Database

@Singleton
class Notifications @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_NOTIFICATION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private val nodeID = configuration.get[String]("node.id")

  private val nodeTimezone = configuration.get[String]("node.timezone")

  private val notificationsPerPageLimit = configuration.get[Int]("notification.notificationsPerPage")

  case class NotificationSerializable(id: String, accountID: String, title: String, message: String, read: Boolean, createdOn: Timestamp, createdBy: String, updatedOn: Option[Timestamp], updatedBy: Option[String], timezone: String) {
    def deserialize(): Notification = Notification(id = id, accountID = accountID, title = title, message = utilities.JSON.convertJsonStringToObject[ActivityMessage](message), read = read, createdOn = createdOn, createdBy = createdBy, updatedBy = updatedBy, updatedOn = updatedOn, timezone = timezone)
  }

  def serialize(notification: Notification): NotificationSerializable =  NotificationSerializable(id = notification.id, accountID = notification.accountID, title = notification.title, message = Json.toJson(notification.message).toString(), read = notification.read, createdOn = notification.createdOn, createdBy = notification.createdBy, updatedBy = notification.updatedBy, updatedOn = notification.updatedOn, timezone = notification.timezone)

  private[models] val notificationTable = TableQuery[NotificationTable]

  private def add(notificationSerializable: NotificationSerializable): Future[String] = db.run((notificationTable returning notificationTable.map(_.accountID) += notificationSerializable).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findNotificationsByAccountId(accountID: String, offset: Int, limit: Int): Future[Seq[NotificationSerializable]] = db.run(notificationTable.filter(_.accountID === accountID).sortBy(_.createdOn.desc).drop(offset).take(limit).result)

  private def findNotificationsByAccountIds(accountIDs: Seq[String], offset: Int, limit: Int): Future[Seq[NotificationSerializable]] = db.run(notificationTable.filter(_.accountID inSet accountIDs).sortBy(_.createdOn.desc).drop(offset).take(limit).result)

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

  private[models] class NotificationTable(tag: Tag) extends Table[NotificationSerializable](tag, "Notification") {

    def * = (id, accountID, title, message, read, createdOn, createdBy, updatedOn.?, updatedBy.?, timezone) <> (NotificationSerializable.tupled, NotificationSerializable.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def title = column[String]("title")

    def message = column[String]("message")

    def read = column[Boolean]("read")

    def createdOn = column[Timestamp]("createdOn")

    def createdBy = column[String]("createdBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedBy = column[String]("updatedBy")

    def timezone = column[String]("timezone")

  }

  object Service {

    def insert(accountID: String, notification: constants.Notification, parameters: String*): Future[String] = add(serialize(Notification(id = utilities.IDGenerator.hexadecimal, accountID = accountID, title = notification.title, message = ActivityMessage(header = notification.message, parameters = parameters), read = false, createdOn = new Timestamp(System.currentTimeMillis()), createdBy = nodeID, timezone = nodeTimezone)))

    def get(accountID: String, pageNumber: Int): Future[Seq[Notification]] = findNotificationsByAccountId(accountID = accountID, offset = (pageNumber - 1) * notificationsPerPageLimit, limit = notificationsPerPageLimit).map(serializedNotifications => serializedNotifications.map(_.deserialize()))

    def getByAccountIDs(accountIDs: Seq[String], pageNumber: Int): Future[Seq[Notification]] = findNotificationsByAccountIds(accountIDs = accountIDs, offset = (pageNumber - 1) * notificationsPerPageLimit, limit = notificationsPerPageLimit).map(serializedNotifications => serializedNotifications.map(_.deserialize()))

    def markAsRead(id: String): Future[Int] = updateReadById(id = id, status = true)

    def getNumberOfUnread(accountID: String): Future[Int] = findNumberOfReadOnStatusByAccountId(accountID = accountID, status = false)

  }

}