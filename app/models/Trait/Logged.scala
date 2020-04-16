package models.Trait

import java.sql.Timestamp

trait Logged {
  val createdOn: Timestamp

  val createdBy: String

  val createdOnTimezone: String

  val updatedOn: Option[Timestamp]

  val updatedBy: Option[String]

  val updatedOnTimeZone: Option[String]
}
