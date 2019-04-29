package models.blockchainTransaction

import akka.actor.ActorSystem
import exceptions.BaseException
import javax.inject.Inject
import models.masterTransaction.FaucetRequests
import models.{blockchain, master}
import org.postgresql.util.PSQLException
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import slick.jdbc.JdbcProfile
import transactions.{GetAccount, GetResponse}
import utilities.PushNotifications

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class SendCoin(from: String, to: String, amount: Int, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class SendCoins @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, transactionSendCoin: transactions.SendCoin, getResponse: GetResponse, actorSystem: ActorSystem, implicit val pushNotifications: PushNotifications, implicit val masterAccounts: master.Accounts, implicit val blockchainAccounts: blockchain.Accounts, implicit val faucetRequests: FaucetRequests, implicit val getAccount: GetAccount)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.BLOCKCHAIN_TRANSACTION_SEND_COIN

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

  private def getAddressByTicketID(ticketID: String)(implicit executionContext: ExecutionContext): Future[String] = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.to).result.head)

  private def getTicketIDsWithEmptyTxHash()(implicit executionContext: ExecutionContext): Future[Seq[String]] = db.run(sendCoinTable.filter(_.txHash.?.isEmpty).map(_.ticketID).result)

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

  //object Iterator {
  if (configuration.get[Boolean]("blockchain.kafka.enabled")) {
    actorSystem.scheduler.schedule(initialDelay = configuration.get[Int]("blockchain.kafka.transactionIterator.initialDelay").seconds, interval = configuration.get[Int]("blockchain.kafka.transactionIterator.interval").second) {
      utilities.TicketIterator.start_(Service.getTicketIDs, transactionSendCoin.Service.getTxHashFromWSResponse, Utility.onSuccess)
    }
  }

  //}


  object Service {

    def addSendCoin(from: String, to: String, amount: Int, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(SendCoin(from = from, to = to, amount = amount, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def addSendCoinKafka(from: String, to: String, amount: Int, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])(implicit executionContext: ExecutionContext): String = Await.result(add(SendCoin(from = from, to = to, amount = amount, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def updateTxHash(ticketID: String, txHash: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateTxHashOnTicketID(ticketID, Option(txHash)), Duration.Inf)

    def updateResponseCode(ticketID: String, responseCode: String)(implicit executionContext: ExecutionContext): Int = Await.result(updateResponseCodeOnTicketID(ticketID, responseCode), Duration.Inf)

    def updateStatus(ticketID: String, status: Boolean)(implicit executionContext: ExecutionContext): Int = Await.result(updateStatusOnTicketID(ticketID, status), Duration.Inf)

    def getTicketIDs()(implicit executionContext: ExecutionContext): Seq[String] = Await.result(getTicketIDsWithEmptyTxHash(), Duration.Inf)

    def getAddress(ticketID: String)(implicit executionContext: ExecutionContext): String = Await.result(getAddressByTicketID(ticketID), Duration.Inf)

    def getTransaction(ticketID: String)(implicit executionContext: ExecutionContext): SendCoin = Await.result(findByTicketID(ticketID), Duration.Inf)

  }

  object Utility {
    def onSuccess(ticketID: String, txHash: String): Unit = {
      try {
        Service.updateTxHash(ticketID, txHash)
        val sendCoin = Service.getTransaction(ticketID)
        faucetRequests.Service.updateStatusAndGasOnTicketID(ticketID, status = true, sendCoin.gas)

        blockchainAccounts.Service.updateDirtyBit(sendCoin.to, dirtyBit = true)
        blockchainAccounts.Service.updateDirtyBit(masterAccounts.Service.getAddress(sendCoin.from), dirtyBit = true)

        val toAccount = masterAccounts.Service.getAccountByAddress(sendCoin.to)
        if (toAccount.userType == constants.User.UNKNOWN) {
          masterAccounts.Service.updateUserType(toAccount.id, constants.User.USER)
        }
        pushNotifications.sendNotification(toAccount.id, constants.Notification.SUCCESS, Seq(sendCoin.txHash.get))
        pushNotifications.sendNotification(sendCoin.from, constants.Notification.SUCCESS, Seq(sendCoin.txHash.get))
      }
      catch {
        case psqlException: PSQLException => logger.error(constants.Error.PSQL_EXCEPTION, psqlException)
          throw new BaseException(constants.Error.PSQL_EXCEPTION)
      }
    }
  }
}