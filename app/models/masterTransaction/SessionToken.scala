package models.masterTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import models.Trait.Logged
import org.joda.time.{DateTime, DateTimeZone}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SessionToken(id: String, sessionTokenHash: String, sessionTokenTime: Long, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class SessionTokens @Inject()(actorSystem: ActorSystem, protected val databaseConfigProvider: DatabaseConfigProvider, configuration: Configuration)(implicit executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_SESSION_TOKEN

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private val sessionTokenTimeout: Long = configuration.get[Long]("play.http.session.token.timeout")

  private implicit val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val sessionTokenTable = TableQuery[SessionTokenTable]

  private def add(sessionToken: SessionToken): Future[String] = db.run((sessionTokenTable returning sessionTokenTable.map(_.id) += sessionToken).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByID(id: String): Future[SessionToken] = db.run(sessionTokenTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def upsert(sessionToken: SessionToken) = db.run(sessionTokenTable.insertOrUpdate(sessionToken).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def getSessionTokenTimeByID(id: String): Future[Long] = db.run(sessionTokenTable.filter(_.id === id).map(_.sessionTokenTime).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getSessionTokenHashByID(id: String): Future[String] = db.run(sessionTokenTable.filter(_.id === id).map(_.sessionTokenHash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def getSessionTimedOutIDs: Future[Seq[String]] = db.run(sessionTokenTable.filter(_.sessionTokenTime < DateTime.now(DateTimeZone.UTC).getMillis - sessionTokenTimeout).map(_.id).result)

  private def deleteByID(id: String) = db.run(sessionTokenTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteByIDs(ids: Seq[String]) = db.run(sessionTokenTable.filter(_.id.inSet(ids)).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class SessionTokenTable(tag: Tag) extends Table[SessionToken](tag, "SessionToken") {

    def * = (id, sessionTokenHash, sessionTokenTime, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (SessionToken.tupled, SessionToken.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def sessionTokenHash = column[String]("sessionTokenHash")

    def sessionTokenTime = column[Long]("sessionTokenTime")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def refresh(id: String): Future[String] = {
      val sessionToken: String = utilities.IDGenerator.hexadecimal
      val upsertToken = upsert(SessionToken(id, util.hashing.MurmurHash3.stringHash(sessionToken).toString, DateTime.now(DateTimeZone.UTC).getMillis))
      for {
        _ <- upsertToken
      } yield sessionToken
    }

    def tryVerifyingSessionToken(id: String, sessionToken: String): Future[Boolean] = {
      getSessionTokenHashByID(id).map { token =>
        if (token == util.hashing.MurmurHash3.stringHash(sessionToken).toString) true
        else throw new BaseException(constants.Response.INVALID_TOKEN)
      }
    }

    def tryVerifyingSessionTokenTime(id: String): Future[Boolean] = {
      getSessionTokenTimeByID(id).map { sessionToken =>
        if (DateTime.now(DateTimeZone.UTC).getMillis - sessionToken < sessionTokenTimeout) true
        else throw new BaseException(constants.Response.TOKEN_TIMEOUT)
      }
    }

    def getTimedOutIDs: Future[Seq[String]] = getSessionTimedOutIDs

    def delete(id: String): Future[Int] = deleteByID(id)

    def deleteSessionTokens(ids: Seq[String]): Future[Int] = deleteByIDs(ids)
  }

  private val runnable = new Runnable {
    def run(): Unit = {
      val ids = Service.getTimedOutIDs

      def deleteSessionTokens(ids: Seq[String]) = Service.deleteSessionTokens(ids)

      val forComplete = (for {
        ids <- ids
        _ <- deleteSessionTokens(ids)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message)
      }
      Await.result(forComplete, Duration.Inf)
    }
  }

  actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = constants.Blockchain.KafkaTxIteratorInitialDelay, delay = constants.Blockchain.KafkaTxIteratorInterval)(runnable)(schedulerExecutionContext)
}

