package models.master

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

case class Account(id: String, secretHash: String, accountAddress: String, tokenHash: Option[String])

class Accounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  private def add(account: Account): Future[String] = db.run(accountTable returning accountTable.map(_.id) += account)

  private def findById(id: String): Future[Account] = db.run(accountTable.filter(_.id === id).result.head)

  private def checkById(id: String): Future[Boolean] = db.run(accountTable.filter(_.id === id).exists.result)

  private def deleteById(id: String) = db.run(accountTable.filter(_.id === id).delete)

  private def refreshTokenOnId(id: String, tokenHash: Option[String]) = db.run(accountTable.filter(_.id === id).map(_.tokenHash.?).update(tokenHash))

  private[models] class AccountTable(tag: Tag) extends Table[Account](tag, "Account") {

    def * = (id, secretHash, accountAddress, tokenHash.?) <> (Account.tupled, Account.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")

    def accountAddress = column[String]("accountAddress")

    def tokenHash = column[String]("tokenHash")

  }

  object Service {

    def validateLogin(username: String, password: String)(implicit ExecutionContext: ExecutionContext): Boolean = Await.result(findById(username), Duration.Inf).secretHash == util.hashing.MurmurHash3.stringHash(password).toString

    def checkUsernameAvailable(username: String): Boolean = {
      !Await.result(checkById(username), 1.seconds)
    }

    def addLogin(username: String, password: String, accountAddress: String): String = {
      Await.result(add(Account(username, util.hashing.MurmurHash3.stringHash(password).toString, accountAddress, null)), Duration.Inf)
      accountAddress
    }

    def refreshToken(username: String): String = {
      val token: String = (Random.nextInt(899999999) + 100000000).toString
      Await.result(refreshTokenOnId(username, Some(util.hashing.MurmurHash3.stringHash(token).toString)), Duration.Inf)
      token
    }

    def verifySession(username: Option[String], token: Option[String]): Boolean = {
      Await.result(findById(username.getOrElse(return false)), Duration.Inf).tokenHash.getOrElse(return false) == util.hashing.MurmurHash3.stringHash(token.getOrElse(return false)).toString
    }

  }

}