package constants

import constants.Notification.{Email, PushNotification, SMS}

class Notification(notificationType: String, sendEmail: Boolean, sendPushNotification: Boolean, sendSMS: Boolean) {

  val email: Option[Email] = if (sendEmail) Option(new Email(notificationType)) else None

  val pushNotification: Option[PushNotification] = if (sendPushNotification) Option(new PushNotification(notificationType)) else None

  val sms: Option[SMS] = if (sendSMS) Option(new SMS(notificationType)) else None

}

object Notification {

  val LOGIN = new Notification(notificationType = "LOGIN", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val SIGN_UP = new Notification(notificationType = "SIGN_UP", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val LOG_OUT = new Notification(notificationType = "LOG_OUT", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val FORGOT_PASSWORD_OTP = new Notification(notificationType = "FORGOT_PASSWORD_OTP", sendEmail = true, sendPushNotification = false, sendSMS = false)
  val VERIFY_PHONE = new Notification(notificationType = "VERIFY_PHONE", sendEmail = false, sendPushNotification = false, sendSMS = true)
  val VERIFY_EMAIL = new Notification(notificationType = "VERIFY_EMAIL", sendEmail = true, sendPushNotification = false, sendSMS = false)
  val IDENTIFICATION_UPDATE = new Notification(notificationType = "IDENTIFICATION_UPDATE", sendEmail = false, sendPushNotification = true, sendSMS = false)

  val TRADER_INVITATION = new Notification(notificationType = "TRADER_INVITATION", sendEmail = true, sendPushNotification = false, sendSMS = false)

  val ADD_ORGANIZATION_REQUESTED = new Notification(notificationType = "ADD_ORGANIZATION_REQUESTED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val ADD_ORGANIZATION_CONFIRMED = new Notification(notificationType = "ADD_ORGANIZATION_CONFIRMED", sendEmail = false, sendPushNotification = true, sendSMS = false)

  val ADD_TRADER_REQUEST = new Notification(notificationType = "ADD_TRADER_REQUEST", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val ADD_TRADER_CONFIRMED = new Notification(notificationType = "ADD_TRADER_CONFIRMED", sendEmail = false, sendPushNotification = true, sendSMS = false)

  val COUNTER_PARTY_INVITED = new Notification(notificationType = "COUNTER_PARTY_INVITED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val COUNTER_PARTY_ACCEPTED = new Notification(notificationType = "COUNTER_PARTY_ACCEPTED", sendEmail = false, sendPushNotification = true, sendSMS = false)

  val SALES_QUOTE_CREATED = new Notification(notificationType = "SALES_QUOTE_CREATED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val COUNTER_PARTY_INVITED_FOR_SALES_QUOTE = new Notification(notificationType = "COUNTER_PARTY_INVITED_FOR_SALES_QUOTE", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val TRADE_ROOM_CREATED = new Notification(notificationType = "TRADE_ROOM_CREATED", sendEmail = false, sendPushNotification = true, sendSMS = false)

  val TERMS_UPDATED = new Notification(notificationType = "TERMS_UPDATED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val SALES_QUOTE_UPDATED = new Notification(notificationType = "SALES_QUOTE_UPDATED", sendEmail = false, sendPushNotification = true, sendSMS = false)


  val SUCCESS = new Notification(notificationType = "SUCCESS", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val FAILURE = new Notification(notificationType = "FAILURE", sendEmail = false, sendPushNotification = true, sendSMS = false)
  private val EMAIL_PREFIX = "EMAIL"
  private val PUSH_NOTIFICATION_PREFIX = "PUSH_NOTIFICATION"
  private val SMS_PREFIX = "SMS"
  private val SUBJECT_SUFFIX = "SUBJECT"
  private val MESSAGE_SUFFIX = "MESSAGE"
  private val TITLE_SUFFIX = "TITLE"

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
