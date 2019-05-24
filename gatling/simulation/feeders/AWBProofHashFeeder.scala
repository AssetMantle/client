package feeders

import constants.Test

import scala.util.Random

object AWBProofHashFeeder {

  val awbProofHashFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_AWB_PROOF_HASH-> Random.alphanumeric.take(8).mkString)
    feed
  }
}
