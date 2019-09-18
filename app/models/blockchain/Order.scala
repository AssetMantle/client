package models.blockchain

import actors.{MainOrderActor, ShutdownActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsValue, Json}
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Order(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], dirtyBit: Boolean)

case class OrderCometMessage(username: String, message: JsValue)

@Singleton
class Orders @Inject()(shutdownActors: ShutdownActor, masterAccounts: master.Accounts, actorSystem: ActorSystem, protected val databaseConfigProvider: DatabaseConfigProvider, getAccount: queries.GetAccount, blockchainNegotiations: Negotiations, blockchainTraderFeedbackHistories: TraderFeedbackHistories, blockchainAssets: Assets, blockchainFiats: Fiats, getOrder: queries.GetOrder, implicit val utilitiesNotification: utilities.Notification)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private val schedulerExecutionContext:ExecutionContext= actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  private implicit val module: String = constants.Module.BLOCKCHAIN_ORDER

  private val actorTimeout = configuration.get[Int]("akka.actors.timeout").seconds

  private val cometActorSleepTime = configuration.get[Long]("akka.actors.cometActorSleepTime")

  val mainOrderActor: ActorRef = actorSystem.actorOf(props = MainOrderActor.props(actorTimeout, actorSystem), name = constants.Module.ACTOR_MAIN_ORDER)

  private[models] val orderTable = TableQuery[OrderTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(order: Order): Future[String] = db.run((orderTable returning orderTable.map(_.id) += order).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(order: Order): Future[Int] = db.run(orderTable.insertOrUpdate(order).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
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

    def * = (id, fiatProofHash.?, awbProofHash.?, dirtyBit) <> (Order.tupled, Order.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def fiatProofHash = column[String]("fiatProofHash")

    def awbProofHash = column[String]("awbProofHash")

    def dirtyBit = column[Boolean]("dirtyBit")

  }

  object Service {

    def create(id: String, fiatProofHash: Option[String], awbProofHash: Option[String]): String = Await.result(add(Order(id = id, fiatProofHash = fiatProofHash, awbProofHash = awbProofHash, dirtyBit = true)), Duration.Inf)

    def insertOrUpdate(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], dirtyBit: Boolean): Int = Await.result(upsert(Order(id = id, fiatProofHash = fiatProofHash, awbProofHash = awbProofHash, dirtyBit = dirtyBit)), Duration.Inf)

    def getDirtyOrders: Seq[Order] = Await.result(getOrdersByDirtyBit(dirtyBit = true), Duration.Inf)

    def getOrders(ids: Seq[String]): Seq[Order] = Await.result(getOrdersByIDs(ids), Duration.Inf)

    def getAllOrderIds: Seq[String] = Await.result(getOrderIDs, Duration.Inf)

    def getAllOrderIdsWithoutFiatProofHash: Seq[String] = Await.result(getOrderIDsWithoutFiatProofHash, Duration.Inf)

    def getAllOrderIdsWithoutAWBProofHash: Seq[String] = Await.result(getOrderIDsWithoutAWBProofHash, Duration.Inf)

    def markDirty(id: String): Int = Await.result(updateDirtyBitById(id, dirtyBit = true), Duration.Inf)

    def orderCometSource(username: String) = {
      shutdownActors.shutdown(constants.Module.ACTOR_MAIN_ORDER, username)
      Thread.sleep(cometActorSleepTime)
      val (systemUserActor, source) = Source.actorRef[JsValue](0, OverflowStrategy.dropHead).preMaterialize()
      mainOrderActor ! actors.CreateOrderChildActorMessage(username = username, actorRef = systemUserActor)
      source
    }
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      val dirtyOrders = Service.getDirtyOrders
      Thread.sleep(sleepTime)
      for (dirtyOrder <- dirtyOrders) {
        try {
          val orderResponse = getOrder.Service.get(dirtyOrder.id)
          val negotiation = blockchainNegotiations.Service.get(dirtyOrder.id)
          if ((orderResponse.value.awbProofHash != "" && orderResponse.value.fiatProofHash != "") || (orderResponse.value.awbProofHash == "" && orderResponse.value.fiatProofHash == "")) {
            val sellerAccount = getAccount.Service.get(negotiation.sellerAddress)
            sellerAccount.value.assetPegWallet.foreach(assets => assets.foreach(asset => blockchainAssets.Service.insertOrUpdate(pegHash = asset.pegHash, documentHash = asset.documentHash, assetType = asset.assetType, assetQuantity = asset.assetQuantity, quantityUnit = asset.quantityUnit, assetPrice = asset.assetPrice, ownerAddress = negotiation.sellerAddress, moderated = asset.moderated, takerAddress = if (asset.takerAddress == "") None else Option(asset.takerAddress), locked = asset.locked, dirtyBit = true)))
            sellerAccount.value.fiatPegWallet.foreach(fiats => fiats.foreach(fiatPeg => blockchainFiats.Service.insertOrUpdate(fiatPeg.pegHash, negotiation.sellerAddress, fiatPeg.transactionID, fiatPeg.transactionAmount, fiatPeg.redeemedAmount, dirtyBit = true)))

            val buyerAccount = getAccount.Service.get(negotiation.buyerAddress)
            buyerAccount.value.assetPegWallet.foreach(assets => assets.foreach(asset => blockchainAssets.Service.insertOrUpdate(pegHash = asset.pegHash, documentHash = asset.documentHash, assetType = asset.assetType, assetQuantity = asset.assetQuantity, quantityUnit = asset.quantityUnit, assetPrice = asset.assetPrice, ownerAddress = negotiation.buyerAddress, moderated = asset.moderated, takerAddress = if (asset.takerAddress == "") None else Option(asset.takerAddress), locked = asset.locked, dirtyBit = true)))
            buyerAccount.value.fiatPegWallet.foreach(fiats => fiats.foreach(fiatPeg => blockchainFiats.Service.insertOrUpdate(fiatPeg.pegHash, negotiation.buyerAddress, fiatPeg.transactionID, fiatPeg.transactionAmount, fiatPeg.redeemedAmount, dirtyBit = true)))

            blockchainFiats.Service.deleteFiatPegWallet(dirtyOrder.id)
          }
          if (orderResponse.value.awbProofHash != "" && orderResponse.value.fiatProofHash != "") {
            blockchainTraderFeedbackHistories.Service.create(negotiation.sellerAddress, negotiation.buyerAddress, negotiation.sellerAddress, negotiation.assetPegHash, rating = "")
            blockchainTraderFeedbackHistories.Service.create(negotiation.buyerAddress, negotiation.buyerAddress, negotiation.sellerAddress, negotiation.assetPegHash, rating = "")
            blockchainNegotiations.Service.deleteNegotiations(negotiation.assetPegHash)
          }
          Service.insertOrUpdate(dirtyOrder.id, awbProofHash = if (orderResponse.value.awbProofHash == "") None else Option(orderResponse.value.awbProofHash), fiatProofHash = if (orderResponse.value.fiatProofHash == "") None else Option(orderResponse.value.fiatProofHash), dirtyBit = false)
          mainOrderActor ! OrderCometMessage(username = masterAccounts.Service.getId(negotiation.buyerAddress), message = Json.toJson(constants.Comet.PING))
          mainOrderActor ! OrderCometMessage(username = masterAccounts.Service.getId(negotiation.sellerAddress), message = Json.toJson(constants.Comet.PING))
        }
        catch {
          case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        }
      }
    }(schedulerExecutionContext)
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }(schedulerExecutionContext)

}