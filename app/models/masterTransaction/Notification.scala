package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable.NotificationTemplate
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Notification(id: String, accountID: Option[String], notificationTemplate: NotificationTemplate, jsRoute: Option[String], read: Boolean = false, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedBy: Option[String] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  val title: String = Seq(constants.Notification.PUSH_NOTIFICATION_PREFIX, notificationTemplate.template, constants.Notification.TITLE_SUFFIX).mkString(".")

  val template: String = Seq(constants.Notification.PUSH_NOTIFICATION_PREFIX, notificationTemplate.template, constants.Notification.MESSAGE_SUFFIX).mkString(".")

}

@Singleton
class Notifications @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_NOTIFICATION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private val notificationsPerPage = configuration.get[Int]("notifications.perPage")

  case class NotificationSerializable(id: String, accountID: Option[String], notificationTemplateJson: String, jsRoute: Option[String], read: Boolean, createdOn: Option[Timestamp], createdBy: Option[String], createdOnTimeZone: Option[String], updatedOn: Option[Timestamp], updatedBy: Option[String], updatedOnTimeZone: Option[String]) {
    def deserialize(): Notification = Notification(id = id, accountID = accountID, notificationTemplate = utilities.JSON.convertJsonStringToObject[NotificationTemplate](notificationTemplateJson),jsRoute = jsRoute,  read = read, createdOn = createdOn, createdBy = createdBy, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(notification: Notification): NotificationSerializable = NotificationSerializable(id = notification.id, accountID = notification.accountID, notificationTemplateJson = Json.toJson(notification.notificationTemplate).toString,jsRoute = notification.jsRoute, read = notification.read, createdOn = notification.createdOn, createdBy = notification.createdBy, createdOnTimeZone = notification.createdOnTimeZone, updatedBy = notification.updatedBy, updatedOn = notification.updatedOn, updatedOnTimeZone = notification.updatedOnTimeZone)

  private[models] val notificationTable = TableQuery[NotificationTable]

  private def add(notification: Notification): Future[String] = db.run((notificationTable returning notificationTable.map(_.accountID) += serialize(notification)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findNotificationsByAccountId(accountID: Option[String], offset: Int, limit: Int): Future[Seq[NotificationSerializable]] = db.run(notificationTable.filter(_.accountID.? === accountID).sortBy(_.createdOn.desc).drop(offset).take(limit).result)

  private def findNumberOfReadOnStatusByAccountId(accountID: String, status: Boolean): Future[Int] = db.run(notificationTable.filter(_.accountID === accountID).filter(_.read === status).length.result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateReadById(id: String, status: Boolean): Future[Int] = db.run(notificationTable.filter(_.id === id).map(_.read).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteById(accountID: String): Future[Int] = db.run(notificationTable.filter(_.accountID === accountID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class NotificationTable(tag: Tag) extends Table[NotificationSerializable](tag, "Notification") {

    def * = (id, accountID.?, notificationTemplateJson, jsRoute.?, read, createdOn.?, createdBy.?, createdOnTimeZone.?, updatedOn.?, updatedBy.?, updatedOnTimeZone.?) <> (NotificationSerializable.tupled, NotificationSerializable.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def notificationTemplateJson = column[String]("notificationTemplateJson")

    def jsRoute = column[String]("jsRoute")

    def read = column[Boolean]("read")

    def createdOn = column[Timestamp]("createdOn")

    def createdBy = column[String]("createdBy")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedBy = column[String]("updatedBy")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(accountID: String, notification: constants.Notification, parameters: String*)(routeParameters: String*): Future[String] = add(Notification(id = utilities.IDGenerator.hexadecimal, accountID = Option(accountID), notificationTemplate = NotificationTemplate(template = notification.notificationType, parameters = parameters),  jsRoute = notification.route.fold[Option[String]](None)(x => Option(utilities.String.getJsRouteString(x, routeParameters: _*)))))

    def get(accountID: String, pageNumber: Int): Future[Seq[Notification]] = findNotificationsByAccountId(accountID = Option(accountID), offset = (pageNumber - 1) * notificationsPerPage, limit = notificationsPerPage).map(serializedNotifications => serializedNotifications.map(_.deserialize()))

    def create(notification: constants.Notification, parameters: String*)(routeParameters: String*): Future[String] = add(Notification(id = utilities.IDGenerator.hexadecimal, accountID = None, notificationTemplate = NotificationTemplate(template = notification.notificationType, parameters = parameters), jsRoute = notification.route.fold[Option[String]](None)(x => Option(utilities.String.getJsRouteString(x, routeParameters: _*)))))

    def getPublic(pageNumber: Int): Future[Seq[Notification]] = findNotificationsByAccountId(accountID = null, offset = (pageNumber - 1) * notificationsPerPage, limit = notificationsPerPage).map(serializedNotifications => serializedNotifications.map(_.deserialize()))

    def markAsRead(id: String): Future[Int] = updateReadById(id = id, status = true)

    def getNumberOfUnread(accountID: String): Future[Int] = findNumberOfReadOnStatusByAccountId(accountID = accountID, status = false)

  }

}