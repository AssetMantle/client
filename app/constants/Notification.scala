package constants

object Notification {

  private val EMAIL_SUBJECT_SUFFIX = ".SUBJECT"
  private val EMAIL_MESSAGE_SUFFIX = ".MESSAGE"
  private val SMS_MESSAGE_SUFFIX = ".MESSAGE"
  private val PUSH_NOTIFICATION_TITLE_SUFFIX = ".TITLE"
  private val PUSH_NOTIFICATION_MESSAGE_SUFFIX = ".MESSAGE"


  //Email
  val EMAIL_LOGIN = new Email("EMAIL_LOGIN")
  val EMAIL_VERIFY_OTP: Email = new Email("EMAIL_VERIFY_OTP")
  val EMAIL_TRADER_INVITATION: Email = new Email("EMAIL_TRADER_INVITATION")
  val EMAIL_FORGOT_PASSWORD_OTP: Email = new Email("EMAIL_FORGOT_PASSWORD_OTP")

  //SMS
  val SMS_OTP = new SMS("SMS_OTP")

  //PushNotification
  val PUSH_NOTIFICATION_LOGIN = new PushNotification("PUSH_NOTIFICATION_LOGIN")
  val PUSH_NOTIFICATION_OTP = new PushNotification("PUSH_NOTIFICATION_OTP")
  val PUSH_NOTIFICATION_SUCCESS = new PushNotification("PUSH_NOTIFICATION_SUCCESS")
  val PUSH_NOTIFICATION_FAILURE = new PushNotification("PUSH_NOTIFICATION_FAILURE")

  //Notification
  val NOTIFICATION_LOGIN = new Notification(Option(EMAIL_LOGIN), Option(PUSH_NOTIFICATION_LOGIN))

  class SMS(private val title: String) {
    val message: String = title + SMS_MESSAGE_SUFFIX
  }

  class PushNotification(private val id: String) {
    val title: String = id + PUSH_NOTIFICATION_TITLE_SUFFIX
    val message: String = id + PUSH_NOTIFICATION_MESSAGE_SUFFIX
  }

  class Email(private val title: String) {
    val subject: String = title + EMAIL_SUBJECT_SUFFIX
    val message: String = title + EMAIL_MESSAGE_SUFFIX
  }

  class Notification(val email: Option[Email] = None, val pushNotification: Option[PushNotification] = None, val sms: Option[SMS] = None)

}
