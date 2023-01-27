package models.traits

trait ModelTable2[PK1, PK2] {

  def id1: slick.lifted.Rep[PK1]

  def id2: slick.lifted.Rep[PK2]
}
