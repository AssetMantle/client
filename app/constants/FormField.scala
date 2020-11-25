package constants

import java.util.Date

import play.api.data.Forms.{boolean, date, number, of, text}
import play.api.data.Mapping
import play.api.data.format.Formats._
import play.api.data.validation.Constraints
import utilities.MicroNumber
import utilities.NumericOperation.checkPrecision

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
  val SEARCH = new StringFormField("SEARCH_TX_HASH_HEIGHT_BLOCK_HEIGHT_ADDRESS", 1, 1000)
  val ID = new StringFormField("ID", 1, 100)
  val DATA_VALUE = new StringFormField("DATA_VALUE", 1, 100)
  val DATA_NAME = new StringFormField("DATA_NAME", 1, 100)
  val FROM_ID = new StringFormField("FROM_ID", 1, 200)
  val TO_ID = new StringFormField("TO_ID", 1, 200)
  val OWNABLE_ID = new StringFormField("OWNABLE_ID", 1, 200)
  val NUB_ID = new StringFormField("NUB_ID", 1, 200)
  val CLASSIFICATION_ID = new StringFormField("CLASSIFICATION_ID", 1, 200)
  val IDENTITY_ID = new StringFormField("IDENTITY_ID", 1, 200)
  val DENOM = new StringFormField("DENOM", 1, 100)
  val MAKER_OWNABLE_ID = new StringFormField("MAKER_OWNABLE_ID", 1, 200)
  val TAKER_OWNABLE_ID = new StringFormField("TAKER_OWNABLE_ID", 1, 200)
  val ORDER_ID = new StringFormField("ORDER_ID", 1, 500)
  val LABEL = new StringFormField("LABEL", 1, 200)
  val ENTITY_ID = new StringFormField("ENTITY_ID", 1, 500)

  //SelectFormField
  val COUNTRY_CODE = new SelectFormField("COUNTRY_CODE", constants.SelectFieldOptions.COUNTRY_CODES)
  val MODE = new SelectFormField("MODE", constants.SelectFieldOptions.MODE)
  val MATCH_DECISION = new SelectFormField("MATCH_DECISION", constants.SelectFieldOptions.MATCH_DECISION)
  val ASSESSED_RISK = new SelectFormField("ASSESSED_RISK", constants.SelectFieldOptions.ASSESSED_RISK)
  val REGISTERED_COUNTRY = new SelectFormField("REGISTERED_COUNTRY", constants.SelectFieldOptions.COUNTRIES)
  val POSTAL_COUNTRY = new SelectFormField("POSTAL_COUNTRY", constants.SelectFieldOptions.COUNTRIES)
  val COUNTRY = new SelectFormField("COUNTRY", constants.SelectFieldOptions.COUNTRIES)
  val CURRENCY = new SelectFormField("CURRENCY", constants.SelectFieldOptions.CURRENCIES)
  val TOKEN_SYMBOL = new SelectFormField("TOKEN_SYMBOL", Seq.empty)
  val DATA_TYPE = new SelectFormField("DATA_TYPE", constants.SelectFieldOptions.DATA_TYPE)
  val ENTITY_TYPE = new SelectFormField("ENTITY_TYPE", constants.SelectFieldOptions.ENTITY_TYPE)

  //IntFormField
  val TRANSACTION_AMOUNT = new IntFormField("TRANSACTION_AMOUNT", 0, Int.MaxValue)
  val RESULT_ID = new IntFormField("RESULT_ID", 0, Int.MaxValue)
  val SCAN_ID = new IntFormField("SCAN_ID", 0, Int.MaxValue)
  val EXPIRES_IN = new IntFormField("EXPIRES_IN", 1, Int.MaxValue)

  //DateFormField
  val INVOICE_DATE = new DateFormField("INVOICE_DATE")
  val DATE_OF_BIRTH = new DateFormField("DATE_OF_BIRTH")

  //BooleanFormField
  val RECEIVE_NOTIFICATIONS = new BooleanFormField("RECEIVE_NOTIFICATIONS")
  val USERNAME_AVAILABLE = new BooleanFormField("USERNAME_AVAILABLE")
  val COMPLETION = new BooleanFormField("COMPLETION")
  val STATUS = new BooleanFormField("STATUS")
  val CONFIRM_MNEMONIC_NOTED = new BooleanFormField("CONFIRM_MNEMONIC_NOTED")
  val ADD_IMMUTABLE_META_FIELD = new BooleanFormField("ADD_IMMUTABLE_META_FIELD")
  val ADD_IMMUTABLE_FIELD = new BooleanFormField("ADD_IMMUTABLE_FIELD")
  val ADD_MUTABLE_META_FIELD = new BooleanFormField("ADD_MUTABLE_META_FIELD")
  val ADD_MUTABLE_FIELD = new BooleanFormField("ADD_MUTABLE_FIELD")
  val ADD_MAINTAINED_TRAITS = new BooleanFormField("ADD_MAINTAINED_TRAITS")
  val ADD_MAINTAINER = new BooleanFormField("ADD_MAINTAINER")
  val REMOVE_MAINTAINER = new BooleanFormField("REMOVE_MAINTAINER")
  val MUTATE_MAINTAINER = new BooleanFormField("MUTATE_MAINTAINER")
  val ADD_FIELD = new BooleanFormField("ADD_FIELD")

  //NestedFormField
  val ADDRESS = new NestedFormField("ADDRESS")
  val IMMUTABLE_META_TRAITS = new NestedFormField("IMMUTABLE_META_TRAITS")
  val IMMUTABLE_TRAITS = new NestedFormField("IMMUTABLE_TRAITS")
  val MUTABLE_META_TRAITS = new NestedFormField("MUTABLE_META_TRAITS")
  val MUTABLE_TRAITS = new NestedFormField("MUTABLE_TRAITS")
  val MAINTAINED_TRAITS = new NestedFormField("MAINTAINED_TRAITS")
  val COINS = new NestedFormField("COINS")
  val REVEAL_FACT = new NestedFormField("REVEAL_FACT")
  val IMMUTABLE_META_PROPERTIES = new NestedFormField("IMMUTABLE_META_PROPERTIES")
  val IMMUTABLE_PROPERTIES = new NestedFormField("IMMUTABLE_PROPERTIES")
  val MUTABLE_META_PROPERTIES = new NestedFormField("MUTABLE_META_PROPERTIES")
  val MUTABLE_PROPERTIES = new NestedFormField("MUTABLE_PROPERTIES")

  //MicroNumberFormField
  val GAS = new MicroNumberFormField("GAS", MicroNumber(0.000001), MicroNumber(10))
  val AMOUNT = new MicroNumberFormField("AMOUNT", MicroNumber(0.000001), new MicroNumber(Double.MaxValue))

  //BigDecimalFormField
  val SPLIT = new BigDecimalFormField("SPLIT", constants.Blockchain.SmallestDec, BigDecimal(Double.MaxValue))
  val TAKER_OWNABLE_SPLIT = new BigDecimalFormField("TAKER_OWNABLE_SPLIT", constants.Blockchain.SmallestDec, BigDecimal(Double.MaxValue))
  val MAKER_OWNABLE_SPLIT = new BigDecimalFormField("MAKER_OWNABLE_SPLIT", constants.Blockchain.SmallestDec, BigDecimal(Double.MaxValue))

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

  class BigDecimalFormField(fieldName: String, val minimumValue: BigDecimal, val maximumValue: BigDecimal) {
    val name: String = fieldName
    val field: Mapping[BigDecimal] = of(bigDecimalFormat).verifying(Constraints.max[BigDecimal](maximumValue), Constraints.min[BigDecimal](minimumValue))
  }

  class BooleanFormField(fieldName: String) {
    val name: String = fieldName
    val field: Mapping[Boolean] = boolean
  }

  class NestedFormField(fieldName: String) {
    val name: String = fieldName
  }

  class MicroNumberFormField(fieldName: String, val minimumValue: MicroNumber, val maximumValue: MicroNumber, precision: Int = 2) {
    val name: String = fieldName
    val field: Mapping[MicroNumber] = of(doubleFormat).verifying(Constraints.max[Double](maximumValue.toDouble), Constraints.min[Double](minimumValue.toDouble)).verifying(constants.Response.PRECISION_MORE_THAN_REQUIRED.message, x => checkPrecision(precision, x.toString)).transform[MicroNumber](x => new MicroNumber(x), y => y.toDouble)
  }

}
