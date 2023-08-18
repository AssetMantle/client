package models.blockchain

import com.assetmantle.modules.assets.{transactions => assetsTransactions}
import models.common.Serializable.Coin
import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import schema.data.base.{HeightData, NumberData}
import schema.document.Document
import schema.id.base._
import schema.list.PropertyList
import schema.property.Property
import schema.property.base.MetaProperty
import schema.qualified.{Immutables, Mutables}
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala

case class Asset(id: Array[Byte], idString: String, classificationID: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getIDString: String = utilities.Secrets.base64URLEncoder(this.id)

  def getID: AssetID = AssetID(HashID(this.id))

  def getClassificationIDString: String = utilities.Secrets.base64URLEncoder(this.classificationID)

  def getClassificationID: ClassificationID = ClassificationID(this.classificationID)

  def getImmutables: Immutables = Immutables(this.immutables)

  def getMutables: Mutables = Mutables(this.mutables)

  def getDocument: Document = Document(this.getClassificationID, this.getImmutables, this.getMutables)

  def getProperty(id: PropertyID): Option[Property] = this.getDocument.getProperty(id)

  def getSupply: NumberData = {
    val supply = this.getProperty(schema.constants.Properties.SupplyProperty.getID)
    NumberData((if (supply.isDefined) MetaProperty(supply.get.getProtoBytes) else schema.constants.Properties.SupplyProperty).getData.getProtoBytes)
  }

  def getBondAmount: NumberData = {
    val value = this.getMutables.getProperty(schema.constants.Properties.BondAmountProperty.getID)
    NumberData((if (value.isDefined) MetaProperty(value.get.getProtoBytes) else schema.constants.Properties.BondAmountProperty).getData.getProtoBytes)
  }

  def getBurnHeight: HeightData = {
    val burnHeight = this.getProperty(schema.constants.Properties.BurnHeightProperty.getID)
    HeightData((if (burnHeight.isDefined) MetaProperty(burnHeight.get.getProtoBytes) else schema.constants.Properties.BurnHeightProperty).getData.getProtoBytes)
  }

  def getLockHeight: HeightData = {
    val lock = this.getProperty(schema.constants.Properties.LockHeightProperty.getID)
    HeightData((if (lock.isDefined) MetaProperty(lock.get.getProtoBytes) else schema.constants.Properties.LockHeightProperty).getData.getProtoBytes)
  }

  def mutate(properties: Seq[Property]): Asset = this.copy(mutables = this.getMutables.mutate(properties).getProtoBytes)
}

private[blockchain] object Assets {

  class AssetTable(tag: Tag) extends Table[Asset](tag, "Asset") with ModelTable[Array[Byte]] {

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

}

@Singleton
class Assets @Inject()(
                        blockchainSplits: Splits,
                        blockchainBalances: Balances,
                        blockchainMaintainers: Maintainers,
                        blockchainClassifications: Classifications,
                        utilitiesOperations: utilities.Operations,
                        protected val dbConfigProvider: DatabaseConfigProvider
                      )(implicit val executionContext: ExecutionContext)
  extends GenericDaoImpl[Assets.AssetTable, Asset, Array[Byte]]() {

  implicit val module: String = constants.Module.BLOCKCHAIN_ASSET

  implicit val logger: Logger = Logger(this.getClass)

  val tableQuery = new TableQuery(tag => new Assets.AssetTable(tag))

  object Service {

    def add(asset: Asset): Future[Asset] = create(asset)

    def add(assets: Seq[Asset]): Future[Int] = create(assets)

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

    def onDefine(msg: assetsTransactions.define.Message): Future[String] = {
      val immutables = Immutables(PropertyList(msg.getImmutableMetaProperties).add(PropertyList(msg.getImmutableProperties).properties))
      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties).add(PropertyList(msg.getMutableProperties).properties))
      val add = blockchainClassifications.Utility.defineAuxiliary(msg.getFrom, mutables, immutables)

      def addMaintainer(classificationID: ClassificationID): Future[String] = blockchainMaintainers.Utility.superAuxiliary(classificationID, IdentityID(msg.getFromID), mutables, schema.utilities.Permissions.getAssetsPermissions(true, true, true))

      for {
        classificationID <- add
        _ <- addMaintainer(classificationID)
      } yield msg.getFrom
    }


    def onMint(msg: assetsTransactions.mint.Message): Future[String] = {
      val immutables = Immutables(PropertyList(msg.getImmutableMetaProperties).add(PropertyList(msg.getImmutableProperties).properties))
      val classificationID = ClassificationID(msg.getClassificationID)
      val assetID = schema.utilities.ID.getAssetID(classificationID = classificationID, immutables = immutables)
      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties).add(PropertyList(msg.getMutableProperties).properties))
      val asset = Asset(id = assetID.getBytes, idString = assetID.asString, classificationID = ClassificationID(msg.getClassificationID).getBytes, immutables = immutables.getProtoBytes, mutables = mutables.getProtoBytes)

      val add = Service.add(asset)
      val bond = blockchainClassifications.Utility.bondAuxiliary(msg.getFrom, classificationID, asset.getBondAmount)

      def mint() = blockchainSplits.Utility.mint(ownerID = IdentityID(msg.getToID), assetID = assetID, value = asset.getSupply.value)

      for {
        _ <- add
        _ <- bond
        _ <- mint()
      } yield msg.getFrom
    }

    def onMutate(msg: assetsTransactions.mutate.Message): Future[String] = {
      val assetID = AssetID(msg.getAssetID)
      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties).add(PropertyList(msg.getMutableProperties).properties))
      val asset = Service.tryGet(assetID)

      def updateAsset(asset: Asset) = Service.update(asset.mutate(mutables.getProperties))

      for {
        asset <- asset
        _ <- updateAsset(asset)
      } yield msg.getFrom
    }

    def onRevoke(msg: assetsTransactions.revoke.Message): Future[String] = {
      val revoke = blockchainMaintainers.Utility.revokeAuxiliary(fromID = IdentityID(msg.getFromID), toID = IdentityID(msg.getToID), maintainedClassificationID = ClassificationID(msg.getClassificationID))
      for {
        _ <- revoke
      } yield msg.getFrom
    }

    def onRenumerate(msg: assetsTransactions.renumerate.Message): Future[String] = {
      val assetID = AssetID(msg.getAssetID)
      val asset = Service.tryGet(assetID)

      def updateSupply(asset: Asset) = blockchainSplits.Utility.renumerate(IdentityID(msg.getFromID), assetID, asset.getSupply.value)

      for {
        asset <- asset
        _ <- updateSupply(asset)
      } yield msg.getFrom
    }

    def onDeputize(msg: assetsTransactions.deputize.Message): Future[String] = {
      val deputize = blockchainMaintainers.Utility.deputizeAuxiliary(fromID = IdentityID(msg.getFromID), toID = IdentityID(msg.getToID), maintainedClassificationID = ClassificationID(msg.getClassificationID), maintainedProperties = PropertyList(msg.getMaintainedProperties), permissionIDs = schema.utilities.Permissions.getAssetsPermissions(canMint = msg.getCanMintAsset, canBurn = msg.getCanBurnAsset, canRenumerate = msg.getCanRenumerateAsset), canAddMaintainer = msg.getCanAddMaintainer, canRemoveMaintainer = msg.getCanRemoveMaintainer, canMutateMaintainer = msg.getCanMutateMaintainer)
      for {
        _ <- deputize
      } yield msg.getFrom
    }

    def onBurn(msg: assetsTransactions.burn.Message): Future[String] = {
      val assetID = AssetID(msg.getAssetID)
      val renumerate = blockchainSplits.Utility.renumerate(ownerID = IdentityID(msg.getFromID), assetID = assetID, value = 0)
      val asset = Service.tryGet(assetID)

      def updateUnbondAndDelete(asset: Asset) = {
        val unbond = blockchainClassifications.Utility.unbondAuxiliary(msg.getFrom, asset.getClassificationID, asset.getBondAmount)
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

    def onSend(msg: assetsTransactions.send.Message): Future[String] = {
      val add = blockchainSplits.Utility.addSplit(ownerId = IdentityID(msg.getToID), assetID = AssetID(msg.getAssetID), value = BigInt(msg.getValue))
      val subtract = blockchainSplits.Utility.subtractSplit(ownerId = IdentityID(msg.getFromID), assetID = AssetID(msg.getAssetID), value = BigInt(msg.getValue))
      for {
        _ <- add
        _ <- subtract
      } yield msg.getFrom
    }

    def onWrap(msg: assetsTransactions.wrap.Message): Future[String] = {
      val updateBalance = blockchainBalances.Utility.insertOrUpdateBalance(msg.getFrom)
      val add = utilitiesOperations.traverse(msg.getCoinsList.asScala.toSeq.map(x => Coin(x))) { coin => blockchainSplits.Utility.addSplit(ownerId = IdentityID(msg.getFromID), assetID = schema.document.CoinAsset.getCoinAssetID(coin.denom), value = coin.amount.value) }
      for {
        _ <- updateBalance
        _ <- add
      } yield msg.getFrom
    }

    def onUnwrap(msg: assetsTransactions.unwrap.Message): Future[String] = {
      val updateBalance = blockchainBalances.Utility.insertOrUpdateBalance(msg.getFrom)
      val subtract = utilitiesOperations.traverse(msg.getCoinsList.asScala.toSeq.map(x => Coin(x))) { coin =>
        blockchainSplits.Utility.subtractSplit(ownerId = IdentityID(msg.getFromID), assetID = schema.document.CoinAsset.getCoinAssetID(coin.denom), value = coin.amount.value)
      }
      for {
        _ <- updateBalance
        _ <- subtract
      } yield msg.getFrom
    }

  }
}