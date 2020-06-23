package feeders

import constants.Test

object GasFeeder {

  val gasFeed: Array[Map[String, Int]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, Int]] = {
    val feed = new Array[Map[String, Int]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_GAS -> 999999)
    feed
  }
}
