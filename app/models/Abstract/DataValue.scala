package models.Abstract

abstract class DataValue {
  val dataType: String

  def GenerateHash: String

  def AsString: String

  def AsDec: BigDecimal

  def AsHeight: Int

  def AsID: String

}
