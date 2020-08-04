package feeders

import constants.Test

import scala.util.Random

object RedeemAmountFeeder {

  val redeemAmountFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_REDEEM_AMOUNT ->(Random.alphanumeric.filter(_.isDigit).filterNot(x => x == '0').take(1).mkString + "." + Random.alphanumeric.filter(_.isDigit).take(2).mkString))
    feed
  }
}
