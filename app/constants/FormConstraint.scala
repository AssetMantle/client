package constants

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import views.companion.master.SignUp

import scala.util.matching.Regex

object FormConstraint {
  val BLOCKCHAIN_ADDRESS_LENGTH = 45
  val PASSWORD_MINIMUM_LENGTH = 6
  val PASSWORD_MAXIMUM_LENGTH = 20
  val NAME_MINIMUM_LENGTH = 4
  val NAME_MAXIMUM_LENGTH = 25
  val USERNAME_MINIMUM_LENGTH = 3
  val USERNAME_MAXIMUM_LENGTH = 20
  val MOBILE_NUMBER_MINIMUM_LENGTH = 8
  val MOBILE_NUMBER_MAXIMUM_LENGTH = 14
  val ZONE_ID_MINIMUM_LENGTH = 8
  val ZONE_ID_MAXIMUM_LENGTH = 12
  val ORGANIZATION_ID_MINIMUM_LENGTH = 8
  val ORGANIZATION_ID_MAXIMUM_LENGTH = 12
  val TRADER_ID_MINIMUM_LENGTH = 8
  val TRADER_ID_MAXIMUM_LENGTH = 12
  val CURRENCY_MINIMUM_LENGTH = 3
  val CURRENCY_MAXIMUM_LENGTH = 3
  val REQUEST_ID_LENGTH = 32
  val HASH_MINIMUM_LENGTH = 1
  val HASH_MAXIMUM_LENGTH = 45
  val PEG_HASH_MINIMUM_LENGTH = 2
  val PEG_HASH_MAXIMUM_LENGTH = 20
  val BID_MINIMUM_VALUE = 1
  val BID_MAXIMUM_VALUE: Int = Int.MaxValue
  val BLOCK_TIME_MINIMUM_VALUE = 1
  val BLOCK_TIME_MAXIMUM_VALUE: Int = Int.MaxValue
  val GAS_MINIMUM_VALUE = 1
  val GAS_MAXIMUM_VALUE: Int = Int.MaxValue
  val PRICE_MINIMUM_VALUE = 1
  val PRICE_MAXIMUM_VALUE: Int = Int.MaxValue
  val ASSET_QUANTITY_MINIMUM_VALUE = 1
  val ASSET_QUANTITY_MAXIMUM_VALUE: Int = Int.MaxValue
  val ASSET_TYPE_MINIMUM_LENGTH = 3
  val ASSET_TYPE_MAXIMUM_LENGTH = 20
  val QUANTITY_LENGTH_MINIMUM_LENGTH = 2
  val QUANTITY_LENGTH_MAXIMUM_LENGTH = 20
  val TRANSACTION_ID_MINIMUM_LENGTH = 2
  val TRANSACTION_ID_MAXIMUM_LENGTH = 30
  val TRANSACTION_AMOUNT_MINIMUM_VALUE = 1
  val TRANSACTION_AMOUNT_MAXIMUM_VALUE: Int = Int.MaxValue
  val REDEEM_AMOUNT_MINIMUM_VALUE = 1
  val REDEEM_AMOUNT_MAXIMUM_VALUE: Int = Int.MaxValue
  val COMMENT_MINIMUM_LENGTH = 0
  val COMMENT_MAXIMUM_LENGTH = 200
  val AMOUNT_MINIMUM_VALUE = 1
  val AMOUNT_MAXIMUM_VALUE: Int = Int.MaxValue
  val RATING_MINIMUM_VALUE = 1
  val RATING_MAXIMUM_VALUE: Int = Int.MaxValue
  val COUNTRY_CODE_MINIMUM_LENGTH = 1
  val COUNTRY_CODE_MAXIMUM_LENGTH = 6
  val OTP_MINIMUM_LENGTH = 6
  val OTP_MAXIMUM_LENGTH = 6
  val COUPON_MINIMUM_LENGTH = 0
  val COUPON_MAXIMUM_LENGTH = 6
  val NOTIFICATION_TOKEN_MAXIMUM_LENGTH = 180

  val allNumbers: Regex = """\d*""".r
  val allLetters: Regex = """[A-Za-z]*""".r

  val usernameCheckConstraint: Constraint[String] = Constraint("constraints.usernameCheck")({ username: String =>
    val errors = if (username.contains(" ")) Seq(ValidationError(constants.Response.USERNAME_CONTAINS_INVALID_CHARACTER.message)) else Nil
    if (errors.isEmpty) {
      Valid
    } else {
      Invalid(errors)
    }
  })

  val passwordCheckConstraint: Constraint[String] = Constraint("constraints.passwordCheck")({ password: String =>
    val errors = password match {
      case allNumbers() => Seq(ValidationError(constants.Response.PASSWORD_IS_ALL_NUMBERS.message))
      case allLetters() => Seq(ValidationError(constants.Response.PASSWORD_IS_ALL_LETTERS.message))
      case _            => Nil
    }
    if (errors.isEmpty) {
      Valid
    } else {
      Invalid(errors)
    }
  })

  val signUpCheckConstraint: Constraint[SignUp.Data] = Constraint("constraints.signUpCheck")({ signUp: SignUp.Data =>
    val errors = {
      if (signUp.password != signUp.confirmPassword) {Seq(ValidationError(constants.Response.PASSWORDS_DO_NOT_MATCH.message))}
      else if (!signUp.usernameAvailable) Seq(ValidationError(constants.Response.USERNAME_UNAVAILABLE.message))
      else Nil
    }
    if (errors.isEmpty) {
      Valid
    } else {
      Invalid(errors)
    }
  })

  val mobileNumberCheckConstraint: Constraint[String] = Constraint("constraints.mobileNumberCheck")({ mobileNumber: String =>
    val errors = if (!mobileNumber.matches("[0-9]+")) Seq(ValidationError(constants.Response.MOBILE_NUMBER_DO_NOT_CONTAINS_ONLY_NUMBERS.message)) else Nil
    if (errors.isEmpty) {
      Valid
    } else {
      Invalid(errors)
    }
  })

}
