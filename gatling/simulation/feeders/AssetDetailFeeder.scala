package feeders

import constants.Test

import scala.util.Random

object AssetDetailFeeder {

  val assetDetailFeed: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) {
      val assetPricePerUnit=Random.alphanumeric.filter(_.isDigit).take(2).mkString
      val quantity=Random.alphanumeric.filter(_.isDigit).take(2).mkString
      feed(id) = Map(Test.TEST_ASSET_TYPE -> constants.SelectFieldOptions.ASSET_TYPES(Random.nextInt(constants.SelectFieldOptions.ASSET_TYPES.length)), Test.TEST_ASSET_DESCRIPTION -> Random.alphanumeric.take(14).mkString, Test.TEST_ASSET_QUANTITY->  quantity, Test.TEST_QUANTITY_UNIT-> Random.alphanumeric.filter(_.isLetter).take(2).mkString, Test.TEST_ASSET_PRICE_PER_UNIT-> assetPricePerUnit, Test.TEST_ASSET_PRICE -> (quantity.toInt*assetPricePerUnit.toInt).toString)
    }
    feed
  }
}
