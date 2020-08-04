package feeders

import constants.Test

import scala.util.Random

object ZoneLoginFeeder {

  val zoneLoginFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
  //  val userSeq=Seq("ZONE103X9kyhnU","ZONE10cY5hEGvn","SELLORG04J210fX0","SELLORG0O5xNSMgv","BUYORG0DRmdhVci","SELL10nW7491so","BUYORG015YlgEtb","BUY10autJga7e","SELL10rGB4zUHn","BUY10jqokVrEI")
   // val passwordSeq=userSeq.map(x=>if (x.slice(0,4)== "ZONE") "123123123" else x)
    for (id <- 0 until users){
      val username = "ZONE1" + id.toString + Random.alphanumeric.take(8).mkString
      feed(id) = Map(Test.TEST_ZONE_USERNAME ->username, Test.TEST_ZONE_PASSWORD -> "123123123")
    }
    feed
  }
}
