package models.Trait

import java.sql.Timestamp

trait Database {
  val createdOn: Timestamp

  val createdBy: String

  val updatedOn: Option[Timestamp]

  val updatedBy: Option[String]

  val timezone: String
}
