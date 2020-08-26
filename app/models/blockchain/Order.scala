package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.DataValue.{DecDataValue, HeightDataValue}
import models.common.Serializable._
import models.common.TransactionMessages.{OrderCancel, OrderMake, OrderTake}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.GetOrder
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Order(id: String, immutables: Immutables, mutables: Mutables, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Orders @Inject()(
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        configuration: Configuration,
                        getOrder: GetOrder,
                        blockchainSplits: Splits,
                        blockchainMetas: Metas,
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

    def onMake(orderMake: OrderMake, blockHeight: Int): Future[Unit] = {
      val transferAuxiliary = blockchainSplits.Utility.auxiliaryTransfer(fromID = orderMake.fromID, toID = constants.Blockchain.Order.Orders, ownableID = orderMake.makerOwnableID, splitValue = orderMake.makerOwnableSplit)
      val immutableScrubs = blockchainMetas.Utility.auxiliaryScrub(orderMake.immutableMetaProperties.metaPropertyList)

      def getImmutables(immutableScrubs: Seq[Property]) = Future(Immutables(Properties(immutableScrubs ++ orderMake.immutableProperties.propertyList)))

      def getOrderID(immutables: Immutables) = Future(getID(classificationID = orderMake.classificationID, makerOwnableID = orderMake.makerOwnableID, takerOwnableID = orderMake.takerOwnableID, fromID = orderMake.fromID, hashID = immutables.getHashID))

      def getNewMakerOwnableSplit(oldOrder: Option[Order]): Future[BigDecimal] = if (oldOrder.isDefined) {
        val oldMakerOwnableSplitProperty = getMakerOwnableSplit(oldOrder.get)
        val oldMakerOwnableSplit = blockchainMetas.Service.get(oldMakerOwnableSplitProperty.fact.hash)

        for {
          oldMakerOwnableSplit <- oldMakerOwnableSplit
        } yield {
          if (oldMakerOwnableSplit.isDefined) orderMake.makerOwnableSplit + oldMakerOwnableSplit.get.data.value.AsDec else orderMake.makerOwnableSplit
        }
      } else {
        Future(orderMake.makerOwnableSplit)
      }

      def getMutableMetaProperties(makerOwnableSplit: BigDecimal) = Future((orderMake.mutableMetaProperties.metaPropertyList :+ MetaProperty(constants.Blockchain.Properties.Expiry, MetaFact(Data(constants.Blockchain.Data.HEIGHT_DATA, HeightDataValue(orderMake.expiresIn + blockHeight))))):+MetaProperty(constants.Blockchain.Properties.MakerOwnableSplit, MetaFact(Data(constants.Blockchain.Data.DEC_DATA, DecDataValue(makerOwnableSplit)))))

      def mutableScrubs(mutableMetaProperties: Seq[MetaProperty]) = blockchainMetas.Utility.auxiliaryScrub(mutableMetaProperties)

      def upsertOrder(oldOrder: Option[Order], mutableScrubs: Seq[Property], orderID: String, immutables: Immutables) = {
        val mutables = Mutables(Properties(mutableScrubs ++ orderMake.mutableProperties.propertyList))
        oldOrder.fold(Service.insertOrUpdate(Order(id = orderID, mutables = mutables, immutables = immutables)))(x => Service.insertOrUpdate(x.copy(mutables = x.mutables.mutate(mutables.properties.propertyList))))
      }
      (for {
        _ <- transferAuxiliary
        immutableScrubs <- immutableScrubs
        immutables <- getImmutables(immutableScrubs)
        orderID <- getOrderID(immutables)
        oldOrder <- Service.get(orderID)
        makerOwnableSplit <- getNewMakerOwnableSplit(oldOrder)
        mutableMetaProperties <- getMutableMetaProperties(makerOwnableSplit)
        mutableScrubs <- mutableScrubs(mutableMetaProperties)
        _ <- upsertOrder(oldOrder, mutableScrubs, orderID, immutables)
      } yield ()).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onTake(orderTake: OrderTake): Future[Unit] = Future()

    def onCancel(orderCancel: OrderCancel): Future[Unit] = Future()

    private def getID(classificationID: String, makerOwnableID: String, takerOwnableID: String, fromID: String, hashID: String) = Seq(classificationID, makerOwnableID, takerOwnableID, fromID, hashID).mkString(constants.Blockchain.IDSeparator)

    private def getFeatures(id: String): (String, String, String, String, String) = {
      val idList = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)
      if (idList.length == 5) (idList(0), idList(1), idList(2), idList(4), idList(5)) else ("", "", "", "", "")
    }

    private def getMakerOwnableSplit(order: Order): Property = order.mutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.MakerOwnableSplit).getOrElse(order.immutables.properties.propertyList.find(_.id == constants.Blockchain.Properties.MakerOwnableSplit).getOrElse(Property(id = constants.Blockchain.Properties.MakerOwnableSplit, fact = NewFact(constants.Blockchain.OneDec.toString))))

  }

}