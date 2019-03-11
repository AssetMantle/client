package utilities

import com.fasterxml.jackson.core.JsonParseException
import exceptions.BlockChainException

import scala.util.{Failure, Success, Try}
import play.api.{Configuration, Logger}
import play.api.libs.ws.{WSClient, WSResponse}
import transactions.GetResponse

import scala.concurrent.ExecutionContext

object TransactionHashIterator   {

  def start(getTickets:() => Seq[String], getValueFromWSResponse: WSResponse => String, updateTicket:(String, String) => Int)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext, logger: Logger){
    implicit val getResponse = new GetResponse()(wsClient, configuration, executionContext)
      val ticketIDsSeq: Seq[String] = getTickets()
      for (ticketID <- ticketIDsSeq) {
        val wsResponse = getResponse.Service.get(ticketID)
        try {
          val value = getValueFromWSResponse(wsResponse)
          updateTicket(ticketID, value)
        }
        catch {
          case blockChainException: BlockChainException  => logger.info(blockChainException.message, blockChainException)
        }
      }
    }
}
