package utilities

import java.text.NumberFormat

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

  def formatNumber(number: MicroNumber, normalize: Boolean = true): String = if (normalize) formatNumber(number.toDouble) else formatNumber(number.value)

  def formatNumber(number: Double): String = try {
    NumberFormat.getNumberInstance().format(number)
  } catch {
    case _: Exception => number.toString
  }

  def formatNumber(number: Int): String = try {
    NumberFormat.getNumberInstance().format(number)
  } catch {
    case _: Exception => number.toString
  }

  def formatNumber(number: Long): String = try {
    NumberFormat.getNumberInstance().format(number)
  } catch {
    case _: Exception => number.toString
  }

  def formatNumber(number: BigInt): String = try {
    NumberFormat.getNumberInstance().format(number)
  } catch {
    case _: Exception => number.toString
  }

  def formatNumber(number: BigDecimal): String = try {
    NumberFormat.getNumberInstance().format(number)
  } catch {
    case _: Exception => number.toString
  }

}