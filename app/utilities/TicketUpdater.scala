package utilities

import exceptions.{BaseException, BlockChainException}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.GetResponse
import transactions.responses.TransactionResponse.Response

import scala.concurrent.ExecutionContext

object TicketUpdater {

  def start(getTickets: () => Seq[String], getValueFromWSResponse: WSResponse => Response, onSuccess: (String, Response) => Unit, onFailure: (String, String) => Unit)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext, logger: Logger) {

    implicit val getResponse = new GetResponse()(wsClient, configuration, executionContext)
    val ticketIDsSeq: Seq[String] = getTickets()
    for (ticketID <- ticketIDsSeq) {
      try {
        val value = getValueFromWSResponse(getResponse.Service.get(ticketID))
        onSuccess(ticketID, value)
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
