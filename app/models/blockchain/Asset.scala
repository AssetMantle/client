package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable._
import models.common.TransactionMessages.{AssetBurn, AssetMint, AssetMutate}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.GetAsset
import queries.responses.AssetResponse.{Response => AssetResponse}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Asset(id: String, burn: Int, lock: Int, immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Assets @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        getAsset: GetAsset
                      )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ASSET

  import databaseConfig.profile.api._

  case class AssetSerialized(id: String, burn: Int, lock: Int, immutables: String, mutables: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Asset = Asset(id = id, burn = burn, lock = lock, immutables = utilities.JSON.convertJsonStringToObject[Immutables](immutables), mutables = utilities.JSON.convertJsonStringToObject[Mutables](mutables), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(asset: Asset): AssetSerialized = AssetSerialized(id = asset.id, burn = asset.burn, lock = asset.lock, immutables = Json.toJson(asset.immutables).toString, mutables = Json.toJson(asset.mutables).toString, createdBy = asset.createdBy, createdOn = asset.createdOn, createdOnTimeZone = asset.createdOnTimeZone, updatedBy = asset.updatedBy, updatedOn = asset.updatedOn, updatedOnTimeZone = asset.updatedOnTimeZone)

  private[models] val assetTable = TableQuery[AssetTable]

  private def add(asset: Asset): Future[String] = db.run((assetTable returning assetTable.map(_.id) += serialize(asset)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(assets: Seq[Asset]): Future[Seq[String]] = db.run((assetTable returning assetTable.map(_.id) ++= assets.map(x => serialize(x))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(asset: Asset): Future[Int] = db.run(assetTable.insertOrUpdate(serialize(asset)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetByID(id: String) = db.run(assetTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ASSET_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByID(id: String) = db.run(assetTable.filter(_.id === id).result.headOption)

  private def getAllAssets = db.run(assetTable.result)

  private def deleteByID(id: String): Future[Int] = db.run(assetTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ASSET_DELETE_FAILED, noSuchElementException)
    }
  }

  private[models] class AssetTable(tag: Tag) extends Table[AssetSerialized](tag, "Asset_BC") {

    def * = (id, burn, lock, immutables, mutables, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AssetSerialized.tupled, AssetSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def burn = column[Int]("burn")

    def lock = column[Int]("lock")

    def immutables = column[String]("immutables")

    def mutables = column[String]("mutables")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(asset: Asset): Future[String] = add(asset)

    def tryGet(id: String): Future[Asset] = tryGetByID(id).map(_.deserialize)

    def get(id: String): Future[Option[Asset]] = getByID(id).map(_.map(_.deserialize))

    def getAll: Future[Seq[Asset]] = getAllAssets.map(_.map(_.deserialize))

    def insertMultiple(assets: Seq[Asset]): Future[Seq[String]] = addMultiple(assets)

    def insertOrUpdate(asset: Asset): Future[Int] = upsert(asset)

    def delete(id: String): Future[Int] = deleteByID(id)
  }

  object Utility {

    private val chainID = configuration.get[String]("blockchain.main.chainID")

    def onMint(assetMint: AssetMint): Future[Unit] = {
      val hashID = Immutables(assetMint.properties).getHashID
      val assetResponse = getAsset.Service.get(getID(chainID = chainID, maintainersID = assetMint.maintainersID, classificationID = assetMint.classificationID, hashID = hashID))

      def insertOrUpdate(assetResponse: AssetResponse) = assetResponse.result.value.assets.value.list.find(x => x.value.id.value.chainID.value.idString == chainID && x.value.id.value.maintainersID.value.idString == assetMint.maintainersID && x.value.id.value.classificationID.value.idString == assetMint.maintainersID && x.value.id.value.hashID.value.idString == hashID).fold(throw new BaseException(constants.Response.ASSET_NOT_FOUND)) { asset =>
        val assetID = getID(chainID = asset.value.id.value.chainID.value.idString, maintainersID = asset.value.id.value.maintainersID.value.idString, classificationID = asset.value.id.value.classificationID.value.idString, hashID = asset.value.id.value.hashID.value.idString)
        Service.insertOrUpdate(Asset(id = assetID, lock = asset.value.lock.toInt, burn = asset.value.burn.toInt, mutables = asset.value.mutables.toMutables, immutables = asset.value.immutables.toImmutables))
      }

      (for {
        assetResponse <- assetResponse
        _ <- insertOrUpdate(assetResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onMutate(assetMutate: AssetMutate): Future[Unit] = {
      val assetResponse = getAsset.Service.get(assetMutate.assetID)

      def insertOrUpdateAll(assetResponse: AssetResponse) = Future.traverse(assetResponse.result.value.assets.value.list) { asset =>
        val assetID = getID(chainID = asset.value.id.value.chainID.value.idString, maintainersID = asset.value.id.value.maintainersID.value.idString, classificationID = asset.value.id.value.classificationID.value.idString, hashID = asset.value.id.value.hashID.value.idString)
        Service.insertOrUpdate(Asset(id = assetID, lock = asset.value.lock.toInt, burn = asset.value.burn.toInt, mutables = asset.value.mutables.toMutables, immutables = asset.value.immutables.toImmutables))
      }

      (for {
        assetResponse <- assetResponse
        _ <- insertOrUpdateAll(assetResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onBurn(assetBurn: AssetBurn): Future[Unit] = {
      val assetResponse = getAsset.Service.get(assetBurn.assetID)
      val (chainID, maintainersID, classificationID, hashID) = getFeatures(assetBurn.assetID)

      def delete(assetResponse: AssetResponse) = if (assetResponse.result.value.assets.value.list.exists(x => x.value.id.value.chainID.value.idString == chainID && x.value.id.value.maintainersID.value.idString == maintainersID && x.value.id.value.classificationID.value.idString == classificationID && x.value.id.value.hashID.value.idString == hashID)) Service.delete(assetBurn.assetID) else Future(0)

      (for {
        assetResponse <- assetResponse
        _ <- delete(assetResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    private def getID(chainID: String, maintainersID: String, classificationID: String, hashID: String) = Seq(chainID, maintainersID, classificationID, hashID).mkString(constants.Blockchain.IDSeparator)

    private def getFeatures(id: String): (String, String, String, String) = {
      val idList = id.split(constants.Blockchain.IDSeparator)
      if (idList.length == 4) (idList(0), idList(1), idList(2), idList(3)) else ("", "", "", "")
    }

  }

}