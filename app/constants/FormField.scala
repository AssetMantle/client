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
  val MOBILE_NUMBER = new StringFormField("MOBILE_NUMBER", 6, 15, RegularExpression.MOBILE_NUMBER)
  val BLOCKCHAIN_ADDRESS = new StringFormField("BLOCKCHAIN_ADDRESS", 45, 45)
  val TO = new StringFormField("TO", 45, 45)
  val ORGANIZATION_ID = new StringFormField("ORGANIZATION_ID", 8, 16, RegularExpression.HASH)
  val NAME = new StringFormField("NAME", 2, 50)
  val ACCOUNT_ID = new StringFormField("ACCOUNT_ID", 3, 50)
  val PUSH_NOTIFICATION_TOKEN = new StringFormField("PUSH_NOTIFICATION_TOKEN", 0, 200)
  val COMMENT = new StringFormField("COMMENT", 0, 200)
  val OTP = new StringFormField("OTP", 4, 10, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val EMAIL_ADDRESS = new StringFormField("EMAIL_ADDRESS", 6, 100, RegularExpression.EMAIL_ADDRESS)
  val FROM = new StringFormField("FROM", 45, 45)
  val VESSEL_NAME = new StringFormField("VESSEL_NAME", 2, 100)
  val ADDRESS_LINE_1 = new StringFormField("ADDRESS_LINE_1", 4, 200)
  val ADDRESS_LINE_2 = new StringFormField("ADDRESS_LINE_2", 4, 200)
  val LANDMARK = new StringFormField("LANDMARK", 4, 100)
  val CITY = new StringFormField("CITY", 2, 100)
  val PHONE = new StringFormField("PHONE", 2, 100, RegularExpression.MOBILE_NUMBER)
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
  val UBO_ID = new StringFormField("UBO_ID", 2, 100)
  val COMPANY_NAME = new StringFormField("COMPANY_NAME", 2, 100)
  val TEXT = new StringFormField("TEXT", 1, 160)
  val REPLY_TO_MESSAGE = new StringFormField("REPLY_TO_MESSAGE", 2, 100)
  val CHAT_WINDOW_ID = new StringFormField("CHAT_WINDOW_ID", 2, 100)
  val INVITATION_CODE = new StringFormField("INVITATION_CODE", 4, 50)
  val ASSET_ID = new StringFormField("ASSET_ID", 1, 100)

  //SelectFormField
  val COUNTRY_CODE = new SelectFormField("COUNTRY_CODE", constants.SelectFieldOptions.COUNTRY_CODES)
  val MODE = new SelectFormField("MODE", constants.SelectFieldOptions.MODE)
  val MATCH_DECISION = new SelectFormField("MATCH_DECISION", constants.SelectFieldOptions.MATCH_DECISION)
  val ASSESSED_RISK = new SelectFormField("ASSESSED_RISK", constants.SelectFieldOptions.ASSESSED_RISK)
  val REGISTERED_COUNTRY = new SelectFormField("REGISTERED_COUNTRY", constants.SelectFieldOptions.COUNTRIES)
  val POSTAL_COUNTRY = new SelectFormField("POSTAL_COUNTRY", constants.SelectFieldOptions.COUNTRIES)
  val COUNTRY = new SelectFormField("COUNTRY", constants.SelectFieldOptions.COUNTRIES)
  val CURRENCY = new SelectFormField("CURRENCY", constants.SelectFieldOptions.CURRENCIES)

  //IntFormField
  val GAS = new IntFormField("GAS", 20000, Int.MaxValue)
  val TRANSACTION_AMOUNT = new IntFormField("TRANSACTION_AMOUNT", 0, Int.MaxValue)
  val RESULT_ID = new IntFormField("RESULT_ID", 0, Int.MaxValue)
  val SCAN_ID = new IntFormField("SCAN_ID", 0,  Int.MaxValue)
  val AMOUNT = new IntFormField("AMOUNT", 0, Int.MaxValue)

  //DateFormField
  val INVOICE_DATE = new DateFormField("INVOICE_DATE")
  val DATE_OF_BIRTH = new DateFormField("DATE_OF_BIRTH")

  //BooleanFormField
  val RECEIVE_NOTIFICATIONS = new BooleanFormField("RECEIVE_NOTIFICATIONS")
  val USERNAME_AVAILABLE = new BooleanFormField("USERNAME_AVAILABLE")
  val COMPLETION = new BooleanFormField("COMPLETION")
  val STATUS = new BooleanFormField("STATUS")
  val CONFIRM_MNEMONIC_NOTED = new BooleanFormField("CONFIRM_MNEMONIC_NOTED")

  //NestedFormField
  val ADDRESS = new NestedFormField("ADDRESS")

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
