package feeders

import constants.Test

import scala.util.Random

object UsernameFeeder {

  val usernameFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    val feed2= Array("SELL115mvaYVnUV","SELL117XBmxp0Eu"," SELL119v3Vrbsrr","SELL121QAApY9W4","SELL123N2ztEzdo","SELL125ZOkVe4OB", "BUY126d6JHoakf","BUY128EE8QovZP", "BUY130Kte8KRZV", "BUY132nf544MrV")

    for (id <- 0 until users) {
      val username = feed2(id)
      println(username)
      feed(id) = Map(Test.TEST_USERNAME -> username, Test.ID -> id.toString)
    }
    /*for (id <- 0 until users) {
      if (id % 2 != 0) {
        val username = "SELL1" + id.toString + Random.alphanumeric.take(8).mkString

        feed(id) = Map(Test.TEST_USERNAME -> username, Test.ID -> id.toString)
      }
      else {
        val username = "BUY1" + id.toString + Random.alphanumeric.take(8).mkString
        feed(id) = Map(Test.TEST_USERNAME -> username, Test.ID -> id.toString)
      }
    }*/
    feed
  }
}
