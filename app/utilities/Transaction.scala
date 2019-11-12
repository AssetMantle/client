package utilities

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import queries.{GetResponse, GetTransactionHashResponse}
import transactions.Abstract.BaseRequest
import transactions.responses.TransactionResponse
import transactions.responses.TransactionResponse.{AsyncResponse, BlockResponse, KafkaResponse, SyncResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Transaction @Inject()(getTxHashResponse: GetTransactionHashResponse, getResponse: GetResponse)(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private val responseErrorTransactionHashNotFound: String = constants.Response.PREFIX + constants.Response.FAILURE_PREFIX + configuration.get[String]("blockchain.response.error.transactionHashNotFound")

  def process[T1 <: BaseTransaction[T1], T2 <: BaseRequest](entity: T1, blockchainTransactionCreate: T1 => Future[String], request: T2, action: T2 => Future[WSResponse], onSuccess: (String, BlockResponse) => Future[Unit], onFailure: (String, String) => Future[Unit], updateTransactionHash: (String, String) => Future[Int])(implicit module: String, logger: Logger): Future[String] = {

    val ticketID: Future[String] = if (kafkaEnabled) utilities.JSON.getResponseFromJson[KafkaResponse](action(request)).map(res => res.ticketID) else Future {
      utilities.IDGenerator.ticketID
    }

    def create(ticketID: String) = blockchainTransactionCreate(entity.mutateTicketID(ticketID))

    def processMatchAction(ticketID: String) = {
      if (!kafkaEnabled) {
        transactionMode match {
          case constants.Transactions.BLOCK_MODE => val responseFromJson = utilities.JSON.getResponseFromJson[BlockResponse](action(request))
            for {
              blockResponse <- responseFromJson
              _ <- onSuccess(ticketID, blockResponse)
            } yield {}
          case constants.Transactions.ASYNC_MODE => val responseFromJson = utilities.JSON.getResponseFromJson[AsyncResponse](action(request))
            for {
              asyncResponse <- responseFromJson
              _ <- updateTransactionHash(ticketID, asyncResponse.txhash)
            } yield {}
          case constants.Transactions.SYNC_MODE => val responseFromJson = utilities.JSON.getResponseFromJson[SyncResponse](action(request))
            for {
              syncResponse <- responseFromJson
              _ <- updateTransactionHash(ticketID, syncResponse.txhash)
            } yield {}
        }
      } else Future {
        Unit
      }
    }

    (for {
      ticketID <- ticketID
      _ <- create(ticketID)
      _ <- processMatchAction(ticketID)
    } yield {}
      ).recover {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        ticketID.map { ticketID =>
          onFailure(ticketID, baseException.failure.message)
        }
    }
    ticketID
  }

  def ticketUpdater(getTickets: () => Future[Seq[String]], getTransactionHash: String => Future[Option[String]], getMode: String => Future[String], onSuccess: (String, BlockResponse) => Future[Unit], onFailure: (String, String) => Future[Unit])(implicit module: String, logger: Logger) {

    val ticketIDsSeq: Future[Seq[String]] = getTickets()

    def getBlockResponse(mode: String, ticketID: String) = {
      mode match {
        case constants.Transactions.BLOCK_MODE =>
          utilities.JSON.getResponseFromJson[BlockResponse](getResponse.Service.get(ticketID))
        case constants.Transactions.ASYNC_MODE =>
          val transactionResponse = utilities.JSON.getResponseFromJson[AsyncResponse](getResponse.Service.get(ticketID))

          def jsonResponse(transactionResponse: TransactionResponse.AsyncResponse) = utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(transactionResponse.txhash))

          for {
            transactionResponse <- transactionResponse
            jsonResponse <- jsonResponse(transactionResponse)
          } yield jsonResponse
        case constants.Transactions.SYNC_MODE =>
          val transactionResponse = utilities.JSON.getResponseFromJson[SyncResponse](getResponse.Service.get(ticketID))

          def jsonResponse(transactionResponse: TransactionResponse.SyncResponse) = utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(transactionResponse.txhash))

          for {
            transactionResponse <- transactionResponse
            jsonResponse <- jsonResponse(transactionResponse)
          } yield jsonResponse
      }
    }

    def successOrFailure(blockResponse: BlockResponse, ticketID: String) = if (blockResponse.code.isEmpty) onSuccess(ticketID, blockResponse) else onFailure(ticketID, blockResponse.code.get.toString)

    def responseSuccessFaliure(ticketIDsSeq: Seq[String]) = Future.sequence {
      ticketIDsSeq.map { ticketID =>
        val blockResponse = if (kafkaEnabled) {
          val mode = getMode(ticketID)
          for {
            mode <- mode
            blockResponse <- getBlockResponse(mode, ticketID)
          } yield blockResponse
        } else {
          val transactionHash = getTransactionHash(ticketID)

          def jsonResponse(transactionHash: Option[String]) = utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(transactionHash.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION))))

          for {
            transactionHash <- transactionHash
            jsonResponse <- jsonResponse(transactionHash)
          } yield jsonResponse
        }

        (for {
          blockResponse <- blockResponse
          _ <- successOrFailure(blockResponse, ticketID)
        } yield {}
          ).recover {
          case baseException: BaseException => if (!baseException.failure.message.matches(responseErrorTransactionHashNotFound)) onFailure(ticketID, baseException.failure.message) else logger.info(baseException.failure.message, baseException)
        }
      }
    }

    for {
      ticketIDsSeq <- ticketIDsSeq
      _ <- responseSuccessFaliure(ticketIDsSeq)
    } yield {}

  }
}

