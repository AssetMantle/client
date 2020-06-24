package feeders

import java.time.LocalDate

import constants.Test

import scala.util.Random

object PaymentTermsFeeder {

  val paymentTermsFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) {
      feed(id) = Map(Test.TEST_ADVANCE_PERCENTAGE -> Random.alphanumeric.filter(_.isDigit).take(2).mkString, Test.TEST_TENTATIVE_DATE -> (if (id % 2 == 0) LocalDate.now().toString else ""), Test.TEST_TENURE -> (if (id % 2 == 1) Random.alphanumeric.filter(_.isDigit).take(2).mkString else ""), Test.TEST_REFRENCE ->(if (id % 2 == 1) Random.shuffle(constants.SelectFieldOptions.REFERENCE_DATES).head else ""))
    }
    feed
  }
}

