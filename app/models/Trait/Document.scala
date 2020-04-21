package models.Trait

trait Document[T] {

  val id: String

  val documentType: String

  val fileName: String

  val file: Option[Array[Byte]]

  val status: Option[Boolean]

  def updateFileName(newFileName: String): T

  def updateFile(newFile: Option[Array[Byte]]): T

}