package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable.NotificationMessage
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Notification(id: String, accountID: String, message: NotificationMessage, read: Boolean = false, createdOn: Timestamp, createdBy: String, createdOnTimezone: String, updatedOn: Option[Timestamp] = None, updatedBy: Option[String] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  val title: String = Seq(constants.Notification.PUSH_NOTIFICATION_PREFIX, message.template, constants.Notification.TITLE_SUFFIX).mkString(".")

  val messageTemplate: String = Seq(constants.Notification.PUSH_NOTIFICATION_PREFIX, message.template, constants.Notification.MESSAGE_SUFFIX).mkString(".")

  val messageParameters: Seq[String] = message.parameters
}

@Singleton
class Notifications @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_NOTIFICATION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private val nodeID = configuration.get[String]("node.id")

  private val nodeTimezone = configuration.get[String]("node.timezone")

  private val notificationsPerPage = configuration.get[Int]("notifications.perPage")

  case class NotificationSerializable(id: String, accountID: String, message: String, read: Boolean, createdOn: Timestamp, createdBy: String, createdOnTimezone: String, updatedOn: Option[Timestamp], updatedBy: Option[String], updatedOnTimeZone: Option[String]) {
    def deserialize(): Notification = Notification(id = id, accountID = accountID, message = utilities.JSON.convertJsonStringToObject[NotificationMessage](message), read = read, createdOn = createdOn, createdBy = createdBy, createdOnTimezone = createdOnTimezone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(notification: Notification): NotificationSerializable = NotificationSerializable(id = notification.id, accountID = notification.accountID, message = Json.toJson(notification.message).toString(), read = notification.read, createdOn = notification.createdOn, createdBy = notification.createdBy, createdOnTimezone = notification.createdOnTimezone, updatedBy = notification.updatedBy, updatedOn = notification.updatedOn, updatedOnTimeZone = notification.updatedOnTimeZone)

  private[models] val notificationTable = TableQuery[NotificationTable]

  private def add(accountID: String, template: String, parameters: String*): Future[String] = db.run((notificationTable returning notificationTable.map(_.accountID) += serialize(Notification(id = utilities.IDGenerator.hexadecimal, accountID = accountID, message = NotificationMessage(template = template, parameters = parameters), createdOn = new Timestamp(System.currentTimeMillis()), createdBy = nodeID, createdOnTimezone = nodeTimezone))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findNotificationsByAccountId(accountID: String, offset: Int, limit: Int): Future[Seq[NotificationSerializable]] = db.run(notificationTable.filter(_.accountID === accountID).sortBy(_.createdOn.desc).drop(offset).take(limit).result)

  private def findNumberOfReadOnStatusByAccountId(accountID: String, status: Boolean): Future[Int] = db.run(notificationTable.filter(_.accountID === accountID).filter(_.read === status).length.result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateReadById(id: String, status: Boolean): Future[Int] = db.run(notificationTable.filter(_.id === id).map(x => (x.read, x.updatedOn, x.updatedBy, x.updatedOnTimezone)).update((status, new Timestamp(System.currentTimeMillis()), nodeID, nodeTimezone)).asTry).map {
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

    def * = (id, accountID, message, read, createdOn, createdBy, createdOnTimezone, updatedOn.?, updatedBy.?, updatedOnTimezone.?) <> (NotificationSerializable.tupled, NotificationSerializable.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def message = column[String]("message")

    def read = column[Boolean]("read")

    def createdOn = column[Timestamp]("createdOn")

    def createdBy = column[String]("createdBy")

    def createdOnTimezone = column[String]("createdOnTimezone")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedBy = column[String]("updatedBy")

    def updatedOnTimezone = column[String]("updatedOnTimezone")
  }

  object Service {

    def create(accountID: String, notification: constants.Notification, parameters: String*): Future[String] = add(accountID = accountID, template = notification.notificationType, parameters = parameters: _*)

    def get(accountID: String, pageNumber: Int): Future[Seq[Notification]] = findNotificationsByAccountId(accountID = accountID, offset = (pageNumber - 1) * notificationsPerPage, limit = notificationsPerPage).map(serializedNotifications => serializedNotifications.map(_.deserialize()))

    def markAsRead(id: String): Future[Int] = updateReadById(id = id, status = true)

    def getNumberOfUnread(accountID: String): Future[Int] = findNumberOfReadOnStatusByAccountId(accountID = accountID, status = false)

  }

}