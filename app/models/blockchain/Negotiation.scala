package models.blockchain

import akka.actor.ActorSystem
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import utilities.PushNotifications

import scala.concurrent.duration.{Duration,_}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Negotiation(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String], dirtyBit: Boolean)

@Singleton
class Negotiations @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, getNegotiation: queries.GetNegotiation, actorSystem: ActorSystem, implicit val pushNotifications: PushNotifications)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_NEGOTIATION

  private[models] val negotiationTable = TableQuery[NegotiationTable]

  private def add(negotiation: Negotiation)(implicit executionContext: ExecutionContext): Future[String] = db.run((negotiationTable returning negotiationTable.map(_.id) += negotiation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def insertOrUpdate(negotiation: Negotiation)(implicit executionContext: ExecutionContext): Future[Int] = db.run(negotiationTable.insertOrUpdate(negotiation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String)(implicit executionContext: ExecutionContext): Future[Negotiation] = db.run(negotiationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getIdByBuyerAddressSellerAddressAndPegHash(buyerAddress: String, sellerAddress: String, pegHash: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(negotiationTable.filter(_.buyerAddress === buyerAddress).filter(_.sellerAddress === sellerAddress).filter(_.assetPegHash === pegHash).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        ""
    }
  }

  private def getNegotiationsByDirtyBit(dirtyBit: Boolean): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.dirtyBit === dirtyBit).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        Nil
    }
  }

  private def updateDirtyBitById(id: String, dirtyBit: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateBidTimeSignatureDirtyBitsById(id: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String], dirtyBit: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(negotiation => (negotiation.bid, negotiation.time, negotiation.buyerSignature.?, negotiation.sellerSignature.?, negotiation.dirtyBit)).update((bid, time, buyerSignature, sellerSignature, dirtyBit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }
  
  private def deleteById(id: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(negotiationTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
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

    def addNegotiation(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(Negotiation(id = id, buyerAddress = buyerAddress, sellerAddress = sellerAddress, assetPegHash = assetPegHash, bid = bid, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature, false)), Duration.Inf)

    def insertOrUpdateNegotiation(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String], dirtyBit: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(insertOrUpdate(Negotiation(id = id, buyerAddress = buyerAddress, sellerAddress = sellerAddress, assetPegHash = assetPegHash, bid = bid, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature, dirtyBit = dirtyBit)), Duration.Inf)

    def getDirtyNegotiations(dirtyBit: Boolean): Seq[Negotiation] = Await.result(getNegotiationsByDirtyBit(dirtyBit), Duration.Inf)

    def getNegotiation(id: String): Negotiation = Await.result(findById(id), Duration.Inf)

    def updateDirtyBit(id: String, dirtyBit: Boolean): Int = Await.result(updateDirtyBitById(id, dirtyBit), Duration.Inf)

    def updateNegotiation(id: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String], dirtyBit: Boolean): Int = Await.result(updateBidTimeSignatureDirtyBitsById(id = id, bid = bid, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature, dirtyBit = dirtyBit), Duration.Inf)

    def getNegotiationID(buyerAddress: String, sellerAddress: String, pegHash: String)(implicit executionContext: ExecutionContext): String = Await.result(getIdByBuyerAddressSellerAddressAndPegHash(buyerAddress = buyerAddress, sellerAddress = sellerAddress, pegHash = pegHash), Duration.Inf)
  }

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val sleepTime = configuration.get[Long]("blockchain.kafka.entityIterator.threadSleep")

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      val dirtyNegotiations = Service.getDirtyNegotiations(true)
      Thread.sleep(sleepTime)
      for (dirtyNegotiation <- dirtyNegotiations) {
        try {
          val responseNegotiation = getNegotiation.Service.get(dirtyNegotiation.id)
          Service.updateNegotiation(id = dirtyNegotiation.id, bid = responseNegotiation.value.bid, time = responseNegotiation.value.time, buyerSignature = responseNegotiation.value.buyerSignature, sellerSignature = responseNegotiation.value.sellerSignature, dirtyBit = false)
        }
        catch {
          case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
          case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
        }
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }
}