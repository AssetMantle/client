package models.masterTransaction

import actors.ShutdownActor
import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import org.joda.time.{DateTime, DateTimeZone}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SessionToken(id: String, sessionTokenHash: String, sessionTokenTime: Long)

@Singleton
class SessionTokens @Inject()(actorSystem: ActorSystem, shutdownActors: ShutdownActor, masterAccounts: master.Accounts, protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_SESSION_TOKEN

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  private val sessionTokenTimeout: Long = configuration.get[Long]("play.http.session.token.timeout")

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private[models] val sessionTokenTable = TableQuery[SessionTokenTable]

  private def add(sessionToken: SessionToken): Future[String] = db.run((sessionTokenTable returning sessionTokenTable.map(_.id) += sessionToken).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[SessionToken] = db.run(sessionTokenTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def upsert(sessionToken: SessionToken) = db.run(sessionTokenTable.insertOrUpdate(sessionToken).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def getSessionTokenTimeByID(id: String): Future[Long] = db.run(sessionTokenTable.filter(_.id === id).map(_.sessionTokenTime).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getSessionTokenHashByID(id: String): Future[String] = db.run(sessionTokenTable.filter(_.id === id).map(_.sessionTokenHash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getSessionTimedOutIDs: Future[Seq[String]] = db.run(sessionTokenTable.filter(_.sessionTokenTime < DateTime.now(DateTimeZone.UTC).getMillis - sessionTokenTimeout).map(_.id).result)
  
  private def deleteByID(id: String) = db.run(sessionTokenTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByIDs(ids: Seq[String]) = db.run(sessionTokenTable.filter(_.id.inSet(ids)).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SessionTokenTable(tag: Tag) extends Table[SessionToken](tag, "SessionToken") {

    def * = (id, sessionTokenHash, sessionTokenTime) <> (SessionToken.tupled, SessionToken.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def sessionTokenHash = column[String]("sessionTokenHash")

    def sessionTokenTime = column[Long]("sessionTokenTime")

  }

  object Service {

    def refresh(id: String): String = {
      val sessionToken: String = utilities.IDGenerator.hexadecimal
      Await.result(upsert(SessionToken(id, util.hashing.MurmurHash3.stringHash(sessionToken).toString, DateTime.now(DateTimeZone.UTC).getMillis)), Duration.Inf)
      sessionToken
    }

    def tryVerifyingSessionToken(id: String, sessionToken: String): Future[Boolean] = {
      getSessionTokenHashByID(id).map{token=>
        if (token == util.hashing.MurmurHash3.stringHash(sessionToken).toString) true
        else throw new BaseException(constants.Response.INVALID_TOKEN)
      }

    }

    def tryVerifyingSessionTokenTime(id: String): Future[Boolean] = {
      getSessionTokenTimeByID(id).map{sessionToken=>
        if (DateTime.now(DateTimeZone.UTC).getMillis - sessionToken < sessionTokenTimeout) true
        else throw new BaseException(constants.Response.TOKEN_TIMEOUT)
      }
    }

    def getTimedOutIDs: Seq[String] = Await.result(getSessionTimedOutIDs, Duration.Inf)

    def delete(id: String): Future[Int] = deleteByID(id)

    def deleteSessionTokens(ids: Seq[String]): Int = Await.result(deleteByIDs(ids), Duration.Inf)
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    val ids = Service.getTimedOutIDs
    ids.foreach { id =>
      shutdownActors.shutdown(constants.Module.ACTOR_MAIN_ACCOUNT, id)
    }
    masterAccounts.Service.filterTraderIDs(ids).foreach { id =>
      shutdownActors.shutdown(constants.Module.ACTOR_MAIN_ASSET, id)
      shutdownActors.shutdown(constants.Module.ACTOR_MAIN_FIAT, id)
      shutdownActors.shutdown(constants.Module.ACTOR_MAIN_NEGOTIATION, id)
      shutdownActors.shutdown(constants.Module.ACTOR_MAIN_ORDER, id)
    }
    Service.deleteSessionTokens(ids)
  }(schedulerExecutionContext)
}

