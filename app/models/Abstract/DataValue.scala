package models.Abstract

abstract class DataValue {
  val dataType: String

  def generateHash: String

  def asString: String

  def asDec: BigDecimal

  def asHeight: Int

  def asID: String

  def toString: String

}
