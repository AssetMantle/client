package constants

import java.util.Date

import play.api.data.Forms.{boolean, date, number, of, text}
import play.api.data.Mapping
import play.api.data.format.Formats._
import play.api.data.validation.Constraints

import scala.util.matching.Regex

object FormField {
  //StringFormField
  val SIGNUP_USERNAME = new StringFormField("USERNAME", 3, 50, RegularExpression.ACCOUNT_ID, Response.INVALID_USERNAME.message)
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
  val STREET_ADDRESS = new StringFormField("STREET_ADDRESS", 6, 100)
  val REQUEST_ID = new StringFormField("REQUEST_ID", 32, 32)
  val ID = new StringFormField("ID", 32, 32)
  val ORDER_ID = new StringFormField("ORDER_ID", 32, 32)
  val ACCOUNT_ID = new StringFormField("ACCOUNT_ID", 3, 50)
  val BUYER_ACCOUNT_ID = new StringFormField("BUYER_ACCOUNT_ID", 3, 50)
  val SELLER_ACCOUNT_ID = new StringFormField("SELLER_ACCOUNT_ID", 3, 50)
  val FIAT_PROOF_HASH = new StringFormField("FIAT_PROOF_HASH", 0, 1000)
  val FIAT_PROOF = new StringFormField("FIAT_PROOF", 0, 1000)
  val AWB_PROOF_HASH = new StringFormField("AWB_PROOF_HASH", 0, 1000)
  val PEG_HASH = new StringFormField("PEG_HASH", 2, 50, RegularExpression.PEG_HASH)
  val COMMODITY_NAME = new StringFormField("COMMODITY_NAME", 2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val QUANTITY_UNIT = new StringFormField("QUANTITY_UNIT", 2, 10, RegularExpression.ALL_LETTERS)
  val TRANSACTION_ID = new StringFormField("TRANSACTION_ID", 2, 40, RegularExpression.ALL_NUMBERS_ALL_CAPITAL_LETTERS)
  val PUSH_NOTIFICATION_TOKEN = new StringFormField("PUSH_NOTIFICATION_TOKEN", 0, 200)
  val COMMENT = new StringFormField("COMMENT", 0, 200)
  val COUPON = new StringFormField("COUPON", 0, 50)
  val OTP = new StringFormField("OTP", 4, 10, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val CURRENCY = new StringFormField("CURRENCY", 2, 30, RegularExpression.ALL_LETTERS)
  val EMAIL_ADDRESS = new StringFormField("EMAIL_ADDRESS", 6, 100, RegularExpression.EMAIL_ADDRESS)
  val BUYER_CONTRACT_HASH = new StringFormField("BUYER_CONTRACT_HASH", 40, 40, RegularExpression.HASH)
  val SELLER_CONTRACT_HASH = new StringFormField("SELLER_CONTRACT_HASH", 40, 40, RegularExpression.HASH)
  val DOCUMENT_HASH = new StringFormField("DOCUMENT_HASH", 4, 500)
  val FROM = new StringFormField("FROM", 45, 45)
  val PORT_OF_LOADING = new StringFormField("PORT_OF_LOADING", 3, 100)
  val PORT_OF_DISCHARGE = new StringFormField("PORT_OF_DISCHARGE", 3, 100)
  val BILL_OF_LADING_NUMBER = new StringFormField("BILL_OF_LADING_NUMBER", 2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val SHIPPER_NAME = new StringFormField("SHIPPER_NAME", 2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val SHIPPER_ADDRESS = new StringFormField("SHIPPER_ADDRESS", 2, 100, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val NOTIFY_PARTY_NAME = new StringFormField("NOTIFY_PARTY_NAME", 2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val NOTIFY_PARTY_ADDRESS = new StringFormField("NOTIFY_PARTY_ADDRESS", 2, 100, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val INVOICE_NUMBER = new StringFormField("INVOICE_NUMBER", 2, 32)
  val CONTRACT_NUMBER = new StringFormField("CONTRACT_NUMBER", 2, 100)
  val TAKER_ADDRESS = new StringFormField("TAKER_ADDRESS", 45, 45)
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
  val MNEMONICS = new StringFormField("MNEMONICS", 1, 200)
  val SEARCH_TX_HASH_HEIGHT = new StringFormField("SEARCH_TX_HASH_HEIGHT", 1, 1000)
  val DOCUMENT_TYPE = new StringFormField("DOCUMENT_TYPE", 2, 500)
  val FILE_ID = new StringFormField("FILE_ID", 2, 500)
  val ID_NUMBER = new StringFormField("ID_NUMBER", 2, 100)
  val ID_TYPE = new StringFormField("ID_TYPE", 2, 100)
  val FIRST_NAME = new StringFormField("FIRST_NAME", 2, 100)
  val LAST_NAME = new StringFormField("LAST_NAME", 2, 100)
  val OTHER_DOCUMENTS = new StringFormField("OTHER_DOCUMENTS", 0, 1000)
  val ASSET_DESCRIPTION = new StringFormField("ASSET_DESCRIPTION", 1, 1000)
  val TRADE_ID = new StringFormField("TRADE_ID", 1, 132)
  val TERM_TYPE = new StringFormField("TERM_TYPE", 1, 100)
  val TRADE_ROOM_ID = new StringFormField("TRADE_ROOM_ID", 2, 100)
  val TEXT = new StringFormField("TEXT", 1, 160)
  val REPLY_TO_MESSAGE = new StringFormField("REPLY_TO_MESSAGE", 2, 100)
  val CHAT_WINDOW_ID = new StringFormField("CHAT_WINDOW_ID", 2, 100)
  val INVITATION_CODE = new StringFormField("INVITATION_CODE", 4, 50)
  val FROM_ID = new StringFormField("FROM_ID", 16, 16)
  val TO_ID = new StringFormField("TO_ID", 16, 16)
  val COUNTER_PARTY = new StringFormField("COUNTER_PARTY", 2, 100)
  val SALES_QUOTE_ID = new StringFormField("SALES_QUOTE_ID", 32, 32)
  val ASSET_ID = new StringFormField("ASSET_ID", 1, 100)
  val NEGOTIATION_ID = new StringFormField("NEGOTIATION_ID", 1, 100)

  //SelectFormField
  val ASSET_TYPE = new SelectFormField("ASSET_TYPE", constants.SelectFieldOptions.ASSET_TYPES)
  val DELIVERY_TERM = new SelectFormField("DELIVERY_TERM", constants.SelectFieldOptions.DELIVERY_TERMS)
  val PHYSICAL_DOCUMENTS_HANDLED_VIA = new SelectFormField("PHYSICAL_DOCUMENTS_HANDLED_VIA", constants.SelectFieldOptions.PHYSICAL_DOCUMENTS_HANDLED_VIA)
  val COUNTRY_CODE = new SelectFormField("COUNTRY_CODE", constants.SelectFieldOptions.COUNTRY_CODES)
  val MODE = new SelectFormField("MODE", constants.SelectFieldOptions.MODE)
  val REFERENCE = new SelectFormField("REFERENCE", constants.SelectFieldOptions.REFERENCE_DATES)

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
  val SHIPPING_PERIOD = new IntFormField("SHIPPING_PERIOD", 0, 1000)
  val TENURE = new IntFormField("TENURE", 0, 500)

  //DateFormField
  val ESTABLISHMENT_DATE = new DateFormField("ESTABLISHMENT_DATE")
  val SHIPMENT_DATE = new DateFormField("SHIPMENT_DATE")
  val INVOICE_DATE = new DateFormField("INVOICE_DATE")
  val DATE_OF_BIRTH = new DateFormField("DATE_OF_BIRTH")
  val TENTATIVE_DATE = new DateFormField("TENTATIVE_DATE")

  //DoubleFormField
  val SHARE_PERCENTAGE = new DoubleFormField("SHARE_PERCENTAGE", 0.0, 100.0)
  val ADVANCE_PERCENTAGE = new DoubleFormField("ADVANCE_PERCENTAGE", 0.0, 100.0)

  //BooleanFormField
  val ISSUE_ASSET = new BooleanFormField("ISSUE_ASSET")
  val ISSUE_FIAT = new BooleanFormField("ISSUE_FIAT")
  val SEND_ASSET = new BooleanFormField("SEND_ASSET")
  val SEND_FIAT = new BooleanFormField("SEND_FIAT")
  val REDEEM_ASSET = new BooleanFormField("REDEEM_ASSET")
  val REDEEM_FIAT = new BooleanFormField("REDEEM_FIAT")
  val SELLER_EXECUTE_ORDER = new BooleanFormField("SELLER_EXECUTE_ORDER")
  val BUYER_EXECUTE_ORDER = new BooleanFormField("BUYER_EXECUTE_ORDER")
  val CHANGE_BUYER_BID = new BooleanFormField("CHANGE_BUYER_BID")
  val CHANGE_SELLER_BID = new BooleanFormField("CHANGE_SELLER_BID")
  val CONFIRM_BUYER_BID = new BooleanFormField("CONFIRM_BUYER_BID")
  val CONFIRM_SELLER_BID = new BooleanFormField("CONFIRM_SELLER_BID")
  val NEGOTIATION = new BooleanFormField("NEGOTIATION")
  val RELEASE_ASSET = new BooleanFormField("RELEASE_ASSET")
  val RECEIVE_NOTIFICATIONS = new BooleanFormField("RECEIVE_NOTIFICATIONS")
  val USERNAME_AVAILABLE = new BooleanFormField("USERNAME_AVAILABLE")
  val MODERATED = new BooleanFormField("MODERATED")
  val COMPLETION = new BooleanFormField("COMPLETION")
  val STATUS = new BooleanFormField("STATUS")
  val CONFIRM_MNEMONIC_NOTED = new BooleanFormField("CONFIRM_MNEMONIC_NOTED")
  val SAME_AS_REGISTERED_ADDRESS = new BooleanFormField("SAME_AS_REGISTERED_ADDRESS")
  val BILL_OF_LADING = new BooleanFormField("BILL_OF_LADING")
  val INVOICE = new BooleanFormField("INVOICE")
  val COO = new BooleanFormField("COO")
  val COA = new BooleanFormField("COA")
  val BILL_OF_EXCHANGE = new BooleanFormField("BILL_OF_EXCHANGE")
  val PRICE = new BooleanFormField("PRICE")
  val QUANTITY = new BooleanFormField("QUANTITY")
  val BUYER_SHIPPING_PERIOD = new BooleanFormField("BUYER_SHIPPING_PERIOD")
  val BUYER_ADVANCE_PAYMENT = new BooleanFormField("BUYER_ADVANCE_PAYMENT")
  val BUYER_CREDIT = new BooleanFormField("BUYER_CREDIT")
  val BUYER_OTHER_DOCUMENTS = new BooleanFormField("BUYER_OTHER_DOCUMENTS")
  val CONFIRM = new BooleanFormField("CONFIRM")
  val DOCUMENT_LIST_COMPLETED = new BooleanFormField("DOCUMENT_LIST_COMPLETED")

  //NestedFormField
  val REGISTERED_ADDRESS = new NestedFormField("REGISTERED_ADDRESS")
  val POSTAL_ADDRESS = new NestedFormField("POSTAL_ADDRESS")
  val ADDRESS = new NestedFormField("ADDRESS")
  val UBOS = new NestedFormField("UBOS")
  val DOCUMENT_LIST = new NestedFormField("DOCUMENT_LIST")
  val CREDIT = new NestedFormField("CREDIT")

  //TODO: Error Response through Messages
  class StringFormField(fieldName: String, minimumLength: Int, maximumLength: Int, regex: Regex = RegularExpression.ANY_STRING, errorMessage: String = "Error Response") {
    val name: String = fieldName
    val field: Mapping[String] = text(minLength = minimumLength, maxLength = maximumLength).verifying(Constraints.pattern(regex = regex, name = regex.pattern.toString, error = errorMessage))
  }

  class SelectFormField(fieldName: String, val options: Seq[String], errorMessage: String = "Error Response") {
    val name: String = fieldName
    val field: Mapping[String] = text.verifying(constraint = field => options contains field, error = errorMessage)
  }

  class IntFormField(fieldName: String, val minimumValue: Int, val maximumValue: Int) {
    val name: String = fieldName
    val field: Mapping[Int] = number(min = minimumValue, max = maximumValue)
  }

  class DateFormField(fieldName: String) {
    val name: String = fieldName
    val field: Mapping[Date] = date
  }

  class DoubleFormField(fieldName: String, val minimumValue: Double, val maximumValue: Double) {
    val name: String = fieldName
    val field: Mapping[Double] = of(doubleFormat).verifying(Constraints.max[Double](maximumValue), Constraints.min[Double](minimumValue))
  }

  class BooleanFormField(fieldName: String) {
    val name: String = fieldName
    val field: Mapping[Boolean] = boolean
  }

  class NestedFormField(fieldName: String) {
    val name: String = fieldName
  }

}
