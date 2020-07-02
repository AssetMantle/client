package utilities

object NumericOperation {

  def roundAt(n: Double, p: Int = 2): Double = {
    val s = math.pow(10, p)
    math.round(n * s) / s
  }

  def roundUp(n: Double, p: Int = 2): Double = {
    val s = math.pow(10, p)
    math.ceil(n * s) / s
  }

  def roundDown(n: Double, p: Int = 2): Double = {
    val s = math.pow(10, p)
    math.floor(n * s) / s
  }

  def checkPrecision(p: Int, value: Double): Boolean = if ((value * math.pow(10, p)) % 1 == 0) true else false
}