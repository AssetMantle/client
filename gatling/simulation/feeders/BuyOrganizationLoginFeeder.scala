package feeders

import constants.Test

import scala.util.Random

object BuyOrganizationLoginFeeder {

  val buyOrganizationLoginFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users){
      val username = "BUYORG" + id.toString + Random.alphanumeric.take(8).mkString
      feed(id) = Map(Test.TEST_BUY_ORGANIZATION_USERNAME -> username, Test.TEST_BUY_ORGANIZATION_PASSWORD -> username)
    }
    feed
  }
}
