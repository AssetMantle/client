package utilities

object NumericOperation {

  def roundAt(p: Int)(n: Double): Double = {
    val s = math.pow(10, p)
    math.round(n * s) / s
  }

  def roundUp(p: Int)(n: Double): Double = {
    val s = math.pow(10, p)
    math.ceil(n * s) / s
  }

  def roundDown(p: Int)(n: Double): Double = {
    val s = math.pow(10, p)
    math.floor(n * s) / s
  }

  def roundAtTwoDecimal(n: Double): Double = roundAt(2)(n)

  def roundUpAtTwoDecimal(n: Double): Double = roundUp(2)(n)

  def roundDownAtTwoDecimal(n: Double): Double = roundDown(2)(n)
}