package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.Serializable._
import models.common.TransactionMessages.{AssetBurn, AssetDefine, AssetMint, AssetMutate}
import models.master
import models.master.{AssetNew => masterAssetNew}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AssetNew(id: String, immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getClassificationID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(0)

  def getHashID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(1)
}

@Singleton
class AssetsNew @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        blockchainClassifications: Classifications,
                        blockchainSplits: Splits,
                        blockchainMetas: Metas,
                        blockchainMaintainers: Maintainers,
                        masterClassifications: master.Classifications,
                        masterAssetsNew: master.AssetsNew,
                        masterProperties: master.Properties,
                      )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ASSET

  import databaseConfig.profile.api._

  case class AssetNewSerialized(id: String, immutables: String, mutables: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: AssetNew = AssetNew(id = id, immutables = utilities.JSON.convertJsonStringToObject[Immutables](immutables), mutables = utilities.JSON.convertJsonStringToObject[Mutables](mutables), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(asset: AssetNew): AssetNewSerialized = AssetNewSerialized(id = asset.id, immutables = Json.toJson(asset.immutables).toString, mutables = Json.toJson(asset.mutables).toString, createdBy = asset.createdBy, createdOn = asset.createdOn, createdOnTimeZone = asset.createdOnTimeZone, updatedBy = asset.updatedBy, updatedOn = asset.updatedOn, updatedOnTimeZone = asset.updatedOnTimeZone)

  private[models] val assetTable = TableQuery[AssetTable]

  private def add(asset: AssetNew): Future[String] = db.run((assetTable returning assetTable.map(_.id) += serialize(asset)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(assets: Seq[AssetNew]): Future[Seq[String]] = db.run((assetTable returning assetTable.map(_.id) ++= assets.map(x => serialize(x))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(asset: AssetNew): Future[Int] = db.run(assetTable.insertOrUpdate(serialize(asset)).asTry).map {
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

  private def checkExistsByID(id: String) = db.run(assetTable.filter(_.id === id).exists.result)

  private[models] class AssetTable(tag: Tag) extends Table[AssetNewSerialized](tag, "Asset_BC") {

    def * = (id, immutables, mutables, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AssetNewSerialized.tupled, AssetNewSerialized.unapply)

    def id = column[String]("id", O.PrimaryKey)

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

    def create(asset: AssetNew): Future[String] = add(asset)

    def tryGet(id: String): Future[AssetNew] = tryGetByID(id).map(_.deserialize)

    def get(id: String): Future[Option[AssetNew]] = getByID(id).map(_.map(_.deserialize))

    def getAll: Future[Seq[AssetNew]] = getAllAssets.map(_.map(_.deserialize))

    def insertMultiple(assets: Seq[AssetNew]): Future[Seq[String]] = addMultiple(assets)

    def insertOrUpdate(asset: AssetNew): Future[Int] = upsert(asset)

    def delete(id: String): Future[Int] = deleteByID(id)

    def checkExists(id: String): Future[Boolean] = checkExistsByID(id)
  }

  object Utility {

    def onDefine(assetDefine: AssetDefine): Future[Unit] = {
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(assetDefine.immutableMetaTraits.metaPropertyList)
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(assetDefine.mutableMetaTraits.metaPropertyList)

      def defineAndSuperAuxiliary(scrubbedImmutableMetaProperties: Seq[Property], scrubbedMutableMetaProperties: Seq[Property]) = {
        val mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ assetDefine.mutableTraits.propertyList))
        val defineAuxiliary = blockchainClassifications.Utility.auxiliaryDefine(immutables = Immutables(Properties(scrubbedImmutableMetaProperties ++ assetDefine.immutableTraits.propertyList)), mutables = mutables)

        def superAuxiliary(classificationID: String) = blockchainMaintainers.Utility.auxiliarySuper(classificationID = classificationID, identityID = assetDefine.fromID, mutableTraits = mutables)

        for {
          classificationID <- defineAuxiliary
          _ <- superAuxiliary(classificationID = classificationID)
        } yield classificationID
      }

      def masterOperations(classificationID: String) = {
        val insert = masterClassifications.Service.insertOrUpdate(id = classificationID, entityType = constants.Blockchain.Entity.ASSET_DEFINITION, maintainerID = assetDefine.fromID, status = Option(true))
        for {
          _ <- insert
        } yield ()
      }

      (for {
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        classificationID <- defineAndSuperAuxiliary(scrubbedImmutableMetaProperties = scrubbedImmutableMetaProperties, scrubbedMutableMetaProperties = scrubbedMutableMetaProperties)
        _ <- masterOperations(classificationID)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onMint(assetMint: AssetMint): Future[Unit] = {
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(assetMint.immutableMetaProperties.metaPropertyList)
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(assetMint.mutableMetaProperties.metaPropertyList)

      def upsert(scrubbedImmutableMetaProperties: Seq[Property], scrubbedMutableMetaProperties: Seq[Property]) = {
        val immutables = Immutables(Properties(scrubbedImmutableMetaProperties ++ assetMint.immutableProperties.propertyList))
        val assetID = utilities.IDGenerator.getAssetID(classificationID = assetMint.classificationID, immutables = immutables)
        val mintAuxiliary = blockchainSplits.Utility.auxiliaryMint(ownerID = assetMint.toID, ownableID = assetID, splitValue = constants.Blockchain.SmallestDec)
        val upsertAsset = Service.insertOrUpdate(AssetNew(id = assetID, immutables = immutables, mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ assetMint.mutableProperties.propertyList))))

        for {
          _ <- mintAuxiliary
          _ <- upsertAsset
        } yield assetID
      }

      def masterOperations(assetID: String) = {
        val insert = masterAssetsNew.Service.insertOrUpdate(masterAssetNew(id = assetID, status = Option(true)))
        for {
          _ <- insert
        } yield ()
      }

      (for {
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        assetID <- upsert(scrubbedImmutableMetaProperties = scrubbedImmutableMetaProperties, scrubbedMutableMetaProperties = scrubbedMutableMetaProperties)
        _ <- masterOperations(assetID)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onMutate(assetMutate: AssetMutate): Future[Unit] = {
      val oldAsset = Service.tryGet(assetMutate.assetID)
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(assetMutate.mutableMetaProperties.metaPropertyList)

      def upsertAsset(oldAsset: AssetNew, scrubbedMutableMetaProperties: Seq[Property]) = Service.insertOrUpdate(oldAsset.copy(mutables = oldAsset.mutables.mutate(scrubbedMutableMetaProperties ++ assetMutate.mutableProperties.propertyList)))

      (for {
        oldAsset <- oldAsset
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        _ <- upsertAsset(oldAsset, scrubbedMutableMetaProperties)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onBurn(assetBurn: AssetBurn): Future[Unit] = {
      val burnAuxiliary = blockchainSplits.Utility.auxiliaryBurn(ownerID = assetBurn.fromID, ownableID = assetBurn.assetID, splitValue = constants.Blockchain.SmallestDec)
      val deleteAsset = Service.delete(assetBurn.assetID)
      val masterOperations = masterAssetsNew.Service.delete(assetBurn.assetID)

      (for {
        _ <- burnAuxiliary
        _ <- deleteAsset
        _ <- masterOperations
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }
  }

}
