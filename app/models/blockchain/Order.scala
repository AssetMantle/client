package models.blockchain

import java.sql.Timestamp

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Serializable._
import models.common.TransactionMessages.{OrderCancel, OrderMake, OrderTake}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import queries.GetOrder
import queries.responses.OrderResponse.{Response => OrderResponse}
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

    def onMake(orderMake: OrderMake, makeHeight: Int): Future[Unit] = {
      val hashID = Immutables(Properties(propertyList = Seq(
        Property(constants.Blockchain.Order.MakerIDProperty, Fact(constants.Blockchain.Fact.FACT, newNonMetaFactValue(orderMake.makerID))),
        Property(constants.Blockchain.Order.TakerIDProperty, Fact(constants.Blockchain.Fact.FACT, newNonMetaFactValue(orderMake.takerID))),
        Property(constants.Blockchain.Order.MakerSplitIDProperty, Fact(constants.Blockchain.Fact.FACT, newNonMetaFactValue(orderMake.makerSplitID))),
        Property(constants.Blockchain.Order.ExchangeRateProperty, Fact(constants.Blockchain.Fact.FACT, newNonMetaFactValue(orderMake.exchangeRate.toString))),
        Property(constants.Blockchain.Order.TakerSplitIDProperty, Fact(constants.Blockchain.Fact.FACT, newNonMetaFactValue(orderMake.takerSplitID))),
        Property(constants.Blockchain.Order.HeightProperty, Fact(constants.Blockchain.Fact.FACT, newNonMetaFactValue(makeHeight.toString))),
      ))).getHashID
      val orderID = getID(chainID = chainID, maintainersID = orderMake.maintainersID, hashID = hashID)
      val orderResponse = getOrder.Service.get(orderID)
      val splitsBurn = blockchainSplits.Utility.splitsBurn(ownerID = orderMake.makerID, ownableID = orderMake.makerSplitID)
      val splitsMint = blockchainSplits.Utility.splitsMint(ownerID = constants.Blockchain.Order.Exchanges, ownableID = orderMake.makerSplitID)

      def insertOrUpdate(orderResponse: OrderResponse) = orderResponse.result.value.Orders.value.list.find(x => x.value.id.value.chainID.value.idString == chainID && x.value.id.value.maintainersID.value.idString == orderMake.maintainersID && x.value.id.value.hashID.value.idString == hashID).fold(throw new BaseException(constants.Response.ORDER_NOT_FOUND)) { order =>
        Service.insertOrUpdate(Order(id = orderID, mutables = order.value.mutables.toMutables, immutables = order.value.immutables.toImmutables))
      }

      (for {
        orderResponse <- orderResponse
        _ <- splitsBurn
        _ <- splitsMint
        _ <- insertOrUpdate(orderResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onTake(orderTake: OrderTake): Future[Unit] = {
      val oldOrder = Service.tryGet(orderTake.orderID)
      val orderResponse = getOrder.Service.get(orderTake.orderID)
      val (chainID, maintainersID, hashID) = getFeatures(orderTake.orderID)

      def updateSplits(oldOrder: Order) = {
        val makerMeta = blockchainMetas.Service.tryGet(oldOrder.immutables.properties.propertyList.find(_.id == constants.Blockchain.Order.MakerIDProperty).getOrElse(throw new BaseException(constants.Response.ORDER_NOT_FOUND)).fact.value.getHash)
        val makerSplitMeta = blockchainMetas.Service.tryGet(oldOrder.immutables.properties.propertyList.find(_.id == constants.Blockchain.Order.MakerSplitIDProperty).getOrElse(throw new BaseException(constants.Response.ORDER_NOT_FOUND)).fact.value.getHash)
        val takerMeta = blockchainMetas.Service.tryGet(oldOrder.immutables.properties.propertyList.find(_.id == constants.Blockchain.Order.TakerIDProperty).getOrElse(throw new BaseException(constants.Response.ORDER_NOT_FOUND)).fact.value.getHash)
        val takerSplitMeta = blockchainMetas.Service.tryGet(oldOrder.immutables.properties.propertyList.find(_.id == constants.Blockchain.Order.TakerSplitIDProperty).getOrElse(throw new BaseException(constants.Response.ORDER_NOT_FOUND)).fact.value.getHash)

        def update(makerMeta: Meta, makerSplitMeta: Meta, takerMeta: Meta, takerSplitMeta: Meta) = {
          val exchangeSplitsBurn = blockchainSplits.Utility.splitsBurn(ownerID = constants.Blockchain.Order.Exchanges, ownableID = makerSplitMeta.data)
          val takerSplitsBurn = blockchainSplits.Utility.splitsBurn(ownerID = takerMeta.data, ownableID = takerSplitMeta.data)
          val makerSplitsMint = blockchainSplits.Utility.splitsMint(ownerID = makerMeta.data, ownableID = takerSplitMeta.data)
          val takerSplitsMint = blockchainSplits.Utility.splitsMint(ownerID = takerMeta.data, ownableID = takerSplitMeta.data)

          for {
            _ <- exchangeSplitsBurn
            _ <- takerSplitsBurn
            _ <- makerSplitsMint
            _ <- takerSplitsMint
          } yield ()
        }

        for {
          makerMeta <- makerMeta
          makerSplitMeta <- makerSplitMeta
          takerMeta <- takerMeta
          takerSplitMeta <- takerSplitMeta
          _ <- update(makerMeta = makerMeta, makerSplitMeta = makerSplitMeta, takerMeta = takerMeta, takerSplitMeta = takerSplitMeta)
        } yield ()
      }

      def updateOrDelete(orderResponse: OrderResponse) = orderResponse.result.value.Orders.value.list.find(x => x.value.id.value.chainID.value.idString == chainID && x.value.id.value.maintainersID.value.idString == maintainersID && x.value.id.value.hashID.value.idString == hashID).fold(
        Service.delete(orderTake.orderID)
      ) { order =>
        Service.insertOrUpdate(Order(id = orderTake.orderID, mutables = order.value.mutables.toMutables, immutables = order.value.immutables.toImmutables))
      }

      (for {
        oldOrder <- oldOrder
        orderResponse <- orderResponse
        _ <- updateSplits(oldOrder)
        _ <- updateOrDelete(orderResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }

    def onCancel(orderCancel: OrderCancel): Future[Unit] = {
      val oldOrder = Service.tryGet(orderCancel.orderID)
      val orderResponse = getOrder.Service.get(orderCancel.orderID)
      val (chainID, maintainersID, hashID) = getFeatures(orderCancel.orderID)

      def updateSplits(oldOrder: Order) = {
        val makerMeta = blockchainMetas.Service.tryGet(oldOrder.immutables.properties.propertyList.find(_.id == constants.Blockchain.Order.MakerIDProperty).getOrElse(throw new BaseException(constants.Response.ORDER_NOT_FOUND)).fact.value.getHash)
        val makerSplitMeta = blockchainMetas.Service.tryGet(oldOrder.immutables.properties.propertyList.find(_.id == constants.Blockchain.Order.MakerSplitIDProperty).getOrElse(throw new BaseException(constants.Response.ORDER_NOT_FOUND)).fact.value.getHash)

        def update(makerMeta: Meta, makerSplitMeta: Meta) = {
          val exchangeSplitsBurn = blockchainSplits.Utility.splitsBurn(ownerID = constants.Blockchain.Order.Exchanges, ownableID = makerSplitMeta.data)
          val makerSplitsMint = blockchainSplits.Utility.splitsMint(ownerID = makerMeta.data, ownableID = makerSplitMeta.data)

          for {
            _ <- exchangeSplitsBurn
            _ <- makerSplitsMint
          } yield ()
        }

        for {
          makerMeta <- makerMeta
          makerSplitMeta <- makerSplitMeta
          _ <- update(makerMeta = makerMeta, makerSplitMeta = makerSplitMeta)
        } yield ()
      }

      def delete(orderResponse: OrderResponse) = if (!orderResponse.result.value.Orders.value.list.exists(x => x.value.id.value.chainID.value.idString == chainID && x.value.id.value.maintainersID.value.idString == maintainersID && x.value.id.value.hashID.value.idString == hashID)) Service.delete(orderCancel.orderID) else Future(0)

      (for {
        oldOrder <- oldOrder
        orderResponse <- orderResponse
        _ <- updateSplits(oldOrder)
        _ <- delete(orderResponse)
      } yield ()
        ).recover {
        case baseException: BaseException => throw baseException
      }
    }
  }

  private def getID(chainID: String, maintainersID: String, hashID: String) = Seq(chainID, maintainersID, hashID).mkString(constants.Blockchain.IDSeparator)

  private def getFeatures(id: String): (String, String, String) = {
    val idList = id.split(constants.RegularExpression.BLOCKCHAIN_ID_SEPARATOR)
    if (idList.length == 3) (idList(0), idList(1), idList(2)) else ("", "", "")
  }

}