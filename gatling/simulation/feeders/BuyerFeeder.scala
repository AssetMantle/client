package feeders

import constants.Test

import scala.util.Random

object BuyerFeeder {

  val buyerFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users){
      val username = "BUY1" + id.toString + Random.alphanumeric.take(8).mkString
      feed(id) = Map(Test.TEST_BUYER_USERNAME -> username, Test.TEST_BUYER_PASSWORD -> username)
    }
    feed
  }
}
