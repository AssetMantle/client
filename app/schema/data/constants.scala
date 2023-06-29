package schema.data

import schema.id.base.StringID

import java.math.MathContext
import java.text.DecimalFormat

object constants {

  val TypeAndValueSeparator = "|"
  val ListSeparator = "\\#"

  val DecFactor: Long = 1000000000000000000L
  val DecStringFormat = new DecimalFormat("#0.000000000000000000")
  val DecPrecisionContext = new MathContext(18)
  val DecDataMaxValue: BigDecimal = BigDecimal(DecFactor, DecPrecisionContext)
  val ZeroDec: BigDecimal = BigDecimal("0.0")

  val AccAddressDataTypeID: StringID = StringID("A")
  val AnyDataTypeID: StringID = StringID("Y")
  val BooleanDataTypeID: StringID = StringID("B")
  val DecDataTypeID: StringID = StringID("D")
  val HeightDataTypeID: StringID = StringID("H")
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
  val AnyDataWeight = 0

}
