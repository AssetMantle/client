package models.Abstract

abstract class BaseTransaction[T] {
  def mutateTicketID(ticketID: String): T
}