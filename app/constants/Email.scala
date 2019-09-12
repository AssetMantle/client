package constants

object Email {
  val LOGIN = "LOGIN"
  val OTP = "OTP"

  //Subjects
  val TRADER_INVITATION = "TRADER_INVITATION"
  val VERIFY_EMAIL_OTP = "VERIFY_EMAIL_OTP"
  val FORGOT_PASSWORD_EMAIL_OTP = "FORGOT_PASSWORD_EMAIL_OTP"

  class Email(subject: String, title: String, message: String)
}
