package utilities

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.Abstract.BaseTransaction
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import queries.{GetResponse, GetTransactionHashResponse}
import transactions.Abstract.BaseRequest
import transactions.responses.TransactionResponse.{AsyncResponse, BlockResponse, KafkaResponse, SyncResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Transaction @Inject()(getTxHashResponse: GetTransactionHashResponse, getResponse: GetResponse)(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val sleepTime = configuration.get[Long]("blockchain.entityIterator.threadSleep")

  private val responseErrorTransactionHashNotFound: String = constants.Response.PREFIX + constants.Response.FAILURE_PREFIX + configuration.get[String]("blockchain.response.error.transactionHashNotFound")

  def process[T1 <: BaseTransaction[T1], T2 <: BaseRequest](entity: T1, blockchainTransactionCreate: T1 => String, request: T2, action: T2 => WSResponse, onSuccess: (String, BlockResponse) => Unit, onFailure: (String, String) => Unit, updateTransactionHash: (String, String) => Int)(implicit module: String, logger: Logger): String = {

    val ticketID: String = if (kafkaEnabled) utilities.JSON.getResponseFromJson[KafkaResponse](action(request)).ticketID else utilities.IDGenerator.ticketID
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
            onFailure(ticketID, baseException.failure.message)
        }
      }
    }
    ticketID
  }


  def ticketUpdater(getTickets: () => Seq[String], getTransactionHash: String => Option[String], getMode: String => String, onSuccess: (String, BlockResponse) => Unit, onFailure: (String, String) => Unit)(implicit module: String, logger: Logger) {
    val ticketIDsSeq: Seq[String] = getTickets()
    Thread.sleep(sleepTime)
    for (ticketID <- ticketIDsSeq) {
      try {
        val blockResponse: BlockResponse = if (kafkaEnabled) {
          getMode(ticketID) match {
            case constants.Transactions.BLOCK_MODE => utilities.JSON.getResponseFromJson[BlockResponse](getResponse.Service.get(ticketID))
            case constants.Transactions.ASYNC_MODE => utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(utilities.JSON.getResponseFromJson[AsyncResponse](getResponse.Service.get(ticketID)).txhash))
            case constants.Transactions.SYNC_MODE => utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(utilities.JSON.getResponseFromJson[SyncResponse](getResponse.Service.get(ticketID)).txhash))
          }
        } else {
          utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(getTransactionHash(ticketID).getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION))))
        }
        if (blockResponse.code.isEmpty) onSuccess(ticketID, blockResponse) else onFailure(ticketID, blockResponse.code.get.toString)
      } catch {
        case baseException: BaseException => if (!baseException.failure.message.matches(responseErrorTransactionHashNotFound)) onFailure(ticketID, baseException.failure.message) else logger.info(baseException.failure.message, baseException)
      }
    }
  }

}

