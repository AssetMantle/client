package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class IssueFiat(from: String, to: String, transactionID: String, transactionAmount: Int, chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class IssueFiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val issueFiatTable = TableQuery[IssueFiatTable]

  private def add(issueFiat: IssueFiat): Future[String] = db.run(issueFiatTable returning issueFiatTable.map(_.ticketID) += issueFiat)

  private def update(issueFiat: IssueFiat): Future[Int] = db.run(issueFiatTable.insertOrUpdate(issueFiat))

  private def findByTicketID(ticketID: String): Future[IssueFiat] = db.run(issueFiatTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(issueFiatTable.filter(_.ticketID === ticketID).delete)

  private[models] class IssueFiatTable(tag: Tag) extends Table[IssueFiat](tag, "IssueFiat") {

    def * = (from, to, transactionID, transactionAmount, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (IssueFiat.tupled, IssueFiat.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def transactionID = column[String]("transactionID")

    def transactionAmount = column[Int]("transactionAmount")

    def chainID = column[String]("chainID")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }
}