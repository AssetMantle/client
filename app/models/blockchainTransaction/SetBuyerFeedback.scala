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
import utilities.PushNotifications

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}
import transactions.responses.TransactionResponse.Response

case class SetBuyerFeedback(from: String, to: String, pegHash: String, rating: Int, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class SetBuyerFeedbacks @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, blockchainFeedbacks: blockchain.Feedbacks, transactionSetBuyerFeedback: transactions.SetBuyerFeedback, actorSystem: ActorSystem, implicit val pushNotifications: PushNotifications, implicit val blockchainAccounts: blockchain.Accounts, implicit val masterAccounts: master.Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext)  {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SET_BUYER_FEEDBACK

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val setBuyerFeedbackTable = TableQuery[SetBuyerFeedbackTable]

  private def add(setBuyerFeedback: SetBuyerFeedback)(implicit executionContext: ExecutionContext): Future[String] = db.run((setBuyerFeedbackTable returning setBuyerFeedbackTable.map(_.ticketID) += setBuyerFeedback).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def update(setBuyerFeedback: SetBuyerFeedback)(implicit executionContext: ExecutionContext): Future[Int] = db.run(setBuyerFeedbackTable.insertOrUpdate(setBuyerFeedback).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[SetBuyerFeedback] = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusAndResponseOnTicketID(ticketID: String, status: Boolean, responseCode: String): Future[Int] = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).map(x => (x.status, x.responseCode)).update((status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithNullStatus()(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run(setBuyerFeedbackTable.filter(_.status.?.isEmpty).map(_.ticketID).result)

  private def updateTxHashStatusAndResponseOnTicketID(ticketID: String, txHash: String, status: Boolean, responseCode: String): Future[Int] = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).map(x => (x.txHash, x.status, x.responseCode)).update((txHash, status, responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(setBuyerFeedbackTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
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

    def addSetBuyerFeedback(from: String, to: String, pegHash: String, rating: Int, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(SetBuyerFeedback(from = from , to = to, pegHash = pegHash, rating = rating, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def updateTxHashStatusResponseCode(ticketID: String, txHash: String, status: Boolean, responseCode: String): Int = Await.result(updateTxHashStatusAndResponseOnTicketID(ticketID, txHash, status, responseCode), Duration.Inf)

    def updateStatusAndResponseCode(ticketID: String, status: Boolean, responseCode: String): Int = Await.result(updateStatusAndResponseOnTicketID(ticketID, status, responseCode), Duration.Inf)

    def getTicketIDsOnStatus(): Seq[String] = Await.result(getTicketIDsWithNullStatus(), Duration.Inf)

    def getTransaction(ticketID: String)(implicit executionContext: ExecutionContext): SetBuyerFeedback = Await.result(findByTicketID(ticketID), Duration.Inf)
    
  }

  private val schedulerInitialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds
  private val schedulerInterval =  configuration.get[Int]("blockchain.kafka.transactionIterator.interval").seconds
  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  object Utility {
    def onSuccess(ticketID: String, response: Response): Future[Unit] = Future {
      try {
        Service.updateTxHashStatusResponseCode(ticketID, response.TxHash, status = true, response.Code)
        val setBuyerFeedback = Service.getTransaction(ticketID)
        blockchainFeedbacks.Service.updateDirtyBit(setBuyerFeedback.to, dirtyBit = true)
        blockchainAccounts.Service.updateDirtyBit(masterAccounts.Service.getAddress(setBuyerFeedback.from), dirtyBit = true)

        pushNotifications.sendNotification(masterAccounts.Service.getId(setBuyerFeedback.to), constants.Notification.SUCCESS, Seq(response.TxHash))
        pushNotifications.sendNotification(setBuyerFeedback.from, constants.Notification.SUCCESS, Seq(response.TxHash))
      }
      catch {
        case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
          throw new BaseException(constants.Error.PSQL_EXCEPTION)
      }
    }

    def onFailure(ticketID: String, message: String): Future[Unit] = Future {
      try {
        Service.updateStatusAndResponseCode(ticketID, status = false, message)
        val setBuyerFeedback = Service.getTransaction(ticketID)
        pushNotifications.sendNotification(masterAccounts.Service.getId(setBuyerFeedback.to), constants.Notification.FAILURE, Seq(message))
        pushNotifications.sendNotification(setBuyerFeedback.from, constants.Notification.FAILURE, Seq(message))
      } catch {
        case baseException: BaseException => logger.error(constants.Error.BASE_EXCEPTION, baseException)
      }
    }
  }

  //scheduler iterates with rows with null as status
  if (kafkaEnabled) {
    actorSystem.scheduler.schedule(initialDelay = schedulerInitialDelay, interval = schedulerInterval) {
      utilities.TicketUpdater.start_(Service.getTicketIDsOnStatus, transactionSetBuyerFeedback.Service.getTxFromWSResponse, Utility.onSuccess, Utility.onFailure)
    }
  }
  
}