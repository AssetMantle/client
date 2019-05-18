package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.Response
import utilities.PushNotifications

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class BuyerExecuteOrder(from: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class BuyerExecuteOrders @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, blockchainNegotiations: blockchain.Negotiations, blockchainOrders: blockchain.Orders, transactionBuyerExecuteOrder: transactions.BuyerExecuteOrder, actorSystem: ActorSystem, pushNotifications: PushNotifications,masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext)  {

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
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[BuyerExecuteOrder] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseOnTicketID(ticketID: String, status: Boolean, responseCode: String): Future[Int] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(x => (x.status, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus()(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run(buyerExecuteOrderTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashStatusAndResponseOnTicketID(ticketID: String, txHash: String, status: Boolean, responseCode: String): Future[Int] = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status, x.responseCode)).update((txHash, status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }
  
  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(buyerExecuteOrderTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

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
  
  object Service {

    def addBuyerExecuteOrder(from: String, buyerAddress: String, sellerAddress: String, fiatProofHash: String, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(BuyerExecuteOrder(from = from, buyerAddress = buyerAddress, sellerAddress = sellerAddress,fiatProofHash = fiatProofHash, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def updateTxHashStatusResponseCode(ticketID: String, txHash: String, status: Boolean, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseOnTicketID(ticketID, txHash, status, responseCode), Duration.Inf)

    def updateStatusAndResponseCode(ticketID: String, status: Boolean, responseCode: String): Int = Await.result(updateStatusAndResponseOnTicketID(ticketID, status, responseCode), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus(), Duration.Inf)

    def getTransaction(ticketID: String)(implicit executionContext: ExecutionContext): BuyerExecuteOrder = Await.result(findByTicketID(ticketID), Duration.Inf)
    
  }

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval =  configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.kafka.entityIterator.threadSleep")

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.updateTxHashStatusResponseCode(ticketID, response.TxHash, status = true, response.Code)
        val buyerExecuteOrder = Service.getTransaction(ticketID)
        val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress = buyerExecuteOrder.buyerAddress, sellerAddress = buyerExecuteOrder.sellerAddress, pegHash = buyerExecuteOrder.pegHash)
        blockchainOrders.Service.updateDirtyBit(id = negotiationID, true)
        blockchainAccounts.Service.updateDirtyBit(buyerExecuteOrder.buyerAddress,true)
        pushNotifications.sendNotification(masterAccounts.Service.getId(buyerExecuteOrder.sellerAddress), constants.Notification.SUCCESS, Seq(response.TxHash))
        pushNotifications.sendNotification(buyerExecuteOrder.from, constants.Notification.SUCCESS, Seq(response.TxHash))
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
          throw new BaseException(constants.Error.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.updateStatusAndResponseCode(ticketID, status = false, message)
        val buyerExecuteOrder = Service.getTransaction(ticketID)
        pushNotifications.sendNotification(masterAccounts.Service.getId(buyerExecuteOrder.sellerAddress), constants.Notification.FAILURE, Seq(message))
        pushNotifications.sendNotification(buyerExecuteOrder.from, constants.Notification.FAILURE, Seq(message))
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
      }
    }
  }

  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start(Service.getTicketIDsOnStatus, transactionBuyerExecuteOrder.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }
}