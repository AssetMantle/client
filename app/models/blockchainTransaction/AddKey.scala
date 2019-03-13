package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class AddKey(name: String, seed: String, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class AddKeys @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val addKeyTable = TableQuery[AddKeyTable]

  private def add(addKey: AddKey): Future[String] = db.run(addKeyTable returning addKeyTable.map(_.ticketID) += addKey)

  private def update(addKey: AddKey): Future[Int] = db.run(addKeyTable.insertOrUpdate(addKey))

  private def findByTicketID(ticketID: String): Future[AddKey] = db.run(addKeyTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(addKeyTable.filter(_.ticketID === ticketID).delete)

  private[models] class AddKeyTable(tag: Tag) extends Table[AddKey](tag, "AddKey") {

    def * = (name, seed, status.?, txHash.?, ticketID, responseCode.?) <> (AddKey.tupled, AddKey.unapply)

    def name = column[String]("name")

    def seed = column[String]("seed")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }
}