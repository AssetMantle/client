package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.Inject
import models.master.Accounts
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import slick.jdbc.JdbcProfile
import transactions.GetResponse
import utilities.PushNotifications

import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class BuyerExecuteOrder(from: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class BuyerExecuteOrders @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, transactionBuyerExecuteOrder: transactions.BuyerExecuteOrder, getResponse: GetResponse, actorSystem: ActorSystem, implicit val pushNotifications: PushNotifications, implicit val accounts: Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext)  {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_BUYER_EXECUTE_ORDER

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val buyerExecuteOrderTable = TableQuery[BuyerExecuteOrderTable]

  private def add(buyerExecuteOrder: BuyerExecuteOrder)(implicit executionContext: ExecutionContext): Future[String] = db.run((buyerExecuteOrderTable returning buyerExecuteOrderTable.map(_.ticketID) += buyerExecuteOrder).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def update(buyerExecuteOrder: BuyerExecuteOrder)(implicit executionContext: ExecutionContext): Future[Int] = db.run(buyerExecuteOrderTable.insertOrUpdate(buyerExecuteOrder).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[BuyerExecuteOrder] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String])(implicit executionContext: ExecutionContext) = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(_.txHash.?).update(txHash))

  private def updateResponseCodeOnTicketID(ticketID: String, responseCode: String)(implicit executionContext: ExecutionContext) = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(_.responseCode.?).update(Option(responseCode)))

  private def updateStatusOnTicketID(ticketID: String, status: Boolean)(implicit executionContext: ExecutionContext) = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(_.status.?).update(Option(status)))

  private def getTicketIDsWithEmptyTxHash()(implicit executionContext: ExecutionContext):Future[Seq[String]] = db.run(buyerExecuteOrderTable.filter(_.txHash.?.isEmpty).map(_.ticketID).result)

  private def getAddressByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(_.buyerAddress).result.head)

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).delete)

  private[models] class BuyerExecuteOrderTable(tag: Tag) extends Table[BuyerExecuteOrder](tag, "BuyerExecuteOrder") {

    def * = (from, buyerAddress, sellerAddress, fiatProofHash, pegHash, gas, status.?, txHash.?, ticketID, responseCode.?) <> (BuyerExecuteOrder.tupled, BuyerExecuteOrder.unapply)

    def from = column[String]("from")

    def buyerAddress = column[String]("buyerAddress")

    def sellerAddress = column[String]("sellerAddress")

    def fiatProofHash = column[String]("fiatProofHash")

    def pegHash = column[String]("pegHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }

  if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
    actorSystem.scheduler.schedule(initialDelay = configuration.get[Int]("blockchain.kafka.ticketIterator.initialDelay").seconds, interval = configuration.get[Int]("blockchain.kafka.ticketIterator.interval").second) {
      utilities.TicketIterator.start(Service.getTicketIDs, transactionBuyerExecuteOrder.Service.getTxHashFromWSResponse, Service.updateTxHash, Service.getAddress)
    }
  }

  object Service {

    def addBuyerExecuteOrder(from: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(BuyerExecuteOrder(from = from, buyerAddress = buyerAddress, sellerAddress = sellerAddress,fiatProofHash = fiatProofHash, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def addBuyerExecuteOrderKafka(from: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(BuyerExecuteOrder(from = from, buyerAddress = buyerAddress, sellerAddress = sellerAddress,fiatProofHash = fiatProofHash, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def updateTxHash(ticketID: String, txHash: String) (implicit executionContext: ExecutionContext): Int = Await.result(updateTxHashOnTicketID(ticketID, Option(txHash)),Duration.Inf)

    def updateResponseCode(ticketID: String, responseCode: String) (implicit executionContext: ExecutionContext): Int = Await.result(updateResponseCodeOnTicketID(ticketID, responseCode), Duration.Inf)

    def updateStatus(ticketID: String, status: Boolean) (implicit executionContext: ExecutionContext): Int = Await.result(updateStatusOnTicketID(ticketID, status), Duration.Inf)

    def getTicketIDs()(implicit executionContext: ExecutionContext): Seq[String] = Await.result(getTicketIDsWithEmptyTxHash(), Duration.Inf)

    def getAddress(ticketID: String)(implicit executionContext: ExecutionContext): String = Await.result(getAddressByTicketID(ticketID), Duration.Inf)

  }
}