package constants

import play.api.data.Forms.{number, text}
import play.api.data.Mapping
import play.api.data.validation.Constraints

import scala.util.matching.Regex

//TODO: Error Response through Messages
class StringFormField (minimumLength: Int, maximumLength: Int, regex: Regex = """.*""".r, errorMessage: String = "Error Response") {
  val field: Mapping[String] =  text(minLength = minimumLength, maxLength = maximumLength).verifying(Constraints.pattern(regex = regex, error = errorMessage))
}

class IntFormField (minimumValue: Int, maximumValue: Int) {
  val field: Mapping[Int] =  number(min = maximumValue, max = maximumValue)
}

object FormField {

  val SIGNUP_USERNAME = new StringFormField(3,  50, RegularExpression.ACCOUNT_ID, Response.INVALID_USERNAME.message)
  val SIGNUP_PASSWORD = new StringFormField(8, 128)
  val PASSWORD = new StringFormField(0, 128)
  val PHONE = new StringFormField(8,15, RegularExpression.MOBILE_NUMBER)
  val BLOCKCHAIN_ADDRESS = new StringFormField(45, 45)
  val ZONE_ID = new StringFormField(8, 16, RegularExpression.HASH)
  val ORGANIZATION_ID = new StringFormField(8, 16, RegularExpression.HASH)
  val TRADER_ID = new StringFormField(8, 16, RegularExpression.HASH)
  val NAME = new StringFormField(2, 50)
  val ADDRESS = new StringFormField(6, 100)
  val REQUEST_ID = new StringFormField(32, 32)
  val ACCOUNT_ID = new StringFormField(3, 50)
  val HASH = new StringFormField(4, 50, RegularExpression.HASH)
  val PEG_HASH = new StringFormField(2, 50, RegularExpression.PEG_HASH)
  val ASSET_TYPE = new StringFormField(2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val QUANTITY_UNIT = new StringFormField(2, 10, RegularExpression.ALL_LETTERS)
  val TRANSACTION_ID = new StringFormField(2, 50, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val NOTIFICATION_TOKEN = new StringFormField(0, 200)
  val COMMENT = new StringFormField(0, 200)
  val COUPON = new StringFormField(2, 50, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val COUNTRY_CODE = new StringFormField(1, 5)
  val OTP = new StringFormField(4, 10, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val CURRENCY = new StringFormField(2, 30, RegularExpression.ALL_LETTERS)

  val GAS = new IntFormField(0, 1000000)
  val BID = new IntFormField(0, Int.MaxValue)
  val BLOCK_TIME = new IntFormField(0, Int.MaxValue)
  val ASSET_QUANTITY = new IntFormField(1, Int.MaxValue)
  val ASSET_PRICE = new IntFormField(0, Int.MaxValue)
  val TRANSACTION_AMOUNT = new IntFormField(0, Int.MaxValue)
  val REDEEM_AMOUNT = new IntFormField(0, Int.MaxValue)
  val AMOUNT = new IntFormField(0, Int.MaxValue)
  val RATING = new IntFormField(0, 100)

}
