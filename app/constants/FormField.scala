package constants

import java.util.Date

import play.api.data.Forms.{date, number, of, text}
import play.api.data.Mapping
import play.api.data.format.Formats._
import play.api.data.validation.Constraints

import scala.util.matching.Regex

object FormField {
  //StringFormField
  val SIGNUP_USERNAME = new StringFormField("USERNAME", 3,  50, RegularExpression.ACCOUNT_ID, Response.INVALID_USERNAME.message)
  val SIGNUP_PASSWORD = new StringFormField("PASSWORD", 8, 128, RegularExpression.PASSWORD, Response.INVALID_PASSWORD.message)
  val SIGNUP_CONFIRM_PASSWORD = new StringFormField("CONFIRM_PASSWORD", 8, 128, RegularExpression.PASSWORD, Response.INVALID_PASSWORD.message)
  val USERNAME = new StringFormField("USERNAME", 3, 50, RegularExpression.ACCOUNT_ID)
  val PASSWORD = new StringFormField("PASSWORD", 1, 128)
  val MOBILE_NUMBER = new StringFormField("MOBILE_NUMBER", 8, 15, RegularExpression.MOBILE_NUMBER)
  val BLOCKCHAIN_ADDRESS = new StringFormField("BLOCKCHAIN_ADDRESS", 45, 45)
  val ACL_ADDRESS = new StringFormField("ACL_ADDRESS", 45, 45)
  val SELLER_ADDRESS = new StringFormField("SELLER_ADDRESS", 45, 45)
  val TO = new StringFormField("TO", 45, 45)
  val BUYER_ADDRESS = new StringFormField("BUYER_ADDRESS", 45, 45)
  val ZONE_ID = new StringFormField("ZONE_ID", 8, 16, RegularExpression.HASH)
  val ORGANIZATION_ID = new StringFormField("ORGANIZATION_ID", 8, 16, RegularExpression.HASH)
  val TRADER_ID = new StringFormField("TRADER_ID", 8, 16, RegularExpression.HASH)
  val NAME = new StringFormField("NAME", 2, 50)
  val PERSON_NAME = new StringFormField("PERSON_NAME", 2, 50)
  val ABBREVIATION = new StringFormField("ABBREVIATION", 2, 10)
  val ADDRESS = new StringFormField("ADDRESS", 6, 100)
  val REQUEST_ID = new StringFormField("REQUEST_ID", 32, 32)
  val ACCOUNT_ID = new StringFormField("ACCOUNT_ID", 3, 50)
  val FIAT_PROOF_HASH = new StringFormField("FIAT_PROOF_HASH", 4, 50, RegularExpression.HASH)
  val AWB_PROOF_HASH = new StringFormField("AWB_PROOF_HASH", 4, 50, RegularExpression.HASH)
  val PEG_HASH = new StringFormField("PEG_HASH", 2, 50, RegularExpression.PEG_HASH)
  val COMMODITY_NAME = new StringFormField("COMMODITY_NAME", 2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val QUANTITY_UNIT = new StringFormField("QUANTITY_UNIT", 2, 10, RegularExpression.ALL_LETTERS)
  val TRANSACTION_ID = new StringFormField("TRANSACTION_ID", 2, 50, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val PUSH_NOTIFICATION_TOKEN = new StringFormField("PUSH_NOTIFICATION_TOKEN", 0, 200)
  val COMMENT = new StringFormField("COMMENT", 0, 200)
  val COUPON = new StringFormField("COUPON", 0, 50)
  val COUNTRY_CODE = new StringFormField("COUNTRY_CODE", 1, 5)
  val OTP = new StringFormField("OTP", 4, 10, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val CURRENCY = new StringFormField("CURRENCY", 2, 30, RegularExpression.ALL_LETTERS)
  val EMAIL_ADDRESS = new StringFormField("EMAIL_ADDRESS", 6, 100, RegularExpression.EMAIL_ADDRESS)
  val BUYER_CONTRACT_HASH = new StringFormField("BUYER_CONTRACT_HASH", 40, 40, RegularExpression.HASH)
  val SELLER_CONTRACT_HASH = new StringFormField("SELLER_CONTRACT_HASH", 40, 40, RegularExpression.HASH)
  val DOCUMENT_HASH = new StringFormField("DOCUMENT_HASH", 4, 500)
  val FROM = new StringFormField("FROM", 45, 45)
  val MODE = new StringFormField("MODE", 4, 5)
  val PORT_OF_LOADING = new StringFormField("PORT_OF_LOADING", 0, 100)
  val PORT_OF_DISCHARGE = new StringFormField("PORT_OF_DISCHARGE", 0, 100)
  val BILL_OF_LADING_NUMBER = new StringFormField("COMMODITY_NAME", 2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val SHIPPER_NAME = new StringFormField("SHIPPER_NAME", 2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val SHIPPER_ADDRESS = new StringFormField("SHIPPER_ADDRESS", 2, 100, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val NOTIFY_PARTY_NAME = new StringFormField("NOTIFY_PARTY_NAME", 2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val NOTIFY_PARTY_ADDRESS = new StringFormField("NOTIFY_PARTY_ADDRESS", 2, 100, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val INVOICE_NUMBER = new StringFormField("INVOICE_NUMBER", 2, 32)
  val TAKER_ADDRESS = new StringFormField( "TAKER_ADDRESS", 45, 45)
  val REGISTERED_ADDRESS_LINE_1 = new StringFormField("REGISTERED_ADDRESS_LINE_1", 4, 200)
  val REGISTERED_ADDRESS_LINE_2 = new StringFormField("REGISTERED_ADDRESS_LINE_2", 4, 200)
  val REGISTERED_LANDMARK = new StringFormField("REGISTERED_LANDMARK", 4, 100)
  val REGISTERED_CITY = new StringFormField("REGISTERED_CITY", 2, 100)
  val REGISTERED_ZIP_CODE = new StringFormField("REGISTERED_ZIP_CODE", 2, 100)
  val REGISTERED_COUNTRY = new StringFormField("REGISTERED_COUNTRY", 2, 100)
  val REGISTERED_PHONE = new StringFormField("REGISTERED_PHONE", 2, 100, RegularExpression.MOBILE_NUMBER)
  val POSTAL_ADDRESS_LINE_1 = new StringFormField("POSTAL_ADDRESS_LINE_1", 4, 200)
  val POSTAL_ADDRESS_LINE_2 = new StringFormField("POSTAL_ADDRESS_LINE_2", 4, 200)
  val POSTAL_LANDMARK = new StringFormField("POSTAL_LANDMARK", 4, 100)
  val POSTAL_CITY = new StringFormField("POSTAL_CITY", 2, 100)
  val POSTAL_ZIP_CODE = new StringFormField("POSTAL_ZIP_CODE", 2, 100)
  val POSTAL_COUNTRY = new StringFormField("POSTAL_COUNTRY", 2, 100)
  val POSTAL_PHONE = new StringFormField("POSTAL_PHONE", 2, 100, RegularExpression.MOBILE_NUMBER)
  val ADDRESS_LINE_1 = new StringFormField("ADDRESS_LINE_1", 4, 200)
  val ADDRESS_LINE_2 = new StringFormField("ADDRESS_LINE_2", 4, 200)
  val LANDMARK = new StringFormField("LANDMARK", 4, 100)
  val CITY = new StringFormField("CITY", 2, 100)
  val PHONE = new StringFormField("PHONE", 2, 100, RegularExpression.MOBILE_NUMBER)
  val RELATIONSHIP = new StringFormField("RELATIONSHIP", 2, 100)
  val TITLE = new StringFormField("TITLE", 2, 100)
  val ACCOUNT_HOLDER_NAME = new StringFormField("ACCOUNT_HOLDER_NAME", 2, 100)
  val NICK_NAME = new StringFormField("NICK_NAME", 2, 100)
  val ACCOUNT_NUMBER = new StringFormField("ACCOUNT_NUMBER", 2, 100, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val BANK_NAME = new StringFormField("BANK_NAME", 2, 100)
  val SWIFT_CODE = new StringFormField("SWIFT_CODE", 2, 100, RegularExpression.SWIFT_CODE)
  val COUNTRY = new StringFormField("COUNTRY", 2, 100)
  val ZIP_CODE = new StringFormField("ZIP_CODE", 2, 100)
  val OLD_PASSWORD = new StringFormField("OLD_PASSWORD", 1, 128)
  val NEW_PASSWORD = new StringFormField("NEW_PASSWORD", 1, 128, RegularExpression.PASSWORD, Response.INVALID_PASSWORD.message)
  val CONFIRM_NEW_PASSWORD = new StringFormField("CONFIRM_NEW_PASSWORD", 1, 128, RegularExpression.PASSWORD, Response.INVALID_PASSWORD.message)
  val SEED = new StringFormField("SEED", 1, 200)
  val SEARCH_TX_HASH_HEIGHT = new StringFormField("SEARCH_TX_HASH_HEIGHT", 1, 1000)
  val DOCUMENT_TYPE = new StringFormField("DOCUMENT_TYPE", 2, 500)
  val FILE_ID = new StringFormField("FILE_ID", 2, 500)

  val ASSET_TYPE = new StringOptionFormField("ASSET_TYPE", constants.Form.ASSET_TYPE_OPTIONS)
  val DELIVERY_TERM = new StringOptionFormField("DELIVERY_TERM", constants.Form.DELIVERY_TERM_OPTIONS)
  val QUALITY = new StringOptionFormField("QUALITY", constants.Form.QUALITY_OPTIONS)
  val TRADE_TYPE = new StringOptionFormField("TRADE_TYPE", constants.Form.TRADE_TYPE_OPTIONS)
  val PHYSICAL_DOCUMENTS_HANDLED_VIA = new StringOptionFormField("PHYSICAL_DOCUMENTS_HANDLED_VIA", constants.Form.PHYSICAL_DOCUMENTS_HANDLED_VIA_OPTIONS)
  val COMDEX_PAYMENT_TERMS = new StringOptionFormField("COMDEX_PAYMENT_TERMS", constants.Form.COMDEX_PAYMENT_TERMS_OPTIONS)

  val SHIPMENT_DATE = new DateFormField("SHIPMENT_DATE")
  val INVOICE_DATE = new DateFormField("INVOICE_DATE")

  //IntFormField
  val GAS = new IntFormField("GAS", 20000, 1000000)
  val BID = new IntFormField("BID", 0, Int.MaxValue)
  val TIME = new IntFormField("TIME", 0, Int.MaxValue)
  val ASSET_QUANTITY = new IntFormField("ASSET_QUANTITY", 1, Int.MaxValue)
  val ASSET_PRICE = new IntFormField("ASSET_PRICE", 0, Int.MaxValue)
  val TRANSACTION_AMOUNT = new IntFormField("TRANSACTION_AMOUNT", 0, Int.MaxValue)
  val REDEEM_AMOUNT = new IntFormField("REDEEM_AMOUNT", 0, Int.MaxValue)
  val AMOUNT = new IntFormField("AMOUNT", 0, Int.MaxValue)
  val RATING = new IntFormField("RATING", 0, 100)

  //DateFormField
  val ESTABLISHMENT_DATE = new DateFormField("ESTABLISHMENT_DATE")

  //DoubleFormField
  val SHARE_PERCENTAGE = new DoubleFormField("SHARE_PERCENTAGE", 0.0, 100.0)
  //TODO: Error Response through Messages
  class StringFormField (fieldName: String, minimumLength: Int, maximumLength: Int, regex: Regex = RegularExpression.GENERIC, errorMessage: String = "Error Response") {
    val name: String = fieldName
    val field: Mapping[String] = text(minLength = minimumLength, maxLength = maximumLength).verifying(Constraints.pattern(regex = regex, name = regex.pattern.toString, error = errorMessage))
  }

  class StringOptionFormField(fieldName: String, option: Seq[String], errorMessage: String = "Error Response") {
    val name: String = fieldName
    val field: Mapping[String] = text.verifying(constraint = field => option contains field, error = errorMessage)
  }

  class IntFormField (fieldName: String, val minimumValue: Int, val maximumValue: Int) {
    val name: String = fieldName
    val field: Mapping[Int] =  number(min = minimumValue, max = maximumValue)
  }

  class DateFormField(fieldName: String) {
    val name: String = fieldName
    val field: Mapping[Date] = date
  }

  class DoubleFormField(fieldName: String, minimumValue: Double, maximumValue: Double) {
    val name: String = fieldName
    val field: Mapping[Double] = of(doubleFormat).verifying(Constraints.max[Double](maximumValue), Constraints.min[Double](minimumValue))
  }
}
