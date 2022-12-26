package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.ID.{AssetID, ClassificationID, IdentityID}
import models.common.Serializable._
import models.common.TransactionMessages.{AssetBurn, AssetDefine, AssetMint, AssetMutate}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.responses.common.Header
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Asset(id: AssetID, immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Assets @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        blockchainClassifications: Classifications,
                        blockchainSplits: Splits,
                        blockchainMetas: Metas,
                        blockchainMaintainers: Maintainers,
                      )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ASSET

  import databaseConfig.profile.api._

  case class AssetSerialized(classificationID: String, hashID: String, immutables: String, mutables: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Asset = Asset(id = AssetID(classificationID = classificationID, hashID = hashID), immutables = utilities.JSON.convertJsonStringToObject[Immutables](immutables), mutables = utilities.JSON.convertJsonStringToObject[Mutables](mutables), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(asset: Asset): AssetSerialized = AssetSerialized(classificationID = asset.id.classificationID.asString, hashID = asset.id.hashID, immutables = Json.toJson(asset.immutables).toString, mutables = Json.toJson(asset.mutables).toString, createdBy = asset.createdBy, createdOn = asset.createdOn, createdOnTimeZone = asset.createdOnTimeZone, updatedBy = asset.updatedBy, updatedOn = asset.updatedOn, updatedOnTimeZone = asset.updatedOnTimeZone)

  private[models] val assetTable = TableQuery[AssetTable]

  private def add(asset: Asset): Future[String] = db.run((assetTable returning assetTable.map(_.hashID) += serialize(asset)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(assets: Seq[Asset]): Future[Seq[String]] = db.run((assetTable returning assetTable.map(_.hashID) ++= assets.map(x => serialize(x))).asTry).map {
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

  private def tryGetByID(classificationID: String, hashID: String) = db.run(assetTable.filter(x => x.classificationID === classificationID && x.hashID === hashID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ASSET_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByID(classificationID: String, hashID: String) = db.run(assetTable.filter(x => x.classificationID === classificationID && x.hashID === hashID).result.headOption)

  private def getAllAssets = db.run(assetTable.result)

  private def deleteByID(classificationID: String, hashID: String): Future[Int] = db.run(assetTable.filter(x => x.classificationID === classificationID && x.hashID === hashID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ASSET_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ASSET_DELETE_FAILED, noSuchElementException)
    }
  }

  private def checkExistsByID(classificationID: String, hashID: String) = db.run(assetTable.filter(x => x.classificationID === classificationID && x.hashID === hashID).exists.result)

  private[models] class AssetTable(tag: Tag) extends Table[AssetSerialized](tag, "Asset_BC") {

    def * = (classificationID, hashID, immutables, mutables, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (AssetSerialized.tupled, AssetSerialized.unapply)

    def classificationID = column[String]("classificationID", O.PrimaryKey)

    def hashID = column[String]("hashID", O.PrimaryKey)

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

    def tryGet(id: AssetID): Future[Asset] = tryGetByID(classificationID = id.classificationID.asString, hashID = id.hashID).map(_.deserialize)

    def get(id: AssetID): Future[Option[Asset]] = getByID(classificationID = id.classificationID.asString, hashID = id.hashID).map(_.map(_.deserialize))

    def getAll: Future[Seq[Asset]] = getAllAssets.map(_.map(_.deserialize))

    def insertMultiple(assets: Seq[Asset]): Future[Seq[String]] = addMultiple(assets)

    def insertOrUpdate(asset: Asset): Future[Int] = upsert(asset)

    def delete(id: AssetID): Future[Int] = deleteByID(classificationID = id.classificationID.asString, hashID = id.hashID)

    def checkExists(id: AssetID): Future[Boolean] = checkExistsByID(classificationID = id.classificationID.asString, hashID = id.hashID)
  }

  object Utility {

    def onDefine(assetDefine: AssetDefine)(implicit header: Header): Future[Unit] = {
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(assetDefine.immutableMetaTraits.metaPropertyList)
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(assetDefine.mutableMetaTraits.metaPropertyList)

      def defineAndSuperAuxiliary(scrubbedImmutableMetaProperties: Seq[Property], scrubbedMutableMetaProperties: Seq[Property]) = {
        val mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ assetDefine.mutableTraits.propertyList))
        val defineAuxiliary = blockchainClassifications.Utility.auxiliaryDefine(immutables = Immutables(Properties(scrubbedImmutableMetaProperties ++ assetDefine.immutableTraits.propertyList)), mutables = mutables)

        def superAuxiliary(classificationID: ClassificationID) = blockchainMaintainers.Utility.auxiliarySuper(classificationID = classificationID, identityID = IdentityID(assetDefine.fromID), mutableTraits = mutables)

        for {
          classificationID <- defineAuxiliary
          _ <- superAuxiliary(classificationID = classificationID)
        } yield classificationID
      }

      (for {
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        classificationID <- defineAndSuperAuxiliary(scrubbedImmutableMetaProperties = scrubbedImmutableMetaProperties, scrubbedMutableMetaProperties = scrubbedMutableMetaProperties)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.ASSET_DEFINE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onMint(assetMint: AssetMint)(implicit header: Header): Future[Unit] = {
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(assetMint.immutableMetaProperties.metaPropertyList)
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(assetMint.mutableMetaProperties.metaPropertyList)

      def upsert(scrubbedImmutableMetaProperties: Seq[Property], scrubbedMutableMetaProperties: Seq[Property]) = {
        val immutables = Immutables(Properties(scrubbedImmutableMetaProperties ++ assetMint.immutableProperties.propertyList))
        val assetID = AssetID(classificationID = assetMint.classificationID, hashID = immutables.getHashID)
        val mintAuxiliary = blockchainSplits.Utility.auxiliaryMint(ownerID = assetMint.toID, ownableID = assetID.asString, splitValue = constants.Blockchain.SmallestDec)
        val upsertAsset = Service.insertOrUpdate(Asset(id = assetID, immutables = immutables, mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ assetMint.mutableProperties.propertyList))))

        for {
          _ <- mintAuxiliary
          _ <- upsertAsset
        } yield assetID
      }

      (for {
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        assetID <- upsert(scrubbedImmutableMetaProperties = scrubbedImmutableMetaProperties, scrubbedMutableMetaProperties = scrubbedMutableMetaProperties)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.ASSET_MINT + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onMutate(assetMutate: AssetMutate)(implicit header: Header): Future[Unit] = {
      val oldAsset = Service.tryGet(AssetID(assetMutate.assetID))
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(assetMutate.mutableMetaProperties.metaPropertyList)

      def upsertAsset(oldAsset: Asset, scrubbedMutableMetaProperties: Seq[Property]) = Service.insertOrUpdate(oldAsset.copy(mutables = oldAsset.mutables.mutate(scrubbedMutableMetaProperties ++ assetMutate.mutableProperties.propertyList)))

      (for {
        oldAsset <- oldAsset
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        _ <- upsertAsset(oldAsset, scrubbedMutableMetaProperties)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.ASSET_MUTATE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onBurn(assetBurn: AssetBurn)(implicit header: Header): Future[Unit] = {
      val burnAuxiliary = blockchainSplits.Utility.auxiliaryBurn(ownerID = assetBurn.fromID, ownableID = assetBurn.assetID, splitValue = constants.Blockchain.SmallestDec)
      val assetID = AssetID(assetBurn.assetID)
      val deleteAsset = Service.delete(assetID)

      (for {
        _ <- burnAuxiliary
        _ <- deleteAsset
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.ASSET_BURN + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }
  }

}