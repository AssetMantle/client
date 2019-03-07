package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class ReleaseAsset(from: String, to: String, pegHash: String, chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class ReleaseAssets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val releaseAssetTable = TableQuery[ReleaseAssetTable]

  private def add(releaseAsset: ReleaseAsset): Future[String] = db.run(releaseAssetTable returning releaseAssetTable.map(_.ticketID) += releaseAsset)

  private def update(releaseAsset: ReleaseAsset): Future[Int] = db.run(releaseAssetTable.insertOrUpdate(releaseAsset))

  private def findByTicketID(ticketID: String): Future[ReleaseAsset] = db.run(releaseAssetTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(releaseAssetTable.filter(_.ticketID === ticketID).delete)

  private[models] class ReleaseAssetTable(tag: Tag) extends Table[ReleaseAsset](tag, "ReleaseAsset") {

    def * = (from, to, pegHash, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (ReleaseAsset.tupled, ReleaseAsset.unapply)

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