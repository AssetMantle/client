package types

object Type {

  type microLong = MicroLong

  class MicroLong(val value: Long) {

    def this(value: String) = this(value.toLong)

    def this(value: Int) = this((value * 1000000).toLong)

    def this(value: Double) = this((value * 1000000).toLong)

    def realString = (value.toDouble / 1000000).toString

    def microString = value.toString

    def realDouble = value.toDouble / 1000000

    def microDouble = value.toDouble

    def +(microLong: MicroLong) = new microLong(this.value + microLong.value)

    def -(microLong: MicroLong) = new microLong(this.value - microLong.value)

    def *(microLong: MicroLong) = new microLong(this.value * microLong.value)

    def /(microLong: MicroLong) = new microLong(this.value / microLong.value)

    def realDoubleWithPrecision(p: Int) = utilities.NumericOperation.roundAt(p)(this.realDouble)

    def realDoubleWithTwoPrecision = realDoubleWithPrecision(2)

    def realStringWithTwoPrecision = realDoubleWithPrecision(2).toString

    def roundUpDoubleWithTwoPrecision = utilities.NumericOperation.roundUp(2)(this.realDouble)

    def roundDownDoubleWithTwoPrecision = utilities.NumericOperation.roundDown(2)(this.realDouble)
  }

}

