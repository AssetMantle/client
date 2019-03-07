package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class SendCoin(from: String, to: String, amount: Int, chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class SendCoins @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sendCoinTable = TableQuery[SendCoinTable]

  private def add(sendCoin: SendCoin): Future[String] = db.run(sendCoinTable returning sendCoinTable.map(_.ticketID) += sendCoin)

  private def update(sendCoin: SendCoin): Future[Int] = db.run(sendCoinTable.insertOrUpdate(sendCoin))

  private def findByTicketID(ticketID: String): Future[SendCoin] = db.run(sendCoinTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(sendCoinTable.filter(_.ticketID === ticketID).delete)

  private[models] class SendCoinTable(tag: Tag) extends Table[SendCoin](tag, "SendCoin") {

    def * = (from, to, amount, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (SendCoin.tupled, SendCoin.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def amount = column[Int]("amount")

    def chainID = column[String]("chainID")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }
}