package models.blockchain

import akka.actor.ActorSystem
import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Logger}
import queries.GetAccount
import slick.jdbc.JdbcProfile

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Fiat(pegHash: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean)

@Singleton
class Fiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, getAccount: GetAccount, owners: Owners)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_FIAT

  import databaseConfig.profile.api._

  private[models] val fiatTable = TableQuery[FiatTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val sleepTime = configuration.get[Long]("blockchain.kafka.entityIterator.threadSleep")

  private def add(fiat: Fiat)(implicit executionContext: ExecutionContext): Future[String] = db.run((fiatTable returning fiatTable.map(_.pegHash) += fiat).asTry).map {
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

  private def findByPegHash(pegHash: String)(implicit executionContext: ExecutionContext): Future[Fiat] = db.run(fiatTable.filter(_.pegHash === pegHash).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getFiatPegWalletByPegHashSeq(pegHashSeq: Seq[String])(implicit executionContext: ExecutionContext): Future[Seq[Fiat]] = db.run(fiatTable.filter(_.pegHash.inSet(pegHashSeq)).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        Nil
    }
  }

  private def getAddressesByDirtyBit(dirtyBit: Boolean): Future[Seq[String]] = db.run(fiatTable.filter(_.dirtyBit === dirtyBit).map(_.pegHash).result.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.info(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        Nil
    }
  }

  private def updateDirtyBitByAddress(pegHash: String, dirtyBit: Boolean): Future[Int] = db.run(fiatTable.filter(_.pegHash === pegHash).map(_.dirtyBit).update(dirtyBit).asTry).map {
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

  private def deleteByPegHash(pegHash: String)(implicit executionContext: ExecutionContext) = db.run(fiatTable.filter(_.pegHash === pegHash).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class FiatTable(tag: Tag) extends Table[Fiat](tag, "Fiat_BC") {

    def * = (pegHash, transactionID, transactionAmount, redeemedAmount, dirtyBit) <> (Fiat.tupled, Fiat.unapply)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def transactionID = column[String]("transactionID")

    def transactionAmount = column[String]("transactionAmount")

    def redeemedAmount = column[String]("redeemedAmount")

    def dirtyBit = column[Boolean]("dirtyBit")
  }

  object Service {

    def addFiat(pegHash: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): String = Await.result(add(Fiat(pegHash, transactionID, transactionAmount, redeemedAmount, dirtyBit)), Duration.Inf)

    def getFiat(pegHash: String)(implicit executionContext: ExecutionContext): Fiat = Await.result(findByPegHash(pegHash), Duration.Inf)

    def insertOrUpdateFiat(pegHash: String, transactionID: String, transactionAmount: String, redeemedAmount: String, dirtyBit: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(insertOrUpdate(Fiat(pegHash = pegHash, transactionID = transactionID, transactionAmount = transactionAmount, redeemedAmount = redeemedAmount, dirtyBit)), Duration.Inf)

    def updateRedeemedAmount(pegHash: String, redeemedAmount: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateRedeemedAmountByPegHash(pegHash, redeemedAmount), Duration.Inf)

    def getFiatPegWallet(pegHashSeq: Seq[String])(implicit executionContext: ExecutionContext): Seq[Fiat] = Await.result(getFiatPegWalletByPegHashSeq(pegHashSeq), Duration.Inf)

    def deleteFiat(pegHash: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByPegHash(pegHash), Duration.Inf)

    def getDirtyAddresses(dirtyBit: Boolean): Seq[String] = Await.result(getAddressesByDirtyBit(dirtyBit), Duration.Inf)

    def updateDirtyBit(pegHash: String, dirtyBit: Boolean): Int = Await.result(updateDirtyBitByAddress(pegHash, dirtyBit), Duration.Inf)

  }

  object Utility {
    def dirtyEntityUpdater(): Future[Unit] = Future {
      try {
        val dirtyAddresses = Service.getDirtyAddresses(dirtyBit = true)
        Thread.sleep(sleepTime)
        for (dirtyAddress <- dirtyAddresses) {
          val responseFiat = getAccount.Service.get(dirtyAddress).value.fiatPegWallet.orNull
          responseFiat.foreach(fiatPegWallet => Service.insertOrUpdateFiat(fiatPegWallet.pegHash, fiatPegWallet.transactionID, fiatPegWallet.transactionAmount, fiatPegWallet.redeemedAmount, dirtyBit = false))
          responseFiat.foreach(fiatPegWallet => owners.Service.deleteOwners(fiatPegWallet.pegHash))
          responseFiat.foreach(fiatPegWallet => fiatPegWallet.owners.foreach(owner => owners.Service.addOwner(fiatPegWallet.pegHash, owner.ownerAddress, owner.amount)))

        }
      } catch {
        case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
      }
    }
  }

  //Scheduler- iterates accounts with dirty tags
  actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
    Utility.dirtyEntityUpdater()
  }
}