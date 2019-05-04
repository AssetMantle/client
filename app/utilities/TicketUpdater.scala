package utilities

import exceptions.{BaseException, BlockChainException}
import models.master.Accounts
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import transactions.GetResponse
import transactions.responses.TransactionResponse.Response

import scala.concurrent.ExecutionContext

object TicketUpdater {


  def start(getTickets: () => Seq[String], getValueFromWSResponse: WSResponse => String, updateTicket: (String, String) => Int, getAddress: String => String)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext, logger: Logger, pushNotifications: PushNotifications, accounts: Accounts) {
    implicit val getResponse: GetResponse = new GetResponse()(wsClient, configuration, executionContext)
    val ticketIDsSeq: Seq[String] = getTickets()
    for (ticketID <- ticketIDsSeq) {
      val wsResponse = getResponse.Service.get(ticketID)
      try {
        val value = getValueFromWSResponse(wsResponse)
        updateTicket(ticketID, value)
        pushNotifications.sendNotification(accounts.Service.getId(getAddress(ticketID)), constants.Notification.SUCCESS, Seq(value))
      }
      catch {
        case blockChainException: BlockChainException => logger.info(blockChainException.message, blockChainException)
        case baseException: BaseException => logger.info(baseException.message, baseException)
      }
    }
  }

  def start_(getTickets: () => Seq[String], getValueFromWSResponse: WSResponse => Response, onSuccess: (String, Response) => Unit, onFailure: (String, String) => Unit)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext, logger: Logger, pushNotifications: PushNotifications, accounts: Accounts) {

    implicit val getResponse = new GetResponse()(wsClient, configuration, executionContext)
    val ticketIDsSeq: Seq[String] = getTickets()
    for (ticketID <- ticketIDsSeq) {
      try {
        val value = getValueFromWSResponse(getResponse.Service.get(ticketID))
        //// hash response check to be done here
        onSuccess(ticketID, value)
      } catch {
        case blockChainException: BlockChainException => logger.error(blockChainException.message, blockChainException)
          if (blockChainException.message != """{"response":"Request in process, wait and try after some time"}""") {
            onFailure(ticketID, blockChainException.message)
          }
        case baseException: BaseException => logger.error(baseException.message, baseException)
      }
    }
  }
}
