package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import org.graalvm.compiler.nodes.memory.address.AddressNode.Address
import org.joda.time.{DateTime, DateTimeZone}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import actors.{Actor, ShutdownActors}
import akka.actor.ActorSystem

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AccountToken(id: String, notificationToken: Option[String], sessionTokenHash: Option[String], sessionTokenTime: Option[Long])

@Singleton
class AccountTokens @Inject()(actorSystem: ActorSystem, shutdownActors: ShutdownActors, masterAccounts: master.Accounts, protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ACCOUNT_TOKEN

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val sessionTokenTimeout: Long = configuration.get[Long]("sessionToken.timeout")

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private[models] val accountTokenTable = TableQuery[AccountTokenTable]

  private def add(accountToken: AccountToken)(implicit executionContext: ExecutionContext): Future[String] = db.run((accountTokenTable returning accountTokenTable.map(_.id) += accountToken).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[AccountToken] = db.run(accountTokenTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def upsert(accountToken: AccountToken) = db.run(accountTokenTable.insertOrUpdate(accountToken))

  private def refreshSessionTokenOnId(id: String, tokenHash: Option[String], tokenTime: Long) = db.run(accountTokenTable.filter(_.id === id).map(accountTokenTable => (accountTokenTable.sessionTokenHash.?, accountTokenTable.sessionTokenTime)).update(tokenHash, tokenTime))

  private def getSessionTimedOutIds: Future[Seq[String]] = db.run(accountTokenTable.filter(_.sessionTokenTime.?.isDefined).filter(_.sessionTokenTime < DateTime.now(DateTimeZone.UTC).getMillis - sessionTokenTimeout).map(_.id).result)

  private def setSessionTokenTimeByIds(ids: Seq[String], sessionTokenTime: Option[Long]) = db.run(accountTokenTable.filter(_.id.inSet(ids)).map(_.sessionTokenTime.?).update(sessionTokenTime))

  private def setSessionTokenTimeById(id: String, sessionTokenTime: Option[Long]) = db.run(accountTokenTable.filter(_.id === id).map(_.sessionTokenTime.?).update(sessionTokenTime))

  private def checkById(id: String): Future[Boolean] = db.run(accountTokenTable.filter(_.id === id).exists.result)

  private def deleteById(id: String) = db.run(accountTokenTable.filter(_.id === id).delete)

  private[models] class AccountTokenTable(tag: Tag) extends Table[AccountToken](tag, "AccountToken") {

    def * = (id, notificationToken.?, sessionTokenHash.?, sessionTokenTime.?) <> (AccountToken.tupled, AccountToken.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def notificationToken = column[String]("notificationToken")

    def sessionTokenHash = column[String]("sessionTokenHash")

    def sessionTokenTime = column[Long]("sessionTokenTime")

  }

  object Service {
    def insertOrUpdate(username: String, notificationToken: String): Int = Await.result(upsert(AccountToken(username, null, Option(notificationToken), Option(DateTime.now(DateTimeZone.UTC).getMillis))), Duration.Inf)

    def updateToken(id: String, notificationToken: String): Int = Await.result(upsert(AccountToken(id, Option(notificationToken), null, Option(DateTime.now(DateTimeZone.UTC).getMillis))), Duration.Inf)

    def getTokenById(id: String)(implicit executionContext: ExecutionContext): Option[String] = Await.result(findById(id), Duration.Inf).notificationToken

    def getSessionTokenTimeById(id: String)(implicit executionContext: ExecutionContext): Long = Await.result(findById(id), Duration.Inf).sessionTokenTime.getOrElse(0.toLong)

    def ifExists(id: String): Boolean = Await.result(checkById(id), Duration.Inf)

    def verifySessionToken(username: Option[String], sessionToken: Option[String])(implicit executionContext: ExecutionContext): Boolean = {
      Await.result(findById(username.getOrElse(return false)), Duration.Inf).sessionTokenHash.get == util.hashing.MurmurHash3.stringHash(sessionToken.getOrElse(return false)).toString
    }

    def tryVerifyingSessionToken(username: String, sessionToken: String)(implicit executionContext: ExecutionContext): Boolean = {
      if (Await.result(findById(username), Duration.Inf).sessionTokenHash.get == util.hashing.MurmurHash3.stringHash(sessionToken).toString) true
      else throw new BaseException(constants.Response.INVALID_TOKEN)
    }

    def verifySessionTokenTime(username: Option[String])(implicit executionContext: ExecutionContext): Boolean = {
      (DateTime.now(DateTimeZone.UTC).getMillis - Await.result(findById(username.getOrElse(return false)), Duration.Inf).sessionTokenTime.getOrElse(return false)) < sessionTokenTimeout
    }

    def tryVerifyingSessionTokenTime(username: String)(implicit executionContext: ExecutionContext): Boolean = {
      if ((DateTime.now(DateTimeZone.UTC).getMillis - Await.result(findById(username), Duration.Inf).sessionTokenTime.getOrElse(return false)) < sessionTokenTimeout) true
      else throw new BaseException(constants.Response.TOKEN_TIMEOUT)
    }

    def refreshSessionToken(username: String): String = {
      val sessionToken: String = "constant token"
      Await.result(refreshSessionTokenOnId(username, Some(util.hashing.MurmurHash3.stringHash(sessionToken).toString), DateTime.now(DateTimeZone.UTC).getMillis), Duration.Inf)
      sessionToken
    }

    def resetSessionTokenTime(username: String): Int = Await.result(setSessionTokenTimeById(username, null), Duration.Inf)

    def resetSessionTokenTimeByIds(usernames: Seq[String]): Int = Await.result(setSessionTokenTimeByIds(usernames, null), Duration.Inf)

    def getTimedOutIds: Seq[String] = Await.result(getSessionTimedOutIds, Duration.Inf)

    def deleteToken(username: String)(implicit executionContext: ExecutionContext): Boolean = if (Await.result(deleteById(username), Duration.Inf) == 1) true else false
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    val ids = Service.getTimedOutIds
    masterAccounts.Service.getTradersAddressFromIds(ids).foreach { address =>
        shutdownActors.shutdown(constants.Module.ACTOR_MAIN_ACCOUNT, address)
        shutdownActors.shutdown(constants.Module.ACTOR_MAIN_ASSET, address)
        shutdownActors.shutdown(constants.Module.ACTOR_MAIN_FIAT, address)
        shutdownActors.shutdown(constants.Module.ACTOR_MAIN_NEGOTIATION, address)
        shutdownActors.shutdown(constants.Module.ACTOR_MAIN_ORDER, address)
    }
    Service.resetSessionTokenTimeByIds(ids)
  }
}

