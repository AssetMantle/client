package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.joda.time.{DateTime, DateTimeZone}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class AccountToken(id: String, notificationToken: Option[String], sessionTokenHash: Option[String], sessionTokenTime: Long)

@Singleton
class AccountTokens @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit configuration: Configuration) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ACCOUNT_TOKEN

  private val sessionTokenTimeout: Long = configuration.get[Long]("sessionToken.timeout")

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

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

  private def checkById(id: String): Future[Boolean] = db.run(accountTokenTable.filter(_.id === id).exists.result)

  private def deleteById(id: String) = db.run(accountTokenTable.filter(_.id === id).delete)

  private[models] class AccountTokenTable(tag: Tag) extends Table[AccountToken](tag, "AccountToken") {

    def * = (id, notificationToken.?, sessionTokenHash.?, sessionTokenTime) <> (AccountToken.tupled, AccountToken.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def notificationToken = column[String]("notificationToken")

    def sessionTokenHash = column[String]("sessionTokenHash")

    def sessionTokenTime = column[Long]("sessionTokenTime")


  }

  object Service {
    def insertOrUpdate(username: String, notificationToken: String): Int = Await.result(upsert(AccountToken(username, null, Option(notificationToken), DateTime.now(DateTimeZone.UTC).getMillis)), Duration.Inf)

    def updateToken(id: String, notificationToken: String): Int = Await.result(upsert(AccountToken(id, Option(notificationToken), null, DateTime.now(DateTimeZone.UTC).getMillis)), Duration.Inf)

    def getTokenById(id: String)(implicit executionContext: ExecutionContext): Option[String] = Await.result(findById(id), Duration.Inf).notificationToken

    def ifExists(id: String): Boolean = Await.result(checkById(id), Duration.Inf)

    def verifySessionToken(username: Option[String], sessionToken: Option[String])(implicit executionContext: ExecutionContext): Boolean = {
      Await.result(findById(username.getOrElse(return false)), Duration.Inf).sessionTokenHash.get == util.hashing.MurmurHash3.stringHash(sessionToken.getOrElse(return false)).toString
    }

    def tryVerifyingSessionToken(username: String, sessionToken: String)(implicit executionContext: ExecutionContext): Boolean = {
      if(Await.result(findById(username), Duration.Inf).sessionTokenHash.get == util.hashing.MurmurHash3.stringHash(sessionToken).toString) true
      else throw new BaseException(constants.Response.INVALID_TOKEN)
    }

    def verifySessionTokenTime(username: Option[String])(implicit executionContext: ExecutionContext): Boolean = {
      (DateTime.now(DateTimeZone.UTC).getMillis - Await.result(findById(username.getOrElse(return false)), Duration.Inf).sessionTokenTime) < configuration.get[Long]("sessionToken.timeout")
    }

    def tryVerifyingSessionTokenTime(username: String)(implicit executionContext: ExecutionContext): Boolean = {
      if ((DateTime.now(DateTimeZone.UTC).getMillis - Await.result(findById(username), Duration.Inf).sessionTokenTime) < sessionTokenTimeout) true
      else throw new BaseException(constants.Response.TOKEN_TIMEOUT)
    }

    def refreshSessionToken(username: String): String = {
      val sessionToken: String = "constant token"
      Await.result(refreshSessionTokenOnId(username, Some(util.hashing.MurmurHash3.stringHash(sessionToken).toString), DateTime.now(DateTimeZone.UTC).getMillis), Duration.Inf)
      sessionToken
    }

    def deleteToken(username: String)(implicit executionContext: ExecutionContext): Boolean = if (Await.result(deleteById(username), Duration.Inf) == 1) true else false
  }

}

