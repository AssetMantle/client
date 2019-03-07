package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class RedeemFiat(from: String, to: String, redeemAmount: Int, chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class RedeemFiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val redeemFiatTable = TableQuery[RedeemFiatTable]

  private def add(redeemFiat: RedeemFiat): Future[String] = db.run(redeemFiatTable returning redeemFiatTable.map(_.ticketID) += redeemFiat)

  private def update(redeemFiat: RedeemFiat): Future[Int] = db.run(redeemFiatTable.insertOrUpdate(redeemFiat))

  private def findByTicketID(ticketID: String): Future[RedeemFiat] = db.run(redeemFiatTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(redeemFiatTable.filter(_.ticketID === ticketID).delete)

  private[models] class RedeemFiatTable(tag: Tag) extends Table[RedeemFiat](tag, "RedeemFiat") {

    def * = (from, to, redeemAmount, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (RedeemFiat.tupled, RedeemFiat.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def redeemAmount = column[Int]("redeemAmount")

    def chainID = column[String]("chainID")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }
}