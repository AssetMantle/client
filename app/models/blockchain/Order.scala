package models.blockchain

import java.sql.Timestamp

import akka.actor.ActorSystem
import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.common.Node
import models.{blockchain, master, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.responses._
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Order(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], dirtyBit: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged[Order] {

  def createLog()(implicit node: Node): Order = copy(createdBy = Option(node.id), createdOn = Option(new Timestamp(System.currentTimeMillis())), createdOnTimeZone = Option(node.timeZone))

  def updateLog()(implicit node: Node): Order = copy(updatedBy = Option(node.id), updatedOn = Option(new Timestamp(System.currentTimeMillis())), updatedOnTimeZone = Option(node.timeZone))

}

@Singleton
class Orders @Inject()(
                        blockchainAccounts: blockchain.Accounts,
                        masterNegotiations: master.Negotiations,
                        masterAssets: master.Assets,
                        actorSystem: ActorSystem,
                        protected val databaseConfigProvider: DatabaseConfigProvider,
                        blockchainNegotiations: Negotiations,
                        blockchainAssets: Assets,
                        blockchainFiats: Fiats,
                        getOrder: queries.GetOrder,
                        masterOrders: master.Orders,
                      )(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ORDER

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private[models] val orderTable = TableQuery[OrderTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private implicit val node: Node = Node(id = configuration.get[String]("node.id"), timeZone = configuration.get[String]("node.timeZone"))

  private def add(order: Order): Future[String] = db.run((orderTable returning orderTable.map(_.id) += order.createLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateByID(order: Order): Future[Int] = db.run(orderTable.filter(_.id === order.id).update(order.updateLog()).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Order] = db.run(orderTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getOrdersByDirtyBit(dirtyBit: Boolean): Future[Seq[Order]] = db.run(orderTable.filter(_.dirtyBit === dirtyBit).result)

  private def updateDirtyBitById(id: String, dirtyBit: Boolean): Future[Int] = db.run(orderTable.filter(_.id === id).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getOrdersByIDs(ids: Seq[String]): Future[Seq[Order]] = db.run(orderTable.filter(_.id.inSet(ids)).result)

  private def getOrderIDs: Future[Seq[String]] = db.run(orderTable.map(_.id).result)

  private def getOrderIDsWithoutFiatProofHash: Future[Seq[String]] = db.run(orderTable.filter(_.fiatProofHash.?.isEmpty).map(_.id).result)

  private def getOrderIDsWithoutAWBProofHash: Future[Seq[String]] = db.run(orderTable.filter(_.awbProofHash.?.isEmpty).map(_.id).result)

  private def checkOrderExistsByID(id: String): Future[Boolean] = db.run(orderTable.filter(_.id === id).exists.result)

  private def deleteById(id: String): Future[Int] = db.run(orderTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class OrderTable(tag: Tag) extends Table[Order](tag, "Order_BC") {

    def * = (id, fiatProofHash.?, awbProofHash.?, dirtyBit, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (Order.tupled, Order.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def fiatProofHash = column[String]("fiatProofHash")

    def awbProofHash = column[String]("awbProofHash")

    def dirtyBit = column[Boolean]("dirtyBit")

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")
  }

  object Service {

    def create(id: String, fiatProofHash: Option[String], awbProofHash: Option[String]): Future[String] = add(Order(id = id, fiatProofHash = fiatProofHash, awbProofHash = awbProofHash, dirtyBit = false))

    def update(order: Order): Future[Int] = updateByID(order)

    def getDirtyOrders: Future[Seq[Order]] = getOrdersByDirtyBit(dirtyBit = true)

    def getOrders(ids: Seq[String]): Future[Seq[Order]] = getOrdersByIDs(ids)

    def getAllOrderIds: Future[Seq[String]] = getOrderIDs

    def getAllOrderIdsWithoutFiatProofHash: Future[Seq[String]] = getOrderIDsWithoutFiatProofHash

    def getAllOrderIdsWithoutAWBProofHash: Future[Seq[String]] = getOrderIDsWithoutAWBProofHash

    def markDirty(id: String): Future[Int] = updateDirtyBitById(id, dirtyBit = true)

    def checkOrderExists(id: String): Future[Boolean] = checkOrderExistsByID(id)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = {
      val dirtyOrders = Service.getDirtyOrders
      Thread.sleep(sleepTime)

      def insertOrUpdateAndSendCometMessage(dirtyOrders: Seq[Order]): Future[Seq[Unit]] = {
        Future.sequence {
          dirtyOrders.map { dirtyOrder =>
            val orderResponse = getOrder.Service.get(dirtyOrder.id)
            val negotiation = blockchainNegotiations.Service.tryGet(dirtyOrder.id)
            val assetPegWallet = blockchainAssets.Service.getAssetPegWallet(dirtyOrder.id)
            val fiatPegWallet = blockchainFiats.Service.getFiatPegWallet(dirtyOrder.id)
            val masterOrder = masterOrders.Service.tryGetOrderByOrderID(dirtyOrder.id)

            def completeOrReverseOrder(masterOrder: master.Order, negotiation: Negotiation, assetPegWallet: Seq[Asset], fiatPegWallet: Seq[Fiat], orderResponse: OrderResponse.Response): Future[Unit] = {
              if (orderResponse.value.awbProofHash != "" && orderResponse.value.fiatProofHash != "") {
                val updateAsset = blockchainAssets.Service.update(assetPegWallet.head.copy(ownerAddress = negotiation.buyerAddress))
                val sellerMarkDirty = blockchainFiats.Service.markDirty(negotiation.sellerAddress)
                val deleteOrderFiats = blockchainFiats.Service.deleteFiatPegWallet(dirtyOrder.id)
                val updateMasterAssetStatus = masterAssets.Service.markTradeCompletedByPegHash(assetPegWallet.head.pegHash, masterOrder.buyerTraderID)
                val markMasterOrderStatusCompleted = masterOrders.Service.markStatusCompletedByBCOrderID(dirtyOrder.id)
                for {
                  _ <- updateAsset
                  _ <- sellerMarkDirty
                  _ <- deleteOrderFiats
                  _ <- updateMasterAssetStatus
                  _ <- markMasterOrderStatusCompleted
                } yield ()
              } else if (orderResponse.value.awbProofHash == "" && orderResponse.value.fiatProofHash == "") {
                val updateAsset = if (assetPegWallet.nonEmpty) blockchainAssets.Service.update(assetPegWallet.head.copy(ownerAddress = negotiation.sellerAddress)) else Future(0)
                val buyerMarkDirty = if (fiatPegWallet.nonEmpty) blockchainFiats.Service.markDirty(negotiation.buyerAddress) else Future(0)
                val deleteOrderFiats = if (fiatPegWallet.nonEmpty) blockchainFiats.Service.deleteFiatPegWallet(dirtyOrder.id) else Future(0)
                val resetMasterAssetStatus = if (assetPegWallet.nonEmpty) masterAssets.Service.resetStatusByPegHash(assetPegWallet.head.pegHash, masterOrder.sellerTraderID) else Future(0)
                val markMasterOrderStatusReversed = masterOrders.Service.markStatusReversedByBCOrderID(dirtyOrder.id)
                for {
                  _ <- updateAsset
                  _ <- buyerMarkDirty
                  _ <- deleteOrderFiats
                  _ <- resetMasterAssetStatus
                  _ <- markMasterOrderStatusReversed
                } yield ()
              } else if (orderResponse.value.awbProofHash != "" && orderResponse.value.fiatProofHash == "") {
                val markBuyerExecuteOrderPendingByBCOrderID = masterOrders.Service.markBuyerExecuteOrderPendingByBCOrderID(dirtyOrder.id)
                for{
                  _ <- markBuyerExecuteOrderPendingByBCOrderID
                } yield ()
              } else {
                val markSellerExecuteOrderPendingByBCOrderID = masterOrders.Service.markSellerExecuteOrderPendingByBCOrderID(dirtyOrder.id)
                for{
                  _ <- markSellerExecuteOrderPendingByBCOrderID
                } yield ()
              }
            }

            def update(orderResponse: queries.responses.OrderResponse.Response): Future[Int] = Service.update(Order(id = dirtyOrder.id, awbProofHash = if (orderResponse.value.awbProofHash == "") None else Option(orderResponse.value.awbProofHash), fiatProofHash = if (orderResponse.value.fiatProofHash == "") None else Option(orderResponse.value.fiatProofHash), dirtyBit = false))

            def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

            for {
              orderResponse <- orderResponse
              negotiation <- negotiation
              assetPegWallet <- assetPegWallet
              fiatPegWallet <- fiatPegWallet
              masterOrder <- masterOrder
              _ <- completeOrReverseOrder(masterOrder = masterOrder, negotiation = negotiation, assetPegWallet = assetPegWallet, fiatPegWallet = fiatPegWallet, orderResponse = orderResponse)
              _ <- update(orderResponse)
              buyerAccountID <- getAccountID(negotiation.buyerAddress)
              sellerAccountID <- getAccountID(negotiation.sellerAddress)
            } yield {
              actors.Service.cometActor ! actors.Message.makeCometMessage(username = buyerAccountID, messageType = constants.Comet.ORDER, messageContent = actors.Message.Order())
              actors.Service.cometActor ! actors.Message.makeCometMessage(username = sellerAccountID, messageType = constants.Comet.ORDER, messageContent = actors.Message.Order())
            }
          }
        }
      }

      (for {
        dirtyOrders <- dirtyOrders
        _ <- insertOrUpdateAndSendCometMessage(dirtyOrders)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }(schedulerExecutionContext)
}