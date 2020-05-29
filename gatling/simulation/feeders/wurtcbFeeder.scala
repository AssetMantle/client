package feeders

import constants.Test

import scala.util.Random

object wurtcbFeeder {

  val wurtcbFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_ID -> ("10000"+Random.alphanumeric.filter(_.isDigit).take(6).mkString), Test.TEST_REFRENCE -> ("CSGSGPDEMO0"+Random.alphanumeric.filter(_.isDigit).take(6).mkString))
    feed
  }
}
