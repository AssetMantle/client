package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class ConfirmSellerBid(from: String, to: String, bid: Int, time: Int, pegHash: String, chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class ConfirmSellerBids @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val confirmSellerBidTable = TableQuery[ConfirmSellerBidTable]

  private def add(confirmSellerBid: ConfirmSellerBid): Future[String] = db.run(confirmSellerBidTable returning confirmSellerBidTable.map(_.ticketID) += confirmSellerBid)

  private def update(confirmSellerBid: ConfirmSellerBid): Future[Int] = db.run(confirmSellerBidTable.insertOrUpdate(confirmSellerBid))

  private def findByTicketID(ticketID: String): Future[ConfirmSellerBid] = db.run(confirmSellerBidTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(confirmSellerBidTable.filter(_.ticketID === ticketID).delete)

  private[models] class ConfirmSellerBidTable(tag: Tag) extends Table[ConfirmSellerBid](tag, "ConfirmSellerBid") {

    def * = (from, to, bid, time, pegHash, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (ConfirmSellerBid.tupled, ConfirmSellerBid.unapply)

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