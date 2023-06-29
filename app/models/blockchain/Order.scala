package models.blockchain

import com.assetmantle.modules.orders.{transactions => ordersTransactions}
import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import queries.responses.common.Header
import schema.data.base.{DecData, HeightData, IDData, NumberData}
import schema.document.Document
import schema.id.OwnableID
import schema.id.base._
import schema.list.PropertyList
import schema.property.Property
import schema.property.base.MetaProperty
import schema.qualified.{Immutables, Mutables}
import schema.types.Height
import slick.jdbc.H2Profile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class Order(id: Array[Byte], idString: String, classificationID: Array[Byte], immutables: Array[Byte], mutables: Array[Byte], createdBy: Option[String] = None, createdOnMillisEpoch: Option[Long] = None, updatedBy: Option[String] = None, updatedOnMillisEpoch: Option[Long] = None) extends Logging with Entity[Array[Byte]] {

  def getIDString: String = utilities.Secrets.base64URLEncoder(this.id)

  def getID: OrderID = OrderID(HashID(this.id))

  def getClassificationIDString: String = utilities.Secrets.base64URLEncoder(this.classificationID)

  def getClassificationID: ClassificationID = ClassificationID(this.classificationID)

  def getImmutables: Immutables = Immutables(this.immutables)

  def getMutables: Mutables = Mutables(this.mutables)

  def getDocument: Document = Document(this.getClassificationID, this.getImmutables, this.getMutables)

  def getProperty(id: PropertyID): Option[Property] = this.getDocument.getProperty(id)

  def getMakerID: IdentityID = {
    val property = this.getProperty(schema.constants.Properties.MakerIDProperty.getID)
    if (property.isDefined && property.get.isMeta) IdentityID(IDData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).getAnyID.getIdentityID) else IdentityID(HashID(Array[Byte]()))
  }

  def getMakerOwnableID: OwnableID = {
    val property = this.getProperty(schema.constants.Properties.MakerOwnableIDProperty.getID)
    if (property.isDefined && property.get.isMeta) IDData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).getID.asInstanceOf[OwnableID] else OwnableID(IDData(StringID("")).getProtoBytes)
  }

  def getTakerOwnableID: OwnableID = {
    val property = this.getProperty(schema.constants.Properties.TakerOwnableIDProperty.getID)
    if (property.isDefined && property.get.isMeta) IDData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).getID.asInstanceOf[OwnableID] else OwnableID(IDData(StringID("")).getProtoBytes)
  }

  def getExchangeRate: BigDecimal = {
    val property = this.getProperty(schema.constants.Properties.ExchangeRateProperty.getID)
    if (property.isDefined && property.get.isMeta) DecData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).getValue else schema.data.constants.ZeroDec
  }

  def getExpiryHeight: Long = {
    val property = this.getProperty(schema.constants.Properties.ExpiryHeightProperty.getID)
    if (property.isDefined && property.get.isMeta) HeightData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).value.value else -1
  }

  def getMakerOwnableSplit: BigInt = {
    val property = this.getProperty(schema.constants.Properties.MakerOwnableSplitProperty.getID)
    if (property.isDefined && property.get.isMeta) NumberData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).value else BigInt(1)
  }

  def mutate(properties: Seq[Property]): Order = this.copy(mutables = this.getMutables.mutate(properties).getProtoBytes)
}

private[blockchain] object Orders {
  class OrderTable(tag: Tag) extends Table[Order](tag, "Order") with ModelTable[Array[Byte]] {

    def * = (id, idString, classificationID, immutables, mutables, createdBy.?, createdOnMillisEpoch.?, updatedBy.?, updatedOnMillisEpoch.?) <> (Order.tupled, Order.unapply)

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
class Orders @Inject()(
                        blockchainClassifications: Classifications,
                        blockchainMaintainers: Maintainers,
                        blockchainSplits: Splits,
                        utilitiesOperations: utilities.Operations,
                        protected val dbConfigProvider: DatabaseConfigProvider
                      )(implicit val executionContext: ExecutionContext)
  extends GenericDaoImpl[Orders.OrderTable, Order, Array[Byte]]() {

  implicit val module: String = constants.Module.BLOCKCHAIN_ORDER

  implicit val logger: Logger = Logger(this.getClass)

  val tableQuery = new TableQuery(tag => new Orders.OrderTable(tag))

  object Service {

    def add(order: Order): Future[String] = create(order).map(_.idString)

    def add(orders: Seq[Order]): Future[Int] = create(orders)

    def get(id: String): Future[Option[Order]] = getById(utilities.Secrets.base64URLDecode(id))

    def get(id: Array[Byte]): Future[Option[Order]] = getById(id)

    def tryGet(id: String): Future[Order] = tryGetById(utilities.Secrets.base64URLDecode(id))

    def tryGet(id: OrderID): Future[Order] = tryGetById(id.getBytes)

    def fetchAll: Future[Seq[Order]] = getAll

    def update(order: Order): Future[Unit] = updateById(order)

    def delete(orderID: OrderID): Future[Int] = deleteById(orderID.getBytes)

  }

  object Utility {

    def onDefine(msg: ordersTransactions.define.Message): Future[String] = {
      val immutables = Immutables(PropertyList(msg.getImmutableMetaProperties)
        .add(PropertyList(msg.getImmutableProperties).properties)
        .add(Seq(
          schema.constants.Properties.ExchangeRateProperty,
          schema.constants.Properties.CreationHeightProperty,
          schema.constants.Properties.MakerOwnableIDProperty,
          schema.constants.Properties.TakerOwnableIDProperty,
          schema.constants.Properties.MakerIDProperty,
          schema.constants.Properties.TakerIDProperty)))
      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties)
        .add(PropertyList(msg.getMutableProperties)
          .add(Seq(
            schema.constants.Properties.ExpiryHeightProperty,
            schema.constants.Properties.MakerOwnableSplitProperty)).getProperties))
      val add = blockchainClassifications.Utility.defineAuxiliary(msg.getFrom, mutables, immutables)

      def addMaintainer(classificationID: ClassificationID): Future[String] = blockchainMaintainers.Utility.superAuxiliary(classificationID, IdentityID(msg.getFromID), mutables)

      for {
        classificationID <- add
        _ <- addMaintainer(classificationID)
      } yield msg.getFrom
    }

    def onMake(msg: ordersTransactions.make.Message)(implicit header: Header): Future[String] = {
      val classificationID = ClassificationID(msg.getClassificationID)
      val makerOwnableSplit = BigInt(msg.getMakerOwnableSplit)

      val immutables = Immutables(PropertyList(msg.getImmutableMetaProperties)
        .add(Seq(
          schema.constants.Properties.ExchangeRateProperty.copy(data = DecData(msg.getTakerOwnableSplit).quotientTruncate(DecData(msg.getMakerOwnableSplit))),
          schema.constants.Properties.CreationHeightProperty.copy(data = HeightData(Height(header.height.toLong))),
          schema.constants.Properties.MakerOwnableIDProperty.copy(data = IDData(OwnableID(msg.getMakerOwnableID))),
          schema.constants.Properties.TakerOwnableIDProperty.copy(data = IDData(OwnableID(msg.getTakerOwnableID))),
          schema.constants.Properties.MakerIDProperty.copy(data = IDData(IdentityID(msg.getFromID))),
          schema.constants.Properties.TakerIDProperty.copy(data = IDData(IdentityID(msg.getTakerID)))))
        .add(PropertyList(msg.getImmutableProperties).properties))
      val orderID = schema.utilities.ID.getOrderID(classificationID = classificationID, immutables = immutables)

      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties)
        .add(Seq(
          schema.constants.Properties.ExpiryHeightProperty.copy(data = HeightData(Height(msg.getExpiresIn.getValue + header.height.toLong))),
          schema.constants.Properties.MakerOwnableSplitProperty.copy(data = NumberData(makerOwnableSplit))))
        .add(PropertyList(msg.getMutableProperties).properties))

      val order = Order(id = orderID.getBytes, idString = orderID.asString, classificationID = ClassificationID(msg.getClassificationID).getBytes, immutables = immutables.getProtoBytes, mutables = mutables.getProtoBytes)
      val add = Service.add(order)
      val transfer = blockchainSplits.Utility.transfer(fromID = IdentityID(msg.getFromID), toID = schema.constants.Document.OrderIdentityID, ownableID = OwnableID(msg.getMakerOwnableID), value = makerOwnableSplit)
      val bond = blockchainClassifications.Utility.bondAuxiliary(msg.getFrom, classificationID)

      for {
        _ <- add
        _ <- transfer
        _ <- bond
      } yield msg.getFrom
    }

    def onModify(msg: ordersTransactions.modify.Message)(implicit header: Header): Future[String] = {
      val orderID = OrderID(msg.getOrderID)
      val makerOwnableSplit = BigInt(msg.getMakerOwnableSplit)
      val mutables = Mutables(PropertyList(PropertyList(msg.getMutableMetaProperties).properties
        ++ Seq(
        schema.constants.Properties.ExpiryHeightProperty.copy(data = HeightData(Height(msg.getExpiresIn.getValue + header.height.toLong))),
        schema.constants.Properties.MakerOwnableSplitProperty.copy(data = NumberData(makerOwnableSplit)))
        ++ PropertyList(msg.getMutableProperties).properties)
      )
      val order = Service.tryGet(orderID)

      def transfer(order: Order) = {
        val transferMakerOwnableSplit = makerOwnableSplit - order.getMakerOwnableSplit
        if (transferMakerOwnableSplit < 0) {
          blockchainSplits.Utility.transfer(fromID = schema.constants.Document.OrderIdentityID, toID = IdentityID(msg.getFromID), ownableID = order.getMakerOwnableID, value = transferMakerOwnableSplit.abs)
        } else if (transferMakerOwnableSplit > 0) {
          blockchainSplits.Utility.transfer(fromID = IdentityID(msg.getFromID), toID = schema.constants.Document.OrderIdentityID, ownableID = order.getMakerOwnableID, value = transferMakerOwnableSplit)
        } else Future()
      }

      def update(order: Order) = Service.update(order.mutate(mutables.getProperties))

      for {
        order <- order
        _ <- transfer(order)
        _ <- update(order)
      } yield msg.getFrom
    }

    def onCancel(msg: ordersTransactions.cancel.Message)(implicit header: Header): Future[String] = {
      val orderID = OrderID(msg.getOrderID)
      val order = Service.tryGet(orderID)

      def transfer(order: Order) = blockchainSplits.Utility.transfer(fromID = schema.constants.Document.OrderIdentityID, toID = IdentityID(msg.getFromID), ownableID = order.getMakerOwnableID, value = order.getMakerOwnableSplit)

      def updateUnbondAndDelete(order: Order) = {
        val delete = Service.delete(order.getID)
        val unbond = blockchainClassifications.Utility.unbondAuxiliary(msg.getFrom, order.getClassificationID)
        for {
          _ <- delete
          _ <- unbond
        } yield ()
      }

      for {
        order <- order
        _ <- transfer(order)
        _ <- updateUnbondAndDelete(order)
      } yield msg.getFrom
    }

    def onTake(msg: ordersTransactions.take.Message)(implicit header: Header): Future[String] = {
      val orderID = OrderID(msg.getOrderID)
      val order = Service.tryGet(orderID)

      def update(order: Order) = {
        val burn = blockchainClassifications.Utility.burnAuxiliary(order.getClassificationID)
        val takerOwnableSplit = BigInt(msg.getTakerOwnableSplit)
        var makerReceiveTakerOwnableSplit = DecData(order.getMakerOwnableSplit.toString()).multiplyTruncate(DecData(order.getExchangeRate)).getValue.toBigInt
        var takerReceiveMakerOwnableSplit = DecData(msg.getTakerOwnableSplit).quotientTruncate(DecData(order.getExchangeRate)).getValue.toBigInt

        val updatedMakerOwnableSplit = order.getMakerOwnableSplit - takerReceiveMakerOwnableSplit
        val updateOrDelete = if (updatedMakerOwnableSplit == 0) {
          Service.delete(orderID)
        } else if (updatedMakerOwnableSplit < 0) {
          takerReceiveMakerOwnableSplit = order.getMakerOwnableSplit
          Service.delete(orderID)
        } else {
          makerReceiveTakerOwnableSplit = BigInt(msg.getTakerOwnableSplit)
          Service.update(order.mutate(Seq(schema.constants.Properties.MakerOwnableSplitProperty.copy(data = NumberData(updatedMakerOwnableSplit)))))
        }

        val makerTransfer = blockchainSplits.Utility.transfer(fromID = IdentityID(msg.getFromID), toID = order.getMakerID, ownableID = order.getTakerOwnableID, value = makerReceiveTakerOwnableSplit)
        val takerTransfer = blockchainSplits.Utility.transfer(fromID = schema.constants.Document.OrderIdentityID, toID = IdentityID(msg.getFromID), ownableID = order.getMakerOwnableID, value = takerReceiveMakerOwnableSplit)

        for {
          _ <- updateOrDelete
          _ <- makerTransfer
          _ <- takerTransfer
          _ <- burn
        } yield ()
      }

      for {
        order <- order
        _ <- update(order)
      } yield msg.getFrom
    }

    def onRevoke(msg: ordersTransactions.revoke.Message): Future[String] = {
      val deputize = blockchainMaintainers.Utility.revokeAuxiliary(fromID = IdentityID(msg.getFromID), toID = IdentityID(msg.getToID), maintainedClassificationID = ClassificationID(msg.getClassificationID))
      for {
        _ <- deputize
      } yield msg.getFrom
    }

    def onDeputize(msg: ordersTransactions.deputize.Message): Future[String] = {
      val deputize = blockchainMaintainers.Utility.deputizeAuxiliary(fromID = IdentityID(msg.getFromID), toID = IdentityID(msg.getToID), maintainedClassificationID = ClassificationID(msg.getClassificationID), maintainedProperties = PropertyList(msg.getMaintainedProperties), canMintAsset = msg.getCanMintAsset, canBurnAsset = msg.getCanBurnAsset, canRenumerateAsset = msg.getCanRenumerateAsset, canAddMaintainer = msg.getCanAddMaintainer, canRemoveMaintainer = msg.getCanRemoveMaintainer, canMutateMaintainer = msg.getCanMutateMaintainer)
      for {
        _ <- deputize
      } yield msg.getFrom
    }

    def onNewBlock(header: Header): Future[Unit] = {
      val allOrders = Service.fetchAll

      def filterAndDelete(orders: Seq[Order]) = utilitiesOperations.traverse(orders) { order =>
        if (header.height.toLong >= order.getExpiryHeight) {
          val delete = Service.delete(order.getID)
          val transferAux = blockchainSplits.Utility.transfer(fromID = schema.constants.Document.OrderIdentityID, toID = order.getMakerID, ownableID = order.getMakerOwnableID, value = order.getMakerOwnableSplit)
          for {
            _ <- delete
            _ <- transferAux
          } yield ()
        } else Future()
      }

      for {
        allOrders <- allOrders
        _ <- filterAndDelete(allOrders)
      } yield ()
    }

  }
}