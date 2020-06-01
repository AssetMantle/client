package feeders

import constants.Test

import scala.util.Random

object AddressDataFeeder {

  val addressDataFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_ADDRESS_LINE_1 -> Random.alphanumeric.take(8).mkString, Test.TEST_ADDRESS_LINE_2 -> Random.alphanumeric.take(8).mkString, Test.TEST_LANDMARK->  Random.alphanumeric.take(8).mkString, Test.TEST_CITY-> Random.alphanumeric.take(8).mkString, Test.TEST_COUNTRY-> constants.SelectFieldOptions.COUNTRIES(Random.nextInt(constants.SelectFieldOptions.COUNTRIES.length)), Test.TEST_ZIP_CODE-> Random.alphanumeric.take(8).mkString, Test.TEST_PHONE-> Random.alphanumeric.filter(_.isDigit).take(10).mkString)
    feed
  }
}
