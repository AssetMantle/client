package models.master

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

case class Account(id: String, secretHash: String, accountAddress: String)

class Accounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  private def add(account: Account): Future[String] = db.run(accountTable returning accountTable.map(_.id) += account)

  private def findById(id: String): Future[Account] = db.run(accountTable.filter(_.id === id).result.head)

  private def deleteById(id: String) = db.run(accountTable.filter(_.id === id).delete)

  private[models] class AccountTable(tag: Tag) extends Table[Account](tag, "Account") {

    def * = (id, secretHash, accountAddress) <> (Account.tupled, Account.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")

    def accountAddress = column[String]("accountAddress")

    def ? = (id.?, secretHash.?, accountAddress.?).shaped.<>({ r => import r._; _1.map(_ => Account.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

  }

  object Service {
    def validateLogin(username: String, password: String)(implicit ExecutionContext: ExecutionContext): Boolean = Await.result(findById(username), 1.seconds).secretHash == util.hashing.MurmurHash3.stringHash(password)

  }

}