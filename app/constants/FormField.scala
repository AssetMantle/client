package constants

import play.api.data.Forms.{number, text}
import play.api.data.Mapping
import play.api.data.validation.Constraints

import scala.util.matching.Regex

//TODO: Error Response through Messages
class StringFormField (fieldName: String, minimumLength: Int, maximumLength: Int, regex: Regex = """.*""".r, errorMessage: String = "Error Response") {
  val name: String = fieldName
  val field: Mapping[String] =  text(minLength = minimumLength, maxLength = maximumLength).verifying(Constraints.pattern(regex = regex, error = errorMessage))
}

class IntFormField (fieldName: String, minimumValue: Int, maximumValue: Int) {
  val name: String = fieldName
  val field: Mapping[Int] =  number(min = minimumValue, max = maximumValue)
}

object FormField {

  val SIGNUP_USERNAME = new StringFormField(Form.USERNAME,3,  50, RegularExpression.ACCOUNT_ID, Response.INVALID_USERNAME.message)
  val SIGNUP_PASSWORD = new StringFormField(Form.PASSWORD, 6, 128, RegularExpression.PASSWORD, Response.INVALID_PASSWORD.message)
  val SIGNUP_CONFIRM_PASSWORD = new StringFormField(Form.CONFIRM_PASSWORD, 6, 128, RegularExpression.PASSWORD, Response.INVALID_PASSWORD.message)
  val USERNAME = new StringFormField(Form.USERNAME, 3, 50, RegularExpression.ACCOUNT_ID)
  val PASSWORD = new StringFormField(Form.PASSWORD,0, 128)
  val PHONE = new StringFormField(Form.PHONE, 8,15, RegularExpression.MOBILE_NUMBER)
  val MOBILE_NUMBER = new StringFormField(Form.MOBILE_NUMBER, 8, 15, RegularExpression.MOBILE_NUMBER)
  val BLOCKCHAIN_ADDRESS = new StringFormField(Form.BLOCKCHAIN_ADDRESS, 45, 45)
  val ACL_ADDRESS = new StringFormField(Form.ACL_ADDRESS, 45, 45)
  val SELLER_ADDRESS = new StringFormField(Form.SELLER_ADDRESS, 45, 45)
  val TO = new StringFormField(Form.TO, 45, 45)
  val BUYER_ADDRESS = new StringFormField(Form.BUYER_ADDRESS, 45, 45)
  val ZONE_ID = new StringFormField(Form.ZONE_ID, 8, 16, RegularExpression.HASH)
  val ORGANIZATION_ID = new StringFormField(Form.ORGANIZATION_ID, 8, 16, RegularExpression.HASH)
  val TRADER_ID = new StringFormField(Form.TRADER_ID, 8, 16, RegularExpression.HASH)
  val NAME = new StringFormField(Form.NAME, 2, 50)
  val ADDRESS = new StringFormField(Form.ADDRESS, 6, 100)
  val REQUEST_ID = new StringFormField(Form.REQUEST_ID, 32, 32)
  val ACCOUNT_ID = new StringFormField(Form.ACCOUNT_ID, 3, 50)
  val FIAT_PROOF_HASH = new StringFormField(Form.FIAT_PROOF_HASH, 4, 50, RegularExpression.HASH)
  val AWB_PROOF_HASH = new StringFormField(Form.AWB_PROOF_HASH, 4, 50, RegularExpression.HASH)
  val PEG_HASH = new StringFormField(Form.PEG_HASH, 2, 50, RegularExpression.PEG_HASH)
  val ASSET_TYPE = new StringFormField(Form.ASSET_TYPE, 2, 20, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val QUANTITY_UNIT = new StringFormField(Form.QUANTITY_UNIT, 2, 10, RegularExpression.ALL_LETTERS)
  val TRANSACTION_ID = new StringFormField(Form.TRANSACTION_ID, 2, 50, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val NOTIFICATION_TOKEN = new StringFormField(Form.NOTIFICATION_TOKEN, 0, 200)
  val COMMENT = new StringFormField(Form.COMMENT, 0, 200)
  val COUPON = new StringFormField(Form.COUPON, 0, 50, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val COUNTRY_CODE = new StringFormField(Form.COUNTRY_CODE, 1, 5)
  val OTP = new StringFormField(Form.OTP, 4, 10, RegularExpression.ALL_NUMBERS_ALL_LETTERS)
  val CURRENCY = new StringFormField(Form.CURRENCY, 2, 30, RegularExpression.ALL_LETTERS)
  val EMAIL = new StringFormField(Form.EMAIL, 6, 100, RegularExpression.EMAIL)
  val BUYER_CONTRACT_HASH = new StringFormField(Form.BUYER_CONTRACT_HASH, 40, 40, RegularExpression.HASH)
  val SELLER_CONTRACT_HASH = new StringFormField(Form.SELLER_CONTRACT_HASH, 40, 40, RegularExpression.HASH)
  val DOCUMENT_HASH = new StringFormField(Form.DOCUMENT_HASH, 4, 50, RegularExpression.HASH)

  val GAS = new IntFormField(Form.GAS, 0, 1000000)
  val BID = new IntFormField(Form.BID, 0, Int.MaxValue)
  val TIME = new IntFormField(Form.TIME, 0, Int.MaxValue)
  val ASSET_QUANTITY = new IntFormField(Form.ASSET_QUANTITY, 1, Int.MaxValue)
  val ASSET_PRICE = new IntFormField(Form.ASSET_PRICE, 0, Int.MaxValue)
  val TRANSACTION_AMOUNT = new IntFormField(Form.TRANSACTION_AMOUNT, 0, Int.MaxValue)
  val REDEEM_AMOUNT = new IntFormField(Form.REDEEM_AMOUNT, 0, Int.MaxValue)
  val AMOUNT = new IntFormField(Form.AMOUNT, 0, Int.MaxValue)
  val RATING = new IntFormField(Form.RATING, 0, 100)

}
