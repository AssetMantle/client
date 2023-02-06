package models.blockchain

import models.traits.{Entity, GenericDaoImpl, Logging, ModelTable}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import queries.responses.common.Header
import schema.data.base.{DecData, HeightData, IDData}
import schema.document.Document
import schema.id.OwnableID
import schema.id.base._
import schema.list.PropertyList
import schema.property.Property
import schema.property.base.MetaProperty
import schema.qualified.{Immutables, Mutables}
import schema.types.Height
import slick.jdbc.H2Profile.api._
import utilities.AttoNumber

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Order(id: Array[Byte], classificationID: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getIDString: String = utilities.Secrets.base64URLEncoder(this.id)

  def getID: OrderID = OrderID(HashID(this.id))

  def getClassificationIDString: String = utilities.Secrets.base64URLEncoder(this.classificationID)

  def getClassificationID: ClassificationID = ClassificationID(this.classificationID)

  def getImmutables: Immutables = Immutables(this.immutables)

  def getMutables: Mutables = Mutables(this.mutables)

  def getDocument: Document = Document(this.getClassificationID, this.getImmutables, this.getMutables)

  def getProperty(id: PropertyID): Option[Property] = this.getDocument.getProperty(id)

  def getMakerID: IdentityID = {
    val property = this.getProperty(constants.Blockchain.MakerIDProperty.getID)
    if (property.isDefined && property.get.isMeta) IdentityID(IDData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).getAnyID.getIdentityID) else IdentityID(HashID(Array[Byte]()))
  }

  def getMakerOwnableID: OwnableID = {
    val property = this.getProperty(constants.Blockchain.MakerOwnableIDProperty.getID)
    if (property.isDefined && property.get.isMeta) OwnableID(IDData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).getProtoBytes) else OwnableID(IDData(StringID("").toAnyID).getProtoBytes)
  }

  def getTakerOwnableID: OwnableID = {
    val property = this.getProperty(constants.Blockchain.TakerOwnableIDProperty.getID)
    if (property.isDefined && property.get.isMeta) OwnableID(IDData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).getProtoBytes) else OwnableID(IDData(StringID("").toAnyID).getProtoBytes)
  }

  def getExchangeRate: BigDecimal = {
    val property = this.getProperty(constants.Blockchain.ExchangeRateProperty.getID)
    if (property.isDefined && property.get.isMeta) DecData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).value.toBigDecimal else constants.Blockchain.SmallestDec
  }

  def getMakerOwnableSplit: BigDecimal = {
    val property = this.getProperty(constants.Blockchain.MakerOwnableSplitProperty.getID)
    if (property.isDefined && property.get.isMeta) DecData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).value.toBigDecimal else constants.Blockchain.SmallestDec
  }

  def mutate(properties: Seq[Property]): Order = this.copy(mutables = this.getMutables.mutate(properties).getProtoBytes)
}

object Orders {

  implicit val module: String = constants.Module.BLOCKCHAIN_ORDER

  implicit val logger: Logger = Logger(this.getClass)

  class DataTable(tag: Tag) extends Table[Order](tag, "Order") with ModelTable[Array[Byte]] {

    def * = (id, classificationID, immutables, mutables, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Order.tupled, Order.unapply)

    def id = column[Array[Byte]]("id", O.PrimaryKey)

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
class Orders @Inject()(
                        blockchainMaintainers: Maintainers,
                        blockchainSplits: Splits,
                        protected val databaseConfigProvider: DatabaseConfigProvider
                      )(implicit override val executionContext: ExecutionContext)
  extends GenericDaoImpl[Orders.DataTable, Order, Array[Byte]](
    databaseConfigProvider,
    Orders.TableQuery,
    executionContext,
    Orders.module,
    Orders.logger
  ) {

  object Service {

    def add(order: Order): Future[String] = create(order).map(x => utilities.Secrets.base64URLEncoder(x))

    def get(id: String): Future[Option[Order]] = getById(utilities.Secrets.base64URLDecode(id))

    def get(id: Array[Byte]): Future[Option[Order]] = getById(id)

    def tryGet(id: String): Future[Order] = tryGetById(utilities.Secrets.base64URLDecode(id))

    def tryGet(id: OrderID): Future[Order] = tryGetById(id.getBytes)

    def fetchAll: Future[Seq[Order]] = getAll

    def update(order: Order): Future[Unit] = updateById(order)

    def delete(orderID: OrderID): Future[Int] = deleteById(orderID.getBytes)

  }

  object Utility {

    def onMake(msg: com.orders.transactions.make.Message)(implicit header: Header): Future[String] = {
      val classificationID = ClassificationID(msg.getClassificationID)

      val immutables = Immutables(PropertyList(PropertyList(msg.getImmutableMetaProperties).propertyList
        ++ Seq(
        constants.Blockchain.ExchangeRateProperty.copy(data = AttoNumber(msg.getTakerOwnableSplit).quotientTruncate(AttoNumber(constants.Blockchain.SmallestDec)).quotientTruncate(AttoNumber(msg.getMakerOwnableSplit)).toDecData.toAnyData),
        constants.Blockchain.CreationHeightProperty.copy(data = HeightData(Height(header.height.toLong)).toAnyData),
        constants.Blockchain.MakerOwnableIDProperty.copy(data = IDData(OwnableID(msg.getMakerOwnableID).toAnyID).toAnyData),
        constants.Blockchain.TakerOwnableIDProperty.copy(data = IDData(OwnableID(msg.getTakerOwnableID).toAnyID).toAnyData),
        constants.Blockchain.MakerIDProperty.copy(data = IDData(IdentityID(msg.getFromID).toAnyID).toAnyData),
        constants.Blockchain.TakerIDProperty.copy(data = IDData(IdentityID(msg.getTakerID).toAnyID).toAnyData))
        ++ PropertyList(msg.getImmutableProperties).propertyList)
      )
      val orderID = utilities.ID.getOrderID(classificationID = classificationID, immutables = immutables)

      val mutables = Mutables(PropertyList(PropertyList(msg.getMutableMetaProperties).propertyList
        ++ Seq(
        constants.Blockchain.ExpiryHeightProperty.copy(data = HeightData(Height(msg.getExpiresIn.getValue + header.height.toLong)).toAnyData),
        constants.Blockchain.MakerOwnableSplitProperty.copy(data = AttoNumber(msg.getMakerOwnableSplit).toDecData.toAnyData))
        ++ PropertyList(msg.getMutableProperties).propertyList)
      )

      val order = Order(id = orderID.getBytes, classificationID = ClassificationID(msg.getClassificationID).getBytes, immutables = immutables.getProtoBytes, mutables = mutables.getProtoBytes)
      val add = Service.add(order)
      val transfer = blockchainSplits.Utility.transfer(fromID = IdentityID(msg.getFromID), toID = constants.Blockchain.OrderIdentityID, ownableID = OwnableID(msg.getMakerOwnableID), value = BigDecimal(msg.getMakerOwnableSplit))

      for {
        _ <- add
        _ <- transfer
      } yield msg.getFrom
    }

    def onModify(msg: com.orders.transactions.modify.Message)(implicit header: Header): Future[String] = {
      val orderID = OrderID(msg.getOrderID)
      val mutables = Mutables(PropertyList(PropertyList(msg.getMutableMetaProperties).propertyList
        ++ Seq(
        constants.Blockchain.ExpiryHeightProperty.copy(data = HeightData(Height(msg.getExpiresIn.getValue + header.height.toLong)).toAnyData),
        constants.Blockchain.MakerOwnableSplitProperty.copy(data = AttoNumber(msg.getMakerOwnableSplit).toDecData.toAnyData))
        ++ PropertyList(msg.getMutableProperties).propertyList)
      )
      val order = Service.tryGet(orderID)

      def transfer(order: Order) = {
        val transferMakerOwnableSplit = BigDecimal(msg.getMakerOwnableSplit) - order.getMakerOwnableSplit
        if (transferMakerOwnableSplit < constants.Blockchain.ZeroDec) {
          blockchainSplits.Utility.transfer(fromID = constants.Blockchain.OrderIdentityID, toID = IdentityID(msg.getFromID), ownableID = order.getMakerOwnableID, value = transferMakerOwnableSplit.abs)
        } else if (transferMakerOwnableSplit > constants.Blockchain.ZeroDec) {
          blockchainSplits.Utility.transfer(fromID = IdentityID(msg.getFromID), toID = constants.Blockchain.OrderIdentityID, ownableID = order.getMakerOwnableID, value = transferMakerOwnableSplit)
        } else Future()
      }

      def update(order: Order) = Service.update(order.mutate(mutables.getProperties))

      for {
        order <- order
        _ <- transfer(order)
        _ <- update(order)
      } yield msg.getFrom
    }

    def onCancel(msg: com.orders.transactions.cancel.Message)(implicit header: Header): Future[String] = {
      val orderID = OrderID(msg.getOrderID)
      val order = Service.tryGet(orderID)

      def transfer(order: Order) = blockchainSplits.Utility.transfer(fromID = constants.Blockchain.OrderIdentityID, toID = IdentityID(msg.getFromID), ownableID = order.getMakerOwnableID, value = order.getMakerOwnableSplit)

      def delete = Service.delete(orderID)

      for {
        order <- order
        _ <- transfer(order)
        _ <- delete
      } yield msg.getFrom
    }

    def onTake(msg: com.orders.transactions.take.Message)(implicit header: Header): Future[String] = {
      val orderID = OrderID(msg.getOrderID)
      val order = Service.tryGet(orderID)

      def update(order: Order) = {
        var makerReceiveTakerOwnableSplit = AttoNumber(order.getMakerOwnableSplit).multiplyTruncate(AttoNumber(order.getExchangeRate)).multiplyTruncate(AttoNumber(constants.Blockchain.SmallestDec))
        var takerReceiveMakerOwnableSplit = AttoNumber(msg.getTakerOwnableSplit).quotientTruncate(AttoNumber(constants.Blockchain.SmallestDec)).quotientTruncate(AttoNumber(order.getExchangeRate))

        val updatedMakerOwnableSplit = order.getMakerOwnableSplit - takerReceiveMakerOwnableSplit.toBigDecimal
        val updateOrDelete = if (updatedMakerOwnableSplit == constants.Blockchain.ZeroDec) {
          Service.delete(orderID)
        } else if (updatedMakerOwnableSplit < constants.Blockchain.ZeroDec) {
          takerReceiveMakerOwnableSplit = AttoNumber(order.getMakerOwnableSplit)
          Service.delete(orderID)
        } else {
          makerReceiveTakerOwnableSplit = AttoNumber(msg.getTakerOwnableSplit)
          Service.update(order.mutate(Seq(constants.Blockchain.MakerOwnableSplitProperty.copy(data = DecData(AttoNumber(updatedMakerOwnableSplit)).toAnyData))))
        }

        val takerTransfer = blockchainSplits.Utility.transfer(fromID = IdentityID(msg.getFromID), toID = order.getMakerID, ownableID = order.getTakerOwnableID, value = makerReceiveTakerOwnableSplit.toBigDecimal)
        val mekerTransfer = blockchainSplits.Utility.transfer(fromID = constants.Blockchain.OrderIdentityID, toID = IdentityID(msg.getFromID), ownableID = order.getMakerOwnableID, value = takerReceiveMakerOwnableSplit.toBigDecimal)

        for {
          _ <- updateOrDelete
          _ <- takerTransfer
          _ <- mekerTransfer
        } yield ()
      }

      for {
        order <- order
        _ <- update(order)
      } yield msg.getFrom
    }

    def onRevoke(msg: com.orders.transactions.revoke.Message): Future[String] = {
      val deputize = blockchainMaintainers.Utility.revoke(fromID = IdentityID(msg.getFromID), toID = IdentityID(msg.getToID), maintainedClassificationID = ClassificationID(msg.getClassificationID))
      for {
        _ <- deputize
      } yield msg.getFrom
    }

    def onDeputize(msg: com.orders.transactions.deputize.Message): Future[String] = {
      val deputize = blockchainMaintainers.Utility.deputize(fromID = IdentityID(msg.getFromID), toID = IdentityID(msg.getToID), maintainedClassificationID = ClassificationID(msg.getClassificationID), maintainedProperties = PropertyList(msg.getMaintainedProperties), canMintAsset = msg.getCanMintAsset, canBurnAsset = msg.getCanBurnAsset, canRenumerateAsset = msg.getCanRenumerateAsset, canAddMaintainer = msg.getCanAddMaintainer, canRemoveMaintainer = msg.getCanRemoveMaintainer, canMutateMaintainer = msg.getCanMutateMaintainer)
      for {
        _ <- deputize
      } yield msg.getFrom
    }

  }
}