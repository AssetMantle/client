package models.Trait

import java.sql.Timestamp
import utilities.MicroLong

trait ReceiveFiat {
  val id: String
  val traderID: String
  val orderID: String
  val amount: MicroLong
  val status: String
  val createdOn: Option[Timestamp]
  val createdBy: Option[String]
  val createdOnTimeZone: Option[String]
  val updatedOn: Option[Timestamp]
  val updatedBy: Option[String]
  val updatedOnTimeZone: Option[String]

  def convertToReceiveFiat: models.masterTransaction.ReceiveFiat
}
