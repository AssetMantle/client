package models.masterTransaction

import exceptions.BaseException
import models.Trait.Logging
import models.common.Serializable.NotificationTemplate
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.Configuration
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Notification(id: String, notificationTemplate: NotificationTemplate, jsRoute: Option[String], read: Boolean = false, createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging {
  val title: String = Seq(constants.Notification.PUSH_NOTIFICATION_PREFIX, notificationTemplate.template, constants.Notification.TITLE_SUFFIX).mkString(".")

  val template: String = Seq(constants.Notification.PUSH_NOTIFICATION_PREFIX, notificationTemplate.template, constants.Notification.MESSAGE_SUFFIX).mkString(".")

}

@Singleton
class Notifications @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_NOTIFICATION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  import databaseConfig.profile.api._

  private val notificationsPerPage = configuration.get[Int]("notifications.perPage")

  case class NotificationSerializable(id: String, notificationTemplateJson: String, jsRoute: Option[String], read: Boolean, createdBy: Option[String], createdOnMillisEpoch: Option[Long], updatedBy: Option[String], updatedOnMillisEpoch: Option[Long]) {
    def deserialize(): Notification = Notification(id = id, notificationTemplate = utilities.JSON.convertJsonStringToObject[NotificationTemplate](notificationTemplateJson), jsRoute = jsRoute, read = read, createdBy = createdBy, createdOnMillisEpoch = createdOnMillisEpoch, updatedBy = updatedBy, updatedOnMillisEpoch = updatedOnMillisEpoch)
  }

  def serialize(notification: Notification): NotificationSerializable = NotificationSerializable(id = notification.id, notificationTemplateJson = Json.toJson(notification.notificationTemplate).toString, jsRoute = notification.jsRoute, read = notification.read, createdBy = notification.createdBy, createdOnMillisEpoch = notification.createdOnMillisEpoch, updatedBy = notification.updatedBy, updatedOnMillisEpoch = notification.updatedOnMillisEpoch)

  private[models] val notificationTable = TableQuery[NotificationTable]

  private def add(notification: Notification): Future[String] = db.run((notificationTable returning notificationTable.map(_.id) += serialize(notification)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateReadById(id: String, status: Boolean): Future[Int] = db.run(notificationTable.filter(_.id === id).map(_.read).update(status).asTry).map {
    case Success(result) => result match {
      case 0 => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
      case _ => result
    }
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findNotifications(offset: Int, limit: Int): Future[Seq[NotificationSerializable]] = db.run(notificationTable.sortBy(_.createdOnMillisEpoch.desc).drop(offset).take(limit).result)

  private def findAll: Future[Seq[NotificationSerializable]] = db.run(notificationTable.result)

  private[models] class NotificationTable(tag: Tag) extends Table[NotificationSerializable](tag, "Notification") {

    def * = (id, notificationTemplateJson, jsRoute.?, read, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (NotificationSerializable.tupled, NotificationSerializable.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def notificationTemplateJson = column[String]("notificationTemplateJson")

    def jsRoute = column[String]("jsRoute")

    def read = column[Boolean]("read")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

  }

  object Service {

    def create(notification: constants.Notification, parameters: String*)(routeParameters: String): Future[String] = add(Notification(id = utilities.IDGenerator.hexadecimal, notificationTemplate = NotificationTemplate(template = notification.notificationType, parameters = parameters), jsRoute = notification.route.fold[Option[String]](None)(x => Option(utilities.String.getJsRouteString(x, routeParameters)))))

    def getPublic(pageNumber: Int): Future[Seq[Notification]] = findNotifications(offset = (pageNumber - 1) * notificationsPerPage, limit = notificationsPerPage).map(serializedNotifications => serializedNotifications.map(_.deserialize()))

    def markAsRead(id: String): Future[Int] = updateReadById(id = id, status = true)

    def getAll: Future[Seq[Notification]] = findAll.map(_.map(_.deserialize()))
  }

}