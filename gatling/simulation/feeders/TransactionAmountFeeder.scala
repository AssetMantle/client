package feeders

import constants.Test

import scala.util.Random

object TransactionAmountFeeder {

  val transactionAmountFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_TRANSACTION_AMOUNT -> "10000")
    feed
  }
}
