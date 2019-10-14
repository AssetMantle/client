package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.master.Account
import models.masterTransaction.FaucetRequests
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import queries.GetAccount
import slick.jdbc.JdbcProfile
import transactions.responses.TransactionResponse.BlockResponse

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendCoin(from: String, to: String, amount: Int, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[SendCoin] {
  def mutateTicketID(newTicketID: String): SendCoin = SendCoin(from = from, to = to, amount = amount, gas = gas, status = status, txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class SendCoins @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, transactionSendCoin: transactions.SendCoin, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts, implicit val faucetRequests: FaucetRequests, implicit val getAccount: GetAccount)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private implicit val logger: Logger = Logger(this.getClass)

  private val schedulerExecutionContext:ExecutionContext= actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SEND_COIN

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sendCoinTable = TableQuery[SendCoinTable]
  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(sendCoin: SendCoin): Future[String] = db.run((sendCoinTable returning sendCoinTable.map(_.ticketID) += sendCoin).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(sendCoin: SendCoin): Future[Int] = db.run(sendCoinTable.insertOrUpdate(sendCoin).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update((txHash, status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[SendCoin] = db.run(sendCoinTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(sendCoinTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String) = db.run(sendCoinTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SendCoinTable(tag: Tag) extends Table[SendCoin](tag, "SendCoin") {

    def * = (from, to, amount, gas, status.?, txHash.?, ticketID, mode, code.?) <> (SendCoin.tupled, SendCoin.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def amount = column[Int]("amount")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(sendCoin: SendCoin): Future[String] =add(SendCoin(from = sendCoin.from, to = sendCoin.to, amount = sendCoin.amount, gas = sendCoin.gas, status = sendCoin.status, txHash = sendCoin.txHash, ticketID = sendCoin.ticketID, mode = sendCoin.mode, code = sendCoin.code))

    def createAsync(sendCoin: SendCoin)=add(SendCoin(from = sendCoin.from, to = sendCoin.to, amount = sendCoin.amount, gas = sendCoin.gas, status = sendCoin.status, txHash = sendCoin.txHash, ticketID = sendCoin.ticketID, mode = sendCoin.mode, code = sendCoin.code))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] =updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def markTransactionFailedAsync(ticketID: String, code: String) = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] = updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionSuccessfulAsync(ticketID: String, txHash: String)=updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def getTransaction(ticketID: String): Future[SendCoin] = findByTicketID(ticketID)

    def getTransactionAsync(ticketID: String)=findByTicketID(ticketID)

    def getTransactionHash(ticketID: String): Option[String] = Await.result(findTransactionHashByTicketID(ticketID), Duration.Inf)

    def getMode(ticketID: String): String = Await.result(findModeByTicketID(ticketID), Duration.Inf)

    def updateTransactionHash(ticketID: String, txHash: String): Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

    def updateTransactionHashAsync(ticketID: String, txHash: String)=updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))
  }

  object Utility {
   /* def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
        val sendCoin = Service.getTransaction(ticketID)
        blockchainAccounts.Service.markDirty(sendCoin.to)
        blockchainAccounts.Service.markDirty(sendCoin.from)
        val toAccount = masterAccounts.Service.getAccountByAddress(sendCoin.to)
        if (toAccount.userType == constants.User.UNKNOWN) {
          masterAccounts.Service.updateUserType(toAccount.id, constants.User.USER)
        }
        utilitiesNotification.send(toAccount.id, constants.Notification.SUCCESS, blockResponse.txhash)
        utilitiesNotification.send(masterAccounts.Service.getId(sendCoin.from), constants.Notification.SUCCESS, blockResponse.txhash)
      }
      val trxSuccess=Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val sendCoin = Service.getTransaction(ticketID)
       val result= for{
          _<- trxSuccess
          sendCoinResult <- sendCoin
          toAccount <- markDirty(sendCoinResult)
        }yield(toAccount,sendCoinResult)

      result.map{case (toAccount,sendCoin)=>
        if (toAccount.userType == constants.User.UNKNOWN) {
          masterAccounts.Service.updateUserType(toAccount.id, constants.User.USER)
        }
        val x=utilitiesNotification.send(toAccount.id, constants.Notification.SUCCESS, blockResponse.txhash)

        utilitiesNotification.send(masterAccounts.Service.getId(sendCoin.from), constants.Notification.SUCCESS, blockResponse.txhash)
      }

      catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }
    }
    def markDirty(sendCoin: SendCoin)={
      val markDirtyTo=blockchainAccounts.Service.markDirty(sendCoin.to)
      val markDirtyFrom=blockchainAccounts.Service.markDirty(sendCoin.from)
      val toAccount=masterAccounts.Service.getAccountByAddress(sendCoin.to)
     for{
       _<-markDirtyTo
       _<-markDirtyFrom
       toAccountResult<- toAccount
     }yield (toAccountResult)
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val sendCoin = Service.getTransaction(ticketID)
        utilitiesNotification.send(masterAccounts.Service.getId(sendCoin.to), constants.Notification.FAILURE, message)
        utilitiesNotification.send(masterAccounts.Service.getId(sendCoin.from), constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }*/

    def onFailure(ticketID: String, message: String) =  {
      /*try {
        Service.markTransactionFailed(ticketID, message)
        val sendCoin = Service.getTransaction(ticketID)
        utilitiesNotification.send(masterAccounts.Service.getId(sendCoin.to), constants.Notification.FAILURE, message)
        utilitiesNotification.send(masterAccounts.Service.getId(sendCoin.from), constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }*/

      val markTransactionFailed=Service.markTransactionFailed(ticketID, message)
      val sendCoin = Service.getTransaction(ticketID)
      def addresses(sendCoin:SendCoin)={
        val to=masterAccounts.Service.getId(sendCoin.to)
        val from=masterAccounts.Service.getId(sendCoin.from)
        for{
          to<-to
          from<-from
        }yield (to,from)
      }
      (for{
        _<-markTransactionFailed
        sendCoin<-sendCoin
        (to,from)<-addresses(sendCoin)
      }yield{
        utilitiesNotification.send(to, constants.Notification.FAILURE, message)
        utilitiesNotification.send(from, constants.Notification.FAILURE, message)
      }).recover{
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }


    def onSuccess(ticketID: String, blockResponse: BlockResponse) = {
     /* try {
        Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
        val sendCoin = Service.getTransaction(ticketID)
        blockchainAccounts.Service.markDirty(sendCoin.to)
        blockchainAccounts.Service.markDirty(sendCoin.from)
        val toAccount = masterAccounts.Service.getAccountByAddress(sendCoin.to)
        if (toAccount.userType == constants.User.UNKNOWN) {
          masterAccounts.Service.updateUserType(toAccount.id, constants.User.USER)
        }
        utilitiesNotification.send(toAccount.id, constants.Notification.SUCCESS, blockResponse.txhash)
        utilitiesNotification.send(masterAccounts.Service.getId(sendCoin.from), constants.Notification.SUCCESS, blockResponse.txhash)
      }
      catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }*/

      val markTransactionSuccessful=Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val sendCoin = Service.getTransaction(ticketID)
      def markDirty(sendCoin:SendCoin)={
        val markDirtyTo=blockchainAccounts.Service.markDirty(sendCoin.to)
        val markDirtyFrom=blockchainAccounts.Service.markDirty(sendCoin.from)
        for{
          _<-markDirtyTo
          _<-markDirtyFrom
        }yield {}
      }
      def toAccount(sendCoin:SendCoin)=masterAccounts.Service.getAccountByAddress(sendCoin.to)
      def updateUserType(toAccount:Account)={
        if (toAccount.userType == constants.User.UNKNOWN) {
          masterAccounts.Service.updateUserType(toAccount.id, constants.User.USER)
        }else Future{Unit}
      }
      def fromId(sendCoin:SendCoin)=masterAccounts.Service.getId(sendCoin.from)
      (for{
        _<-markTransactionSuccessful
        sendCoin<-sendCoin
        _<-markDirty(sendCoin)
        toAccount<-toAccount(sendCoin)
        _<-updateUserType(toAccount)
        fromId<-fromId(sendCoin)
      }yield{
        utilitiesNotification.send(toAccount.id, constants.Notification.SUCCESS, blockResponse.txhash)
        utilitiesNotification.send(fromId, constants.Notification.SUCCESS, blockResponse.txhash)
      }).recover{
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }
    }

  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure)
    }(schedulerExecutionContext)
  }
}