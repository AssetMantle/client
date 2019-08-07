package utilities

import exceptions.{BaseException, BlockChainException}
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}
import transactions.responses.TransactionResponse.{BlockResponse, KafkaResponse}
import transactions.{GetResponse, GetTxHashResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

abstract class TransactionEntity {
  def mutateTicketID(ticketID: String): TransactionEntity
}

@Singleton
class Transaction @Inject()()(implicit executionContext: ExecutionContext, configuration: Configuration, wsClient: WSClient) {

  private val kafkaEnabled = configuration.get[Boolean]("blockchain.kafka.enabled")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.UTILITIES_TRANSACTION

  def process[T1 <: TransactionEntity, T2](entity: T1, blockchainTransactionCreate: T1 => String, request: T2, kafkaAction: T2 => KafkaResponse, action: T2 => BlockResponse, onSuccess: (String, BlockResponse) => Unit, onFailure: (String, String) => Unit): String = {
    try {
      val ticketID: String = if (kafkaEnabled) kafkaAction(request).ticketID else Random.nextString(32)
      blockchainTransactionCreate(entity.mutateTicketID(ticketID))
      if (!kafkaEnabled) {
        Future {
          try {
            onSuccess(ticketID, action(request))
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
    val getTxHashResponse: GetTxHashResponse = new GetTxHashResponse
    val getResponse: GetResponse = new GetResponse
    val ticketIDsSeq: Seq[String] = getTickets()
    for (ticketID <- ticketIDsSeq) {
      try {
        if (kafkaEnabled) {
          val value = utilities.JSON.getResponseFromJson[BlockResponse](getResponse.Service.get(ticketID))
          onSuccess(ticketID, value)
        } else {
          val blockResponse = utilities.JSON.getResponseFromJson[BlockResponse](getTxHashResponse.Service.get(getTransactionHash(ticketID).getOrElse(throw new BaseException(constants.Response.FAILURE))))
          if (blockResponse.code.isEmpty) onSuccess(ticketID, blockResponse) else onFailure(ticketID, blockResponse.code.get.toString)
        }
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

