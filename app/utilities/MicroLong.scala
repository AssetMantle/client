package utilities

class MicroLong(val value: Long) {

  def this(value: String) = this(value.toLong * 1000000)

  def this(value: Int) = this(value.toLong * 1000000)

  def this(value: Double) = this((value * 1000000).toLong)

  def this(value: Float) = this((value * 1000000).toLong)

  def toMicroFloat: Float = value.toFloat

  def toMicroInt: Int = value.toInt

  def toMicroString: String = value.toString

  def toMicroDouble: Double = value.toDouble

  def toMicroLong: Long = value

  override def toString: String = (value.toDouble / 1000000).toString

  def toByte: Byte = (value / 1000000).toByte

  def toShort: Short = (value / 1000000).toShort

  def toChar: Char = (value / 1000000).toChar

  def toInt: Int = (value / 1000000).toInt

  def toLong: Long = value / 1000000

  def toFloat: Float = value.toFloat / 1000000

  def toDouble: Double = value.toDouble / 1000000

  def toRoundedUpString(precision: Int = 2): String = utilities.NumericOperation.roundUp(this.toDouble, precision).toString

  def toRoundedDownString(precision: Int = 2): String = utilities.NumericOperation.roundDown(this.toDouble, precision).toString

  def toRoundedOffString(precision: Int = 2): String = utilities.NumericOperation.roundOff(this.toDouble, precision).toString

  def unary_~ : MicroLong = MicroLong(this.value.unary_~)

  def unary_+ : MicroLong = MicroLong(this.value.unary_+)

  def unary_- : MicroLong = MicroLong(this.value.unary_-)

  def +(x: MicroLong): MicroLong = MicroLong(this.toDouble + x.toDouble)

  def -(x: MicroLong): MicroLong = MicroLong(this.toDouble - x.toDouble)

  def *(x: MicroLong): MicroLong = MicroLong(this.toDouble * x.toDouble)

  def /(x: MicroLong): MicroLong = MicroLong(this.toDouble / x.toDouble)

  def %(x: MicroLong): MicroLong = MicroLong(this.toDouble % x.toDouble)

  def <<(x: MicroLong): MicroLong = MicroLong(this.value << x.value)

  def >>>(x: MicroLong): MicroLong = MicroLong(this.value >>> x.value)

  def >>(x: MicroLong): MicroLong = MicroLong(this.value >> x.value)

  def ==(x: MicroLong): Boolean = this.value == x.value

  def !=(x: MicroLong): Boolean = this.value != x.value

  def >(x: MicroLong): Boolean = this.value > x.value

  def >=(x: MicroLong): Boolean = this.value >= x.value

  def <(x: MicroLong): Boolean = this.value < x.value

  def <=(x: MicroLong): Boolean = this.value <= x.value

  def |(x: MicroLong): MicroLong = MicroLong(this.value | x.value)

  def &(x: MicroLong): MicroLong = MicroLong(this.value & x.value)

  def ^(x: MicroLong): MicroLong = MicroLong(this.value ^ x.value)

  def <<(x: Int): MicroLong = this << MicroLong(x)

  def <<(x: Long): MicroLong = this << MicroLong(x)

  def >>>(x: Int): MicroLong = this >>> MicroLong(x)

  def >>>(x: Long): MicroLong = this >>> MicroLong(x)

  def >>(x: Int): MicroLong = this >> MicroLong(x)

  def >>(x: Long): MicroLong = this >> MicroLong(x)

  def ==(x: Byte): Boolean = MicroLong(x.toLong) == this

  def ==(x: Short): Boolean = MicroLong(x.toLong) == this

  def ==(x: Char): Boolean = MicroLong(x.toLong) == this

  def ==(x: Int): Boolean = MicroLong(x) == this

  def ==(x: Long): Boolean = MicroLong(x) == this

  def ==(x: Float): Boolean = MicroLong(x) == this

  def ==(x: Double): Boolean = MicroLong(x) == this

  def !=(x: Byte): Boolean = MicroLong(x.toLong) != this

  def !=(x: Short): Boolean = MicroLong(x.toLong) != this

  def !=(x: Char): Boolean = MicroLong(x.toLong) != this

  def !=(x: Int): Boolean = MicroLong(x) != this

  def !=(x: Long): Boolean = MicroLong(x) != this

  def !=(x: Float): Boolean = MicroLong(x) != this

  def !=(x: Double): Boolean = MicroLong(x) != this

  def <(x: Byte): Boolean = this < MicroLong(x.toLong)

  def <(x: Short): Boolean = this < MicroLong(x.toLong)

  def <(x: Char): Boolean = this < MicroLong(x.toLong)

  def <(x: Int): Boolean = this < MicroLong(x)

  def <(x: Long): Boolean = this < MicroLong(x)

  def <(x: Float): Boolean = this < MicroLong(x)

  def <(x: Double): Boolean = this < MicroLong(x)

  def <=(x: Byte): Boolean = this <= MicroLong(x.toLong)

  def <=(x: Short): Boolean = this <= MicroLong(x.toLong)

  def <=(x: Char): Boolean = this <= MicroLong(x.toLong)

  def <=(x: Int): Boolean = this <= MicroLong(x)

  def <=(x: Long): Boolean = this <= MicroLong(x)

  def <=(x: Float): Boolean = this <= MicroLong(x)

  def <=(x: Double): Boolean = this <= MicroLong(x)

  def >(x: Byte): Boolean = this > MicroLong(x.toLong)

  def >(x: Short): Boolean = this > MicroLong(x.toLong)

  def >(x: Char): Boolean = this > MicroLong(x.toLong)

  def >(x: Int): Boolean = this > MicroLong(x)

  def >(x: Long): Boolean = this > MicroLong(x)

  def >(x: Float): Boolean = this > MicroLong(x)

  def >(x: Double): Boolean = this > MicroLong(x)

  def >=(x: Byte): Boolean = this >= MicroLong(x.toLong)

  def >=(x: Short): Boolean = this >= MicroLong(x.toLong)

  def >=(x: Char): Boolean = this >= MicroLong(x.toLong)

  def >=(x: Int): Boolean = this >= MicroLong(x)

  def >=(x: Long): Boolean = this >= MicroLong(x)

  def >=(x: Float): Boolean = this >= MicroLong(x)

  def >=(x: Double): Boolean = this >= MicroLong(x)

  def |(x: Byte): MicroLong = this | MicroLong(x.toLong)

  def |(x: Short): MicroLong = this | MicroLong(x.toLong)

  def |(x: Char): MicroLong = this | MicroLong(x.toLong)

  def |(x: Int): MicroLong = this | MicroLong(x)

  def |(x: Long): MicroLong = this | MicroLong(x)

  def &(x: Byte): MicroLong = this & MicroLong(x.toLong)

  def &(x: Short): MicroLong = this & MicroLong(x.toLong)

  def &(x: Char): MicroLong = this & MicroLong(x.toLong)

  def &(x: Int): MicroLong = this & MicroLong(x)

  def &(x: Long): MicroLong = this & MicroLong(x)

  def ^(x: Byte): MicroLong = this ^ MicroLong(x.toLong)

  def ^(x: Short): MicroLong = this ^ MicroLong(x.toLong)

  def ^(x: Char): MicroLong = this ^ MicroLong(x.toLong)

  def ^(x: Int): MicroLong = this ^ MicroLong(x)

  def ^(x: Long): MicroLong = this ^ MicroLong(x)

  def +(x: String): String = this.+(x)

  def +(x: Byte): MicroLong = this + MicroLong(x.toLong)

  def +(x: Short): MicroLong = this + MicroLong(x.toLong)

  def +(x: Char): MicroLong = this + MicroLong(x.toLong)

  def +(x: Int): MicroLong = this + MicroLong(x)

  def +(x: Long): MicroLong = this + MicroLong(x)

  def +(x: Float): MicroLong = this + MicroLong(x)

  def +(x: Double): MicroLong = this + MicroLong(x)

  def -(x: Byte): MicroLong = this - MicroLong(x.toLong)

  def -(x: Short): MicroLong = this - MicroLong(x.toLong)

  def -(x: Char): MicroLong = this - MicroLong(x.toLong)

  def -(x: Int): MicroLong = this - MicroLong(x)

  def -(x: Long): MicroLong = this - MicroLong(x)

  def -(x: Float): MicroLong = this - MicroLong(x)

  def -(x: Double): MicroLong = this - MicroLong(x)

  def *(x: Byte): MicroLong = this * MicroLong(x.toLong)

  def *(x: Short): MicroLong = this * MicroLong(x.toLong)

  def *(x: Char): MicroLong = this * MicroLong(x.toLong)

  def *(x: Int): MicroLong = this * MicroLong(x)

  def *(x: Long): MicroLong = this * MicroLong(x)

  def *(x: Float): MicroLong = this * MicroLong(x)

  def *(x: Double): MicroLong = this * MicroLong(x)

  def /(x: Byte): MicroLong = this / MicroLong(x.toLong)

  def /(x: Short): MicroLong = this / MicroLong(x.toLong)

  def /(x: Char): MicroLong = this / MicroLong(x.toLong)

  def /(x: Int): MicroLong = this / MicroLong(x)

  def /(x: Long): MicroLong = this / MicroLong(x)

  def /(x: Float): MicroLong = this / MicroLong(x)

  def /(x: Double): MicroLong = this / MicroLong(x)

  def %(x: Byte): MicroLong = this % MicroLong(x.toLong)

  def %(x: Short): MicroLong = this % MicroLong(x.toLong)

  def %(x: Char): MicroLong = this % MicroLong(x.toLong)

  def %(x: Int): MicroLong = this % MicroLong(x)

  def %(x: Long): MicroLong = this % MicroLong(x)

  def %(x: Float): MicroLong = this % MicroLong(x)

  def %(x: Double): MicroLong = this % MicroLong(x)
}

object MicroLong {
  def apply(i: Int): MicroLong = new MicroLong(i)

  def apply(d: Double): MicroLong = new MicroLong(d)

  def apply(f: Float): MicroLong = new MicroLong(f)

  def apply(s: String): MicroLong = new MicroLong(s)

  implicit def intToMicroLong(i: Int): MicroLong = MicroLong(i)

  implicit def doubleToMicroLong(d: Double): MicroLong = MicroLong(d)

  implicit def floatToMicroLong(f: Float): MicroLong = MicroLong(f)

  implicit def stringToMicroLong(s: String): MicroLong = MicroLong(s)

}