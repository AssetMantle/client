package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class SellerExecuteOrder(from: String, buyerAddress: String, sellerAddress: String, awbProofHash: String, pegHash: String, chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class SellerExecuteOrders @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sellerExecuteOrderTable = TableQuery[SellerExecuteOrderTable]

  private def add(sellerExecuteOrder: SellerExecuteOrder): Future[String] = db.run(sellerExecuteOrderTable returning sellerExecuteOrderTable.map(_.ticketID) += sellerExecuteOrder)

  private def update(sellerExecuteOrder: SellerExecuteOrder): Future[Int] = db.run(sellerExecuteOrderTable.insertOrUpdate(sellerExecuteOrder))

  private def findByTicketID(ticketID: String): Future[SellerExecuteOrder] = db.run(sellerExecuteOrderTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(sellerExecuteOrderTable.filter(_.ticketID === ticketID).delete)

  private[models] class SellerExecuteOrderTable(tag: Tag) extends Table[SellerExecuteOrder](tag, "SellerExecuteOrder") {

    def * = (from, buyerAddress, sellerAddress, awbProofHash, pegHash, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (SellerExecuteOrder.tupled, SellerExecuteOrder.unapply)

    def from = column[String]("from")

    def buyerAddress = column[String]("buyerAddress")

    def sellerAddress = column[String]("sellerAddress")

    def awbProofHash = column[String]("awbProofHash")

    def pegHash = column[String]("pegHash")

    def chainID = column[String]("chainID")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }
}