package models.Trait

trait Approvable[T] {

  val status: Option[Boolean]

  def updateStatus(status: Option[Boolean]): T

}