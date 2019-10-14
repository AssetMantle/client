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

case class SetBuyerFeedback(from: String, to: String, pegHash: String, rating: Int, gas: Int, status: Option[Boolean] = None, txHash: Option[String] = None, ticketID: String, mode: String, code: Option[String] = None) extends BaseTransaction[SetBuyerFeedback] {
  def mutateTicketID(newTicketID: String): SetBuyerFeedback = SetBuyerFeedback(from = from, to = to, pegHash = pegHash, rating = rating, gas = gas, status = status, txHash = txHash, ticketID = newTicketID, mode = mode, code = code)
}


@Singleton
class SetBuyerFeedbacks @Inject()(actorSystem: ActorSystem, transaction: utilities.Transaction, protected val databaseConfigProvider: DatabaseConfigProvider, transactionSetBuyerFeedback: transactions.SetBuyerFeedback, utilitiesNotification: utilities.Notification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts, blockchainTraderFeedbackHistories: blockchain.TraderFeedbackHistories)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SET_BUYER_FEEDBACK

  private implicit val logger: Logger = Logger(this.getClass)

  private val schedulerExecutionContext:ExecutionContext= actorSystem.dispatchers.lookup("akka.actors.scheduler-dispatcher")

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val setBuyerFeedbackTable = TableQuery[SetBuyerFeedbackTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private def add(setBuyerFeedback: SetBuyerFeedback): Future[String] = db.run((setBuyerFeedbackTable returning setBuyerFeedbackTable.map(_.ticketID) += setBuyerFeedback).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(setBuyerFeedback: SetBuyerFeedback): Future[Int] = db.run(setBuyerFeedbackTable.insertOrUpdate(setBuyerFeedback).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String): Future[SetBuyerFeedback] = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findTransactionHashByTicketID(ticketID: String): Future[Option[String]] = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).map(_.txHash.?).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findModeByTicketID(ticketID: String): Future[String] = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).map(_.mode).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndCodeOnTicketID(ticketID: String, status: Option[Boolean], code: String): Future[Int] = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.code)).update((status, code)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashAndStatusOnTicketID(ticketID: String, txHash: Option[String], status: Option[Boolean]): Future[Int] = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).map(x => (x.txHash.?, x.status.?)).update(txHash, status).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(setBuyerFeedbackTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String) = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]): Future[Int] = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).map(x => x.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SetBuyerFeedbackTable(tag: Tag) extends Table[SetBuyerFeedback](tag, "SetBuyerFeedback") {

    def * = (from, to, pegHash, rating, gas, status.?, txHash.?, ticketID, mode, code.?) <> (SetBuyerFeedback.tupled, SetBuyerFeedback.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def pegHash = column[String]("pegHash")

    def rating = column[Int]("rating")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def mode = column[String]("mode")

    def code = column[String]("code")
  }

  object Service {

    def create(setBuyerFeedback: SetBuyerFeedback): Future[String] = add(SetBuyerFeedback(from = setBuyerFeedback.from, to = setBuyerFeedback.to, pegHash = setBuyerFeedback.pegHash, rating = setBuyerFeedback.rating, gas = setBuyerFeedback.gas, status = setBuyerFeedback.status, txHash = setBuyerFeedback.txHash, ticketID = setBuyerFeedback.ticketID, mode = setBuyerFeedback.mode, code = setBuyerFeedback.code))

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def getTransaction(ticketID: String): Future[SetBuyerFeedback] = findByTicketID(ticketID)

    def markTransactionSuccessful(ticketID: String, txHash: String): Future[Int] =updateTxHashAndStatusOnTicketID(ticketID, Option(txHash), status = Option(true))

    def markTransactionFailed(ticketID: String, code: String): Future[Int] = updateStatusAndCodeOnTicketID(ticketID, status = Option(false), code)

    def getTransactionHash(ticketID: String): Option[String] = Await.result(findTransactionHashByTicketID(ticketID), Duration.Inf)

    def getMode(ticketID: String): String = Await.result(findModeByTicketID(ticketID), Duration.Inf)

    def updateTransactionHash(ticketID: String, txHash: String):Future[Int] = updateTxHashOnTicketID(ticketID = ticketID, txHash = Option(txHash))

  }

  object Utility {
    def addresses(setBuyerFeedback:SetBuyerFeedback)={
      val to=masterAccounts.Service.getId(setBuyerFeedback.to)
      val from=masterAccounts.Service.getId(setBuyerFeedback.from)
      for{
        to<-to
        from<-from
      }yield (to,from)
    }

    def onSuccess(ticketID: String, blockResponse: BlockResponse): Future[Unit] =  {
     /* try {
        Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
        val setBuyerFeedback = Service.getTransaction(ticketID)
        blockchainTraderFeedbackHistories.Service.update(setBuyerFeedback.to, setBuyerFeedback.from, setBuyerFeedback.to, setBuyerFeedback.pegHash, setBuyerFeedback.rating.toString)
        blockchainAccounts.Service.markDirty(setBuyerFeedback.from)
        utilitiesNotification.send(masterAccounts.Service.getId(setBuyerFeedback.to), constants.Notification.SUCCESS, blockResponse.txhash)
        utilitiesNotification.send(masterAccounts.Service.getId(setBuyerFeedback.from), constants.Notification.SUCCESS, blockResponse.txhash)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }*/

      val markTransactionSuccessful= Service.markTransactionSuccessful(ticketID, blockResponse.txhash)
      val setBuyerFeedback = Service.getTransaction(ticketID)
       def updateAndMarkDirty(setBuyerFeedback:SetBuyerFeedback)={
        val update= blockchainTraderFeedbackHistories.Service.update(setBuyerFeedback.to, setBuyerFeedback.from, setBuyerFeedback.to, setBuyerFeedback.pegHash, setBuyerFeedback.rating.toString)
        val markDirtyFrom=blockchainAccounts.Service.markDirty(setBuyerFeedback.from)
        for {
          _<-update
          _<-markDirtyFrom
        }yield{}
      }
      (for{
        _<-markTransactionSuccessful
        setBuyerFeedback<-setBuyerFeedback
        _<-updateAndMarkDirty(setBuyerFeedback)
        (to,from)<-addresses(setBuyerFeedback)
      }yield {
        utilitiesNotification.send(to, constants.Notification.SUCCESS, blockResponse.txhash)
        utilitiesNotification.send(from, constants.Notification.SUCCESS, blockResponse.txhash)

      }).recover{
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }

    }

    def onFailure(ticketID: String, message: String): Future[Unit] =  {
      /*try {
        Service.markTransactionFailed(ticketID, message)
        val setBuyerFeedback = Service.getTransaction(ticketID)
        utilitiesNotification.send(masterAccounts.Service.getId(setBuyerFeedback.to), constants.Notification.FAILURE, message)
        utilitiesNotification.send(masterAccounts.Service.getId(setBuyerFeedback.from), constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }*/
      val markTransactionFailed= Service.markTransactionFailed(ticketID, message)
      val setBuyerFeedback = Service.getTransaction(ticketID)
      for{
        _<-markTransactionFailed
        setBuyerFeedback<-setBuyerFeedback
        (to,from)<-addresses(setBuyerFeedback)
      }yield{
        utilitiesNotification.send(to, constants.Notification.FAILURE, message)
        utilitiesNotification.send(from, constants.Notification.FAILURE, message)
      }

    }
  }

  if (kafkaEnabled || transactionMode != constants.Transactions.BLOCK_MODE) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      transaction.ticketUpdater(Service.getTicketIDsOnStatus, Service.getTransactionHash, Service.getMode, Utility.onSuccess, Utility.onFailure)
    }(schedulerExecutionContext)
  }

}