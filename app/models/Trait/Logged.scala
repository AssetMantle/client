package models.Trait

import java.sql.Timestamp
import models.common.Node

trait Logged[T] {
  val createdOn: Option[Timestamp]

  val createdBy: Option[String]

  val createdOnTimeZone: Option[String]

  val updatedOn: Option[Timestamp]

  val updatedBy: Option[String]

  val updatedOnTimeZone: Option[String]

  def createLog()(implicit node: Node): T

  def updateLog()(implicit node: Node): T
}
