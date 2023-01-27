package models.traits

trait ModelTable3[PK1, PK2, PK3] {

  def id1: slick.lifted.Rep[PK1]

  def id2: slick.lifted.Rep[PK2]

  def id3: slick.lifted.Rep[PK3]
}
