package constants

object Notification {

  private val EMAIL_PREFIX = "EMAIL"
  private val PUSH_NOTIFICATION_PREFIX = "PUSH_NOTIFICATION"
  private val SMS_PREFIX = "SMS"
  private val SUBJECT_SUFFIX = "SUBJECT"
  private val MESSAGE_SUFFIX = "MESSAGE"
  private val TITLE_SUFFIX = "TITLE"

  //LOGIN: Send Notificiation
  val LOGIN = new Notification(notificationType = "LOGIN", emailDefined = false, pushNotificationDefined = true, smsDefined = false)
  val VERIFY_PHONE = new Notification(notificationType = "VERIFY_PHONE", emailDefined = false, pushNotificationDefined = false, smsDefined = true)
  val VERIFY_EMAIL = new Notification(notificationType = "VERIFY_EMAIL", emailDefined = true, pushNotificationDefined = false, smsDefined = false)
  val TRADER_INVITATION = new Notification(notificationType = "TRADER_INVITATION", emailDefined = true, pushNotificationDefined = false, smsDefined = false)
  val FORGOT_PASSWORD_OTP = new Notification(notificationType = "FORGOT_PASSWORD_OTP", emailDefined = true, pushNotificationDefined = false, smsDefined = false)
  val SUCCESS = new Notification(notificationType = "SUCCESS", emailDefined = false, pushNotificationDefined = true, smsDefined = false)
  val FAILURE = new Notification(notificationType = "FAILURE", emailDefined = false, pushNotificationDefined = true, smsDefined = false)

  class SMS(private val title: String) {
    val message: String = Seq(SMS_PREFIX, title, MESSAGE_SUFFIX).mkString(".")
  }

  class PushNotification(private val id: String) {
    val title: String = Seq(PUSH_NOTIFICATION_PREFIX, id, TITLE_SUFFIX).mkString(".")
    val message: String = Seq(PUSH_NOTIFICATION_PREFIX, id, MESSAGE_SUFFIX).mkString(".")
  }

  class Email(private val title: String) {
    val subject: String = Seq(EMAIL_PREFIX, title, SUBJECT_SUFFIX).mkString(".")
    val message: String = Seq(EMAIL_PREFIX, title, MESSAGE_SUFFIX).mkString(".")
  }

  class Notification(notificationType: String, emailDefined: Boolean, pushNotificationDefined: Boolean, smsDefined: Boolean) {

    val email: Option[Email] = if (emailDefined) Option(new Email(notificationType)) else None

    val pushNotification: Option[PushNotification] = if (pushNotificationDefined) Option(new PushNotification(notificationType)) else None

    val sms: Option[SMS] = if (pushNotificationDefined) Option(new SMS(notificationType)) else None

  }

}
