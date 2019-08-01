package models.blockchain

import actors.{MainNegotiationActor, ShutdownActors}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.master
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsValue, Json}
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import utilities.PushNotification

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Negotiation(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String], dirtyBit: Boolean)

case class NegotiationCometMessage(username: String, message: JsValue)

@Singleton
class Negotiations @Inject()(shutdownActors: ShutdownActors, masterAccounts: master.Accounts, actorSystem: ActorSystem, protected val databaseConfigProvider: DatabaseConfigProvider, getNegotiation: queries.GetNegotiation, implicit val pushNotification: PushNotification)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  private implicit val module: String = constants.Module.BLOCKCHAIN_NEGOTIATION

  private val actorTimeout = configuration.get[Int]("akka.actors.timeout").seconds

  private val cometActorSleepTime = configuration.get[Long]("akka.actors.cometActorSleepTime")

  val mainNegotiationActor: ActorRef = actorSystem.actorOf(props = MainNegotiationActor.props(actorTimeout, actorSystem), name = constants.Module.ACTOR_MAIN_NEGOTIATION)

  private[models] val negotiationTable = TableQuery[NegotiationTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(negotiation: Negotiation)(implicit executionContext: ExecutionContext): Future[String] = db.run((negotiationTable returning negotiationTable.map(_.id) += negotiation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(negotiation: Negotiation)(implicit executionContext: ExecutionContext): Future[Int] = db.run(negotiationTable.insertOrUpdate(negotiation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[Negotiation] = db.run(negotiationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findBuyerOrdersInZone(ids: Seq[String], addresses:Seq[String]): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.id inSet ids).filter(_.buyerAddress inSet addresses).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findSellerOrdersInZone(ids: Seq[String], addresses:Seq[String]): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.id inSet ids).filter(_.sellerAddress inSet addresses).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIdByBuyerAddressSellerAddressAndPegHash(buyerAddress: String, sellerAddress: String, pegHash: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(negotiationTable.filter(_.buyerAddress === buyerAddress).filter(_.sellerAddress === sellerAddress).filter(_.assetPegHash === pegHash).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        ""
    }
  }

  private def getNegotiationsByDirtyBit(dirtyBit: Boolean): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.dirtyBit === dirtyBit).result)

  private def getNegotiationsByAddress(address: String): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(negotiation => negotiation.buyerAddress === address || negotiation.sellerAddress === address).result)

  private def deleteNegotiationsByPegHash(pegHash: String) = db.run(negotiationTable.filter(_.assetPegHash === pegHash).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateDirtyBitById(id: String, dirtyBit: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBidTimeSignatureDirtyBitsById(id: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String], dirtyBit: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(negotiation => (negotiation.bid, negotiation.time, negotiation.buyerSignature.?, negotiation.sellerSignature.?, negotiation.dirtyBit)).update((bid, time, buyerSignature, sellerSignature, dirtyBit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(negotiationTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class NegotiationTable(tag: Tag) extends Table[Negotiation](tag, "Negotiation_BC") {

    def * = (id, buyerAddress, sellerAddress, assetPegHash, bid, time, buyerSignature.?, sellerSignature.?, dirtyBit) <> (Negotiation.tupled, Negotiation.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def buyerAddress = column[String]("buyerAddress")

    def sellerAddress = column[String]("sellerAddress")

    def assetPegHash = column[String]("assetPegHash")

    def bid = column[String]("bid")

    def time = column[String]("time")

    def buyerSignature = column[String]("buyerSignature")

    def sellerSignature = column[String]("sellerSignature")

    def dirtyBit = column[Boolean]("dirtyBit")

  }

  object Service {

    def create(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(Negotiation(id = id, buyerAddress = buyerAddress, sellerAddress = sellerAddress, assetPegHash = assetPegHash, bid = bid, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature, false)), Duration.Inf)

    def insertOrUpdate(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String], dirtyBit: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(upsert(Negotiation(id = id, buyerAddress = buyerAddress, sellerAddress = sellerAddress, assetPegHash = assetPegHash, bid = bid, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature, dirtyBit = dirtyBit)), Duration.Inf)

    def getDirtyNegotiations: Seq[Negotiation] = Await.result(getNegotiationsByDirtyBit(dirtyBit = true), Duration.Inf)

    def get(id: String): Negotiation = Await.result(findById(id), Duration.Inf)

    def getBuyerNegotiationsByOrderAndZone(ids:Seq[String], addresses:Seq[String]): Seq[Negotiation] = Await.result(findBuyerOrdersInZone(ids, addresses), Duration.Inf)

    def getSellerNegotiationsByOrderAndZone(ids:Seq[String], addresses:Seq[String]): Seq[Negotiation] = Await.result(findSellerOrdersInZone(ids, addresses), Duration.Inf)

    def markDirty(id: String): Int = Await.result(updateDirtyBitById(id, dirtyBit = true), Duration.Inf)

    def refreshDirty(id: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String]): Int = Await.result(updateBidTimeSignatureDirtyBitsById(id = id, bid = bid, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature, dirtyBit = false), Duration.Inf)

    def getNegotiationID(buyerAddress: String, sellerAddress: String, pegHash: String)(implicit executionContext: ExecutionContext): String = Await.result(getIdByBuyerAddressSellerAddressAndPegHash(buyerAddress = buyerAddress, sellerAddress = sellerAddress, pegHash = pegHash), Duration.Inf)

    def getNegotiationsForAddress(address: String): Seq[Negotiation] = Await.result(getNegotiationsByAddress(address), Duration.Inf)

    def deleteNegotiations(pegHash: String): Int = Await.result(deleteNegotiationsByPegHash(pegHash), Duration.Inf)

    def negotiationCometSource(username: String) = {
      shutdownActors.shutdown(constants.Module.ACTOR_MAIN_NEGOTIATION, username)
      Thread.sleep(cometActorSleepTime)
      val (systemUserActor, source) = Source.actorRef[JsValue](0, OverflowStrategy.dropHead).preMaterialize()
      mainNegotiationActor ! actors.CreateNegotiationChildActorMessage(username = username, actorRef = systemUserActor)
      source
    }
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      val dirtyNegotiations = Service.getDirtyNegotiations
      Thread.sleep(sleepTime)
      for (dirtyNegotiation <- dirtyNegotiations) {
        try {
          val responseNegotiation = getNegotiation.Service.get(dirtyNegotiation.id)
          Service.refreshDirty(id = dirtyNegotiation.id, bid = responseNegotiation.value.bid, time = responseNegotiation.value.time, buyerSignature = responseNegotiation.value.buyerSignature, sellerSignature = responseNegotiation.value.sellerSignature)
          mainNegotiationActor ! NegotiationCometMessage(username = masterAccounts.Service.getId(dirtyNegotiation.sellerAddress), message = Json.toJson(constants.Comet.PING))
          mainNegotiationActor ! NegotiationCometMessage(username = masterAccounts.Service.getId(dirtyNegotiation.buyerAddress), message = Json.toJson(constants.Comet.PING))
        }
        catch {
          case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
          case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        }
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }
}