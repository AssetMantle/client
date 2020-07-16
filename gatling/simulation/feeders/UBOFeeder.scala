package feeders

import constants.Test

import scala.util.Random

object UBOFeeder {

  val uboFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_PERSON_FIRST_NAME -> Random.alphanumeric.take(8).mkString, Test.TEST_PERSON_LAST_NAME -> Random.alphanumeric.take(8).mkString, Test.TEST_SHARE_PERCENTAGE -> Random.alphanumeric.take(8).mkString, Test.TEST_RELATIONSHIP -> Random.alphanumeric.take(8).mkString, Test.TEST_TITLE -> Random.alphanumeric.take(10).mkString)
    feed
  }
}
