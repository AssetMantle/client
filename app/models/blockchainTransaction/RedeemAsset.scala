package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class RedeemAsset(from: String, to: String, pegHash: String, chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class RedeemAssets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val redeemAssetTable = TableQuery[RedeemAssetTable]

  private def add(redeemAsset: RedeemAsset): Future[String] = db.run(redeemAssetTable returning redeemAssetTable.map(_.ticketID) += redeemAsset)

  private def update(redeemAsset: RedeemAsset): Future[Int] = db.run(redeemAssetTable.insertOrUpdate(redeemAsset))

  private def findByTicketID(ticketID: String): Future[RedeemAsset] = db.run(redeemAssetTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(redeemAssetTable.filter(_.ticketID === ticketID).delete)

  private[models] class RedeemAssetTable(tag: Tag) extends Table[RedeemAsset](tag, "RedeemAsset") {

    def * = (from, to, pegHash, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (RedeemAsset.tupled, RedeemAsset.unapply)

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