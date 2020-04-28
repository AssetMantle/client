package models.master

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.common.Serializable.{AssetOtherDetails, ShippingDetails}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Asset(id: String, ownerID: String, pegHash: Option[String] = None, assetType: String, description: String, documentHash: String, quantity: Int, quantityUnit: String, price: Int, moderated: Boolean, takerID: Option[String] = None, otherDetails: AssetOtherDetails, status: String)

@Singleton
class Assets @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  case class AssetSerializable(id: String, ownerID: String, pegHash: Option[String] = None, assetType: String, description: String, documentHash: String, quantity: Int, quantityUnit: String, price: Int, moderated: Boolean, takerID: Option[String], otherDetails: String, status: String) {
    def deserialize(): Asset = Asset(id = id, ownerID = ownerID, pegHash = pegHash, assetType = assetType, description = description, documentHash = documentHash, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = moderated, takerID = takerID, otherDetails = utilities.JSON.convertJsonStringToObject[AssetOtherDetails](otherDetails), status = status)
  }

  def serialize(asset: Asset): AssetSerializable = AssetSerializable(id = asset.id, ownerID = asset.ownerID, pegHash = asset.pegHash, assetType = asset.assetType, description = asset.description, documentHash = asset.documentHash, quantity = asset.quantity, quantityUnit = asset.quantityUnit, price = asset.price, moderated = asset.moderated, takerID = asset.takerID, otherDetails = Json.toJson(asset.otherDetails).toString(), status = asset.status)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private[models] val assetTable = TableQuery[AssetTable]

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ASSET

  import databaseConfig.profile.api._

  private def add(assetSerializable: AssetSerializable): Future[String] = db.run((assetTable returning assetTable.map(_.id) += assetSerializable).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByID(id: String): Future[AssetSerializable] = db.run(assetTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def tryGetOwnerIDByID(id: String): Future[String] = db.run(assetTable.filter(_.id === id).map(_.ownerID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findIDByPegHash(pegHash: String): Future[String] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findPegHashByID(id: String): Future[Option[String]] = db.run(assetTable.filter(_.id === id).map(_.pegHash).result.headOption)

  private def findStatusByID(id: String): Future[String] = db.run(assetTable.filter(_.id === id).map(_.status).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
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
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateOwnerIDByPegHash(pegHash: String, ownerID: String): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.ownerID).update(ownerID).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusByPegHash(pegHash: String, status: String): Future[Int] = db.run(assetTable.filter(_.pegHash === pegHash).map(_.status).update(status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updatePegHashAndStatusByID(id: String, pegHash: Option[String], status: String): Future[Int] = db.run(assetTable.filter(_.id === id).map(x => (x.pegHash.?, x.status)).update((pegHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def generateDocumentHash(assetID: String, ownerID: String, assetType: String, description: String, quantity: Int, quantityUnit: String, price: Int, moderated: Boolean): String = utilities.String.sha256Sum(Seq(assetID, ownerID, assetType, description, quantity.toString(), quantity, price.toString(), moderated.toString()).mkString(""))

  private[models] class AssetTable(tag: Tag) extends Table[AssetSerializable](tag, "Asset") {

    def * = (id, ownerID, pegHash.?, assetType, description, documentHash, quantity, quantityUnit, price, moderated, takerID.?, otherDetails, status) <> (AssetSerializable.tupled, AssetSerializable.unapply)

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

    def addModerated(ownerID: String, assetType: String, description: String, quantity: Int, quantityUnit: String, price: Int, shippingPeriod: Int, portOfLoading: String, portOfDischarge: String): Future[String] = {
      val id = utilities.IDGenerator.requestID()
      val documentHash = generateDocumentHash(assetID = id, ownerID = ownerID, assetType = assetType, description = description, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = true)
      add(serialize(Asset(id = id, ownerID = ownerID, assetType = assetType, description = description, documentHash = documentHash, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = true, otherDetails = AssetOtherDetails(shippingDetails = ShippingDetails(shippingPeriod = shippingPeriod, portOfLoading = portOfLoading, portOfDischarge = portOfDischarge)), status = constants.Status.Asset.REQUESTED_TO_ZONE)))
    }

    def addUnmoderated(ownerID: String, assetType: String, description: String, quantity: Int, quantityUnit: String, price: Int, shippingPeriod: Int, portOfLoading: String, portOfDischarge: String): Future[String] = {
      val id = utilities.IDGenerator.requestID()
      val documentHash = generateDocumentHash(assetID = id, ownerID = ownerID, assetType = assetType, description = description, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = false)
      for {
        _ <- add(serialize(Asset(id = id, ownerID = ownerID, assetType = assetType, description = description, documentHash = documentHash, quantity = quantity, quantityUnit = quantityUnit, price = price, moderated = false, otherDetails = AssetOtherDetails(shippingDetails = ShippingDetails(shippingPeriod = shippingPeriod, portOfLoading = portOfLoading, portOfDischarge = portOfDischarge)), status = constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE)))
      } yield documentHash
    }

    def updateOwnerByPegHash(pegHash: String, ownerID: String): Future[Int] = updateOwnerIDByPegHash(pegHash = pegHash, ownerID = ownerID)

    def tryGet(id: String): Future[Asset] = findByID(id).map(serializedAsset => serializedAsset.deserialize())

    def tryGetOwnerID(id: String): Future[String] = tryGetOwnerIDByID(id)

    def tryGetPegHash(id: String): Future[String] = findPegHashByID(id).map(_.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)))

    def tryGetIDByPegHash(pegHash: String): Future[String] = findIDByPegHash(pegHash)

    def getAllAssets(ownerID: String): Future[Seq[Asset]] = findAllByTraderID(ownerID).map(serializedAssets => serializedAssets.map(_.deserialize()))

    def getAllTradableAssets(ownerID: String): Future[Seq[Asset]] = findAllByTraderIDAndStatuses(ownerID = ownerID, constants.Status.Asset.REQUESTED_TO_ZONE, constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE, constants.Status.Asset.ISSUED).map(serializedAssets => serializedAssets.map(_.deserialize()))

    def getAllAssetsByID(ids: Seq[String]): Future[Seq[Asset]] = findAllByIDs(ids).map(serializedAssets => serializedAssets.map(_.deserialize()))

    def tryGetStatus(id: String): Future[String] = findStatusByID(id)

    def markIssuedByID(id: String, pegHash: String): Future[Int] = updatePegHashAndStatusByID(id = id, pegHash = Option(pegHash), status = constants.Status.Asset.ISSUED)

    def markRedeemedByPegHash(pegHash: String): Future[Int] = updateStatusByPegHash(pegHash = pegHash, status = constants.Status.Asset.REDEEMED)

    def markIssueAssetFailed(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Asset.ISSUE_ASSET_FAILED)

    def markStatusAwaitingBlockchainResponse(id: String): Future[Int] = updateStatusByID(id = id, status = constants.Status.Asset.AWAITING_BLOCKCHAIN_RESPONSE)

    def markTradeCompletedByPegHash(pegHash: String): Future[Int] = updateStatusByPegHash(pegHash = pegHash, status = constants.Status.Asset.TRADED)

    def markStatusInOrderByPegHash(pegHash: String): Future[Int] = updateStatusByPegHash(pegHash = pegHash, status = constants.Status.Asset.IN_ORDER)

    def resetStatusByPegHash(pegHash: String): Future[Int] = updateStatusByPegHash(pegHash = pegHash, status = constants.Status.Asset.ISSUED)

    def getPendingIssueAssetRequests(traderIDs: Seq[String]): Future[Seq[Asset]] = findAllByOwnerIDsAndStatus(ownerIDs = traderIDs, status = constants.Status.Asset.REQUESTED_TO_ZONE).map(serializedAssets => serializedAssets.map(_.deserialize()))

    def verifyAssetPendingRequestStatus(id: String): Future[Boolean] = checkByIDAndStatus(id = id, status = constants.Status.Asset.REQUESTED_TO_ZONE)

  }

}
