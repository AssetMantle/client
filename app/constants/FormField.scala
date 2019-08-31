package constants

import play.api.data.Mapping
import play.api.data.validation.Constraints

import scala.util.matching.Regex
import play.api.data.Forms.{number, text, date}
import java.util.Date

object FormField {

  val SIGNUP_USERNAME = new StringFormField("USERNAME", 3,  50, RegularExpression.ACCOUNT_ID, Response.INVALID_USERNAME.message)
  val SIGNUP_PASSWORD = new StringFormField("PASSWORD", 8, 128, RegularExpression.PASSWORD, Response.INVALID_PASSWORD.message)
  val SIGNUP_CONFIRM_PASSWORD = new StringFormField("CONFIRM_PASSWORD", 8, 128, RegularExpression.PASSWORD, Response.INVALID_PASSWORD.message)
  val USERNAME = new StringFormField("USERNAME", 3, 50, RegularExpression.ACCOUNT_ID)
  val NON_EMPTY_PASSWORD = new StringFormField("PASSWORD", 1, 128)
  val PASSWORD = new StringFormField("PASSWORD", 0, 128)
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
  val ADDRESS = new StringFormField("ADDRESS", 6, 100)
  val REQUEST_ID = new StringFormField("REQUEST_ID", 32, 32)
  val ACCOUNT_ID = new StringFormField("ACCOUNT_ID", 3, 50)
  val FIAT_PROOF_HASH = new StringFormField("FIAT_PROOF_HASH", 4, 50, RegularExpression.HASH)
  val AWB_PROOF_HASH = new StringFormField("AWB_PROOF_HASH", 4, 50, RegularExpression.HASH)
  val PEG_HASH = new StringFormField("PEG_HASH", 2, 50, RegularExpression.PEG_HASH)
  val COMMODITY_NAME = new StringFormField("COMMODITY_NAME", 2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val QUANTITY_UNIT = new StringFormField("QUANTITY_UNIT", 2, 10, RegularExpression.ALL_LETTERS)
  val TRANSACTION_ID = new StringFormField("TRANSACTION_ID", 2, 50, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val NOTIFICATION_TOKEN = new StringFormField("NOTIFICATION_TOKEN", 0, 200)
  val COMMENT = new StringFormField("COMMENT", 0, 200)
  val COUPON = new StringFormField("COUPON", 0, 50)
  val COUNTRY_CODE = new StringFormField("COUNTRY_CODE", 1, 5)
  val OTP = new StringFormField("OTP", 4, 10, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val CURRENCY = new StringFormField("CURRENCY", 2, 30, RegularExpression.ALL_LETTERS)
  val EMAIL_ADDRESS = new StringFormField("EMAIL_ADDRESS", 6, 100, RegularExpression.EMAIL_ADDRESS)
  val BUYER_CONTRACT_HASH = new StringFormField("BUYER_CONTRACT_HASH", 40, 40, RegularExpression.HASH)
  val SELLER_CONTRACT_HASH = new StringFormField("SELLER_CONTRACT_HASH", 40, 40, RegularExpression.HASH)
  val DOCUMENT_HASH = new StringFormField("DOCUMENT_HASH", 4, 50, RegularExpression.HASH)
  val FROM = new StringFormField("FROM", 45, 45)
  val MODE = new StringFormField("MODE", 4, 5)
  val TAKER_ADDRESS = new StringFormField( "TAKER_ADDRESS", 0, 45)
  val PORT_OF_LOADING = new StringFormField( "PORT_OF_LOADING", 0, 100)
  val PORT_OF_DISCHARGE = new StringFormField( "PORT_OF_DISCHARGE", 0, 100)
  val BILL_OF_LADING_NUMBER = new StringFormField("COMMODITY_NAME", 2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val SHIPPER_NAME = new StringFormField("SHIPPER_NAME", 2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val SHIPPER_ADDRESS = new StringFormField("SHIPPER_ADDRESS", 2, 100, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val NOTIFY_PARTY_NAME = new StringFormField("NOTIFY_PARTY_NAME", 2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val NOTIFY_PARTY_ADDRESS = new StringFormField("NOTIFY_PARTY_ADDRESS", 2, 100, RegularExpression.ALL_NUMBERS_ALL_LETTERS)

  val ASSET_TYPE = new StringFormFieldOption("ASSET_TYPE", constants.Option.ASSET_TYPE.options)
  val DELIVERY_TERM = new StringFormFieldOption("DELIVERY_TERM", constants.Option.DELIVERY_TERM.options)
  val QUALITY = new StringFormFieldOption("QUALITY", constants.Option.QUALITY.options)
  val TRADE_TYPE = new StringFormFieldOption("TRADE_TYPE", constants.Option.TRADE_TYPE.options)
  val COUNTRY = new StringFormFieldOption("COUNTRY", constants.Option.COUNTRY.options)
  val PHYSICAL_DOCUMENTS_HANDLED_VIA = new StringFormFieldOption("PHYSICAL_DOCUMENTS_HANDLED_VIA", constants.Option.PHYSICAL_DOCUMENTS_HANDLED_VIA.options)
  val COMDEX_PAYMENT_TERMS = new StringFormFieldOption("COMDEX_PAYMENT_TERMS", constants.Option.COMDEX_PAYMENT_TERMS.options)

  val SHIPMENT_DATE = new DateFormField("SHIPMENT_DATE")

  val OLD_PASSWORD = new StringFormField("OLD_PASSWORD", 1, 128)
  val NEW_PASSWORD = new StringFormField("NEW_PASSWORD", 1, 128, RegularExpression.PASSWORD, Response.INVALID_PASSWORD.message)
  val CONFIRM_NEW_PASSWORD = new StringFormField("CONFIRM_NEW_PASSWORD", 1, 128, RegularExpression.PASSWORD, Response.INVALID_PASSWORD.message)
  val SEED = new StringFormField("SEED", 1, 200)

  val GAS = new IntFormField("GAS", 0, 1000000)
  val BID = new IntFormField("BID", 0, Int.MaxValue)
  val TIME = new IntFormField("TIME", 0, Int.MaxValue)
  val ASSET_QUANTITY = new IntFormField("ASSET_QUANTITY", 1, Int.MaxValue)
  val ASSET_PRICE = new IntFormField("ASSET_PRICE", 0, Int.MaxValue)
  val TRANSACTION_AMOUNT = new IntFormField("TRANSACTION_AMOUNT", 0, Int.MaxValue)
  val REDEEM_AMOUNT = new IntFormField("REDEEM_AMOUNT", 0, Int.MaxValue)
  val AMOUNT = new IntFormField("AMOUNT", 0, Int.MaxValue)
  val RATING = new IntFormField("RATING", 0, 100)

  //TODO: Error Response through Messages
  class StringFormField (fieldName: String, minimumLength: Int, maximumLength: Int, regex: Regex = """.*""".r, errorMessage: String = "Error Response") {
    val name: String = fieldName
    val field: Mapping[String] =  text(minLength = minimumLength, maxLength = maximumLength).verifying(Constraints.pattern(regex = regex, error = errorMessage))
  }

  class StringFormFieldOption(fieldName: String, option: Seq[String], errorMessage: String = "Error Response") {
    val name: String = fieldName
    val field: Mapping[String] =  text.verifying(constraint = field => option contains field, error = errorMessage)
  }

  class IntFormField (fieldName: String, minimumValue: Int, maximumValue: Int) {
    val name: String = fieldName
    val field: Mapping[Int] =  number(min = minimumValue, max = maximumValue)
  }

  class DateFormField (fieldName: String) {
    val name: String = fieldName
    val field: Mapping[Date] =  date("dd-MM-yyyy")
  }

}
