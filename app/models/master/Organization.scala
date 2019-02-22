package models.master

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Random

case class Organization(id: String, accountID: String, name: String, address: String, phone: String, email: String, status: Option[Boolean])

class Organizations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val organizationTable = TableQuery[OrganizationTable]

  private def add(organization: Organization): Future[String] = db.run(organizationTable returning organizationTable.map(_.id) += organization)

  private def findById(id: String): Future[Organization] = db.run(organizationTable.filter(_.id === id).result.head)

  private def deleteById(id: String) = db.run(organizationTable.filter(_.id === id).delete)

  private[models] class OrganizationTable(tag: Tag) extends Table[Organization](tag, "Organization") {

    def * = (id, accountID, name, address, phone, email, status.?) <> (Organization.tupled, Organization.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def accountID = column[String]("accountID")

    def name = column[String]("name")

    def address = column[String]("address")

    def phone = column[String]("phone")

    def email = column[String]("email")

    def status = column[Boolean]("status")

  }

  object Service {
    def addOrganization(accountID: String, name: String, address: String, phone: String, email: String): String = Await.result(add(Organization(Random.nextInt.toHexString.toUpperCase, accountID, name, address, phone, email, null)), Duration.Inf)
  }

}