package models.traits

trait Entity3[PK1, PK2, PK3] {
  def id1: PK1

  def id2: PK2

  def id3: PK3
}
