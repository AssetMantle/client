package utilities

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.Abstract.BaseRequestEntity
import transactions.responses.TransactionResponse.{AsyncResponse, BlockResponse, KafkaResponse, SyncResponse}
import transactions.{GetResponse, GetTxHashResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Transaction @Inject()(getTxHashResponse: GetTxHashResponse, getResponse: GetResponse)(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  def process[T1 <: BaseTransaction[T1], T2 <: BaseRequestEntity](entity: T1, blockchainTransactionCreate: T1 => String, request: T2, action: T2 => WSResponse, onSuccess: (String, BlockResponse) => Unit, onFailure: (String, String) => Unit, updateTransactionHash: (String, String) => Int)(implicit module: String, logger: Logger): String = {

    val ticketID: String = if (kafkaEnabled) utilities.JSON.getResponseFromJson[KafkaResponse](action(request)).ticketID else utilities.IDGenerator.ticketID
    blockchainTransactionCreate(entity.mutateTicketID(ticketID))
    println(ticketID)
    println(System.currentTimeMillis())
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
            onFailure(ticketID, baseException.failure.message)
        }
      }
    }
    ticketID
  }

  def processAsync[T1 <: BaseTransaction[T1], T2 <: BaseRequestEntity](entity: T1, blockchainTransactionCreate: T1 => Future[String], request: T2, action: T2 => Future[WSResponse], onSuccess: (String, BlockResponse) => Future[Unit], onFailure: (String, String) => Future[Unit], updateTransactionHash: (String, String) => Future[Int])(implicit module: String, logger: Logger) = {

    val ticketID2: Future[String] = if (kafkaEnabled) utilities.JSON.getResponseFromJsonAsync[KafkaResponse](action(request)).map(res=> res.ticketID) else Future{utilities.IDGenerator.ticketID}
    ticketID2.map{ticketID=>

        blockchainTransactionCreate(entity.mutateTicketID(ticketID))
        println(ticketID)
        println(System.currentTimeMillis())
        if (!kafkaEnabled){
          try {
            transactionMode match {
              case constants.Transactions.BLOCK_MODE => utilities.JSON.getResponseFromJsonAsync[BlockResponse](action(request)).map{blockResponse=>
                onSuccess(ticketID,blockResponse)
              }

              case constants.Transactions.ASYNC_MODE => utilities.JSON.getResponseFromJsonAsync[AsyncResponse](action(request)).map{asyncResponse=>
                updateTransactionHash(ticketID,asyncResponse.txhash)
              }
              case constants.Transactions.SYNC_MODE =>utilities.JSON.getResponseFromJsonAsync[SyncResponse](action(request)).map{asyncResponse=>
                updateTransactionHash(ticketID,asyncResponse.txhash)
              }

            }
          } catch {
            case baseException: BaseException => logger.error(baseException.failure.message, baseException)
              onFailure(ticketID, baseException.failure.message)
          }
    }
   }
    ticketID2
  }




  def ticketUpdater(getTickets: () => Seq[String], getTransactionHash: String => Option[String], onSuccess: (String, BlockResponse) => Unit, onFailure: (String, String) => Unit)(implicit module: String, logger: Logger) {
    val ticketIDsSeq: Seq[String] = getTickets()
    for (ticketID <- ticketIDsSeq) {
      try {
        val response: WSResponse = if (kafkaEnabled) {
          getResponse.Service.get(ticketID)
        } else getTxHashResponse.Service.get(getTransactionHash(ticketID).getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)))
        val blockResponse: BlockResponse = transactionMode match {
          case constants.Transactions.BLOCK_MODE => utilities.JSON.getResponseFromJson[BlockResponse](response)
          case constants.Transactions.ASYNC_MODE => utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(utilities.JSON.getResponseFromJson[AsyncResponse](response).txhash))
          case constants.Transactions.SYNC_MODE => utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(utilities.JSON.getResponseFromJson[SyncResponse](response).txhash))
        }
        if (blockResponse.code.isEmpty) onSuccess(ticketID, blockResponse) else onFailure(ticketID, blockResponse.code.get.toString)
      } catch {
        case baseException: BaseException =>
          if (!baseException.failure.message.matches("""RESPONSE.FAILURE.Tx. response error. RPC error -32603 - Internal error. Tx .\w+. not found""")) {
            onFailure(ticketID, baseException.failure.message)
          } else {
            logger.error(baseException.failure.message, baseException)
          }
      }
    }
  }

}

