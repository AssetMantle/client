package models.Trait

import java.sql.Timestamp

trait HistoryLogged {
  val createdOn: Option[Timestamp]

  val createdBy: Option[String]

  val createdOnTimeZone: Option[String]

  val updatedOn: Option[Timestamp]

  val updatedBy: Option[String]

  val updatedOnTimeZone: Option[String]

  val deletedBy: String

  val deletedOn: Timestamp

  val deletedOnTimeZone: String
}
