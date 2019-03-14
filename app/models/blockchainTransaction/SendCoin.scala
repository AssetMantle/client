package models.blockchainTransaction

import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import slick.jdbc.JdbcProfile
import transactions.GetResponse
import javax.inject.Inject
import akka.actor.ActorSystem
import exceptions.BaseException
import org.postgresql.util.PSQLException

import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendCoin(from: String, to: String, amount: Int, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class SendCoins @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, transactionSendCoin: transactions.SendCoin, getResponse: GetResponse, actorSystem: ActorSystem)(implicit  wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.Blockchain_TRANSACTION_SEND_COIN

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sendCoinTable = TableQuery[SendCoinTable]

  private def add(sendCoin: SendCoin)(implicit executionContext: ExecutionContext): Future[String] = db.run((sendCoinTable returning sendCoinTable.map(_.ticketID) += sendCoin).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def update(sendCoin: SendCoin)(implicit executionContext: ExecutionContext): Future[Int] = db.run(sendCoinTable.insertOrUpdate(sendCoin).asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
        throw new BaseException(constants.Error.PSQL_EXCEPTION)
    }
  }

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String])(implicit executionContext: ExecutionContext) = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.txHash.?).update(txHash))

  private def updateResponseCodeOnTicketID(ticketID: String, responseCode: String)(implicit executionContext: ExecutionContext) = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.responseCode.?).update(Option(responseCode)))

  private def updateStatusOnTicketID(ticketID: String, status: Boolean)(implicit executionContext: ExecutionContext) = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.status.?).update(Option(status)))

  private def findByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[SendCoin] = db.run(sendCoinTable.filter(_.ticketID === ticketID).result.head.asTry).map {
    case Success(result) => result
    case Failure(exception) => exception match {
      case noSuchElementException: NoSuchElementException => logger.error(constants.Error.NO_SUCH_ELEMENT_EXCEPTION, noSuchElementException)
        throw new BaseException(constants.Error.NO_SUCH_ELEMENT_EXCEPTION)
    }
  }

  private def getTicketIDsWithEmptyTxHash()(implicit executionContext: ExecutionContext):Future[Seq[String]] = db.run(sendCoinTable.filter(_.txHash.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String)(implicit executionContext: ExecutionContext) = db.run(sendCoinTable.filter(_.ticketID === ticketID).delete)

  private[models] class SendCoinTable(tag: Tag) extends Table[SendCoin](tag, "SendCoin") {

    def * = (from, to, amount, gas, status.?, txHash.?, ticketID, responseCode.?) <> (SendCoin.tupled, SendCoin.unapply)

    def from = column[String]("from")

    def to = column[String]("to")

    def amount = column[Int]("amount")

    def gas = column[Int]("gas")

    def status = column[Boolean]("status")

    def txHash = column[String]("txHash")

    def ticketID = column[String]("ticketID", O.PrimaryKey)

    def responseCode = column[String]("responseCode")
  }

  if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
    actorSystem.scheduler.schedule(initialDelay = configuration.get[Int]("blockchain.kafka.ticketIterator.initialDelay").seconds, interval = configuration.get[Int]("blockchain.kafka.ticketIterator.interval").second) {
      utilities.TicketIterator.start(Service.geTicketIDsWithEmptyTxHash, transactionSendCoin.Service.getTxHashFromWSResponse, Service.updateTxHash)
    }
  }

  object Service {

    def addSendCoin(from: String, to: String, amount: Int, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(SendCoin(from = from, to = to, amount = amount, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def addSendCoinKafka(from: String, to: String, amount: Int, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(SendCoin(from = from, to = to, amount = amount, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def updateTxHash(ticketID: String, txHash: String) (implicit executionContext: ExecutionContext): Int = Await.result(updateTxHashOnTicketID(ticketID, Option(txHash)),Duration.Inf)

    def updateResponseCode(ticketID: String, responseCode: String) (implicit executionContext: ExecutionContext): Int = Await.result(updateResponseCodeOnTicketID(ticketID, responseCode), Duration.Inf)

    def updateStatus(ticketID: String, status: Boolean) (implicit executionContext: ExecutionContext): Int = Await.result(updateStatusOnTicketID(ticketID, status), Duration.Inf)

    def geTicketIDsWithEmptyTxHash()(implicit executionContext: ExecutionContext): Seq[String] = Await.result(getTicketIDsWithEmptyTxHash(), Duration.Inf)

  }
}