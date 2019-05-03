package feeders

import constants.Test
import io.gatling.core.feeder.Feeder

import scala.util.Random

object PasswordFeeder {

  val randomPasswordFeeder: Iterator[Map[String, String]] = Iterator.continually(Map(Test.TEST_PASSWORD -> Random.alphanumeric.take(10).mkString))

  def apply(): Feeder[String] = {
    val id: Iterator[Int] = Iterator.from(1)
    Iterator.continually(Map(
      Test.TEST_PASSWORD -> Random.alphanumeric.take(8).mkString,
      Test.ID -> id.next().toString
    ))
  }
}

