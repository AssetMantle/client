package utilities

import schema.data.base.DecData

import java.math.MathContext
import java.text.DecimalFormat
import scala.language.implicitConversions
import scala.math.{Integral, Ordering, ScalaNumber, ScalaNumericConversions}
import scala.util.Try

class AttoNumber(val value: BigDecimal) extends ScalaNumber with ScalaNumericConversions with Ordered[AttoNumber] {

  def this(value: String) = this(BigDecimal(value, AttoNumber.precisionContext))

  def this(value: Int) = this(BigDecimal(value, AttoNumber.precisionContext))

  def this(value: Long) = this(BigDecimal(value, AttoNumber.precisionContext))

  def this(value: Double) = this(BigDecimal(value, AttoNumber.precisionContext))

  def this(value: Float) = this(BigDecimal(value, AttoNumber.precisionContext))

  def this(value: BigInt) = this(BigDecimal(value, AttoNumber.precisionContext))

  override def toString: String = this.value.toString

  def toPlainString: String = AttoNumber.fullFormat.format(this.value)

  def intValue: Int = this.value.toInt

  def longValue: Long = this.value.toLong

  def floatValue: Float = this.value.toFloat

  def doubleValue: Double = this.value.toDouble

  def toBigDecimal: BigDecimal = this.value

  def toDecData: DecData = DecData(this)

  override def byteValue: Byte = intValue.toByte

  override def shortValue: Short = intValue.toShort

  def toByteArray: Array[Byte] = this.toPlainString.getBytes

  def underlying: AnyRef = value

  def isWhole: Boolean = this.value % 1 == 0

  def roundedUp(precision: Int = 2): AttoNumber = new AttoNumber(utilities.NumericOperation.roundUp(this.toDouble, precision))

  def roundedDown(precision: Int = 2): AttoNumber = new AttoNumber(utilities.NumericOperation.roundDown(this.toDouble, precision))

  def roundedOff(precision: Int = 2): AttoNumber = new AttoNumber(utilities.NumericOperation.roundOff(this.toDouble, precision))

  def toRoundedUpString(precision: Int = 2): String = utilities.NumericOperation.roundUp(this.toDouble, precision).toString

  def toRoundedDownString(precision: Int = 2): String = utilities.NumericOperation.roundDown(this.toDouble, precision).toString

  def toRoundedOffString(precision: Int = 2): String = utilities.NumericOperation.roundOff(this.toDouble, precision).toString

  def +(that: AttoNumber): AttoNumber = new AttoNumber(this.value + that.value)

  def -(that: AttoNumber): AttoNumber = new AttoNumber(this.value - that.value)

  def *(that: AttoNumber): AttoNumber = new AttoNumber(this.value * that.value)

  def /(that: AttoNumber): AttoNumber = new AttoNumber(this.value / that.value)

  def %(that: AttoNumber): AttoNumber = new AttoNumber(this.value % that.value)

  def /%(that: AttoNumber): (AttoNumber, AttoNumber) = {
    val dr = this.value /% that.value
    (new AttoNumber(dr._1), new AttoNumber(dr._2))
  }

  def min(that: AttoNumber): AttoNumber = new AttoNumber(this.value.min(that.value))

  def max(that: AttoNumber): AttoNumber = new AttoNumber(this.value.max(that.value))

  def pow(exp: Int): AttoNumber = new AttoNumber(this.value.pow(exp))

  def unary_- : AttoNumber = new AttoNumber(this.value.unary_-)

  def abs: AttoNumber = new AttoNumber(this.value.abs)

  def signum: Int = this.value.signum

  override def equals(that: Any): Boolean = that match {
    case that: AttoNumber => this equals that
    case that: BigInt => this.value.equals(that)
    case that: Int => isValidInt && this == new AttoNumber(that)
    case that: Long => isValidLong && this == new AttoNumber(that)
    case that: Double => isValidDouble && this == new AttoNumber(that)
    case that: Float => isValidFloat && this == new AttoNumber(that)
    case that: Char => isValidChar && (toInt == that.toInt)
    case that: Byte => isValidByte && (toByte == that)
    case that: Short => isValidShort && (toShort == that)
    case that: String => this.toString == that
    case _ => false
  }

  def equals(that: AttoNumber): Boolean = this.value.equals(that.value)

  override def isValidByte: Boolean = this.value.isValidByte

  override def isValidShort: Boolean = this.value.isValidShort

  override def isValidChar: Boolean = this.value.isValidChar

  override def isValidInt: Boolean = this.value.isValidInt

  def isValidLong: Boolean = this.value.isValidLong

  def isValidFloat: Boolean = true

  def isValidDouble: Boolean = true

  def compare(that: AttoNumber): Int = this.value.compare(that.value)

  def isProbablePrime(certainty: Int): Boolean = if (this.isWhole) BigInt(this.toLong).isProbablePrime(certainty) else throw new IllegalArgumentException("NUMBER_FORMAT_EXCEPTION")

  def +(that: String): String = this.toString + that

  def wholePart: BigInt = this.value.toBigInt

  def decimalPart: BigInt = (this.value - BigDecimal(this.wholePart)).toBigInt

  def validSortable: Boolean = this.abs <= AttoNumber.maxValue

  private def getSortableDecBytes: Array[Byte] = {
    if (!this.validSortable) throw new IllegalArgumentException("UNSORTABLE_ATTONUMBER")
    else {
      if (this == AttoNumber.maxValue) "max".getBytes
      else if (this == (-1 * AttoNumber.maxValue)) "--".getBytes
      else {
        val f = java.lang.String.format("%18s", this.abs.toString.split("\\.").head).replace(" ", "0")
        val l = java.lang.String.format("%-18s", this.abs.toString.split("\\.").last).replace(" ", "0")
        if (this < 0) "-".getBytes ++ (f + "." + l).getBytes
        else (f + "." + l).getBytes
      }
    }
  }

  def quotientTruncate(that: AttoNumber): AttoNumber = AttoNumber(this.toBigDecimal / that.toBigDecimal)

  def multiplyTruncate(that: AttoNumber): AttoNumber = AttoNumber(this.toBigDecimal * that.toBigDecimal)
}

object AttoNumber {

  val factor = 1000000000000000000L

  val fullFormat = new DecimalFormat("#0.000000000000000000")

  val precisionContext = new MathContext(18)

  val zero = new AttoNumber(0)

  val maxValue = new AttoNumber(factor)

  val minValue: AttoNumber = new AttoNumber(1) / maxValue

  def apply(value: BigInt): AttoNumber = new AttoNumber(value)

  def apply(value: Int): AttoNumber = new AttoNumber(value)

  def apply(value: Long): AttoNumber = new AttoNumber(value)

  def apply(value: Double): AttoNumber = new AttoNumber(value)

  def apply(value: Float): AttoNumber = new AttoNumber(value)

  def apply(value: String): AttoNumber = new AttoNumber(value)

  def apply(value: BigDecimal): AttoNumber = new AttoNumber(value)

  def unapply(arg: AttoNumber): Option[String] = Option(arg.toString)

  implicit def stringToAttoNumber(s: String): AttoNumber = apply(s)

  implicit def intToAttoNumber(i: Int): AttoNumber = apply(i)

  implicit def longToAttoNumber(l: Long): AttoNumber = apply(l)

  implicit def doubleToAttoNumber(d: Double): AttoNumber = apply(d)

  implicit def floatToAttoNumber(f: Float): AttoNumber = apply(f)

  trait AttoNumberIsIntegral extends Integral[AttoNumber] {
    def plus(x: AttoNumber, y: AttoNumber): AttoNumber = x + y

    def minus(x: AttoNumber, y: AttoNumber): AttoNumber = x - y

    def times(x: AttoNumber, y: AttoNumber): AttoNumber = x * y

    def quot(x: AttoNumber, y: AttoNumber): AttoNumber = x / y

    def rem(x: AttoNumber, y: AttoNumber): AttoNumber = x % y

    def negate(x: AttoNumber): AttoNumber = new AttoNumber(-x.value)

    def fromInt(x: Int): AttoNumber = new AttoNumber(x)

    def toInt(x: AttoNumber): Int = x.toInt

    def toLong(x: AttoNumber): Long = x.toLong

    def toFloat(x: AttoNumber): Float = x.toFloat

    def toDouble(x: AttoNumber): Double = x.toDouble

    override def abs(x: AttoNumber): AttoNumber = x.abs

    override def compare(x: AttoNumber, y: AttoNumber): Int = x.compare(y)
  }

  implicit object AttoNumberIsIntegral extends AttoNumberIsIntegral with Ordering[AttoNumber] {
    def parseString(value: String): Option[AttoNumber] = Try(AttoNumber(value)).toOption
  }

}