package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.DataValue.{DecDataValue, HeightDataValue}
import models.common.Serializable._
import models.common.TransactionMessages.{OrderCancel, OrderDefine, OrderMake, OrderTake}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.GetOrder
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Order(id: String, immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged {
  def getTakerID: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.TakerID).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.TakerID).getOrElse(Property(id = constants.Blockchain.Properties.TakerID, fact = NewFact(constants.Blockchain.FactType.ID, ""))))

  def getExchangeRate: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.ExchangeRate).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.TakerID).getOrElse(Property(id = constants.Blockchain.Properties.TakerID, fact = NewFact(constants.Blockchain.FactType.ID, ""))))

  def getCreation: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.Creation).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.Creation).getOrElse(Property(id = constants.Blockchain.Properties.Creation, fact = NewFact(constants.Blockchain.FactType.HEIGHT, "-1"))))

  def getExpiry: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.Expiry).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.Expiry).getOrElse(Property(id = constants.Blockchain.Properties.Expiry, fact = NewFact(constants.Blockchain.FactType.HEIGHT, "-1"))))

  def getMakerOwnableSplit: Property = immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.MakerOwnableSplit).getOrElse(mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.MakerOwnableSplit).getOrElse(Property(id = constants.Blockchain.Properties.MakerOwnableSplit, fact = NewFact(constants.Blockchain.FactType.DEC, constants.Blockchain.OneDec.toString))))

  def getClassificationID: String = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)(0)

  def getMakerOwnableID: String = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)(1)

  def getTakerOwnableID: String = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)(2)

  def getMakerID: String = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)(3)

  def getHashID: String = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)(4)

}

@Singleton
class Orders @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        getOrder: GetOrder,
                        blockchainSplits: Splits,
                        blockchainMetas: Metas,
                        blockchainClassifications: Classifications,
                        blockchainMaintainers: Maintainers
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
  }

  object Utility {

    private val chainID = configuration.get[String]("blockchain.main.chainID")

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

      (for {
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        scrubbedMutableMetaProperties <- scrubbedMutableMetaProperties
        _ <- defineAndSuperAuxiliary(scrubbedImmutableMetaProperties = scrubbedImmutableMetaProperties, scrubbedMutableMetaProperties = scrubbedMutableMetaProperties)
      } yield ()
        ).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onMake(orderMake: OrderMake, blockHeight: Int): Future[Unit] = {
      val transferAuxiliary = blockchainSplits.Utility.auxiliaryTransfer(fromID = orderMake.fromID, toID = constants.Blockchain.Modules.Orders, ownableID = orderMake.makerOwnableID, splitValue = orderMake.makerOwnableSplit)
      val scrubbedImmutableMetaProperties = blockchainMetas.Utility.auxiliaryScrub(orderMake.immutableMetaProperties.metaPropertyList)

      def getImmutables(scrubbedImmutableMetaProperties: Seq[Property]) = Future(Immutables(Properties(scrubbedImmutableMetaProperties ++ orderMake.immutableProperties.propertyList)))

      def getOrderID(immutables: Immutables) = Future(getID(classificationID = orderMake.classificationID, makerOwnableID = orderMake.makerOwnableID, takerOwnableID = orderMake.takerOwnableID, makerID = orderMake.fromID, hashID = immutables.getHashID))

      def getOldOrder(orderID: String) = Service.get(orderID)

      def getNewMakerOwnableSplit(oldOrder: Option[Order]): Future[BigDecimal] = if (oldOrder.isDefined) {
        val oldMakerOwnableSplitMeta = blockchainMetas.Service.tryGet(oldOrder.get.getMakerOwnableSplit.fact.hash)

        for {
          oldMakerOwnableSplitMeta <- oldMakerOwnableSplitMeta
        } yield orderMake.makerOwnableSplit + oldMakerOwnableSplitMeta.data.value.AsDec
      } else {
        Future(orderMake.makerOwnableSplit)
      }

      def scrubMutableMetaProperties(makerOwnableSplit: BigDecimal) = blockchainMetas.Utility.auxiliaryScrub(orderMake.mutableMetaProperties.metaPropertyList ++ Seq(
        MetaProperty(constants.Blockchain.Properties.Expiry, MetaFact(Data(constants.Blockchain.DataType.HEIGHT_DATA, HeightDataValue(orderMake.expiresIn + blockHeight)))),
        MetaProperty(constants.Blockchain.Properties.MakerOwnableSplit, MetaFact(Data(constants.Blockchain.DataType.DEC_DATA, DecDataValue(makerOwnableSplit))))))

      def upsertOrder(oldOrder: Option[Order], mutableScrubs: Seq[Property], orderID: String, immutables: Immutables) = {
        val mutables = Mutables(Properties(mutableScrubs ++ orderMake.mutableProperties.propertyList))
        oldOrder.fold(Service.insertOrUpdate(Order(id = orderID, mutables = mutables, immutables = immutables)))(x => Service.insertOrUpdate(x.copy(mutables = x.mutables.mutate(mutables.properties.propertyList))))
      }

      (for {
        _ <- transferAuxiliary
        scrubbedImmutableMetaProperties <- scrubbedImmutableMetaProperties
        immutables <- getImmutables(scrubbedImmutableMetaProperties)
        orderID <- getOrderID(immutables)
        oldOrder <- getOldOrder(orderID)
        makerOwnableSplit <- getNewMakerOwnableSplit(oldOrder)
        scrubbedMutableMetaProperties <- scrubMutableMetaProperties(makerOwnableSplit)
        _ <- upsertOrder(oldOrder, scrubbedMutableMetaProperties, orderID, immutables)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onTake(orderTake: OrderTake): Future[Unit] = {
      def getMakerOwnableSplitMeta(oldOrder: Order) = blockchainMetas.Service.tryGet(oldOrder.getMakerOwnableSplit.fact.hash)

      def getExchangeRateMeta(oldOrder: Order) = blockchainMetas.Service.tryGet(oldOrder.getExchangeRate.fact.hash)

      def transferAuxiliary(oldOrder: Order) = blockchainSplits.Utility.auxiliaryTransfer(fromID = orderTake.fromID, toID = oldOrder.getMakerID, ownableID = oldOrder.getTakerOwnableID, splitValue = orderTake.takerOwnableSplit)

      def updateOrRemoveOrder(oldOrder: Order, makerOwnableSplit: BigDecimal, exchangeRate: BigDecimal) = {
        val sendMakerOwnableSplit = orderTake.takerOwnableSplit * exchangeRate
        val updatedMakerOwnableSplit = makerOwnableSplit - sendMakerOwnableSplit
        if (updatedMakerOwnableSplit == 0) Service.delete(orderTake.orderID) else {
          val makerTransferSplits = blockchainSplits.Utility.auxiliaryTransfer(fromID = constants.Blockchain.Modules.Orders, toID = orderTake.fromID, ownableID = oldOrder.getMakerOwnableID, splitValue = sendMakerOwnableSplit)
          val scrubMetaMutables = blockchainMetas.Utility.auxiliaryScrub(Seq(MetaProperty(constants.Blockchain.Properties.MakerSplit, MetaFact(Data(constants.Blockchain.DataType.DEC_DATA, DecDataValue(updatedMakerOwnableSplit))))))

          def update(mutated: Seq[Property]) = Service.insertOrUpdate(oldOrder.copy(mutables = oldOrder.mutables.mutate(mutated)))

          for {
            _ <- makerTransferSplits
            mutated <- scrubMetaMutables
            _ <- update(mutated)
          } yield ()
        }
      }

      (for {
        oldOrder <- Service.tryGet(orderTake.orderID)
        makerOwnableSplitMeta <- getMakerOwnableSplitMeta(oldOrder)
        exchangeRateMeta <- getExchangeRateMeta(oldOrder)
        _ <- transferAuxiliary(oldOrder)
        _ <- updateOrRemoveOrder(oldOrder = oldOrder, makerOwnableSplit = makerOwnableSplitMeta.data.value.AsDec, exchangeRate = exchangeRateMeta.data.value.AsDec)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    def onCancel(orderCancel: OrderCancel): Future[Unit] = {
      val oldOrder = Service.tryGet(orderCancel.orderID)

      def getMakerOwnableSplitMeta(oldOrder: Order) = blockchainMetas.Service.tryGet(oldOrder.getMakerOwnableSplit.fact.hash)

      def auxiliaryTransfer(oldOrder: Order, makerOwnableSplit: BigDecimal) = blockchainSplits.Utility.auxiliaryTransfer(fromID = constants.Blockchain.Modules.Orders, toID = orderCancel.fromID, ownableID = oldOrder.getMakerOwnableID, splitValue = makerOwnableSplit)

      def removeOrder(orderID: String) = Service.delete(orderID)

      (for {
        oldOrder <- oldOrder
        makerOwnableSplitMeta <- getMakerOwnableSplitMeta(oldOrder)
        _ <- auxiliaryTransfer(oldOrder, makerOwnableSplitMeta.data.value.AsDec)
        _ <- removeOrder(orderCancel.orderID)
      } yield ()).recover {
        case _: BaseException => logger.error(constants.Response.TRANSACTION_PROCESSING_FAILED.logMessage)
      }
    }

    private def getID(classificationID: String, makerOwnableID: String, takerOwnableID: String, makerID: String, hashID: String) = Seq(classificationID, makerOwnableID, takerOwnableID, makerID, hashID).mkString(constants.Blockchain.SecondOrderCompositeIDSeparator)

  }

}