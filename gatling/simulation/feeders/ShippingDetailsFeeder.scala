package feeders

import constants.Test

import scala.util.Random

object ShippingDetailsFeeder {

  val shippingDetailsFeeder: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_PORT_OF_LOADING -> Random.alphanumeric.filter(_.isLetter).take(10).mkString, Test.TEST_PORT_OF_DISCHARGE-> Random.alphanumeric.filter(_.isLetter).take(10).mkString, Test.TEST_SHIPPING_PERIOD->Random.alphanumeric.filter(_.isDigit).take(2).mkString)
    feed
  }
}
