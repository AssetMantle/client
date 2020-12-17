package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.DataValue._
import models.common.Serializable._
import models.common.TransactionMessages.{OrderCancel, OrderDefine, OrderMake, OrderTake}
import models.master
import models.master.{Classification => masterClassification, Order => masterOrder}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.GetOrder
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Order(id: String, immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getTakerID: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.TakerID).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.TakerID).getOrElse(Property(id = constants.Blockchain.Properties.TakerID, fact = NewFact(constants.Blockchain.FactType.ID, IDDataValue("")))))

  def getOptionalTakerID: Option[Property] = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.TakerID).fold(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.TakerID))(x => Option(x))

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
                        getOrder: GetOrder,
                        blockchainSplits: Splits,
                        blockchainMetas: Metas,
                        blockchainClassifications: Classifications,
                        blockchainMaintainers: Maintainers,
                        masterClassifications: master.Classifications,
                        masterProperties: master.Properties,
                        masterOrders: master.Orders,
                      )(implicit executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ORDER

  import databaseConfig.profile.api._

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

    def create(order: Order): Future[String] = add(order)

    def tryGet(id: String): Future[Order] = tryGetByID(id).map(_.deserialize)

    def get(id: String): Future[Option[Order]] = getByID(id).map(_.map(_.deserialize))

    def getAll: Future[Seq[Order]] = getAllOrders.map(_.map(_.deserialize))

    def insertMultiple(orders: Seq[Order]): Future[Seq[String]] = addMultiple(orders)

    def insertOrUpdate(order: Order): Future[Int] = upsert(order)

    def delete(id: String): Future[Int] = deleteByID(id)

    def checkExists(id: String): Future[Boolean] = checkExistsByID(id)

    def getAllPublicOrderIDs: Future[Seq[String]] = getAllOrders.map(_.map(_.deserialize).filter(_.getOptionalTakerID.isEmpty).map(_.id))
  }

  object Utility {

    private val chainID = configuration.get[String]("blockchain.chainID")

    def onDefine(orderDefine: OrderDefine): Future[Unit] = {
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
        val classification = masterClassifications.Service.get(classificationID)

        def insertProperties(classification: Option[masterClassification]) = if (classification.isEmpty) masterProperties.Utilities.upsertProperties(entityType = constants.Blockchain.Entity.ORDER_DEFINITION, entityID = classificationID, immutableMetas = orderDefine.immutableMetaTraits, immutables = orderDefine.immutableTraits, mutableMetas = orderDefine.mutableMetaTraits, mutables = orderDefine.mutableTraits) else Future("")

        def upsert(classification: Option[masterClassification]) = classification.fold(masterClassifications.Service.insertOrUpdate(id = classificationID, entityType = constants.Blockchain.Entity.ORDER_DEFINITION, fromID = orderDefine.fromID, label = None, status = Option(true)))(_ => masterClassifications.Service.markStatusSuccessful(id = classificationID, entityType = constants.Blockchain.Entity.ORDER_DEFINITION))

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

    def onMake(orderMake: OrderMake, blockHeight: Int): Future[Unit] = {
      val transferAuxiliary = blockchainSplits.Utility.auxiliaryTransfer(fromID = orderMake.fromID, toID = constants.Blockchain.Modules.Orders, ownableID = orderMake.makerOwnableID, splitValue = orderMake.makerOwnableSplit)
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(orderMake.immutableMetaProperties.metaPropertyList)

      def getImmutables(scrubbedImmutableMetaProperties: Seq[Property]) = Future(Immutables(Properties(scrubbedImmutableMetaProperties ++ orderMake.immutableProperties.propertyList)))

      def getOrderID(immutables: Immutables) = Future(getID(classificationID = orderMake.classificationID, makerOwnableID = orderMake.makerOwnableID, takerOwnableID = orderMake.takerOwnableID, makerID = orderMake.fromID, immutables = immutables))

      def getOldOrder(orderID: String) = Service.get(orderID)

      def getNewMakerOwnableSplit(oldOrder: Option[Order]): Future[BigDecimal] = if (oldOrder.isDefined) {
        val oldMakerOwnableSplitMetaData = blockchainMetas.Service.tryGetData(id = oldOrder.get.getMakerOwnableSplit.fact.hash, dataType = constants.Blockchain.DataType.DEC_DATA)

        for {
          oldMakerOwnableSplitMetaData <- oldMakerOwnableSplitMetaData
        } yield orderMake.makerOwnableSplit + oldMakerOwnableSplitMetaData.value.asDec
      } else Future(orderMake.makerOwnableSplit)

      def scrubMutableMetaProperties(makerOwnableSplit: BigDecimal) = {
        val mutableMetaProperties = orderMake.mutableMetaProperties.metaPropertyList ++ Seq(
          MetaProperty(constants.Blockchain.Properties.Expiry, MetaFact(Data(constants.Blockchain.DataType.HEIGHT_DATA, HeightDataValue(orderMake.expiresIn + blockHeight)))),
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

      def masterOperations(orderID: String, mutableMetaProperties: Seq[MetaProperty]) = {
        val order = masterOrders.Service.get(orderID)

        def insertProperties(order: Option[masterOrder]) = if (order.isEmpty) masterProperties.Utilities.upsertProperties(entityType = constants.Blockchain.Entity.ORDER, entityID = orderID, immutableMetas = orderMake.immutableMetaProperties, immutables = orderMake.immutableProperties, mutableMetas = MetaProperties(mutableMetaProperties), mutables = orderMake.mutableProperties) else Future("")

        def upsertMaster(order: Option[masterOrder]) = order.fold(masterOrders.Service.insertOrUpdate(masterOrder(id = orderID, label = None, makerOwnableID = orderMake.makerOwnableID, takerOwnableID = orderMake.takerOwnableID, makerID = orderMake.fromID, status = Option(true))))(x => masterOrders.Service.insertOrUpdate(x.copy(status = Option(true))))

        for {
          order <- order
          _ <- upsertMaster(order)
          _ <- insertProperties(order)
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
        _ <- masterOperations(orderID, mutableMetaProperties)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onTake(orderTake: OrderTake): Future[Unit] = {
      val oldOrder = Service.tryGet(orderTake.orderID)

      def getData(oldOrder: Order) = {
        val makerOwnableSplitData = blockchainMetas.Service.tryGetData(id = oldOrder.getMakerOwnableSplit.fact.hash, dataType = constants.Blockchain.DataType.DEC_DATA)
        val exchangeRateData = blockchainMetas.Service.tryGetData(id = oldOrder.getExchangeRate.fact.hash, dataType = constants.Blockchain.DataType.DEC_DATA)

        for {
          makerOwnableSplitData <- makerOwnableSplitData
          exchangeRateData <- exchangeRateData
        } yield (makerOwnableSplitData, exchangeRateData)
      }

      //returns (sendMakerOwnableSplit, sendTakerOwnableSplit, orderDeleted, metaMutables)
      def updateOrRemoveOrder(oldOrder: Order, makerOwnableSplit: BigDecimal, exchangeRate: BigDecimal) = {
        val sendTakerOwnableSplit = makerOwnableSplit * exchangeRate
        val sendMakerOwnableSplit = orderTake.takerOwnableSplit.quot(exchangeRate)
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
        (makerOwnableSplitData, exchangeRateData) <- getData(oldOrder)
        (sendMakerOwnableSplit, sendTakerOwnableSplit, orderDeleted, metaMutables) <- updateOrRemoveOrder(oldOrder = oldOrder, makerOwnableSplit = makerOwnableSplitData.value.asDec, exchangeRate = exchangeRateData.value.asDec)
        _ <- transferSplits(oldOrder = oldOrder, sendTakerOwnableSplit = sendTakerOwnableSplit, sendMakerOwnableSplit = sendMakerOwnableSplit)
        _ <- masterOperations(orderID = orderTake.orderID, orderDeleted = orderDeleted, metaMutables = metaMutables)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onCancel(orderCancel: OrderCancel): Future[Unit] = {
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
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def getID(classificationID: String, makerOwnableID: String, takerOwnableID: String, makerID: String, immutables: Immutables): String = Seq(classificationID, makerOwnableID, takerOwnableID, makerID, immutables.getHashID).mkString(constants.Blockchain.SecondOrderCompositeIDSeparator)

  }

}