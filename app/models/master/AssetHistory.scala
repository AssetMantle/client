package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.HistoryLogged
import models.common.Serializable._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AssetHistory(id: String, ownerID: String, pegHash: Option[String] = None, assetType: String, description: String, documentHash: String, quantity: Int, quantityUnit: String, price: Int, moderated: Boolean, takerID: Option[String] = None, otherDetails: AssetOtherDetails, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None, deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) extends HistoryLogged

@Singleton
class AssetHistories @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  def serialize(assetHistory: AssetHistory): AssetHistorySerializable = AssetHistorySerializable(id = assetHistory.id, ownerID = assetHistory.ownerID, pegHash = assetHistory.pegHash, assetType = assetHistory.assetType, description = assetHistory.description, documentHash = assetHistory.documentHash, quantity = assetHistory.quantity, quantityUnit = assetHistory.quantityUnit, price = assetHistory.price, moderated = assetHistory.moderated, takerID = assetHistory.takerID, otherDetails = Json.toJson(assetHistory.otherDetails).toString(), status = assetHistory.status, createdBy = assetHistory.createdBy, createdOn = assetHistory.createdOn, createdOnTimeZone = assetHistory.createdOnTimeZone, updatedBy = assetHistory.updatedBy, updatedOn = assetHistory.updatedOn, updatedOnTimeZone = assetHistory.updatedOnTimeZone, deletedBy = assetHistory.deletedBy, deletedOn = assetHistory.deletedOn, deletedOnTimeZone = assetHistory.deletedOnTimeZone)

  case class AssetHistorySerializable(id: String, ownerID: String, pegHash: Option[String] = None, assetType: String, description: String, documentHash: String, quantity: Int, quantityUnit: String, price: Int, moderated: Boolean, takerID: Option[String], otherDetails: String, status: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String], deletedBy: String, deletedOn: Timestamp, deletedOnTimeZone: String) {
    def deserialize(): AssetHistory = AssetHistory(id = id, ownerID = ownerID, pegHash = pegHash, assetType = assetType, description = description, documentHash = documentHash, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = moderated, takerID = takerID, otherDetails = utilities.JSON.convertJsonStringToObject[AssetOtherDetails](otherDetails), status = status, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone, deletedBy = deletedBy, deletedOn = deletedOn, deletedOnTimeZone = deletedOnTimeZone)
  }

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private[models] val assetHistoryTable = TableQuery[AssetHistoryTable]

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ASSET_HISTORY

  import databaseConfig.profile.api._

  private def tryGetByID(id: String): Future[AssetHistorySerializable] = db.run(assetHistoryTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetOwnerIDByID(id: String): Future[String] = db.run(assetHistoryTable.filter(_.id === id).map(_.ownerID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetPegHashByID(id: String): Future[Option[String]] = db.run(assetHistoryTable.filter(_.id === id).map(_.pegHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findAllByIDs(ids: Seq[String]): Future[Seq[AssetHistorySerializable]] = db.run(assetHistoryTable.filter(_.id.inSet(ids)).result)

  private[models] class AssetHistoryTable(tag: Tag) extends Table[AssetHistorySerializable](tag, "Asset_History") {

    def * = (id, ownerID, pegHash.?, assetType, description, documentHash, quantity, quantityUnit, price, moderated, takerID.?, otherDetails, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?, deletedBy, deletedOn, deletedOnTimeZone) <> (AssetHistorySerializable.tupled, AssetHistorySerializable.unapply)

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

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

    def deletedBy = column[String]("deletedBy")

    def deletedOn = column[Timestamp]("deletedOn")

    def deletedOnTimeZone = column[String]("deletedOnTimeZone")

  }

  object Service {

    def tryGet(id: String): Future[AssetHistory] = tryGetByID(id).map(_.deserialize())

    def tryGetOwnerID(id: String): Future[String] = tryGetOwnerIDByID(id)

    def tryGetPegHash(id: String): Future[String] = tryGetPegHashByID(id).map(_.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)))

    def getAllAssetsByID(ids: Seq[String]): Future[Seq[AssetHistory]] = findAllByIDs(ids).map(serializedAssets => serializedAssets.map(_.deserialize()))

  }

}
