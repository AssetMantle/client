package schema.constants

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

}
