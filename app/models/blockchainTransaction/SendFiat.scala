package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class SendFiat(from: String, to: String, amount: Int, pegHash: String, chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class SendFiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sendFiatTable = TableQuery[SendFiatTable]

  private def add(sendFiat: SendFiat): Future[String] = db.run(sendFiatTable returning sendFiatTable.map(_.ticketID) += sendFiat)

  private def update(sendFiat: SendFiat): Future[Int] = db.run(sendFiatTable.insertOrUpdate(sendFiat))

  private def findByTicketID(ticketID: String): Future[SendFiat] = db.run(sendFiatTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(sendFiatTable.filter(_.ticketID === ticketID).delete)

  private[models] class SendFiatTable(tag: Tag) extends Table[SendFiat](tag, "SendFiat") {

    def * = (from, to, amount, pegHash, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (SendFiat.tupled, SendFiat.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def amount = column[Int]("amount")

    def pegHash = column[String]("pegHash")

    def chainID = column[String]("chainID")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }
}