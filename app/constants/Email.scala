package constants

object Email {
  val LOGIN = "LOGIN"
  val OTP = "OTP"

  val VERIFY_EMAIL_OTP: Email = new Email("SUBJECT_VERIFY_EMAIL_OTP", "TITLE_VERIFY_EMAIL_OTP") {
    def message(messageParameters: Seq[String]): String = messageParameters.head
  }

  val TRADER_INVITATION: Email = new Email("SUBJECT_TRADER_INVITATION", "TITLE_TRADER_INVITATION") {
    def message(messageParameters: Seq[String]): String = messageParameters.head
  }

  val FORGOT_PASSWORD_EMAIL_OTP: Email = new Email("SUBJECT_FORGOT_PASSWORD_EMAIL_OTP", "TITLE_FORGOT_PASSWORD_EMAIL_OTP") {
    def message(messageParameters: Seq[String]): String = messageParameters.head
  }

  abstract class Email(val subject: String, val title: String) {

    def message(messageParameters: Seq[String]): String

  }
}
