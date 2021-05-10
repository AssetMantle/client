package models.blockchain

import java.sql.Timestamp

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Trait.Logged
import models.master.{Trader, Negotiation => masterNegotiation}
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.responses.NegotiationResponse.Response
import slick.jdbc.JdbcProfile
import utilities.MicroNumber

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Negotiation(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: MicroNumber, time: String, buyerSignature: Option[String] = None, sellerSignature: Option[String] = None, buyerBlockHeight: Option[String] = None, sellerBlockHeight: Option[String] = None, buyerContractHash: Option[String] = None, sellerContractHash: Option[String] = None, dirtyBit: Boolean, createdBy: Option[String] = None, createdOn: Option[Timestamp] = None, createdOnTimeZone: Option[String] = None, updatedBy: Option[String] = None, updatedOn: Option[Timestamp] = None, updatedOnTimeZone: Option[String] = None) extends Logged

@Singleton
class Negotiations @Inject()(
                              blockchainAccounts: blockchain.Accounts,
                              actorSystem: ActorSystem,
                              protected val databaseConfigProvider: DatabaseConfigProvider,
                              getNegotiation: queries.GetNegotiation,
                              masterNegotiations: master.Negotiations,
                              masterOrders: master.Orders,
                              masterAssets: master.Assets,
                              masterTraders: master.Traders,
                              configuration: Configuration,
                            )(implicit executionContext: ExecutionContext) {

  def serialize(negotiation: Negotiation): NegotiationSerialized = NegotiationSerialized(id = negotiation.id, buyerAddress = negotiation.buyerAddress, sellerAddress = negotiation.sellerAddress, assetPegHash = negotiation.assetPegHash, bid = negotiation.bid.toMicroString, time = negotiation.time, buyerSignature = negotiation.buyerSignature, sellerSignature = negotiation.sellerSignature, buyerBlockHeight = negotiation.buyerBlockHeight, sellerBlockHeight = negotiation.sellerBlockHeight, buyerContractHash = negotiation.buyerContractHash, sellerContractHash = negotiation.sellerContractHash, dirtyBit = negotiation.dirtyBit, createdBy = negotiation.createdBy, createdOn = negotiation.createdOn, createdOnTimeZone = negotiation.createdOnTimeZone, updatedBy = negotiation.updatedBy, updatedOn = negotiation.updatedOn, updatedOnTimeZone = negotiation.updatedOnTimeZone)

  case class NegotiationSerialized(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: String, time: String, buyerSignature: Option[String] = None, sellerSignature: Option[String] = None, buyerBlockHeight: Option[String] = None, sellerBlockHeight: Option[String] = None, buyerContractHash: Option[String] = None, sellerContractHash: Option[String] = None, dirtyBit: Boolean, createdBy: Option[String], createdOn: Option[Timestamp], createdOnTimeZone: Option[String], updatedBy: Option[String], updatedOn: Option[Timestamp], updatedOnTimeZone: Option[String]) {
    def deserialize: Negotiation = Negotiation(id = id, buyerAddress = buyerAddress, sellerAddress = sellerAddress, assetPegHash = assetPegHash, bid = new MicroNumber(BigInt(bid)), time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature, buyerBlockHeight = buyerBlockHeight, sellerBlockHeight = sellerBlockHeight, buyerContractHash = buyerContractHash, sellerContractHash = sellerContractHash, dirtyBit = dirtyBit, createdBy = createdBy, createdOn = createdOn, createdOnTimeZone = createdOnTimeZone, updatedBy = updatedBy, updatedOn = updatedOn, updatedOnTimeZone = updatedOnTimeZone)
  }

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_NEGOTIATION

  private val schedulerExecutionContext: ExecutionContext = actorSystem.dispatchers.lookup("akka.actor.scheduler-dispatcher")

  private[models] val negotiationTable = TableQuery[NegotiationTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(negotiationSerialized: NegotiationSerialized): Future[String] = db.run((negotiationTable returning negotiationTable.map(_.id) += negotiationSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
    }
  }

  private def updateByID(negotiationSerialized: NegotiationSerialized): Future[Int] = db.run(negotiationTable.filter(_.id === negotiationSerialized.id).update(negotiationSerialized).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetByID(id: String): Future[NegotiationSerialized] = db.run(negotiationTable.filter(_.id === id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def tryGetIDByBuyerAddressSellerAddressAndPegHash(buyerAddress: String, sellerAddress: String, pegHash: String): Future[String] = db.run(negotiationTable.filter(_.buyerAddress === buyerAddress).filter(_.sellerAddress === sellerAddress).filter(_.assetPegHash === pegHash).map(_.id).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def findBuyerOrdersInZone(ids: Seq[String], addresses: Seq[String]): Future[Seq[NegotiationSerialized]] = db.run(negotiationTable.filter(_.id inSet ids).filter(_.buyerAddress inSet addresses).result)

  private def findSellerOrdersInZone(ids: Seq[String], addresses: Seq[String]): Future[Seq[NegotiationSerialized]] = db.run(negotiationTable.filter(_.id inSet ids).filter(_.sellerAddress inSet addresses).result)

  private def getByBuyerAddressSellerAddressAndPegHash(buyerAddress: String, sellerAddress: String, pegHash: String): Future[Option[NegotiationSerialized]] = db.run(negotiationTable.filter(_.buyerAddress === buyerAddress).filter(_.sellerAddress === sellerAddress).filter(_.assetPegHash === pegHash).result.headOption)

  private def getIdByBuyerAddressSellerAddressAndPegHash(buyerAddress: String, sellerAddress: String, pegHash: String): Future[Option[String]] = db.run(negotiationTable.filter(_.buyerAddress === buyerAddress).filter(_.sellerAddress === sellerAddress).filter(_.assetPegHash === pegHash).map(_.id).result.headOption)

  private def getNegotiationsByDirtyBit(dirtyBit: Boolean): Future[Seq[NegotiationSerialized]] = db.run(negotiationTable.filter(_.dirtyBit === dirtyBit).result)

  private def getNegotiationsByAddress(address: String): Future[Seq[NegotiationSerialized]] = db.run(negotiationTable.filter(negotiation => negotiation.buyerAddress === address || negotiation.sellerAddress === address).result)

  private def getNegotiationsByBuyerAddress(address: String): Future[Seq[NegotiationSerialized]] = db.run(negotiationTable.filter(negotiation => negotiation.buyerAddress === address).result)

  private def getNegotiationsBySellerAddress(address: String): Future[Seq[NegotiationSerialized]] = db.run(negotiationTable.filter(negotiation => negotiation.sellerAddress === address).result)

  private def getNegotiationsByBuyerAddresses(addresses: Seq[String]): Future[Seq[NegotiationSerialized]] = db.run(negotiationTable.filter(_.buyerAddress.inSet(addresses)).result)

  private def getNegotiationsBySellerAddresses(addresses: Seq[String]): Future[Seq[NegotiationSerialized]] = db.run(negotiationTable.filter(_.sellerAddress.inSet(addresses)).result)

  private def getNegotiationIDsByBuyerAddress(address: String): Future[Seq[String]] = db.run(negotiationTable.filter(negotiation => negotiation.buyerAddress === address).map(_.id).result)

  private def getNegotiationIDsBySellerAddress(address: String): Future[Seq[String]] = db.run(negotiationTable.filter(negotiation => negotiation.sellerAddress === address).map(_.id).result)

  private def deleteNegotiationsByPegHash(pegHash: String) = db.run(negotiationTable.filter(_.assetPegHash === pegHash).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateDirtyBitById(id: String, dirtyBit: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def updateNegotiationById(id: String, bid: String, time: String, buyerSignature: Option[String], sellerSignature: Option[String], buyerBlockHeight: Option[String], sellerBlockHeight: Option[String], buyerContractHash: Option[String], sellerContractHash: Option[String], dirtyBit: Boolean): Future[Int] = db.run(negotiationTable.filter(_.id === id).map(negotiation => (negotiation.bid, negotiation.time, negotiation.buyerSignature.?, negotiation.sellerSignature.?, negotiation.buyerBlockHeight.?, negotiation.sellerBlockHeight.?, negotiation.buyerContractHash.?, negotiation.sellerContractHash.?, negotiation.dirtyBit)).update((bid, time, buyerSignature, sellerSignature, buyerBlockHeight, sellerBlockHeight, buyerContractHash, sellerContractHash, dirtyBit)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private def deleteById(id: String): Future[Int] = db.run(negotiationTable.filter(_.id === id).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => throw new BaseException(constants.Response.PSQL_EXCEPTION, psqlException)
      case noSuchElementException: NoSuchElementException => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
    }
  }

  private[models] class NegotiationTable(tag: Tag) extends Table[NegotiationSerialized](tag, "Negotiation_BC") {

    def * = (id, buyerAddress, sellerAddress, assetPegHash, bid, time, buyerSignature.?, sellerSignature.?, buyerBlockHeight.?, sellerBlockHeight.?, buyerContractHash.?, sellerContractHash.?, dirtyBit, createdBy.?, createdOn.?, createdOnTimeZone.?, updatedBy.?, updatedOn.?, updatedOnTimeZone.?) <> (NegotiationSerialized.tupled, NegotiationSerialized.unapply)

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

    def createdBy = column[String]("createdBy")

    def createdOn = column[Timestamp]("createdOn")

    def createdOnTimeZone = column[String]("createdOnTimeZone")

    def updatedBy = column[String]("updatedBy")

    def updatedOn = column[Timestamp]("updatedOn")

    def updatedOnTimeZone = column[String]("updatedOnTimeZone")

  }

  object Service {

    def create(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: MicroNumber, time: String, buyerSignature: Option[String], sellerSignature: Option[String], buyerBlockHeight: Option[String], sellerBlockHeight: Option[String], buyerContractHash: Option[String], sellerContractHash: Option[String], dirtyBit: Boolean): Future[String] = add(serialize(Negotiation(id = id, buyerAddress = buyerAddress, sellerAddress = sellerAddress, assetPegHash = assetPegHash, bid = bid, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature, buyerBlockHeight = buyerBlockHeight, sellerBlockHeight = sellerBlockHeight, buyerContractHash = buyerContractHash, sellerContractHash = sellerContractHash, dirtyBit = dirtyBit)))

    def update(id: String, buyerAddress: String, sellerAddress: String, assetPegHash: String, bid: MicroNumber, time: String, buyerSignature: Option[String], sellerSignature: Option[String], buyerBlockHeight: Option[String], sellerBlockHeight: Option[String], buyerContractHash: Option[String], sellerContractHash: Option[String], dirtyBit: Boolean): Future[Int] = updateByID(serialize(Negotiation(id = id, buyerAddress = buyerAddress, sellerAddress = sellerAddress, assetPegHash = assetPegHash, bid = bid, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature, buyerBlockHeight = buyerBlockHeight, sellerBlockHeight = sellerBlockHeight, buyerContractHash = buyerContractHash, sellerContractHash = sellerContractHash, dirtyBit = dirtyBit)))

    def getDirtyNegotiations: Future[Seq[Negotiation]] = getNegotiationsByDirtyBit(dirtyBit = true).map(_.map(_.deserialize))

    def tryGet(id: String): Future[Negotiation] = tryGetByID(id).map(_.deserialize)

    def tryGetID(buyerAddress: String, sellerAddress: String, pegHash: String): Future[String] = tryGetIDByBuyerAddressSellerAddressAndPegHash(buyerAddress = buyerAddress, sellerAddress = sellerAddress, pegHash = pegHash)

    def getBuyerNegotiationsByOrderAndZone(ids: Seq[String], addresses: Seq[String]): Future[Seq[Negotiation]] = findBuyerOrdersInZone(ids, addresses).map(_.map(_.deserialize))

    def getSellerNegotiationsByOrderAndZone(ids: Seq[String], addresses: Seq[String]): Future[Seq[Negotiation]] = findSellerOrdersInZone(ids, addresses).map(_.map(_.deserialize))

    def markDirty(id: String): Future[Int] = updateDirtyBitById(id, dirtyBit = true)

    def refreshDirty(id: String, bid: MicroNumber, time: String, buyerSignature: Option[String], sellerSignature: Option[String], buyerBlockHeight: Option[String], sellerBlockHeight: Option[String], buyerContractHash: Option[String], sellerContractHash: Option[String]): Future[Int] = updateNegotiationById(id = id, bid = bid.toMicroString, time = time, buyerSignature = buyerSignature, sellerSignature = sellerSignature, buyerBlockHeight = buyerBlockHeight, sellerBlockHeight = sellerBlockHeight, buyerContractHash = buyerContractHash, sellerContractHash = sellerContractHash, dirtyBit = false)

    def getNegotiation(buyerAddress: String, sellerAddress: String, pegHash: String): Future[Option[Negotiation]] = getByBuyerAddressSellerAddressAndPegHash(buyerAddress = buyerAddress, sellerAddress = sellerAddress, pegHash = pegHash).map(_.map(_.deserialize))

    def getNegotiationID(buyerAddress: String, sellerAddress: String, pegHash: String): Future[Option[String]] = getIdByBuyerAddressSellerAddressAndPegHash(buyerAddress = buyerAddress, sellerAddress = sellerAddress, pegHash = pegHash)

    def getNegotiationsForAddress(address: String): Future[Seq[Negotiation]] = getNegotiationsByAddress(address).map(_.map(_.deserialize))

    def getNegotiationsForBuyerAddress(address: String): Future[Seq[Negotiation]] = getNegotiationsByBuyerAddress(address).map(_.map(_.deserialize))

    def getNegotiationsForSellerAddress(address: String): Future[Seq[Negotiation]] = getNegotiationsBySellerAddress(address).map(_.map(_.deserialize))

    def getNegotiationIDsForBuyerAddress(address: String): Future[Seq[String]] = getNegotiationIDsByBuyerAddress(address)

    def getNegotiationIDsForSellerAddress(address: String): Future[Seq[String]] = getNegotiationIDsBySellerAddress(address)

    def deleteNegotiations(pegHash: String): Future[Int] = deleteNegotiationsByPegHash(pegHash)

    def getNegotiationsForBuyerAddresses(addresses: Seq[String]): Future[Seq[Negotiation]] = getNegotiationsByBuyerAddresses(addresses).map(_.map(_.deserialize))

    def getNegotiationsForSellerAddresses(addresses: Seq[String]): Future[Seq[Negotiation]] = getNegotiationsBySellerAddresses(addresses).map(_.map(_.deserialize))

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

            def getAccountID(address: String): Future[String] = blockchainAccounts.Service.tryGetUsername(address)

            def getTrader(accountID: String): Future[Trader] = masterTraders.Service.tryGetByAccountID(accountID)

            def getAssetID(pegHash: String): Future[String] = masterAssets.Service.tryGetIDByPegHash(pegHash)

            def getMasterNegotiation(buyerTraderID: String, sellerTraderID: String, assetID: String): Future[masterNegotiation] = masterNegotiations.Service.tryGetByBuyerSellerTraderIDAndAssetID(buyerTraderID = buyerTraderID, sellerTraderID = sellerTraderID, assetID = assetID)

            def updateMasterNegotiationStatus(negotiation: masterNegotiation, negotiationResponse: Response): Future[Int] = {
              if (negotiationResponse.value.buyerSignature.isEmpty && negotiationResponse.value.sellerSignature.isEmpty) masterNegotiations.Service.update(negotiation.copy(price = negotiationResponse.value.bid, buyerAcceptedPrice = if (negotiation.status == constants.Status.Negotiation.STARTED) false else negotiation.buyerAcceptedPrice, time = Option(negotiationResponse.value.time.toInt)))
              else if (negotiationResponse.value.buyerSignature.isDefined && negotiationResponse.value.sellerSignature.isEmpty) masterNegotiations.Service.update(negotiation.copy(price = negotiationResponse.value.bid, time = Option(negotiationResponse.value.time.toInt), status = constants.Status.Negotiation.BUYER_CONFIRMED_SELLER_PENDING))
              else if (negotiationResponse.value.buyerSignature.isEmpty && negotiationResponse.value.sellerSignature.isDefined) masterNegotiations.Service.update(negotiation.copy(price = negotiationResponse.value.bid, time = Option(negotiationResponse.value.time.toInt), status = constants.Status.Negotiation.SELLER_CONFIRMED_BUYER_PENDING))
              else if (negotiationResponse.value.buyerSignature.isDefined && negotiationResponse.value.sellerSignature.isDefined) masterNegotiations.Service.update(negotiation.copy(price = negotiationResponse.value.bid, time = Option(negotiationResponse.value.time.toInt), status = constants.Status.Negotiation.BOTH_PARTIES_CONFIRMED))
              else Future(0)
            }

            for {
              negotiationResponse <- negotiationResponse
              _ <- refreshDirty(negotiationResponse)
              buyerAccountID <- getAccountID(dirtyNegotiation.buyerAddress)
              sellerAccountID <- getAccountID(dirtyNegotiation.sellerAddress)
              buyer <- getTrader(buyerAccountID)
              seller <- getTrader(sellerAccountID)
              assetID <- getAssetID(negotiationResponse.value.pegHash)
              negotiation <- getMasterNegotiation(buyerTraderID = buyer.id, sellerTraderID = seller.id, assetID = assetID)
              _ <- updateMasterNegotiationStatus(negotiation = negotiation, negotiationResponse = negotiationResponse)
            } yield {
              actors.Service.cometActor ! actors.Message.makeCometMessage(username = buyerAccountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(negotiation.id))
              actors.Service.cometActor ! actors.Message.makeCometMessage(username = sellerAccountID, messageType = constants.Comet.NEGOTIATION, messageContent = actors.Message.Negotiation(negotiation.id))
            }
          }
        }
      }

      (for {
        dirtyNegotiations <- dirtyNegotiations
        _ <- refreshDirtyAndSendCometMessage(dirtyNegotiations)
      } yield ()).recover {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  val scheduledTask = new Runnable {
    override def run(): Unit = {
      try {
        Await.result(Utility.dirtyEntityUpdater(), Duration.Inf)
      } catch {
        case exception: Exception => logger.error(exception.getMessage, exception)
      }
    }
  }

  actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = schedulerInitialDelay, delay = schedulerInterval)(scheduledTask)(schedulerExecutionContext)
}