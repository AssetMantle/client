package utilities

class MicroInt(val value: Long) {

  def this(value: String) = this((value.toDouble * 1000000).toLong)

  def this(value: Int) = this((value * 1000000).toLong)

  def this(value: Double) = this((value * 1000000).toLong)

  def string = (value.toDouble / 1000000).toString

  def double = value.toDouble / 1000000

}
