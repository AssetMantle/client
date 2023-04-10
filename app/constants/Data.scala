package constants

import schema.id.base.StringID

object Data {

  val AccAddressDataTypeID: StringID = StringID("A")
  val BooleanDataTypeID: StringID = StringID("B")
  val DecDataTypeID: StringID = StringID("D")
  val HeightDataTypeID: StringID = StringID("H")
  val IDDataTypeID: StringID = StringID("I")
  val ListDataTypeID: StringID = StringID("L")
  val StringDataTypeID: StringID = StringID("S")
  val NumberDataTypeID: StringID = StringID("N")

  val AccAddressBondWeight = 90
  val BooleanBondWeight = 1
  val DecDataWeight = 16
  val HeightDataWeight = 8
  val IDDataWeight = 64
  val ListDataWeight = 1024
  val NumberDataWeight = 8
  val StringDataWeight = 256

}
