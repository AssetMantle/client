package models.blockchain

import akka.actor.ActorSystem
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import slick.jdbc.JdbcProfile
import transactions.GetFiat
import utilities.PushNotifications
import scala.concurrent.duration._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Fiat(pegHash: String, transactionID: String, transactionAmount: Int, redeemedAmount: Int)

class Fiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, actorSystem: ActorSystem, implicit val pushNotifications: PushNotifications)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

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

  private def updateRedeemedAmountByPegHash(pegHash: String, redeemedAmount: Int)(implicit executionContext: ExecutionContext): Future[Int] = db.run(fiatTable.filter(_.pegHash === pegHash).map(_.redeemedAmount).update(redeemedAmount).asTry).map {
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

  private def getAllPegHash()(implicit executionContext: ExecutionContext):Future[Seq[String]] = db.run(fiatTable.map(_.pegHash).result)

  actorSystem.scheduler.schedule(initialDelay = configuration.get[Int]("blockchain.Iterator.initialDelay").seconds, interval = configuration.get[Int]("blockchain.Iterator.interval").second) {
    FiatIterator.start()
  }

  private[models] class FiatTable(tag: Tag) extends Table[Fiat](tag, "Fiat_BC") {

    def * = (pegHash, transactionID, transactionAmount, redeemedAmount) <> (Fiat.tupled, Fiat.unapply)

    def pegHash = column[String]("pegHash", O.PrimaryKey)

    def transactionID = column[String]("transactionID")

    def transactionAmount = column[Int]("transactionAmount")

    def redeemedAmount = column[Int]("redeemedAmount")
  }

  object Service {

    def addFiat(pegHash: String, transactionID: String, transactionAmount: Int, redeemedAmount: Int)(implicit executionContext: ExecutionContext): String = Await.result(add(Fiat(pegHash, transactionID, transactionAmount, redeemedAmount)),Duration.Inf)

    def getFiat(pegHash: String)(implicit executionContext: ExecutionContext): Fiat = Await.result(findByPegHash(pegHash), Duration.Inf)

    def updateRedeemedAmount(pegHash: String, redeemedAmount: Int)(implicit executionContext: ExecutionContext): Int = Await.result(updateRedeemedAmountByPegHash(pegHash, redeemedAmount), Duration.Inf)

    def getFiatPegWallet(pegHashSeq: Seq[String])(implicit executionContext: ExecutionContext): Seq[Fiat] = Await.result(getFiatPegWalletByPegHashSeq(pegHashSeq), Duration.Inf)

    def deleteFiat(pegHash: String)(implicit executionContext: ExecutionContext): Int = Await.result(deleteByPegHash(pegHash), Duration.Inf)

  }

  object FiatIterator {

    def start()(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext, logger: Logger, pushNotifications: PushNotifications): Unit = {
      val getFiat: GetFiat = new GetFiat()(wsClient, configuration, executionContext)
      val owners: Owners = new Owners(databaseConfigProvider)
      val pegHashes: Seq[String] = Await.result(getAllPegHash(), Duration.Inf)
      try {
        for(pegHash <- pegHashes){
          val fiat = getFiat.Service.get(pegHash).value
          Service.updateRedeemedAmount(pegHash, fiat.redeemedAmount.toInt)
          for(owner <- fiat.owners.get){
            owners.Service.insertOrUpdate(pegHash, owner.ownerAddress, owner.amount)
          }
        }
      }
      catch {
        case blockChainException: BlockChainException  => logger.info(blockChainException.message, blockChainException)
        case baseException: BaseException => logger.info(baseException.message, baseException)
      }
    }
  }

}