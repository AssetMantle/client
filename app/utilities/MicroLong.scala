package utilities

class MicroLong(val value: Long) {

  def this(value: String) = this((value.toDouble * 1000000).toLong)

  def this(value: Int) = this((value * 1000000).toLong)

  def this(value: Double) = this((value * 1000000).toLong)

  def realString = (value.toDouble / 1000000).toString

  def microString=value.toString

  def realDouble = value.toDouble / 1000000

  def microDouble=value.toDouble

}
