package constants

import constants.Notification.{Email, PushNotification, SMS}

class Notification(notificationType: String, sendEmail: Boolean, sendPushNotification: Boolean, sendSMS: Boolean) {

  val email: Option[Email] = if (sendEmail) Option(new Email(notificationType)) else None

  val pushNotification: Option[PushNotification] = if (sendPushNotification) Option(new PushNotification(notificationType)) else None

  val sms: Option[SMS] = if (sendSMS) Option(new SMS(notificationType)) else None

}

object Notification {

  private val EMAIL_PREFIX = "EMAIL"
  private val PUSH_NOTIFICATION_PREFIX = "PUSH_NOTIFICATION"
  private val SMS_PREFIX = "SMS"
  private val SUBJECT_SUFFIX = "SUBJECT"
  private val MESSAGE_SUFFIX = "MESSAGE"
  private val TITLE_SUFFIX = "TITLE"

  val LOGIN = new Notification(notificationType = "LOGIN", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val VERIFY_PHONE = new Notification(notificationType = "VERIFY_PHONE", sendEmail = false, sendPushNotification = false, sendSMS = true)
  val VERIFY_EMAIL = new Notification(notificationType = "VERIFY_EMAIL", sendEmail = true, sendPushNotification = false, sendSMS = false)
  val ORGANIZATION_TRADER_INVITATION = new Notification(notificationType = "ORGANIZATION_TRADER_INVITATION", sendEmail = true, sendPushNotification = false, sendSMS = false)
  val FORGOT_PASSWORD_OTP = new Notification(notificationType = "FORGOT_PASSWORD_OTP", sendEmail = true, sendPushNotification = false, sendSMS = false)
  val SUCCESS = new Notification(notificationType = "SUCCESS", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val FAILURE = new Notification(notificationType = "FAILURE", sendEmail = false, sendPushNotification = true, sendSMS = false)

  class SMS(private val notificationType: String) {
    val message: String = Seq(SMS_PREFIX, notificationType, MESSAGE_SUFFIX).mkString(".")
  }

  class PushNotification(private val notificationType: String) {
    val title: String = Seq(PUSH_NOTIFICATION_PREFIX, notificationType, TITLE_SUFFIX).mkString(".")
    val message: String = Seq(PUSH_NOTIFICATION_PREFIX, notificationType, MESSAGE_SUFFIX).mkString(".")
  }

  class Email(private val notificationType: String) {
    val subject: String = Seq(EMAIL_PREFIX, notificationType, SUBJECT_SUFFIX).mkString(".")
    val message: String = Seq(EMAIL_PREFIX, notificationType, MESSAGE_SUFFIX).mkString(".")
  }

}
