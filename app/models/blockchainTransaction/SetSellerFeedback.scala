package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class SetSellerFeedback(from: String, to: String, pegHash: String, rating: Int,  chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class SetSellerFeedbacks @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val setSellerFeedbackTable = TableQuery[SetSellerFeedbackTable]

  private def add(setSellerFeedback: SetSellerFeedback): Future[String] = db.run(setSellerFeedbackTable returning setSellerFeedbackTable.map(_.ticketID) += setSellerFeedback)

  private def update(setSellerFeedback: SetSellerFeedback): Future[Int] = db.run(setSellerFeedbackTable.insertOrUpdate(setSellerFeedback))

  private def findByTicketID(ticketID: String): Future[SetSellerFeedback] = db.run(setSellerFeedbackTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(setSellerFeedbackTable.filter(_.ticketID === ticketID).delete)

  private[models] class SetSellerFeedbackTable(tag: Tag) extends Table[SetSellerFeedback](tag, "SetSellerFeedback") {

    def * = (from, to, pegHash, rating, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (SetSellerFeedback.tupled, SetSellerFeedback.unapply)

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