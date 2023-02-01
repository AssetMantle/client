package commonUtilities

import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json._

import java.math.MathContext
import scala.language.implicitConversions
import scala.math.{Integral, Ordering, ScalaNumber, ScalaNumericConversions}
import scala.util.Try

class AttoNumber(val value: BigInt) extends ScalaNumber with ScalaNumericConversions with Ordered[AttoNumber] {

  def this(value: String) = this((BigDecimal(value, AttoNumber.precisionContext) * AttoNumber.factor).toBigInt)

  def this(value: Int) = this(BigInt(value) * AttoNumber.factor)

  def this(value: Long) = this(BigInt(value) * AttoNumber.factor)

  def this(value: Double) = this((BigDecimal(value, AttoNumber.precisionContext) * AttoNumber.factor).toBigInt)

  def this(value: BigDecimal) = this((value * AttoNumber.factor).toBigInt)

  def this(value: Float) = this((BigDecimal(value.toString, AttoNumber.precisionContext) * AttoNumber.factor).toBigInt)

  def toAttoString: String = this.value.toString

  def toAttoInt: Int = this.value.toInt

  def toAttoLong: Long = this.value.toLong

  def toAttoDouble: Double = this.value.toDouble

  def toAttoFloat: Float = this.value.toFloat

  def toAttoChar: Char = this.value.toChar

  def toAttoByte: Byte = this.value.toByte

  def toAttoShort: Short = this.value.toShort

  def toAttoByteArray: Array[Byte] = this.value.toByteArray

  override def toString: String = (BigDecimal(this.value, AttoNumber.precisionContext) / AttoNumber.factor).toString

  def intValue: Int = (this.value / AttoNumber.factor).toInt

  def longValue: Long = (this.value / AttoNumber.factor).toLong

  def floatValue: Float = (BigDecimal(this.value, AttoNumber.precisionContext) / AttoNumber.factor).toFloat

  def doubleValue: Double = (BigDecimal(this.value, AttoNumber.precisionContext) / AttoNumber.factor).toDouble

  def toBigDecimal: BigDecimal = BigDecimal(this.value, AttoNumber.precisionContext) / AttoNumber.factor

  override def byteValue: Byte = intValue.toByte

  override def shortValue: Short = intValue.toShort

  def toByteArray: Array[Byte] = (this.value / AttoNumber.factor).toByteArray

  def underlying: AnyRef = value

  def isWhole: Boolean = this.value % AttoNumber.factor == 0

  def roundedUp(precision: Int = 2): AttoNumber = new AttoNumber(commonUtilities.NumericOperation.roundUp(this.toDouble, precision))

  def roundedDown(precision: Int = 2): AttoNumber = new AttoNumber(commonUtilities.NumericOperation.roundDown(this.toDouble, precision))

  def roundedOff(precision: Int = 2): AttoNumber = new AttoNumber(commonUtilities.NumericOperation.roundOff(this.toDouble, precision))

  def toRoundedUpString(precision: Int = 2): String = commonUtilities.NumericOperation.roundUp(this.toDouble, precision).toString

  def toRoundedDownString(precision: Int = 2): String = commonUtilities.NumericOperation.roundDown(this.toDouble, precision).toString

  def toRoundedOffString(precision: Int = 2): String = commonUtilities.NumericOperation.roundOff(this.toDouble, precision).toString

  def +(that: AttoNumber): AttoNumber = new AttoNumber(this.value + that.value)

  def -(that: AttoNumber): AttoNumber = new AttoNumber(this.value - that.value)

  def *(that: AttoNumber): AttoNumber = new AttoNumber((this.value * that.value) / AttoNumber.factor)

  def /(that: AttoNumber): AttoNumber = new AttoNumber((this.value * AttoNumber.factor) / that.value)

  def %(that: AttoNumber): AttoNumber = new AttoNumber(this.value % that.value)

  def /%(that: AttoNumber): (AttoNumber, AttoNumber) = {
    val dr = this.value /% that.value
    (new AttoNumber(dr._1), new AttoNumber(dr._2))
  }

  def <<(n: Int): AttoNumber = new AttoNumber(this.value << n)

  def >>(n: Int): AttoNumber = new AttoNumber(this.value >> n)

  def &(that: AttoNumber): AttoNumber = new AttoNumber(this.value & that.value)

  def |(that: AttoNumber): AttoNumber = new AttoNumber(this.value | that.value)

  def ^(that: AttoNumber): AttoNumber = new AttoNumber(this.value ^ that.value)

  def &~(that: AttoNumber): AttoNumber = new AttoNumber(this.value &~ that.value)

  def gcd(that: AttoNumber): AttoNumber = if (this.isWhole && that.isWhole) new AttoNumber(this.value.gcd(that.value)) else throw new IllegalArgumentException("NUMBER_FORMAT_EXCEPTION")

  def mod(that: AttoNumber): AttoNumber = new AttoNumber(this.value.mod(that.value))

  def min(that: AttoNumber): AttoNumber = new AttoNumber(this.value.min(that.value))

  def max(that: AttoNumber): AttoNumber = new AttoNumber(this.value.max(that.value))

  def pow(exp: Int): AttoNumber = new AttoNumber(this.value.pow(exp))

  def modPow(exp: AttoNumber, m: AttoNumber): AttoNumber = new AttoNumber(this.value.modPow(exp.value, m.value))

  def modInverse(m: AttoNumber): AttoNumber = new AttoNumber(this.value.modInverse(m.value))

  def unary_- : AttoNumber = new AttoNumber(this.value.unary_-)

  def abs: AttoNumber = new AttoNumber(this.value.abs)

  def signum: Int = this.value.signum

  def unary_~ : AttoNumber = new AttoNumber(this.value.unary_~)

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

  def isValidFloat: Boolean = this.value.isValidFloat

  def isValidDouble: Boolean = this.value.isValidDouble

  def compare(that: AttoNumber): Int = this.value.compare(that.value)

  def testBit(n: Int): Boolean = this.value.testBit(n)

  def setBit(n: Int): AttoNumber = new AttoNumber(this.value.setBit(n))

  def clearBit(n: Int): AttoNumber = new AttoNumber(this.value.clearBit(n))

  def flipBit(n: Int): AttoNumber = new AttoNumber(this.value.flipBit(n))

  def lowestSetBit: Int = this.value.lowestSetBit

  def bitLength: Int = this.value.bitLength

  def bitCount: Int = this.value.bitCount

  def isProbablePrime(certainty: Int): Boolean = if (this.isWhole) BigInt(this.toLong).isProbablePrime(certainty) else throw new IllegalArgumentException("NUMBER_FORMAT_EXCEPTION")

  def +(that: String): String = this.toString + that

  def wholePart: BigInt = this.value / AttoNumber.factor

  def decimalPart: Int = (this.value - (wholePart * AttoNumber.factor)).toInt

  def validSortable: Boolean = this.abs <= AttoNumber.maxValue

  def getSortableDecBytes: Array[Byte] = {
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
}

object AttoNumber {

  private implicit val module: String = commonConstants.Module.COMMON_UTILITIES_ATTO_NUMBER

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val factor = 1000000000000000000L

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

  //Do not define OWrites and OFormat since it takes a `key` name. Here, AttoNumber(23.5) will serialize to "23.5" and vice versa.
  // The jsObject will not have a key member. Default OFormat will make it {"value": "23.5"}
  implicit val reads: Reads[AttoNumber] = JsPath.read[String].map(apply)

  implicit val writes: Writes[AttoNumber] = (o: AttoNumber) => JsString(o.toString)

  implicit val format: Format[AttoNumber] = Format[AttoNumber](reads, writes)

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