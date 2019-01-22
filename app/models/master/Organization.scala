package models.master

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Organization(id: String, secretHash: String, name: String, address: String, phone: String, email: String)

class Organizations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val organizationTable = TableQuery[OrganizationTable]

  private def add(organization: Organization): Future[String] = db.run(organizationTable returning organizationTable.map(_.id) += organization)

  private def findById(id: String): Future[Organization] = db.run(organizationTable.filter(_.id === id).result.head)

  private def deleteById(id: String) = db.run(organizationTable.filter(_.id === id).delete)

  private[models] class OrganizationTable(tag: Tag) extends Table[Organization](tag, "Organization") {

    def * = (id, secretHash, name, address, phone, email) <> (Organization.tupled, Organization.unapply)

    def ? = (id.?, secretHash.?, name.?, address.?, phone.?, email.?).shaped.<>({ r => import r._; _1.map(_ => Organization.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def id = column[String]("id", O.PrimaryKey)

    def secretHash = column[String]("secretHash")

    def name = column[String]("name")

    def address = column[String]("address")

    def phone = column[String]("phone")

    def email = column[String]("email")

  }

}