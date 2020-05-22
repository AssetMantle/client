package feeders

import constants.Test

import scala.util.Random

object IssueAssetDetailFeeder {

  val issueAssetDetailFeeder: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val assetTypeList=Seq("Coal","Oil","Wheat")
    val qualityList=Seq("A+","A","B")
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_ASSET_TYPE -> assetTypeList(Random.nextInt(assetTypeList.length)), Test.TEST_QUANTITY_UNIT -> Random.alphanumeric.filter(_.isLetter).take(5).mkString, Test.TEST_ASSET_QUANTITY-> Random.alphanumeric.filter(_.isDigit).take(4).mkString, Test.TEST_ASSET_PRICE->(500+Random.nextInt(1000)).toString, Test.TEST_COMMODITY_NAME-> Random.alphanumeric.take(8).mkString, Test.TEST_QUALITY-> qualityList(Random.nextInt(qualityList.length)), Test.TEST_DELIVERY_TERM-> "FOB",Test.TEST_TRADE_TYPE->"POST TRADE",Test.TEST_SHIPMENT_DATE-> "2019-11-11",Test.TEST_COMDEX_PAYMENT_TERMS->"BOTH_PARTIES")
    feed
  }
}
