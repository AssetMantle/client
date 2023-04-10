package models.blockchain

import models.traits.{Entity, GenericDaoImpl, Logging, ModelTable}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.data.base.{DecData, HeightData}
import schema.document.Document
import schema.id.base._
import schema.list.PropertyList
import schema.property.Property
import schema.property.base.MetaProperty
import schema.qualified.{Immutables, Mutables}
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Asset(id: Array[Byte], idString: String, classificationID: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getIDString: String = utilities.Secrets.base64URLEncoder(this.id)

  def getID: AssetID = AssetID(HashID(this.id))

  def getClassificationIDString: String = utilities.Secrets.base64URLEncoder(this.classificationID)

  def getClassificationID: ClassificationID = ClassificationID(this.classificationID)

  def getImmutables: Immutables = Immutables(this.immutables)

  def getMutables: Mutables = Mutables(this.mutables)

  def getDocument: Document = Document(this.getClassificationID, this.getImmutables, this.getMutables)

  def getProperty(id: PropertyID): Option[Property] = this.getDocument.getProperty(id)

  def getSupply: DecData = {
    val supply = this.getProperty(constants.Blockchain.SupplyProperty.getID)
    DecData((if (supply.isDefined) MetaProperty(supply.get.getProtoBytes) else constants.Blockchain.SupplyProperty).getData.getProtoBytes)
  }

  def getBurnHeight: HeightData = {
    val burnHeight = this.getProperty(constants.Blockchain.BurnHeightProperty.getID)
    HeightData((if (burnHeight.isDefined) MetaProperty(burnHeight.get.getProtoBytes) else constants.Blockchain.BurnHeightProperty).getData.getProtoBytes)
  }

  def getLockHeight: HeightData = {
    val lock = this.getProperty(constants.Blockchain.LockProperty.getID)
    HeightData((if (lock.isDefined) MetaProperty(lock.get.getProtoBytes) else constants.Blockchain.LockProperty).getData.getProtoBytes)
  }

  def mutate(properties: Seq[Property]): Asset = this.copy(mutables = this.getMutables.mutate(properties).getProtoBytes)
}

object Assets {

  implicit val module: String = constants.Module.BLOCKCHAIN_ASSET

  implicit val logger: Logger = Logger(this.getClass)

  class DataTable(tag: Tag) extends Table[Asset](tag, "Asset") with ModelTable[Array[Byte]] {

    def * = (id, idString, classificationID, immutables, mutables, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Asset.tupled, Asset.unapply)

    def id = column[Array[Byte]]("id", O.PrimaryKey)

    def idString = column[String]("idString")

    def classificationID = column[Array[Byte]]("classificationID")

    def immutables = column[Array[Byte]]("immutables")

    def mutables = column[Array[Byte]]("mutables")

    def createdBy = column[String]("createdBy")

    def createdOnMillisEpoch = column[Long]("createdOnMillisEpoch")

    def updatedBy = column[String]("updatedBy")

    def updatedOnMillisEpoch = column[Long]("updatedOnMillisEpoch")

  }

  val TableQuery = new TableQuery(tag => new DataTable(tag))

}

@Singleton
class Assets @Inject()(
                        blockchainSplits: Splits,
                        blockchainMaintainers: Maintainers,
                        blockchainClassifications: Classifications,
                        protected val databaseConfigProvider: DatabaseConfigProvider
                      )(implicit override val executionContext: ExecutionContext)
  extends GenericDaoImpl[Assets.DataTable, Asset, Array[Byte]](
    databaseConfigProvider,
    Assets.TableQuery,
    executionContext,
    Assets.module,
    Assets.logger
  ) {

  object Service {

    def add(asset: Asset): Future[String] = create(asset).map(x => utilities.Secrets.base64URLEncoder(x))

    def add(assets: Seq[Asset]): Future[Unit] = create(assets)

    def update(asset: Asset): Future[Unit] = updateById(asset)

    def get(id: String): Future[Option[Asset]] = getById(utilities.Secrets.base64URLDecode(id))

    def get(id: AssetID): Future[Option[Asset]] = getById(id.getBytes)

    def get(id: Array[Byte]): Future[Option[Asset]] = getById(id)

    def tryGet(id: AssetID): Future[Asset] = tryGetById(id.getBytes)

    def tryGet(id: String): Future[Asset] = tryGetById(utilities.Secrets.base64URLDecode(id))

    def fetchAll: Future[Seq[Asset]] = getAll

    def delete(assetID: AssetID): Future[Int] = deleteById(assetID.getBytes)


  }

  object Utility {

    def onMint(msg: com.assets.transactions.mint.Message): Future[String] = {
      val immutables = Immutables(PropertyList(msg.getImmutableMetaProperties).add(PropertyList(msg.getImmutableProperties).properties))
      val classificationID = ClassificationID(msg.getClassificationID)
      val assetID = utilities.ID.getAssetID(classificationID = classificationID, immutables = immutables)
      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties).add(PropertyList(msg.getMutableProperties).properties))
      val asset = Asset(id = assetID.getBytes, idString = assetID.asString, classificationID = ClassificationID(msg.getClassificationID).getBytes, immutables = immutables.getProtoBytes, mutables = mutables.getProtoBytes)

      val add = Service.add(asset)
      val bond = blockchainClassifications.Utility.bondAuxiliary(msg.getFrom, classificationID)

      def mint() = blockchainSplits.Utility.mint(ownerID = IdentityID(msg.getToID), ownableID = assetID, value = asset.getSupply.getValue)

      for {
        _ <- add
        _ <- bond
        _ <- mint()
      } yield msg.getFrom
    }

    def onMutate(msg: com.assets.transactions.mutate.Message): Future[String] = {
      val assetID = AssetID(msg.getAssetID)
      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties).add(PropertyList(msg.getMutableProperties).properties))
      val asset = Service.tryGet(assetID)

      def updateAsset(asset: Asset) = Service.update(asset.mutate(mutables.getProperties))

      for {
        asset <- asset
        _ <- updateAsset(asset)
      } yield msg.getFrom
    }

    def onRevoke(msg: com.assets.transactions.revoke.Message): Future[String] = {
      val deputize = blockchainMaintainers.Utility.revoke(fromID = IdentityID(msg.getFromID), toID = IdentityID(msg.getToID), maintainedClassificationID = ClassificationID(msg.getClassificationID))
      for {
        _ <- deputize
      } yield msg.getFrom
    }

    def onRenumerate(msg: com.assets.transactions.renumerate.Message): Future[String] = {
      val assetID = AssetID(msg.getAssetID)
      val asset = Service.tryGet(assetID)

      def updateSupply(asset: Asset) = blockchainSplits.Utility.renumerate(IdentityID(msg.getFromID), assetID, asset.getSupply.getValue)

      for {
        asset <- asset
        _ <- updateSupply(asset)
      } yield msg.getFrom
    }

    def onDeputize(msg: com.assets.transactions.deputize.Message): Future[String] = {
      val deputize = blockchainMaintainers.Utility.deputize(fromID = IdentityID(msg.getFromID), toID = IdentityID(msg.getToID), maintainedClassificationID = ClassificationID(msg.getClassificationID), maintainedProperties = PropertyList(msg.getMaintainedProperties), canMintAsset = msg.getCanMintAsset, canBurnAsset = msg.getCanBurnAsset, canRenumerateAsset = msg.getCanRenumerateAsset, canAddMaintainer = msg.getCanAddMaintainer, canRemoveMaintainer = msg.getCanRemoveMaintainer, canMutateMaintainer = msg.getCanMutateMaintainer)
      for {
        _ <- deputize
      } yield msg.getFrom
    }

    def onBurn(msg: com.assets.transactions.burn.Message): Future[String] = {
      val assetID = AssetID(msg.getAssetID)
      val renumerate = blockchainSplits.Utility.renumerate(ownerID = IdentityID(msg.getFromID), ownableID = assetID, value = constants.Blockchain.ZeroDec)
      val asset = Service.tryGet(assetID)

      def updateUnbondAndDelete(asset: Asset) = {
        val unbond = blockchainClassifications.Utility.unbondAuxiliary(msg.getFrom, asset.getClassificationID)
        val deleteAsset = Service.delete(assetID)

        for {
          _ <- unbond
          _ <- deleteAsset
        } yield ()
      }

      for {
        _ <- renumerate
        asset <- asset
        _ <- updateUnbondAndDelete(asset)
      } yield msg.getFrom
    }

  }
}