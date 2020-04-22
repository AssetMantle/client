package models.masterTransaction

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Node
import models.common.Serializable.NotificationTemplate
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Notification(id: String, accountID: String, notificationTemplate: NotificationTemplate, read: Boolean = false, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedBy: Option[String] = None, updatedOnTimeZone: Option[String] = None) extends Logged[Notification] {
  val title: String = Seq(constants.Notification.PUSH_NOTIFICATION_PREFIX, notificationTemplate.template, constants.Notification.TITLE_SUFFIX).mkString(".")

  val template: String = Seq(constants.Notification.PUSH_NOTIFICATION_PREFIX, notificationTemplate.template, constants.Notification.MESSAGE_SUFFIX).mkString(".")

  def createLog()(implicit node: Node): Notification = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimeZone = Option(node.timeZone))

  def updateLog()(implicit node: Node): Notification = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

@Singleton
class Notifications @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_NOTIFICATION

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  private val notificationsPerPage = configuration.get[Int]("notifications.perPage")

  case class NotificationSerializable(id: String, accountID: String, notificationTemplateJson: String, read: Boolean, createdOn: Option[Timestamp], createdBy: Option[String], createdOnTimeZone: Option[String], updatedOn: Option[Timestamp], updatedBy: Option[String], updatedOnTimeZone: Option[String]) {
    def deserialize(): Notification = Notification(id = id, accountID = accountID, notificationTemplate = utilities.JSON.convertJsonStringToObject[NotificationTemplate](notificationTemplateJson), read = read, createdOn = createdOn, createdBy = createdBy, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(notification: Notification): NotificationSerializable = NotificationSerializable(id = notification.id, accountID = notification.accountID, notificationTemplateJson = Json.toJson(notification.notificationTemplate).toString, read = notification.read, createdOn = notification.createdOn, createdBy = notification.createdBy, createdOnTimeZone = notification.createdOnTimeZone, updatedBy = notification.updatedBy, updatedOn = notification.updatedOn, updatedOnTimeZone = notification.updatedOnTimeZone)

  private[models] val notificationTable = TableQuery[NotificationTable]

  private def add(notification: Notification): Future[String] = db.run((notificationTable returning notificationTable.map(_.accountID) += serialize(notification.createLog())).asTry).map {
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

  private def updateReadById(id: String, status: Boolean): Future[Int] = db.run(notificationTable.filter(_.id === id).map(x => (x.read, x.updatedOn, x.updatedBy, x.updatedOnTimeZone)).update((status, new Timestamp(System.currentTimeMillis()), node.id, node.timeZone)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(accountID: String): Future[Int] = db.run(notificationTable.filter(_.accountID === accountID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class NotificationTable(tag: Tag) extends Table[NotificationSerializable](tag, "Notification") {

    def * = (id, accountID, notificationTemplateJson, read, createdOn.?, createdBy.?, createdOnTimeZone.?, updatedOn.?, updatedBy.?, updatedOnTimeZone.?) <> (NotificationSerializable.tupled, NotificationSerializable.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def notificationTemplateJson = column[String]("notificationTemplateJson")

    def read = column[Boolean]("read")

    def createdOn = column[Timestamp]("createdOn")

    def createdBy = column[String]("createdBy")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedBy = column[String]("updatedBy")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(accountID: String, notification: constants.Notification, parameters: String*): Future[String] = add(Notification(id = utilities.IDGenerator.hexadecimal, accountID = accountID, notificationTemplate = NotificationTemplate(template = notification.notificationType, parameters = parameters)))

    def get(accountID: String, pageNumber: Int): Future[Seq[Notification]] = findNotificationsByAccountId(accountID = accountID, offset = (pageNumber - 1) * notificationsPerPage, limit = notificationsPerPage).map(serializedNotifications => serializedNotifications.map(_.deserialize()))

    def markAsRead(id: String): Future[Int] = updateReadById(id = id, status = true)

    def getNumberOfUnread(accountID: String): Future[Int] = findNumberOfReadOnStatusByAccountId(accountID = accountID, status = false)

  }

}