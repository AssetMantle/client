package utilities

object NumericOperation {

  def roundAt(p: Int)(n: Double): Double = {
    val s = math.pow(10, p)
    math.round(n * s) / s
  }

  def roundAtTwoDecimal(n: Double): Double = roundAt(2)(n)

  def roundAtTwoDecimal(n: String): String = roundAt(2)(n.toDouble).toString
}