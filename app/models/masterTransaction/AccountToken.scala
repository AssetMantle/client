package models.masterTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import org.joda.time.{DateTime, DateTimeZone}
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

case class AccountToken(id: String, notificationToken: String, sessionTokenHash: Option[String], sessionTokenTime: Long)

@Singleton
class AccountTokens @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit configuration: Configuration) {

  private implicit val module: String = constants.Module.MASTER_TRANSACTION_ACCOUNT_TOKEN

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  private val logger: Logger = Logger(this.getClass)

  import databaseConfig.profile.api._

  private[models] val accountTokenTable = TableQuery[AccountTokenTable]

  private def add(accountToken: AccountToken)(implicit executionContext: ExecutionContext): Future[String] = db.run((accountTokenTable returning accountTokenTable.map(_.id) += accountToken).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[AccountToken] = db.run(accountTokenTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def insertOrUpdate(accountToken: AccountToken) = db.run(accountTokenTable.insertOrUpdate(accountToken))

  private def refreshSessionTokenOnId(id: String, tokenHash: Option[String], tokenTime: Long) = db.run(accountTokenTable.filter(_.id === id).map(accountTokenTable => (accountTokenTable.sessionTokenHash.?, accountTokenTable.sessionTokenTime)).update(tokenHash, tokenTime))

  private def checkById(id: String): Future[Boolean] = db.run(accountTokenTable.filter(_.id === id).exists.result)

  private def deleteById(id: String) = db.run(accountTokenTable.filter(_.id === id).delete)

  private[models] class AccountTokenTable(tag: Tag) extends Table[AccountToken](tag, "AccountToken") {

    def * = (id, notificationToken, sessionTokenHash.?, sessionTokenTime) <> (AccountToken.tupled, AccountToken.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def notificationToken = column[String]("notificationToken")

    def sessionTokenHash = column[String]("sessionTokenHash")

    def sessionTokenTime = column[Long]("sessionTokenTime")


  }

  object Service {
    def addToken(id: String, notificationToken: String)(implicit executionContext: ExecutionContext): String = Await.result(add(models.masterTransaction.AccountToken(id, notificationToken, null, DateTime.now(DateTimeZone.UTC).getMillis)), Duration.Inf)

    def updateNotificationToken(id: String, notificationToken: String): Int = Await.result(insertOrUpdate(AccountToken(id, notificationToken, null, DateTime.now(DateTimeZone.UTC).getMillis)), Duration.Inf)

    def getTokenById(id: String)(implicit executionContext: ExecutionContext): String = Await.result(findById(id), Duration.Inf).notificationToken

    def ifExists(id: String): Boolean = Await.result(checkById(id), Duration.Inf)

    def verifySessionToken(username: Option[String], sessionToken: Option[String])(implicit executionContext: ExecutionContext): Boolean = {
      Await.result(findById(username.getOrElse(return false)), Duration.Inf).sessionTokenHash.get == util.hashing.MurmurHash3.stringHash(sessionToken.getOrElse(return false)).toString
    }

    def tryVerifySessionToken(username: String, sessionToken: String)(implicit executionContext: ExecutionContext): Boolean = {
      if(Await.result(findById(username), Duration.Inf).sessionTokenHash.get == util.hashing.MurmurHash3.stringHash(sessionToken).toString) true
      else throw new BaseException(constants.Error.INVALID_TOKEN)
    }

    def verifySessionTokenTime(username: Option[String])(implicit executionContext: ExecutionContext): Boolean = {
      (DateTime.now(DateTimeZone.UTC).getMillis - Await.result(findById(username.getOrElse(return false)), Duration.Inf).sessionTokenTime) < configuration.get[Long]("sessionToken.timeout")
    }

    def tryVerifySessionTokenTime(username: String)(implicit executionContext: ExecutionContext): Boolean = {
      if((DateTime.now(DateTimeZone.UTC).getMillis - Await.result(findById(username), Duration.Inf).sessionTokenTime) < configuration.get[Long]("sessionToken.timeout")) true
      else throw new BaseException(constants.Error.TOKEN_TIMEOUT)
    }

    def refreshSessionToken(username: String): String = {
      val sessionToken: String = (Random.nextInt(899999999) + 100000000).toString
      Await.result(refreshSessionTokenOnId(username, Some(util.hashing.MurmurHash3.stringHash(sessionToken).toString), DateTime.now(DateTimeZone.UTC).getMillis), Duration.Inf)
      sessionToken
    }

    def deleteToken(username: String)(implicit executionContext: ExecutionContext): Boolean = if (Await.result(deleteById(username), Duration.Inf) == 1) true else false
  }

}

