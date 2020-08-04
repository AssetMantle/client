package feeders

import java.time.LocalDate

import constants.Test
import scala.util.Random

object InvoiceDetails {

  val invoiceDetailsFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_INVOICE_NUMBER -> Random.alphanumeric.take(10).mkString, Test.TEST_INVOICE_AMOUNT -> (Random.alphanumeric.filter(_.isDigit).filterNot(x => x == '0').take(4).mkString + "." + Random.alphanumeric.filter(_.isDigit).take(2).mkString), Test.TEST_INVOICE_DATE -> LocalDate.now().toString)
    feed
  }
}
