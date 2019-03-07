package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class BuyerExecuteOrder(from: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class BuyerExecuteOrders @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val buyerExecuteOrderTable = TableQuery[BuyerExecuteOrderTable]

  private def add(buyerExecuteOrder: BuyerExecuteOrder): Future[String] = db.run(buyerExecuteOrderTable returning buyerExecuteOrderTable.map(_.ticketID) += buyerExecuteOrder)

  private def update(buyerExecuteOrder: BuyerExecuteOrder): Future[Int] = db.run(buyerExecuteOrderTable.insertOrUpdate(buyerExecuteOrder))

  private def findByTicketID(ticketID: String): Future[BuyerExecuteOrder] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).delete)

  private[models] class BuyerExecuteOrderTable(tag: Tag) extends Table[BuyerExecuteOrder](tag, "BuyerExecuteOrder") {

    def * = (from, buyerAddress, sellerAddress, fiatProofHash, pegHash, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (BuyerExecuteOrder.tupled, BuyerExecuteOrder.unapply)

    def from = column[String]("from")

    def buyerAddress = column[String]("buyerAddress")

    def sellerAddress = column[String]("sellerAddress")

    def fiatProofHash = column[String]("fiatProofHash")

    def pegHash = column[String]("pegHash")

    def chainID = column[String]("chainID")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }
}