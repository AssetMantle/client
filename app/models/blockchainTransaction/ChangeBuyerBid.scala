package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class ChangeBuyerBid(from: String, to: String, bid: Int, time: Int, pegHash: String, chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class ChangeBuyerBids @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val changeBuyerBidTable = TableQuery[ChangeBuyerBidTable]

  private def add(changeBuyerBid: ChangeBuyerBid): Future[String] = db.run(changeBuyerBidTable returning changeBuyerBidTable.map(_.ticketID) += changeBuyerBid)

  private def update(changeBuyerBid: ChangeBuyerBid): Future[Int] = db.run(changeBuyerBidTable.insertOrUpdate(changeBuyerBid))

  private def findByTicketID(ticketID: String): Future[ChangeBuyerBid] = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(changeBuyerBidTable.filter(_.ticketID === ticketID).delete)

  private[models] class ChangeBuyerBidTable(tag: Tag) extends Table[ChangeBuyerBid](tag, "ChangeBuyerBid") {

    def * = (from, to, bid, time, pegHash, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (ChangeBuyerBid.tupled, ChangeBuyerBid.unapply)

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