package models.Trait

import java.sql.Timestamp

trait Logged {
  val createdOn: Option[Timestamp]

  val createdBy: Option[String]

  val createdOnTimeZone: Option[String]

  val updatedOn: Option[Timestamp]

  val updatedBy: Option[String]

  val updatedOnTimeZone: Option[String]

}
