package utilities

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import models.blockchain
import models.blockchain.{Transaction => bcTransaction}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import queries.{GetResponse, GetTransaction}
import transactions.Abstract.BaseRequest
import transactions.responses.TransactionResponse.{AsyncResponse, BlockResponse, KafkaResponse, SyncResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class Transaction @Inject()(getTransaction: GetTransaction, getResponse: GetResponse, blockchainTransactions: blockchain.Transactions)(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private val responseErrorTransactionHashNotFound: String = constants.Response.PREFIX + constants.Response.FAILURE_PREFIX + configuration.get[String]("blockchain.response.error.transactionHashNotFound")

  private val awaitingKafkaResponse: String = constants.Response.PREFIX + constants.Response.FAILURE_PREFIX + configuration.get[String]("blockchain.response.error.awaitingKafkaResponse")

  def process[T1 <: BaseTransaction[T1], T2 <: BaseRequest](entity: T1, blockchainTransactionCreate: T1 => Future[String], request: T2, action: T2 => Future[WSResponse], onSuccess: (String, String) => Future[Unit], onFailure: (String, String) => Future[Unit], updateTransactionHash: (String, String) => Future[Int])(implicit module: String, logger: Logger): Future[String] = {

    val ticketID: Future[String] = if (kafkaEnabled) {
      val response = utilities.JSON.getResponseFromJson[KafkaResponse](action(request))
      for {
        response <- response
      } yield response.ticketID
    } else Future(utilities.IDGenerator.ticketID)

    def create(ticketID: String): Future[String] = blockchainTransactionCreate(entity.mutateTicketID(ticketID))

    def modeBasedAction(ticketID: String): Future[Unit] = {
      if (!kafkaEnabled) {
        transactionMode match {
          case constants.Transactions.BLOCK_MODE => val response = utilities.JSON.getResponseFromJson[BlockResponse](action(request))
            for {
              response <- response
              _ <- onSuccess(ticketID, response.txhash)
            } yield Unit
          case constants.Transactions.ASYNC_MODE => val response = utilities.JSON.getResponseFromJson[AsyncResponse](action(request))
            for {
              response <- response
              _ <- updateTransactionHash(ticketID, response.txhash)
            } yield Unit
          case constants.Transactions.SYNC_MODE => val response = utilities.JSON.getResponseFromJson[SyncResponse](action(request))
            for {
              response <- response
              _ <- updateTransactionHash(ticketID, response.txhash)
            } yield Unit
        }
      } else Future(Unit)
    }

    (for {
      ticketID <- ticketID
      _ <- create(ticketID)
      _ <- modeBasedAction(ticketID)
    } yield ticketID
      ).recoverWith {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        for {
          ticketID <- ticketID
        } yield onFailure(ticketID, baseException.failure.message)
        ticketID
    }
  }

  def ticketUpdater(getTickets: () => Future[Seq[String]], getTransactionHash: String => Future[Option[String]], getMode: String => Future[String], onSuccess: (String, String) => Future[Unit], onFailure: (String, String) => Future[Unit])(implicit module: String, logger: Logger) {
    val ticketIDsSeq: Future[Seq[String]] = getTickets()
    Thread.sleep(sleepTime)

    def getTxHash(mode: String, ticketID: String): Future[String] = {
      mode match {
        case constants.Transactions.BLOCK_MODE => utilities.JSON.getResponseFromJson[BlockResponse](getResponse.Service.get(ticketID)).map(_.txhash)
        case constants.Transactions.ASYNC_MODE => utilities.JSON.getResponseFromJson[AsyncResponse](getResponse.Service.get(ticketID)).map(_.txhash)
        case constants.Transactions.SYNC_MODE => utilities.JSON.getResponseFromJson[SyncResponse](getResponse.Service.get(ticketID)).map(_.txhash)
      }
    }

    def getTransaction(txHash: String) = blockchainTransactions.Service.tryGet(txHash)

    def executeSuccessOrFailure(tx: bcTransaction, ticketID: String): Future[Unit] = if (tx.status) onSuccess(ticketID, tx.hash) else onFailure(ticketID, tx.code.get.toString)

    def ticketsIterator(ticketIDsSeq: Seq[String]): Unit = {
      ticketIDsSeq.foreach { ticketID =>
        val tx = if (kafkaEnabled) {
          val mode = getMode(ticketID)
          for {
            mode <- mode
            txHash <- getTxHash(mode, ticketID)
            tx <- getTransaction(txHash)
          } yield tx
        } else {
          val txHash = getTransactionHash(ticketID)
          for {
            txHash <- txHash
            tx <- getTransaction(txHash.getOrElse(throw new BaseException(constants.Response.TRANSACTION_HASH_NOT_FOUND)))
          } yield tx
        }
        val forComplete = (for {
          tx <- tx
          _ <- executeSuccessOrFailure(tx, ticketID)
        } yield Unit
          ).recover {
          case baseException: BaseException =>
            //TODO
            if (baseException.failure == constants.Response.TRANSACTION_HASH_NOT_FOUND || baseException.failure.message.matches(awaitingKafkaResponse))
              logger.info(baseException.failure.message, baseException)
            else onFailure(ticketID, baseException.failure.message)
        }
        Await.result(forComplete, Duration.Inf)
      }
    }

    for {
      ticketIDsSeq <- ticketIDsSeq
    } yield ticketsIterator(ticketIDsSeq)
  }
}

