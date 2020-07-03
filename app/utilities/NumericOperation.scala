package utilities

object NumericOperation {

  def roundOff(value: Double, precision: Int = 2): Double = {
    val s = math.pow(10, precision)
    math.round(value * s) / s
  }

  def roundUp(value: Double, precision: Int = 2): Double = {
    val s = math.pow(10, precision)
    math.ceil(value * s) / s
  }

  def roundDown(value: Double, precision: Int = 2): Double = {
    val s = math.pow(10, precision)
    math.floor(value * s) / s
  }

  def checkPrecision(precision: Int, value: String): Boolean = if (value.split("""\.""")(1).length <= precision) true else false

}