package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.Accounts
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.GetResponse
import utilities.PushNotifications

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendFiat(from: String, to: String, amount: Int, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

@Singleton
class SendFiats @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, transactionSendFiat: transactions.SendFiat, getResponse: GetResponse, actorSystem: ActorSystem, implicit val pushNotifications: PushNotifications, implicit val accounts: Accounts)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext)  {

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SEND_FIAT

  private implicit val logger: Logger = Logger(this.getClass)

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sendFiatTable = TableQuery[SendFiatTable]

  private def add(sendFiat: SendFiat)(implicit executionContext: ExecutionContext): Future[String] = db.run((sendFiatTable returning sendFiatTable.map(_.ticketID) += sendFiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def update(sendFiat: SendFiat)(implicit executionContext: ExecutionContext): Future[Int] = db.run(sendFiatTable.insertOrUpdate(sendFiat).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[SendFiat] = db.run(sendFiatTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String])(implicit executionContext: ExecutionContext) = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(_.txHash.?).update(txHash).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateResponseCodeOnTicketID(ticketID: String, responseCode: String)(implicit executionContext: ExecutionContext) = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(_.responseCode.?).update(Option(responseCode)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def updateStatusOnTicketID(ticketID: String, status: Boolean)(implicit executionContext: ExecutionContext) = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(_.status.?).update(Option(status)).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithEmptyTxHash()(implicit executionContext: ExecutionContext):Future[Seq[String]] = db.run(sendFiatTable.filter(_.txHash.?.isEmpty).map(_.ticketID).result)

  private def getAddressByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(sendFiatTable.filter(_.ticketID === ticketID).map(_.to).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(sendFiatTable.filter(_.ticketID === ticketID).delete.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private[models] class SendFiatTable(tag: Tag) extends Table[SendFiat](tag, "SendFiat") {

    def * = (from, to, amount, pegHash, gas, status.?, txHash.?, ticketID, responseCode.?) <> (SendFiat.tupled, SendFiat.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def amount = column[Int]("amount")

    def pegHash = column[String]("pegHash")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }

  if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
    actorSystem.scheduler.schedule(initialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds, interval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").second) {
      utilities.TicketIterator.start(Service.getTicketIDs, transactionSendFiat.Service.getTxHashFromWSResponse, Service.updateTxHash, Service.getAddress)
    }
  }

  object Service {

    def addSendFiat(from: String, to: String, amount: Int, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(SendFiat(from = from , to = to, amount = amount, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def addSendFiatKafka(from: String, to: String, amount: Int, pegHash: String, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(SendFiat(from = from , to = to, amount = amount, pegHash = pegHash, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def updateTxHash(ticketID: String, txHash: String) (implicit executionContext: ExecutionContext): Int = Await.result(updateTxHashOnTicketID(ticketID, Option(txHash)),Duration.Inf)

    def updateResponseCode(ticketID: String, responseCode: String) (implicit executionContext: ExecutionContext): Int = Await.result(updateResponseCodeOnTicketID(ticketID, responseCode), Duration.Inf)

    def updateStatus(ticketID: String, status: Boolean) (implicit executionContext: ExecutionContext): Int = Await.result(updateStatusOnTicketID(ticketID, status), Duration.Inf)

    def getTicketIDs()(implicit executionContext: ExecutionContext): Seq[String] = Await.result(getTicketIDsWithEmptyTxHash(), Duration.Inf)

    def getAddress(ticketID: String)(implicit executionContext: ExecutionContext): String = Await.result(getAddressByTicketID(ticketID), Duration.Inf)

  }
}