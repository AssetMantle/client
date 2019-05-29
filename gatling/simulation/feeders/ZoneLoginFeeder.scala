package feeders

import constants.Test

object ZoneLoginFeeder {

  val zoneLoginFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_ZONE_USERNAME -> feeders.UsernameFeeder.usernameFeed(id)(Test.TEST_USERNAME), Test.TEST_ZONE_PASSWORD -> feeders.UsernameFeeder.usernameFeed(id)(Test.TEST_USERNAME), Test.ID -> feeders.UsernameFeeder.usernameFeed(id)(Test.ID))
    feed.filter(_ (Test.ID).toInt % 4 == 0)
  }
}
