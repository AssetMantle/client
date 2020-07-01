package types

object Type {

  type Micro = MicroLong

  class MicroLong(val value: Long) {

    def this(realString: String) = this(realString.toLong)

    def this(realInt: Int) = this((realInt * 1000000).toLong)

    def this(realDouble: Double) = this((realDouble * 1000000).toLong)

    def realString = (value.toDouble / 1000000).toString

    def microString = value.toString

    def realDouble = value.toDouble / 1000000

    def microDouble = value.toDouble

    def +(micro: Micro) = new Micro(this.value + micro.value)

    def -(micro: Micro) = new Micro(this.value - micro.value)

    def *(micro: Micro) = new Micro(this.value * micro.value)

    def /(micro: Micro) = new Micro(this.value / micro.value)

    def realDoubleWithPrecision(p: Int) = utilities.NumericOperation.roundAt(p)(this.realDouble)

    def realDoubleWithTwoPrecision = realDoubleWithPrecision(2)

    def realStringWithTwoPrecision = realDoubleWithPrecision(2).toString

    def roundUpDoubleWithTwoPrecision = utilities.NumericOperation.roundUp(2)(this.realDouble)

    def roundDownDoubleWithTwoPrecision = utilities.NumericOperation.roundDown(2)(this.realDouble)
  }

}

