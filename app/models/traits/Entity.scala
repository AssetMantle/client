package models.traits

trait Entity[PK] {
  def id: PK
}
