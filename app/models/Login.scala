package models

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Login(userName: String, address: String, zoneID: Int, organizationID: String, passwordHash: String, phone: String, email: String)

class Logins @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val loginTable = TableQuery[LoginTable]

  def add(login: Login): Future[String] = db.run(loginTable returning loginTable.map(_.userName) += login)

  def findByUsername(username: String): Future[Login] = db.run(loginTable.filter(_.userName === username).result.head)

  def deleteByUsername(username: String): DBIO[Int] = loginTable.filter(_.userName === username).delete

  private[models] class LoginTable(tag: Tag) extends Table[Login](tag, "login") {

    def * = (userName, address, zoneID, organizationID, passwordHash, phone, email) <> (Login.tupled, Login.unapply)

    def ? = (userName.?, address.?, zoneID.?, organizationID.?, passwordHash.?, phone.?, email.?).shaped.<>({ r => import r._; _1.map(_ => Login.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def userName = column[String]("username", O.PrimaryKey)

    def address = column[String]("address")

    def zoneID = column[Int]("zoneid")

    def organizationID = column[String]("organizationid")

    def passwordHash = column[String]("passwordhash")

    def phone = column[String]("phone")

    def email = column[String]("email")
  }

}