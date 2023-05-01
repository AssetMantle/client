package schema.constants

import schema.id.base.StringID

import java.math.MathContext
import java.text.DecimalFormat

object Data {

  val DecFactor = 1000000000000000000L

  val DecStringFormat = new DecimalFormat("#0.000000000000000000")

  val DecPrecisionContext = new MathContext(18)

  val DecDataMaxValue: BigDecimal = BigDecimal(DecFactor, DecPrecisionContext)

  val OneDec: BigDecimal = BigDecimal("1.000000000000000000")
  val ZeroDec: BigDecimal = BigDecimal("0.0")
  val SmallestDec: BigDecimal = BigDecimal("0.000000000000000001")

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

  def getBondedWeight(dataTypeID: StringID): Int = dataTypeID.value match {
    case "A" => AccAddressBondWeight
    case "B" => BooleanBondWeight
    case "D" => DecDataWeight
    case "H" => HeightDataWeight
    case "I" => IDDataWeight
    case "L" => ListDataWeight
    case "N" => NumberDataWeight
    case "S" => StringDataWeight
    case _ => 0
  }
}
