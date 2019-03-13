package models.blockchainTransaction

import play.api.{Configuration, Logger}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient
import slick.jdbc.JdbcProfile
import transactions.GetResponse

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

case class SendCoin(from: String, to: String, amount: Int, gas: Int,  status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String])

class SendCoins @Inject()(protected val databaseConfigProvider: DatabaseConfigProvider, transactionSendCoin: transactions.SendCoin, getResponse: GetResponse, actorSystem: ActorSystem)(implicit  wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext) {

  val databaseConfig = databaseConfigProvider.get[JdbcProfile]

  private implicit val logger: Logger = Logger(this.getClass)

  val db = databaseConfig.db

  import databaseConfig.profile.api._

  private[models] val sendCoinTable = TableQuery[SendCoinTable]

  private def add(sendCoin: SendCoin): Future[String] = db.run(sendCoinTable returning sendCoinTable.map(_.ticketID) += sendCoin)

  private def update(sendCoin: SendCoin): Future[Int] = db.run(sendCoinTable.insertOrUpdate(sendCoin))

  private def updateTxHashOnTicketID(ticketID: String, txHash: Option[String]) = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.txHash.?).update(txHash))

  private def updateResponseCodeOnTicketID(ticketID: String, responseCode: String) = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.responseCode.?).update(Option(responseCode)))

  private def updateStatusOnTicketID(ticketID: String, status: Boolean) = db.run(sendCoinTable.filter(_.ticketID === ticketID).map(_.status.?).update(Option(status)))

  private def findByTicketID(ticketID: String): Future[SendCoin] = db.run(sendCoinTable.filter(_.ticketID === ticketID).result.head)

  private def getTicketIDsWithEmptyTxHash() = db.run(sendCoinTable.filter(_.txHash.?.isEmpty).map(_.ticketID).result)

  private def deleteByTicketID(ticketID: String) = db.run(sendCoinTable.filter(_.ticketID === ticketID).delete)

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

  if (configuration.get[Boolean]("blockchain.kafkaEnabled")) {
    actorSystem.scheduler.schedule(initialDelay = 0.seconds, interval = 1.second) {
      utilities.TransactionHashIterator.start(Service.geTicketIDsWithEmptyTxHash, transactionSendCoin.Service.getTxHashFromWSResponse, Service.updateTxHash)(wsClient, configuration, executionContext, logger)
    }
  }

  object Service {

    def addSendCoin(from: String, to: String, amount: Int, gas: Int, status: Option[Boolean], txHash: Option[String], ticketID: String, responseCode: Option[String]) (implicit executionContext: ExecutionContext): String = Await.result(add(SendCoin(from = from, to = to, amount = amount, gas = gas, status = status, txHash = txHash, ticketID = ticketID, responseCode = responseCode)), Duration.Inf)

    def updateTxHash(ticketID: String, txHash: String) (implicit executionContext: ExecutionContext): Int = Await.result(updateTxHashOnTicketID(ticketID, Option(txHash)),Duration.Inf)

    def updateResponseCode(ticketID: String, responseCode: String) (implicit executionContext: ExecutionContext): Int = Await.result(updateResponseCodeOnTicketID(ticketID, responseCode), Duration.Inf)

    def updateStatus(ticketID: String, status: Boolean) (implicit executionContext: ExecutionContext): Int = Await.result(updateStatusOnTicketID(ticketID, status), Duration.Inf)

    def geTicketIDsWithEmptyTxHash()(implicit executionContext: ExecutionContext): Seq[String] = Await.result(getTicketIDsWithEmptyTxHash(), Duration.Inf)

  }
}