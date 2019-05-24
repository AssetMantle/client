package feeders

import constants.Test
import scala.util.Random

object UsernameFeeder {

  val usernameFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) {
      if (id % 4 == 0) {
        val username = "ZONE_" + id.toString + Random.alphanumeric.take(8).mkString
        feed(id) = Map(Test.TEST_USERNAME -> username, Test.ID -> id.toString)
      }
      if (id % 4 == 1) {
        val username = "ORG_" + id.toString + Random.alphanumeric.take(8).mkString
        feed(id) = Map(Test.TEST_USERNAME -> username, Test.ID -> id.toString)
      }
      if (id % 4 == 2) {
        val username = "SELL_" + id.toString + Random.alphanumeric.take(8).mkString
        feed(id) = Map(Test.TEST_USERNAME -> username, Test.ID -> id.toString)
      }
      if (id % 4 == 3) {
        val username = "BUY_" + id.toString + Random.alphanumeric.take(8).mkString
        feed(id) = Map(Test.TEST_USERNAME -> username, Test.ID -> id.toString)
      }
    }
    feed
  }
}
