package feeders

import constants.Test

object GenesisFeeder {

  val genesisFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_MAIN_USERNAME -> "main", Test.TEST_MAIN_PASSWORD -> "aS=$S7ak#2y5rNkV")
    feed
  }
}
