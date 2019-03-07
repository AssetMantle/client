package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class ChangeSellerBid(from: String, to: String, bid: Int, time: Int, pegHash: String, chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class ChangeSellerBids @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val changeSellerBidTable = TableQuery[ChangeSellerBidTable]

  private def add(changeSellerBid: ChangeSellerBid): Future[String] = db.run(changeSellerBidTable returning changeSellerBidTable.map(_.ticketID) += changeSellerBid)

  private def update(changeSellerBid: ChangeSellerBid): Future[Int] = db.run(changeSellerBidTable.insertOrUpdate(changeSellerBid))

  private def findByTicketID(ticketID: String): Future[ChangeSellerBid] = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(changeSellerBidTable.filter(_.ticketID === ticketID).delete)

  private[models] class ChangeSellerBidTable(tag: Tag) extends Table[ChangeSellerBid](tag, "ChangeSellerBid") {

    def * = (from, to, bid, time, pegHash, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (ChangeSellerBid.tupled, ChangeSellerBid.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def bid = column[Int]("bid")

    def time = column[Int]("time")

    def pegHash = column[String]("pegHash")

    def chainID = column[String]("chainID")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }
}