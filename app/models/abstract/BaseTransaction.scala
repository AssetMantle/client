package models.`abstract`

abstract class BaseTransaction[T] {
  def mutateTicketID(ticketID: String): T
}

