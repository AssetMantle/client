package models.Trait

import java.sql.Timestamp
import utilities.MicroNumber

trait SendFiatRequest {
  val id: String
  val traderID: String
  val ticketID: String
  val negotiationID: String
  val amount: MicroNumber
  val status: String
  val createdOn: Option[Timestamp]
  val createdBy: Option[String]
  val createdOnTimeZone: Option[String]
  val updatedOn: Option[Timestamp]
  val updatedBy: Option[String]
  val updatedOnTimeZone: Option[String]
  def convertToSendFiatRequest : models.masterTransaction.SendFiatRequest
}
