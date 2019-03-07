package models.blockchainTransaction

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class IssueAsset(from: String, to: String, documentHash: String, assetType: String, assetPrice: Int, quantityUnit: String, assetQuantity: Int, chainID: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class IssueAssets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val issueAssetTable = TableQuery[IssueAssetTable]

  private def add(issueAsset: IssueAsset): Future[String] = db.run(issueAssetTable returning issueAssetTable.map(_.ticketID) += issueAsset)

  private def update(issueAsset: IssueAsset): Future[Int] = db.run(issueAssetTable.insertOrUpdate(issueAsset))

  private def findByTicketID(ticketID: String): Future[IssueAsset] = db.run(issueAssetTable.filter(_.ticketID === ticketID).result.head)

  private def deleteByTicketID(ticketID: String) = db.run(issueAssetTable.filter(_.ticketID === ticketID).delete)

  private[models] class IssueAssetTable(tag: Tag) extends Table[IssueAsset](tag, "IssueAsset") {

    def * = (from, to, documentHash, assetType, assetPrice, quantityUnit, assetQuantity, chainID, gas, status.?, txHash.?, ticketID, responseCode.?) <> (IssueAsset.tupled, IssueAsset.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def documentHash = column[String]("documentHash")

    def assetType = column[String]("assetType")

    def assetPrice = column[Int]("assetPrice")

    def quantityUnit = column[String]("quantityUnit")

    def assetQuantity = column[Int]("assetQuantity")

    def chainID = column[String]("chainID")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }
}