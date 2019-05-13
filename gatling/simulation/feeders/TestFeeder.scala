package feeders

import models.master.Accounts
import constants.Test
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext
import scala.util.Random

class TestFeeder @Inject()(accounts: Accounts)(implicit executionContext: ExecutionContext) {

  object testFeeder {
    val testFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

    def arrayConstructor(users: Int): Array[Map[String, String]] = {
      val feed = new Array[Map[String, String]](users)
      for (id <- 0 until users) feed(id) = Map(Test.TEST_TO -> accounts.Service.getAddress(feeders.UsernameFeeder.usernameFeed(id)(Test.TEST_USERNAME)))
      feed
    }
  }

}
