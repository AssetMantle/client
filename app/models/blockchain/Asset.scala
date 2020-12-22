package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable._
import models.common.TransactionMessages.{AssetBurn, AssetDefine, AssetMint, AssetMutate}
import models.master
import models.master.{Asset => masterAsset, Classification => masterClassification, Split => masterSplit}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.GetAsset
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Asset(id: String, immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getClassificationID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(0)

  def getHashID: String = id.split(constants.RegularExpression.BLOCKCHAIN_FIRST_ORDER_COMPOSITE_ID_SEPARATOR)(1)
}

@Singleton
class Assets @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        getAsset: GetAsset,
                        blockchainClassifications: Classifications,
                        blockchainSplits: Splits,
                        blockchainMetas: Metas,
                        blockchainMaintainers: Maintainers,
                        masterClassifications: master.Classifications,
                        masterAssets: master.Assets,
                        masterSplits: master.Splits,
                        masterProperties: master.Properties,
                      )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ASSET

  import databaseConfig.profile.api._

  case class AssetSerialized(id: String, immutables: String, mutables: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Asset = Asset(id = id, immutables = utilities.JSON.convertJsonStringToObject[Immutables](immutables), mutables = utilities.JSON.convertJsonStringToObject[Mutables](mutables), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(asset: Asset): AssetSerialized = AssetSerialized(id = asset.id, immutables = Json.toJson(asset.immutables).toString, mutables = Json.toJson(asset.mutables).toString, createdBy = asset.createdBy, createdOn = asset.createdOn, createdOnTimeZone = asset.createdOnTimeZone, updatedBy = asset.updatedBy, updatedOn = asset.updatedOn, updatedOnTimeZone = asset.updatedOnTimeZone)

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

  private def checkExistsByID(id: String) = db.run(assetTable.filter(_.id === id).exists.result)

  private[models] class AssetTable(tag: Tag) extends Table[AssetSerialized](tag, "Asset_BC") {

    def * = (id, immutables, mutables, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AssetSerialized.tupled, AssetSerialized.unapply)

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

    def create(asset: Asset): Future[String] = add(asset)

    def tryGet(id: String): Future[Asset] = tryGetByID(id).map(_.deserialize)

    def get(id: String): Future[Option[Asset]] = getByID(id).map(_.map(_.deserialize))

    def getAll: Future[Seq[Asset]] = getAllAssets.map(_.map(_.deserialize))

    def insertMultiple(assets: Seq[Asset]): Future[Seq[String]] = addMultiple(assets)

    def insertOrUpdate(asset: Asset): Future[Int] = upsert(asset)

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
        val classification = masterClassifications.Service.get(classificationID)

        def insertProperties(classification: Option[masterClassification]) = if (classification.isEmpty) masterProperties.Utilities.upsertProperties(entityType = constants.Blockchain.Entity.ASSET_DEFINITION, entityID = classificationID, immutableMetas = assetDefine.immutableMetaTraits, immutables = assetDefine.immutableTraits, mutableMetas = assetDefine.mutableMetaTraits, mutables = assetDefine.mutableTraits) else Future("")

        def upsert(classification: Option[masterClassification]) = classification.fold(masterClassifications.Service.insertOrUpdate(id = classificationID, entityType = constants.Blockchain.Entity.ASSET_DEFINITION, fromID = assetDefine.fromID, label = None, status = Option(true)))(_ => masterClassifications.Service.markStatusSuccessful(id = classificationID, entityType = constants.Blockchain.Entity.ASSET_DEFINITION))

        for {
          classification <- classification
          _ <- upsert(classification)
          _ <- insertProperties(classification)
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
        val assetID = getID(classificationID = assetMint.classificationID, immutables = immutables)
        val mintAuxiliary = blockchainSplits.Utility.auxiliaryMint(ownerID = assetMint.toID, ownableID = assetID, splitValue = constants.Blockchain.SmallestDec)
        val upsertAsset = Service.insertOrUpdate(Asset(id = assetID, immutables = immutables, mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ assetMint.mutableProperties.propertyList))))

        for {
          _ <- mintAuxiliary
          _ <- upsertAsset
        } yield assetID
      }

      def masterOperations(assetID: String) = {
        val asset = masterAssets.Service.get(assetID)

        def insertProperties(asset: Option[masterAsset]) = if (asset.isEmpty) masterProperties.Utilities.upsertProperties(entityType = constants.Blockchain.Entity.ASSET, entityID = assetID, immutableMetas = assetMint.immutableMetaProperties, immutables = assetMint.immutableProperties, mutableMetas = assetMint.mutableMetaProperties, mutables = assetMint.mutableProperties) else Future("")

        def upsertMaster(asset: Option[masterAsset]) = asset.fold(masterAssets.Service.insertOrUpdate(masterAsset(id = assetID, label = None, ownerID = assetMint.toID, status = Option(true))))(x => masterAssets.Service.insertOrUpdate(x.copy(status = Option(true))))

        def upsertSplit = masterSplits.Service.insertOrUpdate(masterSplit(entityID = assetID, ownerID = assetMint.toID, entityType = constants.Blockchain.Entity.ASSET, label = None, status = Option(true)))

        for {
          asset <- asset
          _ <- upsertMaster(asset)
          _ <- insertProperties(asset)
          _ <- upsertSplit
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

      def upsertAsset(oldAsset: Asset, scrubbedMutableMetaProperties: Seq[Property]) = Service.insertOrUpdate(oldAsset.copy(mutables = oldAsset.mutables.mutate(scrubbedMutableMetaProperties ++ assetMutate.mutableProperties.propertyList)))

      def masterOperations(assetID: String) = {
        val updateProperties = masterProperties.Utilities.updateProperties(entityType = constants.Blockchain.Entity.ASSET, entityID = assetID, mutableMetas = assetMutate.mutableMetaProperties, mutables = assetMutate.mutableProperties)

        for {
          _ <- updateProperties
        } yield ()
      }

      (for {
        oldAsset <- oldAsset
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        _ <- upsertAsset(oldAsset, scrubbedMutableMetaProperties)
        _ <- masterOperations(assetMutate.assetID)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onBurn(assetBurn: AssetBurn): Future[Unit] = {
      val burnAuxiliary = blockchainSplits.Utility.auxiliaryBurn(ownerID = assetBurn.fromID, ownableID = assetBurn.assetID, splitValue = constants.Blockchain.SmallestDec)
      val deleteAsset = Service.delete(assetBurn.assetID)

      def masterOperations(assetID: String) = {
        val deleteProperties = masterProperties.Service.deleteAll(entityType = constants.Blockchain.Entity.ASSET, entityID = assetID)
        val deleteAsset = masterAssets.Service.delete(assetID)

        for {
          _ <- deleteProperties
          _ <- deleteAsset
        } yield ()
      }

      (for {
        _ <- burnAuxiliary
        _ <- deleteAsset
        _ <- masterOperations(assetBurn.assetID)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def getID(classificationID: String, immutables: Immutables): String = Seq(classificationID, immutables.getHashID).mkString(constants.Blockchain.FirstOrderCompositeIDSeparator)

  }

}