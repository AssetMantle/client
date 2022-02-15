package models.blockchain

import exceptions.BaseException
import models.Trait.Logged
import models.common.DataValue._
import models.common.ID.{ClassificationID, IdentityID, OrderID}
import models.common.Serializable._
import models.common.TransactionMessages.{OrderCancel, OrderDefine, OrderMake, OrderTake}
import models.master
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

case class Order(id: OrderID, immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getTakerID: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.TakerID).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.TakerID).getOrElse(Property(id = constants.Blockchain.Properties.TakerID, fact = NewFact(constants.Blockchain.FactType.ID, IDDataValue("")))))

  def getExchangeRate: Property = Property(id = constants.Blockchain.Properties.ExchangeRate, fact = NewFact(constants.Blockchain.FactType.DEC, DecDataValue(BigDecimal(this.id.rateID))))

  def getCreation: Property = Property(id = constants.Blockchain.Properties.Creation, fact = NewFact(constants.Blockchain.FactType.HEIGHT, HeightDataValue(this.id.creationID.toInt)))

  def getExpiry: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.Expiry).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.Expiry).getOrElse(Property(id = constants.Blockchain.Properties.Expiry, fact = NewFact(constants.Blockchain.FactType.HEIGHT, HeightDataValue(-1)))))

  def getMakerOwnableSplit: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.MakerOwnableSplit).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.MakerOwnableSplit).getOrElse(Property(id = constants.Blockchain.Properties.MakerOwnableSplit, fact = NewFact(constants.Blockchain.FactType.DEC, DecDataValue(constants.Blockchain.SmallestDec)))))
}

@Singleton
class Orders @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        blockchainSplits: Splits,
                        blockchainMetas: Metas,
                        blockchainClassifications: Classifications,
                        blockchainMaintainers: Maintainers,
                        masterClassifications: master.Classifications,
                        masterOrders: master.Orders,
                      )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ORDER

  import databaseConfig.profile.api._

  case class OrderSerialized(classificationID: String, makerOwnableID: String, takerOwnableID: String, rateID: String, creationID: String, makerID: String, hashID: String, immutables: String, mutables: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Order = Order(id = OrderID(classificationID = classificationID, makerOwnableID = makerOwnableID, takerOwnableID = takerOwnableID, rateID = rateID, creationID = creationID, makerID = makerID, hashID = hashID), immutables = utilities.JSON.convertJsonStringToObject[Immutables](immutables), mutables = utilities.JSON.convertJsonStringToObject[Mutables](mutables), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(order: Order): OrderSerialized = OrderSerialized(classificationID = order.id.classificationID.asString, makerOwnableID = order.id.makerOwnableID, takerOwnableID = order.id.takerOwnableID, rateID = order.id.rateID, creationID = order.id.creationID, makerID = order.id.makerID.asString, hashID = order.id.hashID, immutables = Json.toJson(order.immutables).toString, mutables = Json.toJson(order.mutables).toString, createdBy = order.createdBy, createdOn = order.createdOn, createdOnTimeZone = order.createdOnTimeZone, updatedBy = order.updatedBy, updatedOn = order.updatedOn, updatedOnTimeZone = order.updatedOnTimeZone)

  private[models] val orderTable = TableQuery[OrderTable]

  private def add(order: Order): Future[String] = db.run((orderTable returning orderTable.map(_.hashID) += serialize(order)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(orders: Seq[Order]): Future[Seq[String]] = db.run((orderTable returning orderTable.map(_.hashID) ++= orders.map(x => serialize(x))).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_INSERT_FAILED, psqlException)
    }
  }

  private def upsert(order: Order): Future[Int] = db.run(orderTable.insertOrUpdate(serialize(order)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_UPSERT_FAILED, psqlException)
    }
  }

  private def tryGetByID(classificationID: String, makerOwnableID: String, takerOwnableID: String, rateID: String, creationID: String, makerID: String, hashID: String) = db.run(orderTable.filter(x => x.classificationID === classificationID && x.makerOwnableID === makerOwnableID && x.takerOwnableID === takerOwnableID && x.rateID === rateID && x.creationID === creationID && x.makerID === makerID && x.hashID === hashID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ORDER_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByID(classificationID: String, makerOwnableID: String, takerOwnableID: String, rateID: String, creationID: String, makerID: String, hashID: String) = db.run(orderTable.filter(x => x.classificationID === classificationID && x.makerOwnableID === makerOwnableID && x.takerOwnableID === takerOwnableID && x.rateID === rateID && x.creationID === creationID && x.makerID === makerID && x.hashID === hashID).result.headOption)

  private def getAllOrders = db.run(orderTable.result)

  private def deleteByID(classificationID: String, makerOwnableID: String, takerOwnableID: String, rateID: String, creationID: String, makerID: String, hashID: String): Future[Int] = db.run(orderTable.filter(x => x.classificationID === classificationID && x.makerOwnableID === makerOwnableID && x.takerOwnableID === takerOwnableID && x.rateID === rateID && x.creationID === creationID && x.makerID === makerID && x.hashID === hashID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ORDER_DELETE_FAILED, noSuchElementException)
    }
  }

  private def checkExistsByID(classificationID: String, makerOwnableID: String, takerOwnableID: String, rateID: String, creationID: String, makerID: String, hashID: String) = db.run(orderTable.filter(x => x.classificationID === classificationID && x.makerOwnableID === makerOwnableID && x.takerOwnableID === takerOwnableID && x.rateID === rateID && x.creationID === creationID && x.makerID === makerID && x.hashID === hashID).exists.result)

  private[models] class OrderTable(tag: Tag) extends Table[OrderSerialized](tag, "Order_BC") {

    def * = (classificationID, makerOwnableID, takerOwnableID, rateID, creationID, makerID, hashID, immutables, mutables, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (OrderSerialized.tupled, OrderSerialized.unapply)

    def classificationID = column[String]("classificationID", O.PrimaryKey)

    def makerOwnableID = column[String]("makerOwnableID", O.PrimaryKey)

    def takerOwnableID = column[String]("takerOwnableID", O.PrimaryKey)

    def rateID = column[String]("rateID", O.PrimaryKey)

    def creationID = column[String]("creationID", O.PrimaryKey)

    def makerID = column[String]("makerID", O.PrimaryKey)

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

    def create(order: Order): Future[String] = add(order)

    def tryGet(id: OrderID): Future[Order] = tryGetByID(classificationID = id.classificationID.asString, makerOwnableID = id.makerOwnableID, takerOwnableID = id.takerOwnableID, rateID = id.rateID, creationID = id.creationID, makerID = id.makerID.asString, hashID = id.hashID).map(_.deserialize)

    def get(id: OrderID): Future[Option[Order]] = getByID(classificationID = id.classificationID.asString, makerOwnableID = id.makerOwnableID, takerOwnableID = id.takerOwnableID, rateID = id.rateID, creationID = id.creationID, makerID = id.makerID.asString, hashID = id.hashID).map(_.map(_.deserialize))

    def getAll: Future[Seq[Order]] = getAllOrders.map(_.map(_.deserialize))

    def insertMultiple(orders: Seq[Order]): Future[Seq[String]] = addMultiple(orders)

    def insertOrUpdate(order: Order): Future[Int] = upsert(order)

    def delete(id: OrderID): Future[Int] = deleteByID(classificationID = id.classificationID.asString, makerOwnableID = id.makerOwnableID, takerOwnableID = id.takerOwnableID, rateID = id.rateID, creationID = id.creationID, makerID = id.makerID.asString, hashID = id.hashID)

    def checkExists(id: OrderID): Future[Boolean] = checkExistsByID(classificationID = id.classificationID.asString, makerOwnableID = id.makerOwnableID, takerOwnableID = id.takerOwnableID, rateID = id.rateID, creationID = id.creationID, makerID = id.makerID.asString, hashID = id.hashID)

    def getAllPublicOrderIDs: Future[Seq[String]] = getAllOrders.map(_.map(_.deserialize).filter(_.getTakerID.fact.hash == "").map(_.id.asString))

    def getAllPrivateOrderIDs(identityIDs: Seq[String]): Future[Seq[String]] = {
      val hashedIdentityIDs = identityIDs.map(utilities.Hash.getHash(_))
      getAllOrders.map(_.map(_.deserialize).filter(_.getTakerID.fact.hash.contains(hashedIdentityIDs)).map(_.id.asString))
    }
  }

  object Utility {

    def onDefine(orderDefine: OrderDefine)(implicit header: Header): Future[Unit] = {
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(orderDefine.immutableMetaTraits.metaPropertyList)
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(orderDefine.mutableMetaTraits.metaPropertyList)

      def defineAndSuperAuxiliary(scrubbedImmutableMetaProperties: Seq[Property], scrubbedMutableMetaProperties: Seq[Property]) = {
        val mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ orderDefine.mutableTraits.propertyList))
        val defineAuxiliary = blockchainClassifications.Utility.auxiliaryDefine(immutables = Immutables(Properties(scrubbedImmutableMetaProperties ++ orderDefine.immutableTraits.propertyList)), mutables = mutables)

        def superAuxiliary(classificationID: ClassificationID) = blockchainMaintainers.Utility.auxiliarySuper(classificationID = classificationID, identityID = IdentityID(orderDefine.fromID), mutableTraits = mutables)

        for {
          classificationID <- defineAuxiliary
          _ <- superAuxiliary(classificationID = classificationID)
        } yield classificationID
      }

      def masterOperations(classificationID: ClassificationID) = {
        val insert = masterClassifications.Service.insertOrUpdate(id = classificationID.asString, entityType = constants.Blockchain.Entity.ORDER_DEFINITION, maintainerID = orderDefine.fromID, status = Option(true))

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
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.ORDER_DEFINE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onMake(orderMake: OrderMake)(implicit header: Header): Future[Unit] = {
      val transferAuxiliary = blockchainSplits.Utility.auxiliaryTransfer(fromID = orderMake.fromID, toID = constants.Blockchain.Modules.Orders, ownableID = orderMake.makerOwnableID, splitValue = orderMake.makerOwnableSplit)
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(orderMake.immutableMetaProperties.metaPropertyList)

      val exchangeRate = BigDecimal((((orderMake.takerOwnableSplit * constants.Blockchain.SmallestDecReciprocal) / orderMake.makerOwnableSplit) * constants.Blockchain.SmallestDecReciprocal).toBigInt()) * constants.Blockchain.SmallestDec

      def getImmutables(scrubbedImmutableMetaProperties: Seq[Property]) = Future(Immutables(Properties(scrubbedImmutableMetaProperties ++ orderMake.immutableProperties.propertyList)))

      def getOrderID(immutables: Immutables): Future[OrderID] = Future(OrderID(classificationID = orderMake.classificationID, makerOwnableID = orderMake.makerOwnableID, takerOwnableID = orderMake.takerOwnableID, rateID = exchangeRate.toString, creationID = header.height.toString, makerID = orderMake.fromID, hashID = immutables.getHashID))

      def getOldOrder(orderID: OrderID) = Service.get(orderID)

      def getNewMakerOwnableSplit(oldOrder: Option[Order]): Future[BigDecimal] = if (oldOrder.isDefined) {
        val oldMakerOwnableSplitMetaData = blockchainMetas.Service.tryGetData(id = oldOrder.get.getMakerOwnableSplit.fact.getMetaID)

        for {
          oldMakerOwnableSplitMetaData <- oldMakerOwnableSplitMetaData
        } yield orderMake.makerOwnableSplit + oldMakerOwnableSplitMetaData.value.asDec
      } else Future(orderMake.makerOwnableSplit)

      def scrubMutableMetaProperties(makerOwnableSplit: BigDecimal) = {
        val mutableMetaProperties = orderMake.mutableMetaProperties.metaPropertyList ++ Seq(
          MetaProperty(constants.Blockchain.Properties.Expiry, MetaFact(Data(constants.Blockchain.DataType.HEIGHT_DATA, HeightDataValue(orderMake.expiresIn + header.height)))),
          MetaProperty(constants.Blockchain.Properties.MakerOwnableSplit, MetaFact(Data(constants.Blockchain.DataType.DEC_DATA, DecDataValue(makerOwnableSplit)))))

        val scrub = blockchainMetas.Utility.auxiliaryScrub(mutableMetaProperties)

        for {
          scrubbedMutableMetaProperties <- scrub
        } yield (scrubbedMutableMetaProperties, mutableMetaProperties)
      }

      def upsertOrder(oldOrder: Option[Order], scrubbedMutables: Seq[Property], orderID: OrderID, immutables: Immutables) = {
        val mutables = Mutables(Properties(scrubbedMutables ++ orderMake.mutableProperties.propertyList))
        oldOrder.fold(Service.insertOrUpdate(Order(id = orderID, mutables = mutables, immutables = immutables)))(x => Service.insertOrUpdate(x.copy(mutables = x.mutables.mutate(mutables.properties.propertyList))))
      }

      def masterOperations(orderID: OrderID) = {
        val insert = masterOrders.Service.insertOrUpdate(master.Order(id = orderID.asString, makerOwnableID = orderMake.makerOwnableID, takerOwnableID = orderMake.takerOwnableID, makerID = orderMake.fromID, status = Option(true)))

        for {
          _ <- insert
        } yield ()
      }

      (for {
        _ <- transferAuxiliary
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        immutables <- getImmutables(scrubbedImmutableMetaProperties)
        orderID <- getOrderID(immutables)
        oldOrder <- getOldOrder(orderID)
        makerOwnableSplit <- getNewMakerOwnableSplit(oldOrder)
        (scrubbedMutableMetaProperties, mutableMetaProperties) <- scrubMutableMetaProperties(makerOwnableSplit)
        _ <- upsertOrder(oldOrder, scrubbedMutableMetaProperties, orderID, immutables)
        _ <- masterOperations(orderID)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.ORDER_MAKE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onTake(orderTake: OrderTake)(implicit header: Header): Future[Unit] = {
      val orderID = OrderID(orderTake.orderID)
      val oldOrder = Service.tryGet(orderID)

      def getData(oldOrder: Order) = {
        val makerOwnableSplitData = blockchainMetas.Service.tryGetData(id = oldOrder.getMakerOwnableSplit.fact.getMetaID)
        val exchangeRateData = blockchainMetas.Service.tryGetData(id = oldOrder.getExchangeRate.fact.getMetaID)
        val takerID = blockchainMetas.Service.get(id = oldOrder.getTakerID.fact.getMetaID)

        for {
          makerOwnableSplitData <- makerOwnableSplitData
          exchangeRateData <- exchangeRateData
          takerID <- takerID
        } yield (makerOwnableSplitData.value.asDec, exchangeRateData.value.asDec, takerID)
      }

      //returns (sendMakerOwnableSplit, sendTakerOwnableSplit, orderDeleted, metaMutables)
      def updateOrRemoveOrder(oldOrder: Order, makerOwnableSplit: BigDecimal, exchangeRate: BigDecimal) = {
        val sendTakerOwnableSplit = makerOwnableSplit * exchangeRate
        //In BC, it's written Quo but in actual it happens only division, a = 0.3, b = 2, a.Quo(b) = 0 but value comes out 0.15
        val sendMakerOwnableSplit = orderTake.takerOwnableSplit / exchangeRate
        val updatedMakerOwnableSplit = makerOwnableSplit - sendMakerOwnableSplit
        if (updatedMakerOwnableSplit < 0) {
          val deleteOrder = Service.delete(orderID)
          for {
            _ <- deleteOrder
          } yield (makerOwnableSplit, sendTakerOwnableSplit, true, Seq.empty)
        } else if (updatedMakerOwnableSplit == 0) {
          val deleteOrder = Service.delete(orderID)
          for {
            _ <- deleteOrder
          } yield (sendMakerOwnableSplit, sendTakerOwnableSplit, true, Seq.empty)
        } else {
          val metaMutables = Seq(MetaProperty(constants.Blockchain.Properties.MakerOwnableSplit, MetaFact(Data(constants.Blockchain.DataType.DEC_DATA, DecDataValue(updatedMakerOwnableSplit)))))
          val scrubMetaMutables = blockchainMetas.Utility.auxiliaryScrub(metaMutables)

          def update(scrubbedMetaMutables: Seq[Property]) = Service.insertOrUpdate(oldOrder.copy(mutables = oldOrder.mutables.mutate(scrubbedMetaMutables)))

          for {
            scrubbedMetaMutables <- scrubMetaMutables
            _ <- update(scrubbedMetaMutables)
          } yield (sendMakerOwnableSplit, orderTake.takerOwnableSplit, false, metaMutables)
        }
      }

      def transferSplits(oldOrder: Order, sendTakerOwnableSplit: BigDecimal, sendMakerOwnableSplit: BigDecimal) = {
        val makerTransferSplits = blockchainSplits.Utility.auxiliaryTransfer(fromID = orderTake.fromID, toID = oldOrder.id.makerID.asString, ownableID = oldOrder.id.takerOwnableID, splitValue = sendTakerOwnableSplit)
        val takerTransferSplits = blockchainSplits.Utility.auxiliaryTransfer(fromID = constants.Blockchain.Modules.Orders, toID = orderTake.fromID, ownableID = oldOrder.id.makerOwnableID, splitValue = sendMakerOwnableSplit)

        for {
          _ <- makerTransferSplits
          _ <- takerTransferSplits
        } yield ()
      }

      def masterOperations(orderID: String, orderDeleted: Boolean, metaMutables: Seq[MetaProperty]): Future[Unit] = if (orderDeleted) {
        val deleteOrder = masterOrders.Service.delete(orderID)
        for {
          _ <- deleteOrder
        } yield ()
      } else Future()

      (for {
        oldOrder <- oldOrder
        (makerOwnableSplit, exchangeRate, takerID) <- getData(oldOrder)
        (sendMakerOwnableSplit, sendTakerOwnableSplit, orderDeleted, metaMutables) <- updateOrRemoveOrder(oldOrder = oldOrder, makerOwnableSplit = makerOwnableSplit, exchangeRate = exchangeRate)
        _ <- transferSplits(oldOrder = oldOrder, sendTakerOwnableSplit = sendTakerOwnableSplit, sendMakerOwnableSplit = sendMakerOwnableSplit)
        _ <- masterOperations(orderID = orderTake.orderID, orderDeleted = orderDeleted, metaMutables = metaMutables)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.ORDER_TAKE + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }

    def onCancel(orderCancel: OrderCancel)(implicit header: Header): Future[Unit] = {
      val orderID = OrderID(orderCancel.orderID)
      val oldOrder = Service.tryGet(orderID)

      def getMakerOwnableSplitData(oldOrder: Order) = blockchainMetas.Service.tryGetData(id = oldOrder.getMakerOwnableSplit.fact.getMetaID)

      def auxiliaryTransfer(oldOrder: Order, makerOwnableSplit: BigDecimal) = blockchainSplits.Utility.auxiliaryTransfer(fromID = constants.Blockchain.Modules.Orders, toID = orderCancel.fromID, ownableID = oldOrder.id.makerOwnableID, splitValue = makerOwnableSplit)

      def removeOrder() = Service.delete(orderID)

      def masterOperations(orderID: String): Future[Unit] = {
        val deleteOrder = masterOrders.Service.delete(orderID)
        for {
          _ <- deleteOrder
        } yield ()
      }

      (for {
        oldOrder <- oldOrder
        makerOwnableSplitData <- getMakerOwnableSplitData(oldOrder)
        _ <- auxiliaryTransfer(oldOrder, makerOwnableSplitData.value.asDec)
        _ <- removeOrder()
        _ <- masterOperations(orderCancel.orderID)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.ORDER_CANCEL + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }
  }

}