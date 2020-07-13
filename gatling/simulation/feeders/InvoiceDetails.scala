package feeders

import java.time.LocalDate

import constants.Test
import scala.util.Random

object InvoiceDetails {

  val invoiceDetailsFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_INVOICE_NUMBER -> Random.alphanumeric.take(10).mkString, Test.TEST_INVOICE_AMOUNT -> Random.nextInt(5000).toString, Test.TEST_INVOICE_DATE -> LocalDate.now().toString)
    feed
  }
}
