package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class AddOrganization(from: String, to: String, organizationID: String, chainID: String,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class AddOrganizations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val addOrganizationTable = TableQuery[AddOrganizationTable]

  private def add(addOrganization: AddOrganization): Future[String] = db.run(addOrganizationTable returning addOrganizationTable.map(_.ticketID) += addOrganization)

  private def update(addOrganization: AddOrganization): Future[Int] = db.run(addOrganizationTable.insertOrUpdate(addOrganization))

  private def findByTicketID(ticketID: String): Future[AddOrganization] = db.run(addOrganizationTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(addOrganizationTable.filter(_.ticketID === ticketID).delete)

  private[models] class AddOrganizationTable(tag: Tag) extends Table[AddOrganization](tag, "AddOrganization") {

    def * = (from, to, organizationID, chainID, status.?, txHash.?, ticketID, responseCode.?) <> (AddOrganization.tupled, AddOrganization.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def organizationID = column[String]("organizationID")

    def chainID = column[String]("chainID")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }
}