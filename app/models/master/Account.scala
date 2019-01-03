package models.master

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Account(id: String, secretHash: String, accountAddress: String)

class Accounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  def add(account: Account): Future[String] = db.run(accountTable returning accountTable.map(_.id) += account)

  def findById(id: String): Future[Account] = db.run(accountTable.filter(_.id === id).result.head)

  def deleteById(id: String) = db.run(accountTable.filter(_.id === id).delete)

  private[models] class AccountTable(tag: Tag) extends Table[Account](tag,"Account") {

    def * = (id, secretHash, accountAddress) <> (Account.tupled, Account.unapply)

    def ? = (id.?, secretHash.?, accountAddress.?).shaped.<>({ r => import r._; _1.map(_ => Account.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))


    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")

    def accountAddress = column[String]("accountAddress")


  }

}