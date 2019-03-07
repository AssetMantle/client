package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class AddZone(from: String, to: String, zoneID: String, chainID: String,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class AddZones @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val addZoneTable = TableQuery[AddZoneTable]

  private def add(addZone: AddZone): Future[String] = db.run(addZoneTable returning addZoneTable.map(_.ticketID) += addZone)

  private def update(addZone: AddZone): Future[Int] = db.run(addZoneTable.insertOrUpdate(addZone))

  private def findByTicketID(ticketID: String): Future[AddZone] = db.run(addZoneTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(addZoneTable.filter(_.ticketID === ticketID).delete)

  private[models] class AddZoneTable(tag: Tag) extends Table[AddZone](tag, "AddZone") {

    def * = (from, to, zoneID, chainID, status.?, txHash.?, ticketID, responseCode.?) <> (AddZone.tupled, AddZone.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def zoneID = column[String]("zoneID")

    def chainID = column[String]("chainID")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }
}