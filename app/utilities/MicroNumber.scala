package utilities

import exceptions.BaseException
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.language.implicitConversions
import scala.math.{Integral, Ordering, ScalaNumber, ScalaNumericConversions}
import scala.util.Try

class MicroNumber(val value: BigInt) extends ScalaNumber with ScalaNumericConversions with Ordered[MicroNumber] {

  def this(value: String) = this((BigDecimal(value) * MicroNumber.factor).toBigInt)

  def this(value: Int) = this(BigInt(value) * MicroNumber.factor)

  def this(value: Long) = this(BigInt(value) * MicroNumber.factor)

  def this(value: Double) = this((BigDecimal(value) * MicroNumber.factor).toBigInt)

  def this(value: Float) = this((BigDecimal(value.toDouble) * MicroNumber.factor).toBigInt)

  def toMicroString: String = this.value.toString

  def toMicroInt: Int = this.value.toInt

  def toMicroLong: Long = this.value.toLong

  def toMicroDouble: Double = this.value.toDouble

  def toMicroFloat: Float = this.value.toFloat

  def toMicroChar: Char = this.value.toChar

  def toMicroByte: Byte = this.value.toByte

  def toMicroShort: Short = this.value.toShort

  def toMicroByteArray: Array[Byte] = this.value.toByteArray

  override def toString: String = (BigDecimal(this.value) / MicroNumber.factor).toString

  def intValue: Int = (this.value / MicroNumber.factor).toInt

  def longValue: Long = (this.value / MicroNumber.factor).toLong

  def floatValue: Float = (BigDecimal(this.value) / MicroNumber.factor).toFloat

  def doubleValue: Double = (BigDecimal(this.value) / MicroNumber.factor).toDouble

  def toBigDecimal: BigDecimal = BigDecimal(this.value) / MicroNumber.factor

  override def byteValue: Byte = intValue.toByte

  override def shortValue: Short = intValue.toShort

  def toByteArray: Array[Byte] = (this.value / MicroNumber.factor).toByteArray

  def underlying: AnyRef = value

  def isWhole: Boolean = this.value % MicroNumber.factor == 0

  def roundedUp(precision: Int = 2): MicroNumber = new MicroNumber(utilities.NumericOperation.roundUp(this.toDouble, precision))

  def roundedDown(precision: Int = 2): MicroNumber = new MicroNumber(utilities.NumericOperation.roundDown(this.toDouble, precision))

  def roundedOff(precision: Int = 2): MicroNumber = new MicroNumber(utilities.NumericOperation.roundOff(this.toDouble, precision))

  def toRoundedUpString(precision: Int = 2): String = utilities.NumericOperation.roundUp(this.toDouble, precision).toString

  def toRoundedDownString(precision: Int = 2): String = utilities.NumericOperation.roundDown(this.toDouble, precision).toString

  def toRoundedOffString(precision: Int = 2): String = utilities.NumericOperation.roundOff(this.toDouble, precision).toString

  def +(that: MicroNumber): MicroNumber = new MicroNumber(this.value + that.value)

  def -(that: MicroNumber): MicroNumber = new MicroNumber(this.value - that.value)

  def *(that: MicroNumber): MicroNumber = new MicroNumber((this.value * that.value) / MicroNumber.factor)

  def /(that: MicroNumber): MicroNumber = new MicroNumber((this.value * MicroNumber.factor) / that.value)

  def %(that: MicroNumber): MicroNumber = new MicroNumber(this.value % that.value)

  def /%(that: MicroNumber): (MicroNumber, MicroNumber) = {
    val dr = this.value /% that.value
    (new MicroNumber(dr._1), new MicroNumber(dr._2))
  }

  def <<(n: Int): MicroNumber = new MicroNumber(this.value << n)

  def >>(n: Int): MicroNumber = new MicroNumber(this.value >> n)

  def &(that: MicroNumber): MicroNumber = new MicroNumber(this.value & that.value)

  def |(that: MicroNumber): MicroNumber = new MicroNumber(this.value | that.value)

  def ^(that: MicroNumber): MicroNumber = new MicroNumber(this.value ^ that.value)

  def &~(that: MicroNumber): MicroNumber = new MicroNumber(this.value &~ that.value)

  def gcd(that: MicroNumber): MicroNumber = if (this.isWhole && that.isWhole) new MicroNumber(this.value.gcd(that.value)) else throw new BaseException(constants.Response.NUMBER_FORMAT_EXCEPTION)(MicroNumber.module, MicroNumber.logger)

  def mod(that: MicroNumber): MicroNumber = new MicroNumber(this.value.mod(that.value))

  def min(that: MicroNumber): MicroNumber = new MicroNumber(this.value.min(that.value))

  def max(that: MicroNumber): MicroNumber = new MicroNumber(this.value.max(that.value))

  def pow(exp: Int): MicroNumber = new MicroNumber(this.value.pow(exp))

  def modPow(exp: MicroNumber, m: MicroNumber): MicroNumber = new MicroNumber(this.value.modPow(exp.value, m.value))

  def modInverse(m: MicroNumber): MicroNumber = new MicroNumber(this.value.modInverse(m.value))

  def unary_- : MicroNumber = new MicroNumber(this.value.unary_-)

  def abs: MicroNumber = new MicroNumber(this.value.abs)

  def signum: Int = this.value.signum

  def unary_~ : MicroNumber = new MicroNumber(this.value.unary_~)

  override def equals(that: Any): Boolean = that match {
    case that: MicroNumber => this equals that
    case that: BigInt => this.value.equals(that)
    case that: Int => isValidInt && this == new MicroNumber(that)
    case that: Long => isValidLong && this == new MicroNumber(that)
    case that: Double => isValidDouble && this == new MicroNumber(that)
    case that: Float => isValidFloat && this == new MicroNumber(that)
    case that: Char => isValidChar && (toInt == that.toInt)
    case that: Byte => isValidByte && (toByte == that)
    case that: Short => isValidShort && (toShort == that)
    case that: String => this.toString == that
    case _ => false
  }

  def equals(that: MicroNumber): Boolean = this.value.equals(that.value)

  override def isValidByte: Boolean = this.value.isValidByte

  override def isValidShort: Boolean = this.value.isValidShort

  override def isValidChar: Boolean = this.value.isValidChar

  override def isValidInt: Boolean = this.value.isValidInt

  def isValidLong: Boolean = this.value.isValidLong

  def isValidFloat: Boolean = this.value.isValidFloat

  def isValidDouble: Boolean = this.value.isValidDouble

  def compare(that: MicroNumber): Int = this.value.compare(that.value)

  def testBit(n: Int): Boolean = this.value.testBit(n)

  def setBit(n: Int): MicroNumber = new MicroNumber(this.value.setBit(n))

  def clearBit(n: Int): MicroNumber = new MicroNumber(this.value.clearBit(n))

  def flipBit(n: Int): MicroNumber = new MicroNumber(this.value.flipBit(n))

  def lowestSetBit: Int = this.value.lowestSetBit

  def bitLength: Int = this.value.bitLength

  def bitCount: Int = this.value.bitCount

  def isProbablePrime(certainty: Int): Boolean = if (this.isWhole) BigInt(this.toLong).isProbablePrime(certainty) else throw new BaseException(constants.Response.NUMBER_FORMAT_EXCEPTION)(MicroNumber.module, MicroNumber.logger)

  def +(that: String): String = this.toString + that
}

object MicroNumber {

  private val factor = 1000000

  private val module: String = constants.Module.UTILITIES_MICRO_NUMBER

  private val logger: Logger = Logger(this.getClass)

  val zero = new MicroNumber(0)

  def apply(value: BigInt): MicroNumber = new MicroNumber(value)

  def apply(value: Int): MicroNumber = new MicroNumber(value)

  def apply(value: Long): MicroNumber = new MicroNumber(value)

  def apply(value: Double): MicroNumber = new MicroNumber(value)

  def apply(value: Float): MicroNumber = new MicroNumber(value)

  def apply(value: String): MicroNumber = new MicroNumber(value)

  def unapply(arg: MicroNumber): Option[String] = Option(arg.toMicroString)

  //Do not define OWrites and OFormat since it takes a `key` name. Here, MicroNumber(23.5) will serialize to "23.5" and vice versa.
  // The jsObject will not have a key member. Default OFormat will make it {"value": "23.5"}
  implicit val reads: Reads[MicroNumber] = JsPath.read[String].map(apply)

  implicit val writes: Writes[MicroNumber] = (o: MicroNumber) => JsString(o.toString)

  implicit val format: Format[MicroNumber] = Format[MicroNumber](reads, writes)

  implicit def stringToMicroNumber(s: String): MicroNumber = apply(s)

  implicit def intToMicroNumber(i: Int): MicroNumber = apply(i)

  implicit def longToMicroNumber(l: Long): MicroNumber = apply(l)

  implicit def doubleToMicroNumber(d: Double): MicroNumber = apply(d)

  implicit def floatToMicroNumber(f: Float): MicroNumber = apply(f)

  trait MicroNumberIsIntegral extends Integral[MicroNumber] {
    def plus(x: MicroNumber, y: MicroNumber): MicroNumber = x + y

    def minus(x: MicroNumber, y: MicroNumber): MicroNumber = x - y

    def times(x: MicroNumber, y: MicroNumber): MicroNumber = x * y

    def quot(x: MicroNumber, y: MicroNumber): MicroNumber = x / y

    def rem(x: MicroNumber, y: MicroNumber): MicroNumber = x % y

    def negate(x: MicroNumber): MicroNumber = new MicroNumber(-x.value)

    def fromInt(x: Int): MicroNumber = new MicroNumber(x)

    def toInt(x: MicroNumber): Int = x.toInt

    def toLong(x: MicroNumber): Long = x.toLong

    def toFloat(x: MicroNumber): Float = x.toFloat

    def toDouble(x: MicroNumber): Double = x.toDouble

    override def abs(x: MicroNumber): MicroNumber = x.abs

    override def compare(x: MicroNumber, y: MicroNumber): Int = x.compare(y)
  }

  implicit object MicroNumberIsIntegral extends MicroNumberIsIntegral with Ordering[MicroNumber] {
    def parseString(value: String): Option[MicroNumber] = Try(MicroNumber(value)).toOption
  }

}