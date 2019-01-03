package models.blockchain

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Asset(pegHash: String, documentHash: String, assetType: String, assetQuantity: Int, assetPrice: Int, quantityUnit: String, ownerAddress: String, locked: Boolean)

class Assets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]
  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val assetTable = TableQuery[AssetTable]

  def add(asset: Asset): Future[String] = db.run(assetTable returning assetTable.map(_.pegHash) += asset)

  def findByPegHash(pegHash: String): Future[Asset] = db.run(assetTable.filter(_.pegHash === pegHash).result.head)

  def deleteByPegHash(pegHash: String) = db.run(assetTable.filter(_.pegHash === pegHash).delete)

  private[models] class AssetTable(tag: Tag) extends Table[Asset](tag, "Asset") {

    def * = (pegHash, documentHash, assetType, assetQuantity, assetPrice, quantityUnit, ownerAddress, locked) <> (Asset.tupled, Asset.unapply)

    def ? = (pegHash.?, documentHash.?, assetType.?, assetQuantity.?, assetPrice.?, quantityUnit.?, ownerAddress.?, locked.?).shaped.<>({ r => import r._; _1.map(_ => Asset.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def documentHash = column[String]("documentHash")

    def assetType = column[String]("assetType")

    def assetQuantity = column[Int]("assetQuantity")

    def assetPrice = column[Int]("assetPrice")

    def quantityUnit = column[String]("quantityUnit")

    def ownerAddress = column[String]("ownerAddress")

    def locked = column[Boolean]("locked")
  }

}