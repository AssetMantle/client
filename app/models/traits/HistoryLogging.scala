package models.traits

trait HistoryLogging {

  val createdOnMillisEpoch: Option[Long]

  val createdBy: Option[String]

  val updatedOnMillisEpoch: Option[Long]

  val updatedBy: Option[String]

  val deletedOnMillisEpoch: Option[Long]

  val deletedBy: Option[String]

}
