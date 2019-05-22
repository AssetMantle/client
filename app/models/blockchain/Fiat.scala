package models.blockchain

import akka.actor.ActorSystem
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.{GetAccount, GetOrder}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Fiat(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean)

@Singleton
class Fiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, blockchainNegotiations: Negotiations, actorSystem: ActorSystem, getAccount: GetAccount, masterTransactionIssueFiatRequests: masterTransaction.IssueFiatRequests, masterAccounts: master.Accounts, getOrder: GetOrder)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_FIAT

  import databaseConfig.profile.api._

  private[models] val fiatTable = TableQuery[FiatTable]

  private def add(fiat: Fiat)(implicit executionContext: ExecutionContext): Future[String] = db.run((fiatTable returning fiatTable.map(_.pegHash) += fiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def addMultiple(fiats: Seq[Fiat])(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run((fiatTable returning fiatTable.map(_.pegHash) ++= fiats).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def insertOrUpdate(fiat: Fiat)(implicit executionContext: ExecutionContext): Future[Int] = db.run(fiatTable.insertOrUpdate(fiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findByPegHashAndOwnerAddress(pegHash: String, ownerAddress: String)(implicit executionContext: ExecutionContext): Future[Fiat] = db.run(fiatTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === ownerAddress).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFiatPegWalletByAddress(address: String)(implicit executionContext: ExecutionContext): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.ownerAddress === address).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        Nil
    }
  }

  private def getFiatsByDirtyBit(dirtyBit: Boolean): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.dirtyBit === dirtyBit).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        Nil
    }
  }

  private def updateDirtyBitByAddress(address: String, dirtyBit: Boolean): Future[Int] = db.run(fiatTable.filter(_.ownerAddress === address).map(_.dirtyBit).update(dirtyBit).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateOwnerAddressByPegHashAndPreviousOwnerAddress(pegHash: String, newOwnerAddress: String, previousOwnerAddress: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(fiatTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === previousOwnerAddress).map(_.ownerAddress).update(newOwnerAddress).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateOwnerAddressByPreviousOwnerAddress(newOwnerAddress: String, previousOwnerAddress: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(fiatTable.filter(_.ownerAddress === previousOwnerAddress).map(_.ownerAddress).update(newOwnerAddress).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateRedeemedAmountByPegHash(pegHash: String, redeemedAmount: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(fiatTable.filter(_.pegHash === pegHash).map(_.redeemedAmount).update(redeemedAmount).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByPegHashAndAddress(pegHash: String, address: String)(implicit executionContext: ExecutionContext) = db.run(fiatTable.filter(_.pegHash === pegHash).filter(_.ownerAddress === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByAddress(address: String)(implicit executionContext: ExecutionContext) = db.run(fiatTable.filter(_.ownerAddress === address).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
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

    def addFiat(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): String = Await.result(add(Fiat(pegHash, ownerAddress, transactionID, transactionAmount, redeemedAmount, dirtyBit)), Duration.Inf)

    def addFiats(fiats: Seq[Fiat])(implicit executionContext: ExecutionContext): Seq[String] = Await.result(addMultiple(fiats), Duration.Inf)

    def getFiatPegWallet(address: String)(implicit executionContext: ExecutionContext): Seq[Fiat] = Await.result(getFiatPegWalletByAddress(address), Duration.Inf)

    def insertOrUpdateFiat(pegHash: String, ownerAddress: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(insertOrUpdate(Fiat(pegHash = pegHash, ownerAddress, transactionID = transactionID, transactionAmount = transactionAmount, redeemedAmount = redeemedAmount, dirtyBit)), Duration.Inf)

    def updateRedeemedAmount(pegHash: String, redeemedAmount: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateRedeemedAmountByPegHash(pegHash, redeemedAmount), Duration.Inf)

    def updateOwnerAddress(pegHash: String, newOwnerAddress: String, previousOwnerAddress: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateOwnerAddressByPegHashAndPreviousOwnerAddress(pegHash = pegHash, newOwnerAddress = newOwnerAddress, previousOwnerAddress = previousOwnerAddress), Duration.Inf)

    def updateOwnerAddressWithoutPegHash(newOwnerAddress: String, previousOwnerAddress: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateOwnerAddressByPreviousOwnerAddress(newOwnerAddress = newOwnerAddress, previousOwnerAddress = previousOwnerAddress), Duration.Inf)

    def deleteFiat(pegHash: String, address: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByPegHashAndAddress(pegHash, address), Duration.Inf)

    def deleteFiatPegWallet(address: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByAddress(address), Duration.Inf)

    def getDirtyFiats(dirtyBit: Boolean): Seq[Fiat] = Await.result(getFiatsByDirtyBit(dirtyBit), Duration.Inf)

    def updateDirtyBit(address: String, dirtyBit: Boolean): Int = Await.result(updateDirtyBitByAddress(address, dirtyBit), Duration.Inf)
  }

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      val dirtyFiats = Service.getDirtyFiats(dirtyBit = true)
      Thread.sleep(sleepTime)
      for (dirtyFiat <- dirtyFiats) {
        try {
          val fiatPegWallet = getAccount.Service.get(dirtyFiat.ownerAddress).value.fiatPegWallet.getOrElse(throw new BaseException(constants.Error.NO_RESPONSE))
          fiatPegWallet.foreach(fiatPeg => if (fiatPegWallet.map(_.pegHash) contains dirtyFiat.pegHash) Service.insertOrUpdateFiat(fiatPeg.pegHash, dirtyFiat.ownerAddress, fiatPeg.transactionID, fiatPeg.transactionAmount, fiatPeg.redeemedAmount, dirtyBit = false) else Service.deleteFiat(dirtyFiat.pegHash, dirtyFiat.ownerAddress))
        }
        catch {
          case baseException: BaseException => logger.info(constants.Error.BASE_EXCEPTION, baseException)
            if (baseException.message == module + "." + constants.Error.NO_RESPONSE) {
              Service.deleteFiatPegWallet(dirtyFiat.ownerAddress)
            }
          case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
        }
      }
    }
  }

  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }
}