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

  def realDoubleWithPrecision(p: Int = 2) = utilities.NumericOperation.roundAt(this.realDouble, p)

  def realStringWithPrecision(p: Int = 2) = realDoubleWithPrecision(p).toString

  def roundUpDoubleWithPrecision(p: Int = 2) = utilities.NumericOperation.roundUp(this.realDouble, p)

  def roundDownDoubleWithPrecision(p: Int = 2) = utilities.NumericOperation.roundDown(this.realDouble, p)
}