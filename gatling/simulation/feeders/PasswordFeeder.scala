package feeders

import constants.Test

object PasswordFeeder {

  val passwordFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_PASSWORD -> feeders.UsernameFeeder.usernameFeed(id)(Test.TEST_USERNAME),Test.ID -> feeders.UsernameFeeder.usernameFeed(id)(Test.ID))
    feed
  }
}

