package feeders

import constants.Test

import scala.util.Random

object TempFeeder {

  val timeFeed: Array[Map[String, String]] = arrayConstructor(10)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {

    val sellers=Seq("SELL10tT51sjXl","SELL114j2kyuwi","SELL12oXyrBLMI","SELL13jElPWlPW","SELL14rvXGODn4","SELL15KGcixUfK","SELL16FpaW0e1s","SELL17B21E3Hpi","SELL18bF6M5pMn","SELL19jA0AsZZQ")
    val buyers=Seq("BUY10jhsMti6P","BUY11In1O4yOe","BUY12qDq3yKW8","BUY13VNzatKdQ","BUY14UpqNqQlv","BUY15Q1EBoSsf","BUY16zjwmmSuz","BUY17YuWfocdj","BUY18AHZEm5d0","BUY19c8PnSQyS")
    val zones=Seq("ZONE10zYYoFNPS","ZONE11XO86AQXY","ZONE12HaO2rhHD","ZONE13mMuivo8l","ZONE14eXMCS0iQ","ZONE15ps3YyZAA","ZONE16YIgqZB0D","ZONE17m8jZkbRF","ZONE18aw8KWJlb","ZONE193A2JExGe")

    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_SELLER_USERNAME -> sellers(id), Test.TEST_SELLER_PASSWORD-> sellers(id),Test.TEST_BUYER_USERNAME-> buyers(id), Test.TEST_BUYER_PASSWORD-> buyers(id),Test.TEST_ZONE_USERNAME-> zones(id), Test.TEST_ZONE_PASSWORD-> "123123123")
    feed
  }
}
