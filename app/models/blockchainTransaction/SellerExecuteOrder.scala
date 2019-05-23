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

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SellerExecuteOrder(from: String, buyerAddress: String, sellerAddress: String, awbProofHash: String, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class SellerExecuteOrders @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, blockchainNegotiations: blockchain.Negotiations, blockchainOrders: blockchain.Orders, transactionSellerExecuteOrder: transactions.SellerExecuteOrder, actorSystem: ActorSystem, pushNotifications: PushNotifications, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SELLER_EXECUTE_ORDER

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sellerExecuteOrderTable = TableQuery[SellerExecuteOrderTable]

  private def add(sellerExecuteOrder: SellerExecuteOrder)(implicit executionContext: ExecutionContext): Future[String] = db.run((sellerExecuteOrderTable returning sellerExecuteOrderTable.map(_.ticketID) += sellerExecuteOrder).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def update(sellerExecuteOrder: SellerExecuteOrder)(implicit executionContext: ExecutionContext): Future[Int] = db.run(sellerExecuteOrderTable.insertOrUpdate(sellerExecuteOrder).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.kafka.entityIterator.threadSleep")

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(sellerExecuteOrderTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SellerExecuteOrderTable(tag: Tag) extends Table[SellerExecuteOrder](tag, "SellerExecuteOrder") {

    def * = (from, buyerAddress, sellerAddress, awbProofHash, pegHash, gas, status.?, txHash.?, ticketID, responseCode.?) <> (SellerExecuteOrder.tupled, SellerExecuteOrder.unapply)

    def from = column[String]("from")

    def buyerAddress = column[String]("buyerAddress")

    def sellerAddress = column[String]("sellerAddress")

    def awbProofHash = column[String]("awbProofHash")

    def pegHash = column[String]("pegHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }

  private def updateStatusAndResponseOnTicketID(ticketID: String, status: Boolean, responseCode: String): Future[Int] = db.run(sellerExecuteOrderTable.filter(_.ticketID === ticketID).map(x => (x.status, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus()(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run(sellerExecuteOrderTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashStatusAndResponseOnTicketID(ticketID: String, txHash: String, status: Boolean, responseCode: String): Future[Int] = db.run(sellerExecuteOrderTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status, x.responseCode)).update((txHash, status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[SellerExecuteOrder] = db.run(sellerExecuteOrderTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  object Service {

    def addSellerExecuteOrder(from: String, buyerAddress: String, sellerAddress: String, awbProofHash: String, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(SellerExecuteOrder(from = from, buyerAddress = buyerAddress, sellerAddress = sellerAddress, awbProofHash = awbProofHash, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def updateTxHashStatusResponseCode(ticketID: String, txHash: String, status: Boolean, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseOnTicketID(ticketID, txHash, status, responseCode), Duration.Inf)

    def updateStatusAndResponseCode(ticketID: String, status: Boolean, responseCode: String): Int = Await.result(updateStatusAndResponseOnTicketID(ticketID, status, responseCode), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus(), Duration.Inf)

    def getTransaction(ticketID: String)(implicit executionContext: ExecutionContext): SellerExecuteOrder = Await.result(findByTicketID(ticketID), Duration.Inf)

  }

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.updateTxHashStatusResponseCode(ticketID, response.TxHash, status = true, response.Code)
        val sellerExecuteOrder = Service.getTransaction(ticketID)
        val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress = sellerExecuteOrder.buyerAddress, sellerAddress = sellerExecuteOrder.sellerAddress, pegHash = sellerExecuteOrder.pegHash)
        blockchainOrders.Service.updateDirtyBit(id = negotiationID, true)
        blockchainAccounts.Service.updateDirtyBit(sellerExecuteOrder.sellerAddress, dirtyBit = true)
        pushNotifications.sendNotification(masterAccounts.Service.getId(sellerExecuteOrder.buyerAddress), constants.Notification.SUCCESS, Seq(response.TxHash))
        pushNotifications.sendNotification(sellerExecuteOrder.from, constants.Notification.SUCCESS, Seq(response.TxHash))
      }
      catch {
        case baseException: BaseException => logger.error(constants.Response.BASE_EXCEPTION.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.updateStatusAndResponseCode(ticketID, status = false, message)
        val sellerExecuteOrder = Service.getTransaction(ticketID)
        pushNotifications.sendNotification(masterAccounts.Service.getId(sellerExecuteOrder.buyerAddress), constants.Notification.FAILURE, Seq(message))
        pushNotifications.sendNotification(sellerExecuteOrder.from, constants.Notification.FAILURE, Seq(message))
      } catch {
        case baseException: BaseException => logger.error(constants.Response.BASE_EXCEPTION.message, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start(Service.getTicketIDsOnStatus, transactionSellerExecuteOrder.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }
}