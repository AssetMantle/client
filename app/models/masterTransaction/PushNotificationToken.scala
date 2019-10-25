package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class PushNotificationToken(id: String, token: String)

@Singleton
class PushNotificationTokens @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_PUSH_NOTIFICATION_TOKEN

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val pushNotificationTokenTable = TableQuery[PushNotificationTokenTable]

  private def add(pushNotificationToken: PushNotificationToken): Future[String] = db.run((pushNotificationTokenTable returning pushNotificationTokenTable.map(_.id) += pushNotificationToken).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[PushNotificationToken] = db.run(pushNotificationTokenTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def upsert(pushNotificationToken: PushNotificationToken): Future[Int] = db.run(pushNotificationTokenTable.insertOrUpdate(pushNotificationToken).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getPushNotificationTokenByID(id: String): Future[String] = db.run(pushNotificationTokenTable.filter(_.id === id).map(_.token).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByID(id: String): Future[Int] = db.run(pushNotificationTokenTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class PushNotificationTokenTable(tag: Tag) extends Table[PushNotificationToken](tag, "PushNotificationToken") {

    def * = (id, token) <> (PushNotificationToken.tupled, PushNotificationToken.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def token = column[String]("token")

  }

  object Service {

    def update(id: String, token: String): Future[Int] = upsert(PushNotificationToken(id, token = token))

    def getPushNotificationToken(id: String): String = Await.result(getPushNotificationTokenByID(id), Duration.Inf)

    def delete(id: String): Future[Int] = deleteByID(id)

  }
}

