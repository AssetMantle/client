package models.masterTransaction

import exceptions.BaseException
import javax.inject.Inject
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Notification(id: String, notificationTitle: String, notificationMessage: String, time: Long)

class Notifications @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  private implicit val module: String = constants.Module.MASTER_ACCOUNT

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val notificationTable = TableQuery[NotificationTable]

  private def add(notification: Notification)(implicit executionContext: ExecutionContext): Future[String] = db.run((notificationTable returning notificationTable.map(_.id) += notification).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[Notification] = db.run(notificationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findTimeById(id: String)(implicit executionContext: ExecutionContext): Future[Long] = db.run(notificationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result.time
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def checkById(id: String): Future[Boolean] = db.run(notificationTable.filter(_.id === id).exists.result)

  private def deleteById(id: String) = db.run(notificationTable.filter(_.id === id).delete)

  private[models] class NotificationTable(tag: Tag) extends Table[Notification](tag, "Notification") {

    def * = (id, notificationTitle, notificationMessage, time) <> (Notification.tupled, Notification.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def notificationTitle = column[String]("notificationTitle")

    def notificationMessage = column[String]("notificationMessage")

    def time = column[Long]("time", O.PrimaryKey)

  }

  object Service {

    def addNotification(id: String, notificationTitle: String, notificationMessage: String, time: Long)(implicit executionContext: ExecutionContext)= {
      Await.result(add(Notification(id, notificationTitle, notificationMessage, time)), Duration.Inf)
    }

    def getNotification(username: String)(implicit executionContext: ExecutionContext) = Await.result(findById(username), Duration.Inf)

    def getTimeById(id: String)(implicit executionContext: ExecutionContext) = Await.result(findTimeById(id), Duration.Inf)

  }

}