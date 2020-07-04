package utilities

import exceptions.BaseException
import play.api.Logger
import play.api.libs.json.{Json, OFormat}

import scala.math.{Integral, Ordering}

class MicroNumber(val value: BigInt) extends Ordered[MicroNumber] {

  def this(value: String) = this((BigDecimal(value) * MicroNumber.factor).toBigInt)

  def this(value: Int) = this(BigInt(value) * MicroNumber.factor)

  def this(value: Long) = this(BigInt(value) * MicroNumber.factor)

  def this(value: Double) = this(BigDecimal(value * MicroNumber.factor).toBigInt)

  def this(value: Float) = this(BigDecimal(value * MicroNumber.factor).toBigInt)

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

  def toInt: Int = this.toMicroInt / MicroNumber.factor

  def toLong: Long = this.toMicroLong / MicroNumber.factor

  def toDouble: Double = this.toMicroDouble / MicroNumber.factor

  def toFloat: Float = this.toMicroFloat / MicroNumber.factor

  def toChar: Char = (this.value / MicroNumber.factor).toChar

  def toByte: Byte = (this.value / MicroNumber.factor).toByte

  def toShort: Short = (this.value / MicroNumber.factor).toShort

  def toByteArray: Array[Byte] = (this.value / MicroNumber.factor).toByteArray

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

  def ==(that: MicroNumber): Boolean = this.value == that.value

  def !=(that: MicroNumber): Boolean = this.value != that.value

  def &(that: MicroNumber): MicroNumber = new MicroNumber(this.value & that.value)

  def |(that: MicroNumber): MicroNumber = new MicroNumber(this.value | that.value)

  def ^(that: MicroNumber): MicroNumber = new MicroNumber(this.value ^ that.value)

  def &~(that: MicroNumber): MicroNumber = new MicroNumber(this.value &~ that.value)

  //Throws exception if MicroNumber has non-zero decimal places, example: 2.3, "1.7", works with cases: 23, 17.0, "23"
  def gcd(that: MicroNumber): MicroNumber = if ((this.value % MicroNumber.factor == 0) && (that.value % MicroNumber.factor == 0)) new MicroNumber(this.value.gcd(that.value)) else throw new BaseException(constants.Response.NUMBER_FORMAT_EXCEPTION)(MicroNumber.module, MicroNumber.logger)

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

  override def equals(that: Any): Boolean = this.value.equals(that)

  def isValidByte: Boolean = this.value.isValidByte

  def isValidShort: Boolean = this.value.isValidShort

  def isValidChar: Boolean = this.value.isValidChar

  def isValidInt: Boolean = this.value.isValidInt

  def isValidLong: Boolean = this.value.isValidLong

  def isValidFloat: Boolean = this.value.isValidFloat

  def isValidDouble: Boolean = this.value.isValidDouble

  def equals(that: MicroNumber): Boolean = this.value.equals(that.value)

  def compare(that: MicroNumber): Int = this.value.compare(that.value)

  def testBit(n: Int): Boolean = this.value.testBit(n)

  def setBit(n: Int): MicroNumber = new MicroNumber(this.value.setBit(n))

  def clearBit(n: Int): MicroNumber = new MicroNumber(this.value.clearBit(n))

  def flipBit(n: Int): MicroNumber = new MicroNumber(this.value.flipBit(n))

  def lowestSetBit: Int = this.value.lowestSetBit

  def bitLength: Int = this.value.bitLength

  def bitCount: Int = this.value.bitCount

  //Throws exception if MicroNumber has non-zero decimal places, example: 2.3, "1.7", works with cases: 23, 17.0, "23"
  def isProbablePrime(certainty: Int): Boolean = if (this.value % MicroNumber.factor == 0) BigInt(this.toLong).isProbablePrime(certainty) else throw new BaseException(constants.Response.NUMBER_FORMAT_EXCEPTION)(MicroNumber.module, MicroNumber.logger)

  def +(that: String): String = this.toString + that
}

object MicroNumber {

  private val factor = 1000000

  private val module: String = constants.Module.UTILITIES_MICRO_NUMBER

  private val logger: Logger = Logger(this.getClass)

  def apply(bi: BigInt): MicroNumber = new MicroNumber(bi)

  def apply(s: String): MicroNumber = new MicroNumber(s)

  def apply(i: Int): MicroNumber = new MicroNumber(i)

  def apply(l: Long): MicroNumber = new MicroNumber(l)

  def apply(d: Double): MicroNumber = new MicroNumber(d)

  def apply(f: Float): MicroNumber = new MicroNumber(f)

  def unapply(arg: MicroNumber): Option[String] = Option(arg.toMicroString)

  implicit val reads: OFormat[MicroNumber] = Json.format[MicroNumber]

  implicit def bigIntToMicroNumber(bi: BigInt): MicroNumber = new MicroNumber(bi)

  implicit def stringToMicroNumber(s: String): MicroNumber = new MicroNumber(s)

  implicit def intToMicroNumber(i: Int): MicroNumber = new MicroNumber(i)

  implicit def intToMicroNumber(l: Long): MicroNumber = new MicroNumber(l)

  implicit def doubleToMicroNumber(d: Double): MicroNumber = new MicroNumber(d)

  implicit def floatToMicroNumber(f: Float): MicroNumber = new MicroNumber(f)

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

    override def signum(x: MicroNumber): Int = x.signum

    override def compare(x: MicroNumber, y: MicroNumber): Int = x.compare(y)
  }

  implicit object MicroNumberIsIntegral extends MicroNumberIsIntegral with Ordering[MicroNumber]

}

