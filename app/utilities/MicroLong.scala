package utilities

class MicroLong(val value: Long) {

  def this(microString: String) = this(microString.toLong)

  def this(realInt: Int) = this((realInt * 1000000).toLong)

  def this(realDouble: Double) = this((realDouble * 1000000).toLong)

  def realString = (value.toDouble / 1000000).toString

  def microString = value.toString

  def realDouble = value.toDouble / 1000000

  def microDouble = value.toDouble

  def +(microLong: MicroLong) = new MicroLong(this.realDouble + microLong.realDouble)

  def -(microLong: MicroLong) = new MicroLong(this.realDouble - microLong.realDouble)

  def *(microLong: MicroLong) = new MicroLong(this.realDouble * microLong.realDouble)

  def /(microLong: MicroLong) = new MicroLong(this.realDouble / microLong.realDouble)

  def realDoubleWithPrecision(precision: Int = 2) = utilities.NumericOperation.roundOff(this.realDouble, precision)

  def realStringWithPrecision(precision: Int = 2) = realDoubleWithPrecision(precision).toString

  def roundUpDoubleWithPrecision(precision: Int = 2) = utilities.NumericOperation.roundUp(this.realDouble, precision)

  def roundDownDoubleWithPrecision(precision: Int = 2) = utilities.NumericOperation.roundDown(this.realDouble, precision)
}