package feeders

import constants.Test
import java.time.LocalDate

object DateFeeder {

  val dateFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_DATE -> LocalDate.now().toString)
    feed
  }
}
