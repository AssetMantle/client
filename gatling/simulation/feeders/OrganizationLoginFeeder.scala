package feeders

import constants.Test

import scala.util.Random

object OrganizationLoginFeeder {

  val organizationLoginFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    //for (id <- 0 until users) feed(id) = Map(Test.TEST_ORGANIZATION_USERNAME -> feeders.UsernameFeeder.usernameFeed(id)(Test.TEST_USERNAME), Test.TEST_ORGANIZATION_PASSWORD -> feeders.UsernameFeeder.usernameFeed(id)(Test.TEST_USERNAME), Test.ID -> id.toString)
    for (id <- 0 until users){
      val username = "ORG1" + id.toString + Random.alphanumeric.take(8).mkString
      feed(id) = Map(Test.TEST_ORGANIZATION_USERNAME -> username, Test.TEST_ORGANIZATION_PASSWORD -> username)
    }
    feed
  }
}
