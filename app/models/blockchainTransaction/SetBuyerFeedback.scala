package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class SetBuyerFeedback(from: String, to: String, pegHash: String, rating: Int,  chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class SetBuyerFeedbacks @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val setBuyerFeedbackTable = TableQuery[SetBuyerFeedbackTable]

  private def add(setBuyerFeedback: SetBuyerFeedback): Future[String] = db.run(setBuyerFeedbackTable returning setBuyerFeedbackTable.map(_.ticketID) += setBuyerFeedback)

  private def update(setBuyerFeedback: SetBuyerFeedback): Future[Int] = db.run(setBuyerFeedbackTable.insertOrUpdate(setBuyerFeedback))

  private def findByTicketID(ticketID: String): Future[SetBuyerFeedback] = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).delete)

  private[models] class SetBuyerFeedbackTable(tag: Tag) extends Table[SetBuyerFeedback](tag, "SetBuyerFeedback") {

    def * = (from, to, pegHash, rating, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (SetBuyerFeedback.tupled, SetBuyerFeedback.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def pegHash = column[String]("pegHash")

    def rating = column[Int]("rating")

    def chainID = column[String]("chainID")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }
}