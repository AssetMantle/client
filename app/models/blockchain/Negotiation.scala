package models.blockchain

import actors.{Create, MainActor, ShutdownActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.Source
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsValue, Json}
import play.api.{Configuration, Logger}
import queries.responses.NegotiationResponse.Response
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Negotiation(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: String, time: String, buyerSignature: Option[String] = None, sellerSignature: Option[String] = None, buyerBlockHeight: Option[String] = None, sellerBlockHeight: Option[String] = None, buyerContractHash: Option[String] = None, sellerContractHash: Option[String] = None, dirtyBit: Boolean)

@Singleton
class Negotiations @Inject()(shutdownActors: ShutdownActor, actorsCreate: actors.Create, masterAccounts: master.Accounts, actorSystem: ActorSystem, protected val databaseConfigProvider: DatabaseConfigProvider, getNegotiation: queries.GetNegotiation, implicit val utilitiesNotification: utilities.Notification)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_NEGOTIATION
  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  private[models] val negotiationTable = TableQuery[NegotiationTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(negotiation: Negotiation): Future[String] = db.run((negotiationTable returning negotiationTable.map(_.id) += negotiation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(negotiation: Negotiation): Future[Int] = db.run(negotiationTable.insertOrUpdate(negotiation).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findById(id: String): Future[Negotiation] = db.run(negotiationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findBuyerOrdersInZone(ids: Seq[String], addresses: Seq[String]): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.id inSet ids).filter(_.buyerAddress inSet addresses).result)

  private def findSellerOrdersInZone(ids: Seq[String], addresses: Seq[String]): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.id inSet ids).filter(_.sellerAddress inSet addresses).result)

  private def getIdByBuyerAddressSellerAddressAndPegHash(buyerAddress: String, sellerAddress: String, pegHash: String): Future[String] = db.run(negotiationTable.filter(_.buyerAddress === buyerAddress).filter(_.sellerAddress === sellerAddress).filter(_.assetPegHash === pegHash).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        ""
    }
  }

  private def getNegotiationsByDirtyBit(dirtyBit: Boolean): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(_.dirtyBit === dirtyBit).result)

  private def getNegotiationsByAddress(address: String): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(negotiation => negotiation.buyerAddress === address || negotiation.sellerAddress === address).result)

  private def getNegotiationsByBuyerAddress(address: String): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(negotiation => negotiation.buyerAddress === address).result)

  private def getNegotiationsBySellerAddress(address: String): Future[Seq[Negotiation]] = db.run(negotiationTable.filter(negotiation => negotiation.sellerAddress === address).result)

  private def getNegotiationIDsByBuyerAddress(address: String): Future[Seq[String]] = db.run(negotiationTable.filter(negotiation => negotiation.buyerAddress === address).map(_.id).result)

  private def getNegotiationIDsBySellerAddress(address: String): Future[Seq[String]] = db.run(negotiationTable.filter(negotiation => negotiation.sellerAddress === address).map(_.id).result)

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

  private def updateNegotiationById(id: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String], buyerBlockHeight: Option[String], sellerBlockHeight: Option[String], buyerContractHash: Option[String], sellerContractHash: Option[String], dirtyBit: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(negotiation => (negotiation.bid, negotiation.time, negotiation.buyerSignature.?, negotiation.sellerSignature.?, negotiation.buyerBlockHeight.?, negotiation.sellerBlockHeight.?, negotiation.buyerContractHash.?, negotiation.sellerContractHash.?, negotiation.dirtyBit)).update((bid, time, buyerSignature, sellerSignature, buyerBlockHeight, sellerBlockHeight, buyerContractHash, sellerContractHash, dirtyBit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteById(id: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class NegotiationTable(tag: Tag) extends Table[Negotiation](tag, "Negotiation_BC") {

    def * = (id, buyerAddress, sellerAddress, assetPegHash, bid, time, buyerSignature.?, sellerSignature.?, buyerBlockHeight.?, sellerBlockHeight.?, buyerContractHash.?, sellerContractHash.?, dirtyBit) <> (Negotiation.tupled, Negotiation.unapply)

    def id = column[String]("id", O.PrimaryKey)

    def buyerAddress = column[String]("buyerAddress")

    def sellerAddress = column[String]("sellerAddress")

    def assetPegHash = column[String]("assetPegHash")

    def bid = column[String]("bid")

    def time = column[String]("time")

    def buyerSignature = column[String]("buyerSignature")

    def sellerSignature = column[String]("sellerSignature")

    def buyerBlockHeight = column[String]("buyerBlockHeight")

    def sellerBlockHeight = column[String]("sellerBlockHeight")

    def buyerContractHash = column[String]("buyerContractHash")

    def sellerContractHash = column[String]("sellerContractHash")

    def dirtyBit = column[Boolean]("dirtyBit")

  }

  object Service {

    def create(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String]): Future[String] = add(Negotiation(id = id, buyerAddress = buyerAddress, sellerAddress = sellerAddress, assetPegHash = assetPegHash, bid = bid, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature, dirtyBit = false))

    def insertOrUpdate(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String], buyerBlockHeight: Option[String], sellerBlockHeight: Option[String], buyerContractHash: Option[String], sellerContractHash: Option[String], dirtyBit: Boolean): Future[Int] = upsert(Negotiation(id = id, buyerAddress = buyerAddress, sellerAddress = sellerAddress, assetPegHash = assetPegHash, bid = bid, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature, buyerBlockHeight = buyerBlockHeight, sellerBlockHeight = sellerBlockHeight, buyerContractHash = buyerContractHash, sellerContractHash = sellerContractHash, dirtyBit = dirtyBit))

    def getDirtyNegotiations: Future[Seq[Negotiation]] = getNegotiationsByDirtyBit(dirtyBit = true)

    def get(id: String): Future[Negotiation] = findById(id)

    def getBuyerNegotiationsByOrderAndZone(ids: Seq[String], addresses: Seq[String]): Future[Seq[Negotiation]] = findBuyerOrdersInZone(ids, addresses)

    def getSellerNegotiationsByOrderAndZone(ids: Seq[String], addresses: Seq[String]): Future[Seq[Negotiation]] = findSellerOrdersInZone(ids, addresses)

    def markDirty(id: String): Future[Int] = updateDirtyBitById(id, dirtyBit = true)

    def refreshDirty(id: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String], buyerBlockHeight: Option[String], sellerBlockHeight: Option[String], buyerContractHash: Option[String], sellerContractHash: Option[String]): Future[Int] = updateNegotiationById(id = id, bid = bid, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature, buyerBlockHeight = buyerBlockHeight, sellerBlockHeight = sellerBlockHeight, buyerContractHash = buyerContractHash, sellerContractHash = sellerContractHash, dirtyBit = false)

    def getNegotiationID(buyerAddress: String, sellerAddress: String, pegHash: String): Future[String] = getIdByBuyerAddressSellerAddressAndPegHash(buyerAddress = buyerAddress, sellerAddress = sellerAddress, pegHash = pegHash)

    def getNegotiationsForAddress(address: String): Future[Seq[Negotiation]] = getNegotiationsByAddress(address)

    def getNegotiationsForBuyerAddress(address: String): Future[Seq[Negotiation]] = getNegotiationsByBuyerAddress(address)

    def getNegotiationsForSellerAddress(address: String): Future[Seq[Negotiation]] = getNegotiationsBySellerAddress(address)

    def getNegotiationIDsForBuyerAddress(address: String): Future[Seq[String]] = getNegotiationIDsByBuyerAddress(address)

    def getNegotiationIDsForSellerAddress(address: String): Future[Seq[String]] = getNegotiationIDsBySellerAddress(address)

    def deleteNegotiations(pegHash: String): Future[Int] = deleteNegotiationsByPegHash(pegHash)
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = {
      val dirtyNegotiations = Service.getDirtyNegotiations
      Thread.sleep(sleepTime)

      def refreshDirtyAndSendCometMessage(dirtyNegotiations: Seq[Negotiation]): Future[Seq[Unit]] = {
        Future.sequence {
          dirtyNegotiations.map { dirtyNegotiation =>
            val negotiationResponse = getNegotiation.Service.get(dirtyNegotiation.id)

            def refreshDirty(negotiationResponse: Response): Future[Int] = Service.refreshDirty(id = negotiationResponse.value.negotiationID, bid = negotiationResponse.value.bid, time = negotiationResponse.value.time, buyerSignature = negotiationResponse.value.buyerSignature, sellerSignature = negotiationResponse.value.sellerSignature, buyerBlockHeight = negotiationResponse.value.buyerBlockHeight, sellerBlockHeight = negotiationResponse.value.sellerBlockHeight, buyerContractHash = negotiationResponse.value.buyerContractHash, sellerContractHash = negotiationResponse.value.sellerContractHash)

            def getIDs(dirtyNegotiation: Negotiation): Future[(String, String)] = {
              val sellerAddressID = masterAccounts.Service.getId(dirtyNegotiation.sellerAddress)
              val buyerAddressID = masterAccounts.Service.getId(dirtyNegotiation.buyerAddress)
              for {
                sellerAddressID <- sellerAddressID
                buyerAddressID <- buyerAddressID
              } yield (sellerAddressID, buyerAddressID)
            }

            (for {
              negotiationResponse <- negotiationResponse
              _ <- refreshDirty(negotiationResponse)
              (sellerAddressID, buyerAddressID) <- getIDs(dirtyNegotiation)
            } yield {
              actorsCreate.mainActor ! actors.Message.makeCometMessage(username = sellerAddressID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation())
              actorsCreate.mainActor ! actors.Message.makeCometMessage(username = buyerAddressID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation())
            }).recover {
              case baseException: BaseException => logger.error(baseException.failure.message, baseException)
            }
          }
        }
      }

      (for {
        dirtyNegotiations <- dirtyNegotiations
        _ <- refreshDirtyAndSendCometMessage(dirtyNegotiations)
      } yield {}) (schedulerExecutionContext)
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }(schedulerExecutionContext)
}