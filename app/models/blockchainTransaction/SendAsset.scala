package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class SendAsset(from: String, to: String, pegHash: String, chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class SendAssets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sendAssetTable = TableQuery[SendAssetTable]

  private def add(sendAsset: SendAsset): Future[String] = db.run(sendAssetTable returning sendAssetTable.map(_.ticketID) += sendAsset)

  private def update(sendAsset: SendAsset): Future[Int] = db.run(sendAssetTable.insertOrUpdate(sendAsset))

  private def findByTicketID(ticketID: String): Future[SendAsset] = db.run(sendAssetTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(sendAssetTable.filter(_.ticketID === ticketID).delete)

  private[models] class SendAssetTable(tag: Tag) extends Table[SendAsset](tag, "SendAsset") {

    def * = (from, to, pegHash, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (SendAsset.tupled, SendAsset.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def pegHash = column[String]("pegHash")

    def chainID = column[String]("chainID")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }
}