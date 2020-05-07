package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AssetHistory(id: String, ownerID: String, pegHash: Option[String] = None, assetType: String, description: String, documentHash: String, quantity: Int, quantityUnit: String, price: Int, moderated: Boolean, takerID: Option[String] = None, otherDetails: AssetOtherDetails, status: String)

@Singleton
class AssetHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  case class AssetHistorySerializable(id: String, ownerID: String, pegHash: Option[String] = None, assetType: String, description: String, documentHash: String, quantity: Int, quantityUnit: String, price: Int, moderated: Boolean, takerID: Option[String], otherDetails: String, status: String) {
    def deserialize(): Asset = Asset(id = id, ownerID = ownerID, pegHash = pegHash, assetType = assetType, description = description, documentHash = documentHash, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = moderated, takerID = takerID, otherDetails = utilities.JSON.convertJsonStringToObject[AssetOtherDetails](otherDetails), status = status)
  }

  def serialize(asset: Asset): AssetHistorySerializable = AssetHistorySerializable(id = asset.id, ownerID = asset.ownerID, pegHash = asset.pegHash, assetType = asset.assetType, description = asset.description, documentHash = asset.documentHash, quantity = asset.quantity, quantityUnit = asset.quantityUnit, price = asset.price, moderated = asset.moderated, takerID = asset.takerID, otherDetails = Json.toJson(asset.otherDetails).toString(), status = asset.status)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private[models] val assetHistoryTable = TableQuery[AssetHistoryTable]

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ASSET_HISTORY

  import databaseConfig.profile.api._

  private def tryGetByID(id: String): Future[AssetHistorySerializable] = db.run(assetHistoryTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetOwnerIDByID(id: String): Future[String] = db.run(assetHistoryTable.filter(_.id === id).map(_.ownerID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getPegHashByID(id: String): Future[Option[String]] = db.run(assetHistoryTable.filter(_.id === id).map(_.pegHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class AssetHistoryTable(tag: Tag) extends Table[AssetHistorySerializable](tag, "Asset_History") {

    def * = (id, ownerID, pegHash.?, assetType, description, documentHash, quantity, quantityUnit, price, moderated, takerID.?, otherDetails, status) <> (AssetHistorySerializable.tupled, AssetHistorySerializable.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def ownerID = column[String]("ownerID")

    def pegHash = column[String]("pegHash")

    def assetType = column[String]("assetType")

    def description = column[String]("description")

    def documentHash = column[String]("documentHash")

    def quantity = column[Int]("quantity")

    def quantityUnit = column[String]("quantityUnit")

    def price = column[Int]("price")

    def moderated = column[Boolean]("moderated")

    def takerID = column[String]("takerID")

    def otherDetails = column[String]("otherDetails")

    def status = column[String]("status")

  }

  object Service {

    def tryGet(id: String): Future[Asset] = tryGetByID(id).map(serializedAsset => serializedAsset.deserialize())

    def tryGetOwnerID(id: String): Future[String] = tryGetOwnerIDByID(id)

    def tryGetPegHash(id: String): Future[String] = getPegHashByID(id).map(_.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)))

  }

}
