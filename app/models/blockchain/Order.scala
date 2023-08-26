package models.blockchain

import com.assetmantle.modules.orders.{transactions => ordersTransactions}
import exceptions.BaseException
import models.traits._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import queries.responses.common.Header
import schema.data.base.{DecData, HeightData, IDData, NumberData}
import schema.document.Document
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

  def getMakerAssetID: AssetID = {
    val property = this.getProperty(schema.constants.Properties.MakerAssetIDProperty.getID)
    if (property.isDefined && property.get.isMeta) IDData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).getID.asInstanceOf[AssetID] else AssetID(IDData(StringID("")).getProtoBytes)
  }

  def getTakerAssetID: AssetID = {
    val property = this.getProperty(schema.constants.Properties.TakerAssetIDProperty.getID)
    if (property.isDefined && property.get.isMeta) IDData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).getID.asInstanceOf[AssetID] else AssetID(IDData(StringID("")).getProtoBytes)
  }

  def getBondAmount: NumberData = {
    val value = this.getMutables.getProperty(schema.constants.Properties.BondAmountProperty.getID)
    NumberData((if (value.isDefined) MetaProperty(value.get.getProtoBytes) else schema.constants.Properties.BondAmountProperty).getData.getProtoBytes)
  }


  def getExchangeRate: BigDecimal = {
    val property = this.getProperty(schema.constants.Properties.ExchangeRateProperty.getID)
    if (property.isDefined && property.get.isMeta) DecData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).getValue else schema.data.constants.ZeroDec
  }

  def getExpiryHeight: Long = {
    val property = this.getProperty(schema.constants.Properties.ExpiryHeightProperty.getID)
    if (property.isDefined && property.get.isMeta) HeightData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).value.value else -1
  }

  def getMakerSplit: BigInt = {
    val property = this.getProperty(schema.constants.Properties.MakerSplitProperty.getID)
    if (property.isDefined && property.get.isMeta) NumberData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).value else BigInt(0)
  }

  def getTakerSplit: BigInt = {
    val property = this.getProperty(schema.constants.Properties.TakerSplitProperty.getID)
    if (property.isDefined && property.get.isMeta) NumberData(MetaProperty(property.get.getProtoBytes).getData.getProtoBytes).value else BigInt(0)
  }

  def mutate(properties: Seq[Property]): Order = this.copy(mutables = this.getMutables.mutate(properties).getProtoBytes)

  def getDocumentType: String = constants.Document.Type.ORDER
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

  val OrderModuleIdentityID: IdentityID = schema.document.ModuleIdentity.getModuleIdentityID("orders")

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

    def countAll: Future[Int] = countTotal()

  }

  object Utility {

    def onDefine(msg: ordersTransactions.define.Message)(implicit header: Header): Future[String] = {
      val immutables = Immutables(PropertyList(msg.getImmutableMetaProperties)
        .add(PropertyList(msg.getImmutableProperties).properties)
        .add(Seq(
          schema.constants.Properties.ExchangeRateProperty,
          schema.constants.Properties.CreationHeightProperty,
          schema.constants.Properties.MakerAssetIDProperty,
          schema.constants.Properties.TakerAssetIDProperty,
          schema.constants.Properties.MakerIDProperty,
          schema.constants.Properties.TakerIDProperty)))
      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties)
        .add(PropertyList(msg.getMutableProperties)
          .add(Seq(
            schema.constants.Properties.ExpiryHeightProperty,
            schema.constants.Properties.MakerSplitProperty)).getProperties))
      val add = blockchainClassifications.Utility.defineAuxiliary(msg.getFrom, mutables, immutables, constants.Document.ClassificationType.ORDER)

      def addMaintainer(classificationID: ClassificationID): Future[String] = blockchainMaintainers.Utility.superAuxiliary(classificationID, IdentityID(msg.getFromID), mutables, schema.utilities.Permissions.getOrdersPermissions(true, true))

      (for {
        classificationID <- add
        _ <- addMaintainer(classificationID)
      } yield msg.getFrom).recover {
        case _: BaseException => logger.error(schema.constants.Messages.ORDER_DEFINE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          msg.getFrom
      }
    }

    def onMake(msg: ordersTransactions.make.Message)(implicit header: Header): Future[String] = {
      val classificationID = ClassificationID(msg.getClassificationID)
      val makerOwnableSplit = BigInt(msg.getMakerSplit)

      val immutables = Immutables(PropertyList(msg.getImmutableMetaProperties)
        .add(Seq(
          schema.constants.Properties.ExchangeRateProperty.copy(data = DecData(msg.getTakerSplit).quotientTruncate(DecData(msg.getMakerSplit))),
          schema.constants.Properties.CreationHeightProperty.copy(data = HeightData(Height(header.height.toLong))),
          schema.constants.Properties.MakerAssetIDProperty.copy(data = IDData(AssetID(msg.getMakerAssetID))),
          schema.constants.Properties.TakerAssetIDProperty.copy(data = IDData(AssetID(msg.getTakerAssetID))),
          schema.constants.Properties.MakerIDProperty.copy(data = IDData(IdentityID(msg.getFromID))),
          schema.constants.Properties.TakerIDProperty.copy(data = IDData(IdentityID(msg.getTakerID)))))
        .add(PropertyList(msg.getImmutableProperties).properties))
      val orderID = schema.utilities.ID.getOrderID(classificationID = classificationID, immutables = immutables)

      val mutables = Mutables(PropertyList(msg.getMutableMetaProperties)
        .add(Seq(
          schema.constants.Properties.ExpiryHeightProperty.copy(data = HeightData(Height(msg.getExpiresIn.getValue + header.height.toLong))),
          schema.constants.Properties.MakerSplitProperty.copy(data = NumberData(makerOwnableSplit))))
        .add(PropertyList(msg.getMutableProperties).properties))

      val order = Order(id = orderID.getBytes, idString = orderID.asString, classificationID = ClassificationID(msg.getClassificationID).getBytes, immutables = immutables.getProtoBytes, mutables = mutables.getProtoBytes)
      val add = Service.add(order)
      val transfer = blockchainSplits.Utility.transfer(fromID = IdentityID(msg.getFromID), toID = OrderModuleIdentityID, assetID = AssetID(msg.getMakerAssetID), value = makerOwnableSplit)
      val bond = blockchainClassifications.Utility.bondAuxiliary(msg.getFrom, classificationID, order.getBondAmount)

      (for {
        _ <- add
        _ <- transfer
        _ <- bond
      } yield msg.getFrom).recover {
        case _: BaseException => logger.error(schema.constants.Messages.ORDER_MAKE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          msg.getFrom
      }
    }

    def onModify(msg: ordersTransactions.modify.Message)(implicit header: Header): Future[String] = {
      val orderID = OrderID(msg.getOrderID)
      val makerOwnableSplit = BigInt(msg.getMakerSplit)
      val mutables = Mutables(PropertyList(PropertyList(msg.getMutableMetaProperties).properties
        ++ Seq(
        schema.constants.Properties.ExpiryHeightProperty.copy(data = HeightData(Height(msg.getExpiresIn.getValue + header.height.toLong))),
        schema.constants.Properties.MakerSplitProperty.copy(data = NumberData(makerOwnableSplit)))
        ++ PropertyList(msg.getMutableProperties).properties)
      )
      val order = Service.tryGet(orderID)

      def transfer(order: Order) = {
        val transferMakerOwnableSplit = makerOwnableSplit - order.getMakerSplit
        if (transferMakerOwnableSplit < 0) {
          blockchainSplits.Utility.transfer(fromID = OrderModuleIdentityID, toID = IdentityID(msg.getFromID), assetID = order.getMakerAssetID, value = transferMakerOwnableSplit.abs)
        } else if (transferMakerOwnableSplit > 0) {
          blockchainSplits.Utility.transfer(fromID = IdentityID(msg.getFromID), toID = OrderModuleIdentityID, assetID = order.getMakerAssetID, value = transferMakerOwnableSplit)
        } else Future()
      }

      def update(order: Order) = Service.update(order.mutate(mutables.getProperties))

      (for {
        order <- order
        _ <- transfer(order)
        _ <- update(order)
      } yield msg.getFrom).recover {
        case _: BaseException => logger.error(schema.constants.Messages.ORDER_MODIFY + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          msg.getFrom
      }
    }

    def onCancel(msg: ordersTransactions.cancel.Message)(implicit header: Header): Future[String] = {
      val orderID = OrderID(msg.getOrderID)
      val order = Service.tryGet(orderID)

      def transfer(order: Order) = blockchainSplits.Utility.transfer(fromID = OrderModuleIdentityID, toID = IdentityID(msg.getFromID), assetID = order.getMakerAssetID, value = order.getMakerSplit)

      def updateUnbondAndDelete(order: Order) = {
        val delete = Service.delete(order.getID)
        val unbond = blockchainClassifications.Utility.unbondAuxiliary(msg.getFrom, order.getClassificationID, order.getBondAmount)
        for {
          _ <- delete
          _ <- unbond
        } yield ()
      }

      (for {
        order <- order
        _ <- transfer(order)
        _ <- updateUnbondAndDelete(order)
      } yield msg.getFrom).recover {
        case _: BaseException => logger.error(schema.constants.Messages.ORDER_CANCEL + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          msg.getFrom
      }
    }

    def onTake(msg: ordersTransactions.take.Message)(implicit header: Header): Future[String] = {
      val orderID = OrderID(msg.getOrderID)
      val order = Service.tryGet(orderID)

      def update(order: Order) = {
        val burn = blockchainClassifications.Utility.burnAuxiliary(order.getClassificationID, order.getBondAmount)
        val takerOwnableSplit = BigInt(msg.getTakerSplit)
        var makerReceiveTakerOwnableSplit = DecData(order.getMakerSplit.toString()).multiplyTruncate(DecData(order.getExchangeRate)).getValue.toBigInt
        var takerReceiveMakerOwnableSplit = DecData(msg.getTakerSplit).quotientTruncate(DecData(order.getExchangeRate)).getValue.toBigInt

        val updatedMakerOwnableSplit = order.getMakerSplit - takerReceiveMakerOwnableSplit
        val updateOrDelete = if (updatedMakerOwnableSplit == 0) {
          Service.delete(orderID)
        } else if (updatedMakerOwnableSplit < 0) {
          takerReceiveMakerOwnableSplit = order.getMakerSplit
          Service.delete(orderID)
        } else {
          makerReceiveTakerOwnableSplit = BigInt(msg.getTakerSplit)
          Service.update(order.mutate(Seq(schema.constants.Properties.MakerSplitProperty.copy(data = NumberData(updatedMakerOwnableSplit)))))
        }

        val makerTransfer = blockchainSplits.Utility.transfer(fromID = IdentityID(msg.getFromID), toID = order.getMakerID, assetID = order.getTakerAssetID, value = makerReceiveTakerOwnableSplit)
        val takerTransfer = blockchainSplits.Utility.transfer(fromID = OrderModuleIdentityID, toID = IdentityID(msg.getFromID), assetID = order.getMakerAssetID, value = takerReceiveMakerOwnableSplit)

        for {
          _ <- updateOrDelete
          _ <- makerTransfer
          _ <- takerTransfer
          _ <- burn
        } yield ()
      }

      (for {
        order <- order
        _ <- update(order)
      } yield msg.getFrom).recover {
        case _: BaseException => logger.error(schema.constants.Messages.ORDER_TAKE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          msg.getFrom
      }
    }

    def onRevoke(msg: ordersTransactions.revoke.Message)(implicit header: Header): Future[String] = {
      val revoke = blockchainMaintainers.Utility.revokeAuxiliary(fromID = IdentityID(msg.getFromID), toID = IdentityID(msg.getToID), maintainedClassificationID = ClassificationID(msg.getClassificationID))
      (for {
        _ <- revoke
      } yield msg.getFrom).recover {
        case _: BaseException => logger.error(schema.constants.Messages.ORDER_REVOKE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          msg.getFrom
      }
    }

    def onDeputize(msg: ordersTransactions.deputize.Message)(implicit header: Header): Future[String] = {
      val deputize = blockchainMaintainers.Utility.deputizeAuxiliary(fromID = IdentityID(msg.getFromID), toID = IdentityID(msg.getToID), maintainedClassificationID = ClassificationID(msg.getClassificationID), maintainedProperties = PropertyList(msg.getMaintainedProperties), permissionIDs = schema.utilities.Permissions.getOrdersPermissions(canMake = msg.getCanMakeOrder, canCancel = msg.getCanCancelOrder), canAddMaintainer = msg.getCanAddMaintainer, canRemoveMaintainer = msg.getCanRemoveMaintainer, canMutateMaintainer = msg.getCanMutateMaintainer)
      (for {
        _ <- deputize
      } yield msg.getFrom).recover {
        case _: BaseException => logger.error(schema.constants.Messages.ORDER_DEPUTIZE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          msg.getFrom
      }
    }

    def onPut(msg: ordersTransactions.put.Message)(implicit header: Header): Future[String] = {
      val document = schema.document.PutOrder.getPutOrderDocument(
        makerID = IdentityID(msg.getFromID),
        makerAssetID = AssetID(msg.getMakerAssetID),
        takerAssetID = AssetID(msg.getTakerAssetID),
        makerSplit = NumberData(BigInt(msg.getMakerSplit)),
        takerSplit = NumberData(BigInt(msg.getTakerSplit)),
        expiryHeight = HeightData(Height(msg.getExpiryHeight))
      )
      val addOrder = Service.add(utilities.Document.getOrder(document))
      val makerTransfer = blockchainSplits.Utility.transfer(fromID = IdentityID(msg.getFromID), toID = OrderModuleIdentityID, assetID = AssetID(msg.getMakerAssetID), value = BigInt(msg.getMakerSplit))
      (for {
        _ <- addOrder
        _ <- makerTransfer
      } yield msg.getFrom).recover {
        case _: BaseException => logger.error(schema.constants.Messages.ORDER_PUT + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          msg.getFrom
      }
    }

    def onGet(msg: ordersTransactions.get.Message)(implicit header: Header): Future[String] = {
      val order = Service.tryGet(OrderID(msg.getOrderID))

      def transfer(order: Order) = {
        val makerTransfer = blockchainSplits.Utility.transfer(fromID = IdentityID(msg.getFromID), toID = order.getMakerID, assetID = order.getTakerAssetID, value = order.getTakerSplit)
        val takerTransfer = blockchainSplits.Utility.transfer(fromID = OrderModuleIdentityID, toID = IdentityID(msg.getFromID), assetID = order.getMakerAssetID, value = order.getMakerSplit)
        for {
          _ <- makerTransfer
          _ <- takerTransfer
        } yield ()
      }

      (for {
        order <- order
        _ <- transfer(order)
      } yield msg.getFrom).recover {
        case _: BaseException => logger.error(schema.constants.Messages.ORDER_GET + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
          msg.getFrom
      }
    }

    def onNewBlock(header: Header): Future[Unit] = {
      val allOrders = Service.fetchAll

      def filterAndDelete(orders: Seq[Order]) = utilitiesOperations.traverse(orders) { order =>
        if (header.height.toLong >= order.getExpiryHeight) {
          val delete = Service.delete(order.getID)
          val transferAux = blockchainSplits.Utility.transfer(fromID = OrderModuleIdentityID, toID = order.getMakerID, assetID = order.getMakerAssetID, value = order.getMakerSplit)
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