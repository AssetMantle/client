package models.Trait

trait Logging {
  val createdOnMillisEpoch: Option[Long]
  val createdBy: Option[String]
  val updatedOnMillisEpoch: Option[Long]
  val updatedBy: Option[String]
}
