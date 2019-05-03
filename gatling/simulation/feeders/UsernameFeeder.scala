package feeders

import constants.Test
import io.gatling.core.feeder.Feeder

import scala.util.Random

object UsernameFeeder {

  val randomPasswordFeeder: Iterator[Map[String, String]] = Iterator.continually(Map(Test.TEST_USERNAME -> Random.alphanumeric.take(8).mkString))

  def apply(): Feeder[String] = {
    val id: Iterator[Int] = Iterator.from(1)
    Iterator.continually(Map(
      Test.TEST_USERNAME -> Random.alphanumeric.take(8).mkString,
      "id" -> id.next().toString
    ))
  }
}
