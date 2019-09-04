package feeders

import constants.Test

object AccountIDFeeder {

  val accountIDFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_ACCOUNT_ID -> feeders.UsernameFeeder.usernameFeed(id)(Test.TEST_USERNAME))
    feed
  }
}