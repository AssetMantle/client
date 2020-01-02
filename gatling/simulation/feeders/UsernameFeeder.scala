package feeders

import constants.Test

import scala.util.Random

object UsernameFeeder {

  val usernameFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    //val userArray=Array("BUY10RkHGo134","BUY10bJ8ihVxQ","SELL11UHcHLZgJ","BUY126XujlwCO","SELL131zMCtdfT","BUY14kRFQL6jV","SELL15nA0kLlgn","BUY16C3ibJDa3","SELL17JZw5pMRc","BUY18bkslzEVk")
    for (id <- 0 until users) {
     // feed(id) = Map(Test.TEST_USERNAME -> userArray(id), Test.ID -> id.toString)
      if (id % 2 != 0) {
        val username = "SELL1" + id.toString + Random.alphanumeric.take(8).mkString
        feed(id) = Map(Test.TEST_USERNAME -> username, Test.ID -> id.toString)
      }
      else {
        val username = "BUY1" + id.toString + Random.alphanumeric.take(8).mkString
        feed(id) = Map(Test.TEST_USERNAME -> username, Test.ID -> id.toString)
      }
    }
    feed
  }
}
