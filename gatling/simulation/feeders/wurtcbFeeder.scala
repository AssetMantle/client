package feeders

import constants.Test

import scala.util.Random

object wurtcbFeeder {

  val wurtcbFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_ID -> ("10000" + Random.alphanumeric.filter(_.isDigit).take(6).mkString), Test.TEST_REFRENCE -> ("CSGSGPDEMO0" + Random.alphanumeric.filter(_.isDigit).take(6).mkString), Test.TEST_WU_INVOICE_NUMBER -> ("INV" + Random.alphanumeric.filter(_.isDigit).take(6).mkString), Test.TEST_BUYER_BUSINESS_ID -> ("STU" + Random.alphanumeric.filter(_.isDigit).take(6).mkString), Test.TEST_BUYER_FIRST_NAME -> Random.alphanumeric.filter(_.isLetter).take(6).mkString, Test.TEST_BUYER_LAST_NAME -> Random.alphanumeric.filter(_.isLetter).take(6).mkString, Test.TEST_CREATED_DATE -> "2019-04-01 06:00:00", Test.TEST_LAST_UPDATED_DATE -> "2019-04-01 17:30:15", Test.TEST_WU_STATUS -> "DEAL_POSTED", Test.TEST_DEAL_TYPE -> "Sale", Test.TEST_PAYMENT_TYPE_ID -> "WIRE")
    feed
  }
}
