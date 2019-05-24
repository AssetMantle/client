package feeders

import constants.Test

object SellerLoginFeeder {

  val sellerLoginFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_SELLER_USERNAME -> feeders.UsernameFeeder.usernameFeed(id)(Test.TEST_USERNAME), Test.TEST_SELLER_PASSWORD -> feeders.UsernameFeeder.usernameFeed(id)(Test.TEST_USERNAME), Test.ID -> id.toString)
    feed.filter(_ (Test.ID).toInt % 4 == 2)
  }
}
