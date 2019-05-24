package feeders

import constants.Test

import scala.util.Random

object FiatProofHashFeeder {

  val fiatProofHashFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_FIAT_PROOF_HASH -> Random.alphanumeric.take(8).mkString)
    feed
  }
}
