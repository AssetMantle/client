package models.master


import exceptions.BaseException
import javax.inject.Inject
import models.masterTransaction.EmailOTP
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Notification(id: String, registrationToken: String)

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
  private def update(notification: Notification) = db.run(notificationTable.insertOrUpdate(notification))

  private def checkById(id: String): Future[Boolean] = db.run(notificationTable.filter(_.id === id).exists.result)

  private def deleteById(id: String) = db.run(notificationTable.filter(_.id === id).delete)

  private[models] class NotificationTable(tag: Tag) extends Table[Notification](tag, "Notification") {

    def * = (id, registrationToken) <> (Notification.tupled, Notification.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def registrationToken = column[String]("registrationToken")

    def ? = (id.?, registrationToken.?).shaped.<>({ r => import r._; _1.map(_ => Notification.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

  }

  object Service {
    def addToken(id: String, registrationToken: String)(implicit executionContext: ExecutionContext) : String = Await.result(add(models.master.Notification(id, registrationToken)),Duration.Inf)
    def updateToken(id: String, token: String):Int= Await.result(update(new Notification(id,token)), Duration.Inf)
    def getTokenById(id: String)(implicit executionContext: ExecutionContext): String = Await.result(findById(id), Duration.Inf).registrationToken
    def ifExists(id: String): Boolean = Await.result(checkById(id), Duration.Inf)
  }

}

