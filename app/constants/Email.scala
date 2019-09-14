package constants

object Email {
  val LOGIN = "LOGIN"
  val OTP = "OTP"

  val VERIFY_EMAIL_OTP: Email = new Email("VERIFY_EMAIL_OTP", "VERIFY_EMAIL_OTP") {
    def message(messageParameters: Seq[String]): String = messageParameters.head
  }

  val TRADER_INVITATION: Email = new Email("TRADER_INVITATION", "TRADER_INVITATION") {
    def message(messageParameters: Seq[String]): String = messageParameters.head
  }

  val FORGOT_PASSWORD_EMAIL_OTP: Email = new Email("FORGOT_PASSWORD_EMAIL_OTP", "FORGOT_PASSWORD_EMAIL_OTP") {
    def message(messageParameters: Seq[String]): String = messageParameters.head
  }

  abstract class Email(val subject: String, val title: String) {

    def message(messageParameters: Seq[String]): String

  }
}
