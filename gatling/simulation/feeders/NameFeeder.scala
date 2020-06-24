package feeders

import constants.Test

import scala.util.Random

object NameFeeder {

  val nameFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_NAME -> Random.alphanumeric.take(8).mkString, Test.TEST_FIRST_NAME->Random.alphanumeric.take(8).mkString,Test.TEST_LAST_NAME->Random.alphanumeric.take(8).mkString)
    feed
  }
}
