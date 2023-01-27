package models.traits

trait Entity2[PK1, PK2] {
  def id1: PK1

  def id2: PK2
}
