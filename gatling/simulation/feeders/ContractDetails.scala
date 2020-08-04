package feeders

import java.time.LocalDate

import constants.Test

import scala.util.Random

object ContractDetails {

  val contractDetailsFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_CONTRACT_NUMBER -> Random.alphanumeric.take(10).mkString)
    feed
  }
}
