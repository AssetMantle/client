package feeders

import constants.Test

import scala.util.Random

object IssueAssetOBLFeeder {

  val issueAssetOBLFeeder: Array[Map[String, String]] = arrayConstructor(Test.NUMBER_OF_USERS)

  def arrayConstructor(users: Int): Array[Map[String, String]] = {
    val feed = new Array[Map[String, String]](users)
    for (id <- 0 until users) feed(id) = Map(Test.TEST_BILL_OF_LADING_NUMBER -> Random.alphanumeric.take(8).mkString, Test.TEST_CONSIGNEE_TO->Random.alphanumeric.take(8).mkString, Test.TEST_VESSEL_NAME->Random.alphanumeric.take(8).mkString , Test.TEST_SHIPPER_NAME->  Random.alphanumeric.take(8).mkString, Test.TEST_SHIPPER_ADDRESS-> Random.alphanumeric.take(8).mkString, Test.TEST_NOTIFY_PARTY_NAME-> Random.alphanumeric.take(8).mkString, Test.TEST_NOTIFY_PARTY_ADDRESS-> Random.alphanumeric.take(8).mkString, Test.TEST_SHIPMENT_DATE->  "2019-11-11", Test.TEST_DELIVERY_TERM -> constants.SelectFieldOptions.DELIVERY_TERMS(Random.nextInt(constants.SelectFieldOptions.DELIVERY_TERMS.length)))
    feed
  }
}
