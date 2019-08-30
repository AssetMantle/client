package models.Trait

trait Document{

  val documentType: String

  val fileName: String

  val file: Option[Array[Byte]]

  val status: Option[Boolean]
}