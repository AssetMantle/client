package feeders

import constants.Test

import scala.util.Random

object BankAccountDetailFeeder {

  val bankAccountDetailFeeder: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_ACCOUNT_HOLDER_NAME -> Random.alphanumeric.take(8).mkString, Test.TEST_NICK_NAME -> Random.alphanumeric.take(8).mkString, Test.TEST_ACCOUNT_NUMBER->  Random.alphanumeric.take(8).mkString,Test.TEST_BANK_NAME -> Random.alphanumeric.take(8).mkString, Test.TEST_SWIFT_CODE->  Random.alphanumeric.filter(_.isUpper).take(10).mkString, Test.TEST_COUNTRY-> Random.alphanumeric.take(8).mkString, Test.TEST_ZIP_CODE-> Random.alphanumeric.take(8).mkString, Test.TEST_STREET_ADDRESS-> Random.alphanumeric.take(8).mkString)
    feed
  }
}
