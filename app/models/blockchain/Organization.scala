package models.blockchain

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Organization(id: String, address: String)

class Organizations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val organizationTable = TableQuery[OrganizationTable]

  def add(organization: Organization): Future[String] = db.run(organizationTable returning organizationTable.map(_.id) += organization)

  def findById(id: String): Future[Organization] = db.run(organizationTable.filter(_.id === id).result.head)

  def deleteById(id: String) = db.run(organizationTable.filter(_.id === id).delete)

  private[models] class OrganizationTable(tag: Tag) extends Table[Organization](tag, "Organization_BC") {

    def * = (id, address) <> (Organization.tupled, Organization.unapply)

    def ? = (id.?, address.?).shaped.<>({ r => import r._; _1.map(_ => Organization.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def id = column[String]("id", O.PrimaryKey)

    def address = column[String]("address")

  }

}