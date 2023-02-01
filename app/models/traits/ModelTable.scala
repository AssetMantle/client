package models.traits

trait ModelTable[PK] {

  def id : slick.lifted.Rep[PK]
}
