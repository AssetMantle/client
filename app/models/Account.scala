package models

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Account(userName: String, address: String, zoneID: Int, organizationID: String, passwordHash: String, phone: String, email: String)

class Accounts @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val accountTable = TableQuery[AccountTable]

  def add(account: Account): Future[String] = db.run(accountTable returning accountTable.map(_.userName) += account)

  def findByUsername(username: String): Future[Account] = db.run(accountTable.filter(_.userName === username).result.head)

  def deleteByUsername(username: String): DBIO[Int] = accountTable.filter(_.userName === username).delete

  private[models] class AccountTable(tag: Tag) extends Table[Account](tag, "Account") {

    def * = (userName, address, zoneID, organizationID, passwordHash, phone, email) <> (Account.tupled, Account.unapply)

    def ? = (userName.?, address.?, zoneID.?, organizationID.?, passwordHash.?, phone.?, email.?).shaped.<>({ r => import r._; _1.map(_ => Account.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def userName = column[String]("username", O.PrimaryKey)

    def address = column[String]("address")

    def zoneID = column[Int]("zoneID")

    def organizationID = column[String]("organizationID")

    def passwordHash = column[String]("passwordHash")

    def phone = column[String]("phone")

    def email = column[String]("email")
  }

}