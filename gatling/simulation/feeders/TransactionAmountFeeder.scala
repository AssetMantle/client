package feeders

import scala.util.Random
import constants.Test

object TransactionAmountFeeder {

  val transactionAmountFeed: Array[Map[String, String]] = arrayConstructor(5)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_TRANSACTION_AMOUNT -> (Random.alphanumeric.filter(_.isDigit).filterNot(x => x == '0').take(5).mkString + "." + Random.alphanumeric.filter(_.isDigit).take(2).mkString))
    feed
  }
}
