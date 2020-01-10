package feeders

import constants.Test

import scala.util.Random

object ModeFeeder {

  val modeFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) {
      val modeSeq=Seq("async","sync","block")
      feed(id) = Map(Test.TEST_MODE -> Random.shuffle(modeSeq).head)
    }
      feed
  }
}
