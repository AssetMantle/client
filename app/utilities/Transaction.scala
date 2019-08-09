package utilities

import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.responses.TransactionResponse.{AsyncResponse, BlockResponse, KafkaResponse, SyncResponse}
import transactions.{GetResponse, GetTxHashResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

abstract class TransactionEntity[T] {
  def mutateTicketID(ticketID: String): T
}

abstract class RequestEntity {

}

@Singleton
class Transaction @Inject()(getTxHashResponse: GetTxHashResponse, getResponse: GetResponse)(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.UTILITIES_TRANSACTION

  def process[T1 <: TransactionEntity[T1], T2 <: RequestEntity](entity: T1, blockchainTransactionCreate: T1 => String, request: T2, kafkaAction: T2 => KafkaResponse, blockAction: T2 => BlockResponse, asyncAction: T2 => AsyncResponse, syncAction: T2 => SyncResponse, onSuccess: (String, BlockResponse) => Unit, onFailure: (String, String) => Unit, updateTransactionHash:(String, String) => Int): String = {
    try {
      val ticketID: String = if (kafkaEnabled) kafkaAction(request).ticketID else Random.nextString(32)
      blockchainTransactionCreate(entity.mutateTicketID(ticketID))
      if (!kafkaEnabled) {
        Future {
          try {
            transactionMode match {
              case constants.Transactions.BLOCK_MODE => onSuccess(ticketID, blockAction(request))
              case constants.Transactions.ASYNC_MODE => updateTransactionHash(ticketID, asyncAction(request).txhash)
              case constants.Transactions.SYNC_MODE => updateTransactionHash(ticketID, syncAction(request).txhash)
            }
          } catch {
            case baseException: BaseException => logger.error(baseException.failure.message, baseException)
            case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
              onFailure(ticketID, blockChainException.failure.message)
          }
        }
      }
      ticketID
    }
  }

  def process2[T1 <: TransactionEntity[T1], T2 <: RequestEntity](entity: T1, blockchainTransactionCreate: T1 => String, request: T2, action: T2 => WSResponse,  onSuccess: (String, BlockResponse) => Unit, onFailure: (String, String) => Unit, updateTransactionHash:(String, String) => Int): String = {
    try {
      val ticketID: String = if (kafkaEnabled) utilities.JSON.getResponseFromJson[KafkaResponse](action(request)).ticketID else Random.nextString(32)
      blockchainTransactionCreate(entity.mutateTicketID(ticketID))
      if (!kafkaEnabled) {
        Future {
          try {
            transactionMode match {
              case constants.Transactions.BLOCK_MODE => onSuccess(ticketID, utilities.JSON.getResponseFromJson[BlockResponse](action(request)))
              case constants.Transactions.ASYNC_MODE => updateTransactionHash(ticketID, utilities.JSON.getResponseFromJson[AsyncResponse](action(request)).txhash)
              case constants.Transactions.SYNC_MODE => updateTransactionHash(ticketID, utilities.JSON.getResponseFromJson[SyncResponse](action(request)).txhash)
            }
          } catch {
            case baseException: BaseException => logger.error(baseException.failure.message, baseException)
            case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
              onFailure(ticketID, blockChainException.failure.message)
          }
        }
      }
      ticketID
    }
  }


  def ticketUpdater(getTickets: () => Seq[String], getTransactionHash: String => Option[String], onSuccess: (String, BlockResponse) => Unit, onFailure: (String, String) => Unit)(implicit logger: Logger) {
    val ticketIDsSeq: Seq[String] = getTickets()
    for (ticketID <- ticketIDsSeq) {
      try {
        val response: WSResponse = if (kafkaEnabled) {getResponse.Service.get(ticketID)} else getTxHashResponse.Service.get(getTransactionHash(ticketID).getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)))
        val blockResponse = transactionMode match {
          case constants.Transactions.BLOCK_MODE => utilities.JSON.getResponseFromJson[BlockResponse](response)
          case constants.Transactions.ASYNC_MODE => utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(utilities.JSON.getResponseFromJson[AsyncResponse](response).txhash))
          case constants.Transactions.SYNC_MODE => utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(utilities.JSON.getResponseFromJson[SyncResponse](response).txhash))
        }
        if (blockResponse.code.isEmpty) onSuccess(ticketID, blockResponse) else onFailure(ticketID, blockResponse.code.get.toString)
      } catch {
        case blockChainException: BlockChainException => logger.error(blockChainException.failure.message, blockChainException)
          if (blockChainException.failure.message != """RESPONSE.FAILURE.{"response":"Request in process, wait and try after some time"}""") {
            onFailure(ticketID, blockChainException.failure.message)
          }
        case baseException: BaseException => logger.error(baseException.failure.message, baseException)
      }
    }
  }

}

