package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.blockchain.TraderFeedbackHistories
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.GetResponse
import transactions.responses.TransactionResponse.Response
import utilities.PushNotifications

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SetSellerFeedback(from: String, to: String, pegHash: String, rating: Int, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class SetSellerFeedbacks @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, transactionSetSellerFeedback: transactions.SetSellerFeedback, getResponse: GetResponse, actorSystem: ActorSystem, pushNotifications: PushNotifications, masterAccounts: master.Accounts, blockchainAccounts: blockchain.Accounts, traderFeedbackHistories: TraderFeedbackHistories)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SET_SELLER_FEEDBACK

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val setSellerFeedbackTable = TableQuery[SetSellerFeedbackTable]

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")
  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")


  private def add(setSellerFeedback: SetSellerFeedback)(implicit executionContext: ExecutionContext): Future[String] = db.run((setSellerFeedbackTable returning setSellerFeedbackTable.map(_.ticketID) += setSellerFeedback).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def upsert(setSellerFeedback: SetSellerFeedback)(implicit executionContext: ExecutionContext): Future[Int] = db.run(setSellerFeedbackTable.insertOrUpdate(setSellerFeedback).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[SetSellerFeedback] = db.run(setSellerFeedbackTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashStatusAndResponseCodeOnTicketID(ticketID: String, txHash: String, status: Boolean, responseCode: String)(implicit executionContext: ExecutionContext): Future[Int] = db.run(setSellerFeedbackTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status, x.responseCode)).update(txHash, status, responseCode).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseOnTicketID(ticketID: String, status: Boolean, responseCode: String): Future[Int] = db.run(setSellerFeedbackTable.filter(_.ticketID === ticketID).map(x => (x.status, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus()(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run(setSellerFeedbackTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(setSellerFeedbackTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SetSellerFeedbackTable(tag: Tag) extends Table[SetSellerFeedback](tag, "SetSellerFeedback") {

    def * = (from, to, pegHash, rating, gas, status.?, txHash.?, ticketID, responseCode.?) <> (SetSellerFeedback.tupled, SetSellerFeedback.unapply)

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

    def addSetSellerFeedback(from: String, to: String, pegHash: String, rating: Int, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(SetSellerFeedback(from = from, to = to, pegHash = pegHash, rating = rating, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus(), Duration.Inf)

    def getSetSellerFeedbackOnTicketID(ticketID: String): SetSellerFeedback = Await.result(findByTicketID(ticketID), Duration.Inf)

    def markTransactionSuccessful(ticketID: String, txHash: String, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseCodeOnTicketID(ticketID, txHash, status = true, responseCode), Duration.Inf)

    def markTransactionFailed(ticketID: String, responseCode: String): Int = Await.result(updateStatusAndResponseOnTicketID(ticketID, status = false, responseCode), Duration.Inf)

  }

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.markTransactionSuccessful(ticketID, response.TxHash, response.Code)
        val setSellerFeedback = Service.getSetSellerFeedbackOnTicketID(ticketID)
        val fromAddress = masterAccounts.Service.getAddress(setSellerFeedback.from)
        traderFeedbackHistories.Service.addTraderFeedbackHistory(setSellerFeedback.to, setSellerFeedback.to, fromAddress, setSellerFeedback.pegHash, setSellerFeedback.rating.toString)
        blockchainAccounts.Service.markDirty(fromAddress)
        pushNotifications.sendNotification(masterAccounts.Service.getId(setSellerFeedback.to), constants.Notification.SUCCESS, response.TxHash)
        pushNotifications.sendNotification(setSellerFeedback.from, constants.Notification.SUCCESS, response.TxHash)
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
          throw new BaseException(constants.Error.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.markTransactionFailed(ticketID, message)
        val setSellerFeedback = Service.getSetSellerFeedbackOnTicketID(ticketID)
        pushNotifications.sendNotification(masterAccounts.Service.getId(setSellerFeedback.to), constants.Notification.FAILURE, message)
        pushNotifications.sendNotification(setSellerFeedback.from, constants.Notification.FAILURE, message)
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
      }
    }
  }


  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start(Service.getTicketIDsOnStatus, transactionSetSellerFeedback.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }

}