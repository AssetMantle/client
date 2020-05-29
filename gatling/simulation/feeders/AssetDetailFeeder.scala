package feeders

import constants.Test

import scala.util.Random

object AssetDetailFeeder {

  val assetDetailFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_ASSET_TYPE -> constants.SelectFieldOptions.ASSET_TYPES(Random.nextInt(constants.SelectFieldOptions.ASSET_TYPES.length)), Test.TEST_ASSET_DESCRIPTION -> Random.alphanumeric.take(14).mkString, Test.TEST_ASSET_QUANTITY->  Random.alphanumeric.filter(_.isDigit).take(3).mkString, Test.TEST_QUANTITY_UNIT-> Random.alphanumeric.take(2).mkString, Test.TEST_ASSET_PRICE-> Random.alphanumeric.filter(_.isDigit).take(3).mkString)
    feed
  }
}
