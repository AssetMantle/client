package constants

object Email {
  val LOGIN = "LOGIN"
  val OTP = "OTP"

  lazy val PREFIX = "EMAIL."
  lazy val SUBJECT_SUFFIX = ".SUBJECT"
  lazy val MESSAGE_SUFFIX = ".MESSAGE"

  val VERIFY_EMAIL_OTP: Email = new Email("VERIFY_EMAIL_OTP")

  val TRADER_INVITATION: Email = new Email("TRADER_INVITATION")

  val FORGOT_PASSWORD_EMAIL_OTP: Email = new Email("FORGOT_PASSWORD_EMAIL_OTP")

  class Email(private val title: String) {
    val subject: String = PREFIX + title + SUBJECT_SUFFIX
    val message: String = PREFIX + title + MESSAGE_SUFFIX

  }
}
