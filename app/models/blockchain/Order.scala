package models.blockchain

import akka.pattern.{ask, pipe}
import akka.util.Timeout
import models.blockchain.Orders.{CheckExistsOrder, CreateOrder, DeleteOrder, GetAllOrder, GetAllPrivateOrderIDs, GetAllPublicOrderIDs, GetOrder, InsertMultipleOrder, InsertOrUpdateOrder, OrderActor, TryGetOrder}
import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import constants.Actor.{NUMBER_OF_ENTITIES, NUMBER_OF_SHARDS}
import exceptions.BaseException
import models.Abstract.ShardedActorRegion
import models.Trait.Logged
import models.common.DataValue._
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
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Order(id: String, immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getTakerID: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.TakerID).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.TakerID).getOrElse(Property(id = constants.Blockchain.Properties.TakerID, fact = NewFact(constants.Blockchain.FactType.ID, IDDataValue("")))))

  def getExchangeRate: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.ExchangeRate).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.ExchangeRate).getOrElse(Property(id = constants.Blockchain.Properties.ExchangeRate, fact = NewFact(constants.Blockchain.FactType.DEC, DecDataValue(constants.Blockchain.OneDec)))))

  def getCreation: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.Creation).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.Creation).getOrElse(Property(id = constants.Blockchain.Properties.Creation, fact = NewFact(constants.Blockchain.FactType.HEIGHT, HeightDataValue(-1)))))

  def getExpiry: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.Expiry).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.Expiry).getOrElse(Property(id = constants.Blockchain.Properties.Expiry, fact = NewFact(constants.Blockchain.FactType.HEIGHT, HeightDataValue(-1)))))

  def getMakerOwnableSplit: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.MakerOwnableSplit).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.MakerOwnableSplit).getOrElse(Property(id = constants.Blockchain.Properties.MakerOwnableSplit, fact = NewFact(constants.Blockchain.FactType.DEC, DecDataValue(constants.Blockchain.SmallestDec)))))

  def getClassificationID: String = id.split(constants.RegularExpression.BLOCKCHAIN_SECOND_ORDER_COMPOSITE_ID_SEPARATOR)(0)

  def getMakerOwnableID: String = id.split(constants.RegularExpression.BLOCKCHAIN_SECOND_ORDER_COMPOSITE_ID_SEPARATOR)(1)

  def getTakerOwnableID: String = id.split(constants.RegularExpression.BLOCKCHAIN_SECOND_ORDER_COMPOSITE_ID_SEPARATOR)(2)

  def getMakerID: String = id.split(constants.RegularExpression.BLOCKCHAIN_SECOND_ORDER_COMPOSITE_ID_SEPARATOR)(3)

  def getHashID: String = id.split(constants.RegularExpression.BLOCKCHAIN_SECOND_ORDER_COMPOSITE_ID_SEPARATOR)(4)

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
                        masterProperties: master.Properties,
                        masterOrders: master.Orders,
                      )(implicit executionContext: ExecutionContext) extends ShardedActorRegion {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ORDER

  import databaseConfig.profile.api._

  private val uniqueId: String = UUID.randomUUID().toString

  private implicit val timeout = Timeout(constants.Actor.ACTOR_ASK_TIMEOUT)

  override def idExtractor: ShardRegion.ExtractEntityId = {
    case attempt@CreateOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@TryGetOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertMultipleOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@DeleteOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@InsertOrUpdateOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@CheckExistsOrder(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllOrder(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllPublicOrderIDs(id) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
    case attempt@GetAllPrivateOrderIDs(id, _) => ((id.hashCode.abs % NUMBER_OF_ENTITIES).toString, attempt)
  }

  override def shardResolver: ShardRegion.ExtractShardId = {
    case CreateOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case TryGetOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertMultipleOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case DeleteOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case InsertOrUpdateOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case CheckExistsOrder(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllOrder(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllPublicOrderIDs(id) => (id.hashCode % NUMBER_OF_SHARDS).toString
    case GetAllPrivateOrderIDs(id, _) => (id.hashCode % NUMBER_OF_SHARDS).toString
  }

  override def regionName: String = "orderRegion"

  override def props: Props = Orders.props(Orders.this)
  
  case class OrderSerialized(id: String, immutables: String, mutables: String, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Order = Order(id = id, immutables = utilities.JSON.convertJsonStringToObject[Immutables](immutables), mutables = utilities.JSON.convertJsonStringToObject[Mutables](mutables), createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  def serialize(order: Order): OrderSerialized = OrderSerialized(id = order.id, immutables = Json.toJson(order.immutables).toString, mutables = Json.toJson(order.mutables).toString, createdBy = order.createdBy, createdOn = order.createdOn, createdOnTimeZone = order.createdOnTimeZone, updatedBy = order.updatedBy, updatedOn = order.updatedOn, updatedOnTimeZone = order.updatedOnTimeZone)

  private[models] val orderTable = TableQuery[OrderTable]

  private def add(order: Order): Future[String] = db.run((orderTable returning orderTable.map(_.id) += serialize(order)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_INSERT_FAILED, psqlException)
    }
  }

  private def addMultiple(orders: Seq[Order]): Future[Seq[String]] = db.run((orderTable returning orderTable.map(_.id) ++= orders.map(x => serialize(x))).asTry).map {
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

  private def tryGetByID(id: String) = db.run(orderTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ORDER_NOT_FOUND, noSuchElementException)
    }
  }

  private def getByID(id: String) = db.run(orderTable.filter(_.id === id).result.headOption)

  private def getAllOrders = db.run(orderTable.result)

  private def deleteByID(id: String): Future[Int] = db.run(orderTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.ORDER_DELETE_FAILED, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.ORDER_DELETE_FAILED, noSuchElementException)
    }
  }

  private def checkExistsByID(id: String) = db.run(orderTable.filter(_.id === id).exists.result)

  private[models] class OrderTable(tag: Tag) extends Table[OrderSerialized](tag, "Order_BC") {

    def * = (id, immutables, mutables, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (OrderSerialized.tupled, OrderSerialized.unapply)

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

    def createOrderWithActor(order: Order): Future[String] = (actorRegion ? CreateOrder(uniqueId, order)).mapTo[String]

    def create(order: Order): Future[String] = add(order)

    def tryGetOrderWithActor(id: String): Future[Order] = (actorRegion ? TryGetOrder(uniqueId, id)).mapTo[Order]

    def tryGet(id: String): Future[Order] = tryGetByID(id).map(_.deserialize)

    def getOrderWithActor(id: String): Future[Option[Order]] = (actorRegion ? GetOrder(uniqueId, id)).mapTo[Option[Order]]

    def get(id: String): Future[Option[Order]] = getByID(id).map(_.map(_.deserialize))

    def getAllOrderWithActor: Future[Seq[Order]] = (actorRegion ? GetAllOrder(uniqueId)).mapTo[Seq[Order]]

    def getAll: Future[Seq[Order]] = getAllOrders.map(_.map(_.deserialize))

    def insertMultipleOrderWithActor(orders: Seq[Order]): Future[Seq[String]] = (actorRegion ? InsertMultipleOrder(uniqueId, orders)).mapTo[Seq[String]]

    def insertMultiple(orders: Seq[Order]): Future[Seq[String]] = addMultiple(orders)

    def insertOrUpdateOrderWithActor(order: Order): Future[Int] = (actorRegion ? InsertOrUpdateOrder(uniqueId, order)).mapTo[Int]

    def insertOrUpdate(order: Order): Future[Int] = upsert(order)

    def deleteOrderWithActor(id: String): Future[Int] = (actorRegion ? DeleteOrder(uniqueId, id)).mapTo[Int]

    def delete(id: String): Future[Int] = deleteByID(id)

    def checkExistsOrderWithActor(id: String): Future[Boolean] = (actorRegion ? CheckExistsOrder(uniqueId, id)).mapTo[Boolean]

    def checkExists(id: String): Future[Boolean] = checkExistsByID(id)

    def getAllPublicOrderIDsOrderWithActor: Future[Seq[String]] = (actorRegion ? GetAllPublicOrderIDs(uniqueId)).mapTo[Seq[String]]

    def getAllPublicOrderIDs: Future[Seq[String]] = getAllOrders.map(_.map(_.deserialize).filter(_.getTakerID.fact.hash == "").map(_.id))

    def getAllPrivateOrderIDsOrderWithActor(identityIDs: Seq[String]): Future[Seq[String]] = (actorRegion ? GetAllPrivateOrderIDs(uniqueId, identityIDs)).mapTo[Seq[String]]

    def getAllPrivateOrderIDs(identityIDs: Seq[String]): Future[Seq[String]] = {
      val hashedIdentityIDs = identityIDs.map(utilities.Hash.getHash(_))
      getAllOrders.map(_.map(_.deserialize).filter(_.getTakerID.fact.hash.contains(hashedIdentityIDs)).map(_.id))
    }
  }

  object Utility {

    private val chainID = configuration.get[String]("blockchain.chainID")

    def onDefine(orderDefine: OrderDefine)(implicit header: Header): Future[Unit] = {
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(orderDefine.immutableMetaTraits.metaPropertyList)
      val scrubbedMutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(orderDefine.mutableMetaTraits.metaPropertyList)

      def defineAndSuperAuxiliary(scrubbedImmutableMetaProperties: Seq[Property], scrubbedMutableMetaProperties: Seq[Property]) = {
        val mutables = Mutables(Properties(scrubbedMutableMetaProperties ++ orderDefine.mutableTraits.propertyList))
        val defineAuxiliary = blockchainClassifications.Utility.auxiliaryDefine(immutables = Immutables(Properties(scrubbedImmutableMetaProperties ++ orderDefine.immutableTraits.propertyList)), mutables = mutables)

        def superAuxiliary(classificationID: String) = blockchainMaintainers.Utility.auxiliarySuper(classificationID = classificationID, identityID = orderDefine.fromID, mutableTraits = mutables)

        for {
          classificationID <- defineAuxiliary
          _ <- superAuxiliary(classificationID = classificationID)
        } yield classificationID
      }

      def masterOperations(classificationID: String) = {
        val insert = masterClassifications.Service.insertOrUpdate(id = classificationID, entityType = constants.Blockchain.Entity.ORDER_DEFINITION, maintainerID = orderDefine.fromID, status = Option(true))

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

      def getImmutables(scrubbedImmutableMetaProperties: Seq[Property]) = Future(Immutables(Properties(scrubbedImmutableMetaProperties ++ orderMake.immutableProperties.propertyList)))

      def getOrderID(immutables: Immutables) = Future(utilities.IDGenerator.getOrderID(classificationID = orderMake.classificationID, makerOwnableID = orderMake.makerOwnableID, takerOwnableID = orderMake.takerOwnableID, makerID = orderMake.fromID, immutables = immutables))

      def getOldOrder(orderID: String) = Service.get(orderID)

      def getNewMakerOwnableSplit(oldOrder: Option[Order]): Future[BigDecimal] = if (oldOrder.isDefined) {
        val oldMakerOwnableSplitMetaData = blockchainMetas.Service.tryGetData(id = oldOrder.get.getMakerOwnableSplit.fact.hash, dataType = constants.Blockchain.DataType.DEC_DATA)

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

      def upsertOrder(oldOrder: Option[Order], scrubbedMutables: Seq[Property], orderID: String, immutables: Immutables) = {
        val mutables = Mutables(Properties(scrubbedMutables ++ orderMake.mutableProperties.propertyList))
        oldOrder.fold(Service.insertOrUpdate(Order(id = orderID, mutables = mutables, immutables = immutables)))(x => Service.insertOrUpdate(x.copy(mutables = x.mutables.mutate(mutables.properties.propertyList))))
      }

      def masterOperations(orderID: String) = {
        val insert = masterOrders.Service.insertOrUpdate(master.Order(id = orderID, makerOwnableID = orderMake.makerOwnableID, takerOwnableID = orderMake.takerOwnableID, makerID = orderMake.fromID, status = Option(true)))

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
      val oldOrder = Service.tryGet(orderTake.orderID)

      def getData(oldOrder: Order) = {
        val makerOwnableSplitData = blockchainMetas.Service.tryGetData(id = oldOrder.getMakerOwnableSplit.fact.hash, dataType = constants.Blockchain.DataType.DEC_DATA)
        val exchangeRateData = blockchainMetas.Service.tryGetData(id = oldOrder.getExchangeRate.fact.hash, dataType = constants.Blockchain.DataType.DEC_DATA)
        val takerID = blockchainMetas.Service.get(id = oldOrder.getTakerID.fact.hash, dataType = constants.Blockchain.DataType.ID_DATA)

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
          val deleteOrder = Service.delete(orderTake.orderID)
          for {
            _ <- deleteOrder
          } yield (makerOwnableSplit, sendTakerOwnableSplit, true, Seq.empty)
        } else if (updatedMakerOwnableSplit == 0) {
          val deleteOrder = Service.delete(orderTake.orderID)
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
        val makerTransferSplits = blockchainSplits.Utility.auxiliaryTransfer(fromID = orderTake.fromID, toID = oldOrder.getMakerID, ownableID = oldOrder.getTakerOwnableID, splitValue = sendTakerOwnableSplit)
        val takerTransferSplits = blockchainSplits.Utility.auxiliaryTransfer(fromID = constants.Blockchain.Modules.Orders, toID = orderTake.fromID, ownableID = oldOrder.getMakerOwnableID, splitValue = sendMakerOwnableSplit)

        for {
          _ <- makerTransferSplits
          _ <- takerTransferSplits
        } yield ()
      }

      def masterOperations(orderID: String, orderDeleted: Boolean, metaMutables: Seq[MetaProperty]): Future[Unit] = {
        if (orderDeleted) {
          val deleteOrder = masterOrders.Service.delete(orderID)
          val deleteProperties = masterProperties.Service.deleteAll(entityID = orderID, entityType = constants.Blockchain.Entity.ORDER)
          for {
            _ <- deleteOrder
            _ <- deleteProperties
          } yield ()
        } else {
          val updateProperties = masterProperties.Utilities.updateProperties(entityType = constants.Blockchain.Entity.ORDER, entityID = orderID, mutableMetas = MetaProperties(metaMutables), mutables = Properties(Seq.empty))

          for {
            _ <- updateProperties
          } yield ()
        }
      }

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
      val oldOrder = Service.tryGet(orderCancel.orderID)

      def getMakerOwnableSplitData(oldOrder: Order) = blockchainMetas.Service.tryGetData(id = oldOrder.getMakerOwnableSplit.fact.hash, dataType = constants.Blockchain.DataType.DEC_DATA)

      def auxiliaryTransfer(oldOrder: Order, makerOwnableSplit: BigDecimal) = blockchainSplits.Utility.auxiliaryTransfer(fromID = constants.Blockchain.Modules.Orders, toID = orderCancel.fromID, ownableID = oldOrder.getMakerOwnableID, splitValue = makerOwnableSplit)

      def removeOrder(orderID: String) = Service.delete(orderID)

      def masterOperations(orderID: String): Future[Unit] = {
        val deleteOrder = masterOrders.Service.delete(orderID)
        val deleteProperties = masterProperties.Service.deleteAll(entityID = orderID, entityType = constants.Blockchain.Entity.ORDER)
        for {
          _ <- deleteOrder
          _ <- deleteProperties
        } yield ()
      }

      (for {
        oldOrder <- oldOrder
        makerOwnableSplitData <- getMakerOwnableSplitData(oldOrder)
        _ <- auxiliaryTransfer(oldOrder, makerOwnableSplitData.value.asDec)
        _ <- removeOrder(orderCancel.orderID)
        _ <- masterOperations(orderCancel.orderID)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Blockchain.TransactionMessage.ORDER_CANCEL + ": " + constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage + " at height " + header.height.toString)
      }
    }
  }

}

object Orders {
  def props(blockchainOrders: models.blockchain.Orders) (implicit executionContext: ExecutionContext) = Props(new OrderActor(blockchainOrders))

  @Singleton
  class OrderActor @Inject()(
                              blockchainOrders: models.blockchain.Orders
                            ) (implicit executionContext: ExecutionContext) extends Actor with ActorLogging {
    private implicit val logger: Logger = Logger(this.getClass)

    override def receive: Receive = {
      case CreateOrder(_, order) => {
        blockchainOrders.Service.create(order) pipeTo sender()
      }
      case TryGetOrder(_, id) => {
        blockchainOrders.Service.tryGet(id) pipeTo sender()
      }
      case GetOrder(_, id) => {
        blockchainOrders.Service.get(id) pipeTo sender()
      }
      case InsertMultipleOrder(_, orders) => {
        blockchainOrders.Service.insertMultiple(orders) pipeTo sender()
      }
      case DeleteOrder(_, id) => {
        blockchainOrders.Service.delete(id) pipeTo sender()
      }
      case GetAllOrder(_) => {
        blockchainOrders.Service.getAll pipeTo sender()
      }
      case CheckExistsOrder(_, id) => {
        blockchainOrders.Service.checkExists(id) pipeTo sender()
      }
      case InsertOrUpdateOrder(_, order) => {
        blockchainOrders.Service.insertOrUpdate(order) pipeTo sender()
      }
      case GetAllPublicOrderIDs(_) => {
        blockchainOrders.Service.getAllPublicOrderIDs pipeTo sender()
      }
      case GetAllPrivateOrderIDs(_, identityIDs) => {
        blockchainOrders.Service.getAllPrivateOrderIDs(identityIDs) pipeTo sender()
      }
    }
  }

  case class CreateOrder(uid: String, order: Order)
  case class TryGetOrder(uid: String, id: String)
  case class GetOrder(uid: String, id: String)
  case class InsertMultipleOrder(uid: String, order: Seq[Order])
  case class DeleteOrder(uid: String, id: String)
  case class InsertOrUpdateOrder(uid: String, order: Order)
  case class CheckExistsOrder(uid: String, id: String)
  case class GetAllOrder(uid: String)
  case class GetAllPublicOrderIDs(uid: String)
  case class GetAllPrivateOrderIDs(uid: String, identityIDs: Seq[String])
}