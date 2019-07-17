package models.blockchainTransaction

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.GetResponse
import transactions.responses.TransactionResponse.Response
import utilities.PushNotification
import actors.Actor
import akka.actor.ActorSystem

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SetBuyerFeedback(from: String, to: String, pegHash: String, rating: Int, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class SetBuyerFeedbacks @Inject()(actorSystem: ActorSystem, protected val databaseConfigProvider: DatabaseConfigProvider, transactionSetBuyerFeedback: transactions.SetBuyerFeedback, getResponse: GetResponse, pushNotification: PushNotification, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts, blockchainTraderFeedbackHistories: blockchain.TraderFeedbackHistories)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SET_BUYER_FEEDBACK

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val setBuyerFeedbackTable = TableQuery[SetBuyerFeedbackTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private def add(setBuyerFeedback: SetBuyerFeedback)(implicit executionContext: ExecutionContext): Future[String] = db.run((setBuyerFeedbackTable returning setBuyerFeedbackTable.map(_.ticketID) += setBuyerFeedback).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
    }
  }

  private def upsert(setBuyerFeedback: SetBuyerFeedback)(implicit executionContext: ExecutionContext): Future[Int] = db.run(setBuyerFeedbackTable.insertOrUpdate(setBuyerFeedback).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[SetBuyerFeedback] = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseCodeOnTicketID(ticketID: String, status: Option[Boolean], responseCode: String): Future[Int] = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).map(x => (x.status.?, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashStatusAndResponseCodeOnTicketID(ticketID: String, txHash: String, status: Option[Boolean], responseCode: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status.?, x.responseCode)).update(txHash, status, responseCode).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus: Future[Seq[String]] = db.run(setBuyerFeedbackTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Response.PSQL_EXCEPTION.message, psqlException)
        throw new BaseException(constants.Response.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Response.NO_SUCH_ELEMENT_EXCEPTION.message, noSuchElementException)
        throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SetBuyerFeedbackTable(tag: Tag) extends Table[SetBuyerFeedback](tag, "SetBuyerFeedback") {

    def * = (from, to, pegHash, rating, gas, status.?, txHash.?, ticketID, responseCode.?) <> (SetBuyerFeedback.tupled, SetBuyerFeedback.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def pegHash = column[String]("pegHash")

    def rating = column[Int]("rating")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }

  object Service {

    def create(from: String, to: String, pegHash: String, rating: Int, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(SetBuyerFeedback(from = from, to = to, pegHash = pegHash, rating = rating, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus, Duration.Inf)

    def getTransaction(ticketID: String): SetBuyerFeedback = Await.result(findByTicketID(ticketID), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseCodeOnTicketID(ticketID, txHash, status = Option(true), responseCode), Duration.Inf)

    def markTransactionFailed(ticketID: String, responseCode: String): Int = Await.result(updateStatusAndResponseCodeOnTicketID(ticketID, status = Option(false), responseCode), Duration.Inf)

  }

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, response.TxHash, response.Code)
        val setBuyerFeedback = Service.getTransaction(ticketID)
        val fromAddress = masterAccounts.Service.getAddress(setBuyerFeedback.from)
        blockchainTraderFeedbackHistories.Service.create(setBuyerFeedback.to, fromAddress, setBuyerFeedback.to, setBuyerFeedback.pegHash, setBuyerFeedback.rating.toString)
        blockchainAccounts.Service.markDirty(fromAddress)
        pushNotification.sendNotification(masterAccounts.Service.getId(setBuyerFeedback.to), constants.Notification.SUCCESS, response.TxHash)
        pushNotification.sendNotification(setBuyerFeedback.from, constants.Notification.SUCCESS, response.TxHash)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
          throw new BaseException(constants.Response.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val setBuyerFeedback = Service.getTransaction(ticketID)
        pushNotification.sendNotification(masterAccounts.Service.getId(setBuyerFeedback.to), constants.Notification.FAILURE, message)
        pushNotification.sendNotification(setBuyerFeedback.from, constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start(Service.getTicketIDsOnStatus, transactionSetBuyerFeedback.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }

}