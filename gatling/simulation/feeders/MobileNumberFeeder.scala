package feeders

import constants.Test

import scala.util.Random

object MobileNumberFeeder {

  val mobileNumberFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_COUNTRY_CODE -> constants.SelectFieldOptions.COUNTRY_CODES(Random.nextInt(constants.SelectFieldOptions.COUNTRY_CODES.length)),Test.TEST_MOBILE_NUMBER -> Random.alphanumeric.filter(_.isDigit).take(10).mkString)
    feed
  }
}
