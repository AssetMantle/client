package feeders

import constants.Test
import scala.util.Random

object usernameFeeder {

  def customFeeder(users: Int): Array[Map[String, String]] ={
    val feedArray = new Array[Map[String,String]](users)
    for(i <- feedArray.indices) feedArray(i)= Map(Test.TEST_USERNAME -> Random.alphanumeric.take(6).mkString)
    feedArray
  }

  val arrayUsernameFeeder = Array(
    Map(Test.TEST_USERNAME -> "00000001"),
    Map(Test.TEST_USERNAME -> "00000002"),
    Map(Test.TEST_USERNAME -> "00000003")
  )

  val randomUsernameFeeder: Iterator[Map[String, String]] = Iterator.continually(Map(Test.TEST_USERNAME -> Random.alphanumeric.take(10).mkString))

}