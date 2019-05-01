package feeders

import constants.Test
import scala.util.Random

object passwordFeeder {

  def customFeeder(users: Int): Array[Map[String, String]] ={
    val feedArray = new Array[Map[String,String]](users)
    for(i <- feedArray.indices) feedArray(i)= Map(Test.TEST_PASSWORD -> Random.alphanumeric.take(8).mkString)
    feedArray
  }

  val arrayPassWordFeeder = Array(
    Map(Test.TEST_PASSWORD -> "00000001"),
    Map(Test.TEST_PASSWORD -> "00000002"),
    Map(Test.TEST_PASSWORD -> "00000003")
  )

  val randomPasswordFeeder: Iterator[Map[String, String]] = Iterator.continually(Map(Test.TEST_PASSWORD -> Random.alphanumeric.take(10).mkString))

}