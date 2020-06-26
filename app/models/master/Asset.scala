package models.master

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable.{AssetOtherDetails, ShippingDetails}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import utilities.MicroInt

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Asset(id: String, ownerID: String, pegHash: Option[String] = None, assetType: String, description: String, documentHash: String, quantity: Int, quantityUnit: String, price: MicroInt, moderated: Boolean, takerID: Option[String] = None, otherDetails: AssetOtherDetails, status: String, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Assets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  def serialize(asset: Asset): AssetSerializable = AssetSerializable(id = asset.id, ownerID = asset.ownerID, pegHash = asset.pegHash, assetType = asset.assetType, description = asset.description, documentHash = asset.documentHash, quantity = asset.quantity, quantityUnit = asset.quantityUnit, price = asset.price.value, moderated = asset.moderated, takerID = asset.takerID, otherDetails = Json.toJson(asset.otherDetails).toString(), status = asset.status, createdBy = asset.createdBy, createdOn = asset.createdOn, createdOnTimeZone = asset.createdOnTimeZone, updatedBy = asset.updatedBy, updatedOn = asset.updatedOn, updatedOnTimeZone = asset.updatedOnTimeZone)

  case class AssetSerializable(id: String, ownerID: String, pegHash: Option[String], assetType: String, description: String, documentHash: String, quantity: Int, quantityUnit: String, price: Long, moderated: Boolean, takerID: Option[String], otherDetails: String, status: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize(): Asset = Asset(id = id, ownerID = ownerID, pegHash = pegHash, assetType = assetType, description = description, documentHash = documentHash, quantity = quantity, quantityUnit = quantityUnit, price = new MicroInt(price), moderated = moderated, takerID = takerID, otherDetails = utilities.JSON.convertJsonStringToObject[AssetOtherDetails](otherDetails), status = status, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private[models] val assetTable = TableQuery[AssetTable]

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ASSET

  import databaseConfig.profile.api._

  private def add(assetSerializable: AssetSerializable): Future[String] = db.run((assetTable returning assetTable.map(_.id) += assetSerializable).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def findByID(id: String): Future[AssetSerializable] = db.run(assetTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetOwnerIDByID(id: String): Future[String] = db.run(assetTable.filter(_.id === id).map(_.ownerID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findIDByPegHash(pegHash: String): Future[String] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetPegHashByID(id: String): Future[Option[String]] = db.run(assetTable.filter(_.id === id).map(_.pegHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findStatusByID(id: String): Future[String] = db.run(assetTable.filter(_.id === id).map(_.status).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findAllByTraderID(ownerID: String): Future[Seq[AssetSerializable]] = db.run(assetTable.filter(_.ownerID === ownerID).result)

  private def findAllByTraderIDAndStatuses(ownerID: String, statuses: String*): Future[Seq[AssetSerializable]] = db.run(assetTable.filter(_.ownerID === ownerID).filter(_.status.inSet(statuses)).result)

  private def findAllByIDs(ids: Seq[String]): Future[Seq[AssetSerializable]] = db.run(assetTable.filter(_.id.inSet(ids)).result)

  private def findAllByOwnerIDsAndStatus(ownerIDs: Seq[String], status: String): Future[Seq[AssetSerializable]] = db.run(assetTable.filter(_.ownerID.inSet(ownerIDs)).filter(_.status === status).result)

  private def checkByIDAndStatus(id: String, status: String): Future[Boolean] = db.run(assetTable.filter(_.id === id).filter(_.status === status).exists.result)

  private def updateStatusByID(id: String, status: String): Future[Int] = db.run(assetTable.filter(_.id === id).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateOwnerIDAndStatusByPegHash(pegHash: String, ownerID: String, status: String): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(x => (x.ownerID, x.status)).update((ownerID, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateStatusByPegHash(pegHash: String, status: String): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updatePegHashAndStatusByID(id: String, pegHash: Option[String], status: String): Future[Int] = db.run(assetTable.filter(_.id === id).map(x => (x.pegHash.?, x.status)).update((pegHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def generateDocumentHash(assetID: String, ownerID: String, assetType: String, description: String, quantity: Int, quantityUnit: String, price: MicroInt, moderated: Boolean): String = utilities.String.sha256Sum(Seq(assetID, ownerID, assetType, description, quantity.toString(), quantity, price.value.toString, moderated.toString()).mkString(""))

  private[models] class AssetTable(tag: Tag) extends Table[AssetSerializable](tag, "Asset") {

    def * = (id, ownerID, pegHash.?, assetType, description, documentHash, quantity, quantityUnit, price, moderated, takerID.?, otherDetails, status, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AssetSerializable.tupled, AssetSerializable.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def ownerID = column[String]("ownerID")

    def pegHash = column[String]("pegHash")

    def assetType = column[String]("assetType")

    def description = column[String]("description")

    def documentHash = column[String]("documentHash")

    def quantity = column[Int]("quantity")

    def quantityUnit = column[String]("quantityUnit")

    def price = column[Long]("price")

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

  }

  object Service {

    def addModerated(ownerID: String, assetType: String, description: String, quantity: Int, quantityUnit: String, price: MicroInt, shippingPeriod: Int, portOfLoading: String, portOfDischarge: String): Future[String] = {
      val id = utilities.IDGenerator.requestID()
      val documentHash = generateDocumentHash(assetID = id, ownerID = ownerID, assetType = assetType, description = description, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = true)
      add(serialize(Asset(id = id, ownerID = ownerID, assetType = assetType, description = description, documentHash = documentHash, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = true, otherDetails = AssetOtherDetails(shippingDetails = ShippingDetails(shippingPeriod = shippingPeriod, portOfLoading = portOfLoading, portOfDischarge = portOfDischarge)), status = constants.Status.Asset.REQUESTED_TO_ZONE)))
    }

    def addUnmoderated(ownerID: String, assetType: String, description: String, quantity: Int, quantityUnit: String, price: MicroInt, shippingPeriod: Int, portOfLoading: String, portOfDischarge: String): Future[String] = {
      val id = utilities.IDGenerator.requestID()
      val documentHash = generateDocumentHash(assetID = id, ownerID = ownerID, assetType = assetType, description = description, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = false)
      for {
        _ <- add(serialize(Asset(id = id, ownerID = ownerID, assetType = assetType, description = description, documentHash = documentHash, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = false, otherDetails = AssetOtherDetails(shippingDetails = ShippingDetails(shippingPeriod = shippingPeriod, portOfLoading = portOfLoading, portOfDischarge = portOfDischarge)), status = constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE)))
      } yield documentHash
    }

    def markAssetSendToOrderByPegHash(pegHash: String, ownerID: String): Future[Int] = updateOwnerIDAndStatusByPegHash(pegHash = pegHash, ownerID = ownerID, status = constants.Status.Asset.IN_ORDER)

    def tryGet(id: String): Future[Asset] = findByID(id).map(serializedAsset => serializedAsset.deserialize())

    def tryGetOwnerID(id: String): Future[String] = tryGetOwnerIDByID(id)

    def tryGetPegHash(id: String): Future[String] = tryGetPegHashByID(id).map(_.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)))

    def tryGetIDByPegHash(pegHash: String): Future[String] = findIDByPegHash(pegHash)

    def getAllAssets(ownerID: String): Future[Seq[Asset]] = findAllByTraderID(ownerID).map(serializedAssets => serializedAssets.map(_.deserialize()))

    def getAllTradableAssets(ownerID: String): Future[Seq[Asset]] = findAllByTraderIDAndStatuses(ownerID = ownerID, constants.Status.Asset.REQUESTED_TO_ZONE, constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.Asset.ISSUED).map(serializedAssets => serializedAssets.map(_.deserialize()))

    def getAllAssetsByID(ids: Seq[String]): Future[Seq[Asset]] = findAllByIDs(ids).map(serializedAssets => serializedAssets.map(_.deserialize()))

    def tryGetStatus(id: String): Future[String] = findStatusByID(id)

    def markIssuedByID(id: String, pegHash: String): Future[Int] = updatePegHashAndStatusByID(id = id, pegHash = Option(pegHash), status = constants.Status.Asset.ISSUED)

    def markRedeemedByPegHash(pegHash: String): Future[Int] = updateStatusByPegHash(pegHash = pegHash, status = constants.Status.Asset.REDEEMED)

    def markIssueAssetFailed(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Asset.ISSUE_ASSET_FAILED)

    def markStatusAwaitingBlockchainResponse(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE)

    def markTradeCompletedByPegHash(pegHash: String, ownerID: String): Future[Int] = updateOwnerIDAndStatusByPegHash(pegHash = pegHash, ownerID = ownerID, status = constants.Status.Asset.TRADED)

    def resetStatusByPegHash(pegHash: String, ownerID: String): Future[Int] = updateOwnerIDAndStatusByPegHash(pegHash = pegHash, ownerID = ownerID, status = constants.Status.Asset.ISSUED)

    def getPendingIssueAssetRequests(traderIDs: Seq[String]): Future[Seq[Asset]] = findAllByOwnerIDsAndStatus(ownerIDs = traderIDs, status = constants.Status.Asset.REQUESTED_TO_ZONE).map(serializedAssets => serializedAssets.map(_.deserialize()))

    def verifyAssetPendingRequestStatus(id: String): Future[Boolean] = checkByIDAndStatus(id = id, status = constants.Status.Asset.REQUESTED_TO_ZONE)

  }

}
