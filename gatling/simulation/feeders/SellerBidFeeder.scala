package feeders

import constants.Test

object SellerBidFeeder {

  val sellerBidFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_SELLER_BID -> "950")
    feed
  }
}
