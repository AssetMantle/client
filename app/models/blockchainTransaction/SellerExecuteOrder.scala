package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SellerExecuteOrder(from: String, buyerAddress: String, sellerAddress: String, awbProofHash: String, pegHash: String, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[SellerExecuteOrder] {
  def mutateTicketID(newTicketID: String): SellerExecuteOrder = SellerExecuteOrder(from = from, buyerAddress = buyerAddress, sellerAddress = sellerAddress, awbProofHash = awbProofHash, pegHash = pegHash, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class SellerExecuteOrders @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, blockchainTransactionFeedbacks: blockchain.TransactionFeedbacks, blockchainNegotiations: blockchain.Negotiations, blockchainOrders: blockchain.Orders, transactionSellerExecuteOrder: transactions.SellerExecuteOrder, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SELLER_EXECUTE_ORDER

  private implicit val logger: Logger = Logger(this.getClass)

  private val schedulerExecutionContext:ExecutionContext= actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sellerExecuteOrderTable = TableQuery[SellerExecuteOrderTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(sellerExecuteOrder: SellerExecuteOrder): Future[String] = db.run((sellerExecuteOrderTable returning sellerExecuteOrderTable.map(_.ticketID) += sellerExecuteOrder).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(sellerExecuteOrder: SellerExecuteOrder): Future[Int] = db.run(sellerExecuteOrderTable.insertOrUpdate(sellerExecuteOrder).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(sellerExecuteOrderTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(sellerExecuteOrderTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(sellerExecuteOrderTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[SellerExecuteOrder] = db.run(sellerExecuteOrderTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(sellerExecuteOrderTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(sellerExecuteOrderTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String) = db.run(sellerExecuteOrderTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(sellerExecuteOrderTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SellerExecuteOrderTable(tag: Tag) extends Table[SellerExecuteOrder](tag, "SellerExecuteOrder") {

    def * = (from, buyerAddress, sellerAddress, awbProofHash, pegHash, gas, status.?, txHash.?, ticketID, mode, code.?) <> (SellerExecuteOrder.tupled, SellerExecuteOrder.unapply)

    def from = column[String]("from")

    def buyerAddress = column[String]("buyerAddress")

    def sellerAddress = column[String]("sellerAddress")

    def awbProofHash = column[String]("awbProofHash")

    def pegHash = column[String]("pegHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(sellerExecuteOrder: SellerExecuteOrder): Future[String] = add(SellerExecuteOrder(from = sellerExecuteOrder.from, buyerAddress = sellerExecuteOrder.buyerAddress, sellerAddress = sellerExecuteOrder.sellerAddress, awbProofHash = sellerExecuteOrder.awbProofHash, pegHash = sellerExecuteOrder.pegHash, gas=sellerExecuteOrder.gas,status = sellerExecuteOrder.status, txHash = sellerExecuteOrder.txHash, ticketID = sellerExecuteOrder.ticketID, mode = sellerExecuteOrder.mode, code = sellerExecuteOrder.code))

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def getTicketIDsOnStatus(): Future[Seq[String]] = getTicketIDsWithNullStatus

    def getTransaction(ticketID: String): Future[SellerExecuteOrder] = findByTicketID(ticketID)

    def getTransactionHash(ticketID: String): Future[Option[String]] = findTransactionHashByTicketID(ticketID)

    def getMode(ticketID: String): Future[String] = findModeByTicketID(ticketID)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] =  {
      /*try {
        Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
        val sellerExecuteOrder = Service.getTransaction(ticketID)
        val negotiationID = blockchainNegotiations.Service.getNegotiationID(buyerAddress = sellerExecuteOrder.buyerAddress, sellerAddress = sellerExecuteOrder.sellerAddress, pegHash = sellerExecuteOrder.pegHash)
        blockchainOrders.Service.markDirty(id = negotiationID)
        blockchainAccounts.Service.markDirty(sellerExecuteOrder.sellerAddress)
        blockchainTransactionFeedbacks.Service.markDirty(sellerExecuteOrder.buyerAddress)
        blockchainTransactionFeedbacks.Service.markDirty(sellerExecuteOrder.sellerAddress)
        utilitiesNotification.send(masterAccounts.Service.getId(sellerExecuteOrder.buyerAddress), constants.Notification.SUCCESS, blockResponse.txhash)
        utilitiesNotification.send(masterAccounts.Service.getId(sellerExecuteOrder.sellerAddress), constants.Notification.SUCCESS, blockResponse.txhash)
        if (sellerExecuteOrder.from != sellerExecuteOrder.sellerAddress) {
          blockchainAccounts.Service.markDirty(sellerExecuteOrder.from)
          utilitiesNotification.send(masterAccounts.Service.getId(sellerExecuteOrder.from), constants.Notification.SUCCESS, blockResponse.txhash)
        }
      }
      catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }*/
      val markTransactionSuccessful = Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val sellerExecuteOrder = Service.getTransaction(ticketID)
      def negotiationID(sellerExecuteOrder:SellerExecuteOrder)=blockchainNegotiations.Service.getNegotiationID(buyerAddress = sellerExecuteOrder.buyerAddress, sellerAddress = sellerExecuteOrder.sellerAddress, pegHash = sellerExecuteOrder.pegHash)
      def markDirty(negotiationID:String,sellerExecuteOrder:SellerExecuteOrder)= {
        val markDirtyNegotiationID= blockchainOrders.Service.markDirty(id = negotiationID)
        val markDirtySellerAddressAccount=blockchainAccounts.Service.markDirty(sellerExecuteOrder.sellerAddress)
        val markDirtyBuyerAddressTransactionFeedbacks=blockchainTransactionFeedbacks.Service.markDirty(sellerExecuteOrder.buyerAddress)
        val markDirtySellerAddressTransactionFeedbacks=blockchainTransactionFeedbacks.Service.markDirty(sellerExecuteOrder.sellerAddress)
        val markDirtyFromAddress= {
          if (sellerExecuteOrder.from != sellerExecuteOrder.sellerAddress) {
            val markDirtyFromAddress = blockchainAccounts.Service.markDirty(sellerExecuteOrder.from)
            val id = masterAccounts.Service.getId(sellerExecuteOrder.from)
            for {
              _ <- markDirtyFromAddress
              id <- id
            } yield utilitiesNotification.send(id, constants.Notification.SUCCESS, blockResponse.txhash)

          } else Future {Unit}
        }

        for{
          _<-markDirtyNegotiationID
          _<-markDirtySellerAddressAccount
          _<-markDirtyBuyerAddressTransactionFeedbacks
          _<-markDirtySellerAddressTransactionFeedbacks
          _<- markDirtyFromAddress
        }yield {}
      }
      def getAddresses(sellerExecuteOrder:SellerExecuteOrder)={

        val addressList=List(masterAccounts.Service.getId(sellerExecuteOrder.sellerAddress),masterAccounts.Service.getId(sellerExecuteOrder.buyerAddress))
        val addressListFuture=Future.sequence(addressList)
        addressListFuture
      }

      (for{
        _<-markTransactionSuccessful
        sellerExecuteOrder<-sellerExecuteOrder
        negotiationID<-negotiationID(sellerExecuteOrder)
        _<- markDirty(negotiationID,sellerExecuteOrder)
        addressList <- getAddresses(sellerExecuteOrder)
      }yield{
        for(address<- addressList){
          utilitiesNotification.send(address, constants.Notification.SUCCESS, blockResponse.txhash)
        }
      }).recover{
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = {
      /*try {
        Service.markTransactionFailed(ticketID, message)
        val sellerExecuteOrder = Service.getTransaction(ticketID)
        blockchainTransactionFeedbacks.Service.markDirty(sellerExecuteOrder.buyerAddress)
        blockchainTransactionFeedbacks.Service.markDirty(sellerExecuteOrder.sellerAddress)
        utilitiesNotification.send(masterAccounts.Service.getId(sellerExecuteOrder.buyerAddress), constants.Notification.FAILURE, message)
        utilitiesNotification.send(masterAccounts.Service.getId(sellerExecuteOrder.sellerAddress), constants.Notification.FAILURE, message)
        if (sellerExecuteOrder.from != sellerExecuteOrder.sellerAddress) {
          blockchainAccounts.Service.markDirty(sellerExecuteOrder.from)
          utilitiesNotification.send(masterAccounts.Service.getId(sellerExecuteOrder.from), constants.Notification.FAILURE, message)
        }
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }*/

      val markTransactionFailed=Service.markTransactionFailed(ticketID, message)
      val sellerExecuteOrder = Service.getTransaction(ticketID)
      def markDirty(sellerExecuteOrder:SellerExecuteOrder)={
        val markDirtyBuyerAddress=blockchainTransactionFeedbacks.Service.markDirty(sellerExecuteOrder.buyerAddress)
        val markDirtySellerAddress=blockchainTransactionFeedbacks.Service.markDirty(sellerExecuteOrder.sellerAddress)
        def markDirtyFromAddress={
          if(sellerExecuteOrder.from != sellerExecuteOrder.sellerAddress){
            val markDirtyFromAddress= blockchainAccounts.Service.markDirty(sellerExecuteOrder.from)
            val id= masterAccounts.Service.getId(sellerExecuteOrder.from)
            for{
              _<- markDirtyFromAddress
              id<-id
            }yield utilitiesNotification.send(id, constants.Notification.FAILURE, message)
          }else{ Future{Unit}}
        }
        for{
          _<- markDirtyBuyerAddress
          _<-markDirtySellerAddress
          _<- markDirtyFromAddress
        }yield{}
      }

      def getAddresses(sellerExecuteOrder:SellerExecuteOrder)={

        val addressList=List(masterAccounts.Service.getId(sellerExecuteOrder.sellerAddress),masterAccounts.Service.getId(sellerExecuteOrder.buyerAddress))
        val addressListFuture=Future.sequence(addressList)
        addressListFuture
      }
      (for{
        _<- markTransactionFailed
        sellerExecuteOrder<- sellerExecuteOrder
        _<- markDirty(sellerExecuteOrder)
        addressList<-getAddresses(sellerExecuteOrder)
      }yield{
        for(address<- addressList){
          utilitiesNotification.send(address, constants.Notification.FAILURE, message)
        }
      }
        ).recover{
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure)
    }(schedulerExecutionContext)
  }
}