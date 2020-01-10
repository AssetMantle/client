package feeders

import constants.Test

import scala.util.Random

object SellerFeeder {

  val sellerFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users){
      val username = "SELL1" + id.toString + Random.alphanumeric.take(8).mkString
      feed(id) = Map(Test.TEST_SELLER_USERNAME -> username, Test.TEST_SELLER_PASSWORD -> username)
    }
    feed
  }
}
