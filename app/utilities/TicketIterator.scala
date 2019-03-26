package utilities

import exceptions.{BaseException, BlockChainException}
import models.master.Accounts
import play.api.{Configuration, Logger}
import play.api.libs.ws.{WSClient, WSResponse}
import transactions.GetResponse

import scala.concurrent.ExecutionContext

object TicketIterator   {

  def start(getTickets:() => Seq[String], getValueFromWSResponse: WSResponse => String, updateTicket:(String, String) => Int, getAddress:String => String)(implicit wsClient: WSClient, configuration: Configuration, executionContext: ExecutionContext, logger: Logger,  pushNotifications: PushNotifications, accounts: Accounts){
    implicit val getResponse = new GetResponse()(wsClient, configuration, executionContext)
      val ticketIDsSeq: Seq[String] = getTickets()
      for (ticketID <- ticketIDsSeq) {
        val wsResponse = getResponse.Service.get(ticketID)
        try {
          val value = getValueFromWSResponse(wsResponse)
          updateTicket(ticketID, value)
          pushNotifications.sendNotification(accounts.Service.getId(getAddress(ticketID)), constants.Notification.SUCCESS, Seq(value))
        }
        catch {
          case blockChainException: BlockChainException  => logger.info(blockChainException.message, blockChainException)
          case baseException: BaseException => logger.info(baseException.message, baseException)
        }
      }
    }
}
