package models.Abstract

abstract class BaseTransaction[T] {
  def mutateTicketID(ticketID: String): T
  val txHash: Option[String]
  val ticketID: String
}

