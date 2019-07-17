package models.blockchain

import actors.{MainFiatActor, ShutdownActors}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsValue, Json}
import play.api.{Configuration, Logger}
import queries.{GetAccount, GetOrder}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Fiat(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean)

case class FiatCometMessage(ownerAddress: String, message: JsValue)

@Singleton
class Fiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, shutdownActors: ShutdownActors, blockchainNegotiations: Negotiations, getAccount: GetAccount, masterTransactionIssueFiatRequests: masterTransaction.IssueFiatRequests, masterAccounts: master.Accounts, getOrder: GetOrder)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_FIAT

  private val actorTimeout = configuration.get[Int]("akka.actors.timeout").seconds

  val mainFiatActor: ActorRef = actorSystem.actorOf(props = MainFiatActor.props(actorTimeout, actorSystem), name = constants.Module.ACTOR_MAIN_FIAT)

  import databaseConfig.profile.api._

  private[models] val fiatTable = TableQuery[FiatTable]

  private implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds

  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private def add(fiat: Fiat)(implicit executionContext: ExecutionContext): Future[String] = db.run((fiatTable returning fiatTable.map(_.pegHash) += fiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(fiat: Fiat)(implicit executionContext: ExecutionContext): Future[Int] = db.run(fiatTable.insertOrUpdate(fiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByPegHashAndOwnerAddress(pegHash: String, ownerAddress: String)(implicit executionContext: ExecutionContext): Future[Fiat] = db.run(fiatTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === ownerAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFiatPegWalletByAddress(address: String): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.ownerAddress === address).result)

  private def getFiatsByDirtyBit(dirtyBit: Boolean): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.dirtyBit === dirtyBit).result)

  private def updateDirtyBitByAddress(address: String, dirtyBit: Boolean): Future[Int] = db.run(fiatTable.filter(_.ownerAddress === address).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByPegHashAndAddress(pegHash: String, address: String)(implicit executionContext: ExecutionContext) = db.run(fiatTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByAddress(address: String)(implicit executionContext: ExecutionContext) = db.run(fiatTable.filter(_.ownerAddress === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class FiatTable(tag: Tag) extends Table[Fiat](tag, "Fiat_BC") {

    def * = (pegHash, ownerAddress, transactionID, transactionAmount, redeemedAmount, dirtyBit) <> (Fiat.tupled, Fiat.unapply)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def ownerAddress = column[String]("ownerAddress", O.PrimaryKey)

    def transactionID = column[String]("transactionID")

    def transactionAmount = column[String]("transactionAmount")

    def redeemedAmount = column[String]("redeemedAmount")

    def dirtyBit = column[Boolean]("dirtyBit")
  }

  object Service {

    def create(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): String = Await.result(add(Fiat(pegHash, ownerAddress, transactionID, transactionAmount, redeemedAmount, dirtyBit)), Duration.Inf)

    def getFiatPegWallet(address: String)(implicit executionContext: ExecutionContext): Seq[Fiat] = Await.result(getFiatPegWalletByAddress(address), Duration.Inf)

    def insertOrUpdate(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(upsert(Fiat(pegHash = pegHash, ownerAddress, transactionID = transactionID, transactionAmount = transactionAmount, redeemedAmount = redeemedAmount, dirtyBit)), Duration.Inf)

    def deleteFiat(pegHash: String, address: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByPegHashAndAddress(pegHash, address), Duration.Inf)

    def deleteFiatPegWallet(address: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByAddress(address), Duration.Inf)

    def getDirtyFiats: Seq[Fiat] = Await.result(getFiatsByDirtyBit(dirtyBit = true), Duration.Inf)

    def markDirty(address: String): Int = Await.result(updateDirtyBitByAddress(address, dirtyBit = true), Duration.Inf)

    def fiatCometSource(username: String) = {
      val address = masterAccounts.Service.getAddress(username)
      shutdownActors.shutdown(constants.Module.ACTOR_MAIN_FIAT, address)
      Thread.sleep(500)
      val (systemUserActor, source) = Source.actorRef[JsValue](0, OverflowStrategy.dropHead).preMaterialize()
      mainFiatActor ! actors.CreateFiatChildActorMessage(address = address, actorRef = systemUserActor)
      source
    }
  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      val dirtyFiats = Service.getDirtyFiats
      Thread.sleep(sleepTime)
      for (dirtyFiat <- dirtyFiats) {
        try {
          val fiatPegWallet = getAccount.Service.get(dirtyFiat.ownerAddress).value.fiatPegWallet.getOrElse(throw new BaseException(constants.Response.NO_RESPONSE))
          fiatPegWallet.foreach(fiatPeg => if (fiatPegWallet.map(_.pegHash) contains dirtyFiat.pegHash) Service.insertOrUpdate(fiatPeg.pegHash, dirtyFiat.ownerAddress, fiatPeg.transactionID, fiatPeg.transactionAmount, fiatPeg.redeemedAmount, dirtyBit = false) else Service.deleteFiat(dirtyFiat.pegHash, dirtyFiat.ownerAddress))
          mainFiatActor ! FiatCometMessage(ownerAddress = dirtyFiat.ownerAddress, message = Json.toJson(fiatPegWallet.map(_.transactionAmount.toInt).sum.toString))
        }
        catch {
          case baseException: BaseException => logger.info(baseException.failure.message, baseException)
            if (baseException.failure == constants.Response.NO_RESPONSE) {
              Service.deleteFiatPegWallet(dirtyFiat.ownerAddress)
            }
          case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
        }
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }
}