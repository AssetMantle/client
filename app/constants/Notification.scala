package constants

import constants.Notification.{Email, PushNotification, SMS}

class Notification(val notificationType: String, sendEmail: Boolean, sendPushNotification: Boolean, sendSMS: Boolean) {

  val email: Option[Email] = if (sendEmail) Option(new Email(notificationType)) else None

  val pushNotification: Option[PushNotification] = if (sendPushNotification) Option(new PushNotification(notificationType)) else None

  val sms: Option[SMS] = if (sendSMS) Option(new SMS(notificationType)) else None

}

object Notification {

  val EMAIL_PREFIX = "EMAIL"
  val PUSH_NOTIFICATION_PREFIX = "PUSH_NOTIFICATION"
  val SMS_PREFIX = "SMS"
  val SUBJECT_SUFFIX = "SUBJECT"
  val MESSAGE_SUFFIX = "MESSAGE"
  val TITLE_SUFFIX = "TITLE"
  val FROM_EMAIL_ADDRESS = "FROM_EMAIL_ADDRESS"

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

  //Invitation Emails
  val ZONE_INVITATION = new Email(notificationType = "ZONE_INVITATION")
  val TRADER_INVITATION = new Email(notificationType = "TRADER_INVITATION")

  val LOGIN = new Notification(notificationType = "LOGIN", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val SIGN_UP = new Notification(notificationType = "SIGN_UP", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val LOG_OUT = new Notification(notificationType = "LOG_OUT", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val VERIFY_PHONE = new Notification(notificationType = "VERIFY_PHONE", sendEmail = false, sendPushNotification = false, sendSMS = true)
  val PHONE_VERIFIED = new Notification(notificationType = "PHONE_VERIFIED", sendEmail = false, sendPushNotification = false, sendSMS = true)
  val VERIFY_EMAIL = new Notification(notificationType = "VERIFY_EMAIL", sendEmail = true, sendPushNotification = false, sendSMS = false)
  val EMAIL_VERIFIED = new Notification(notificationType = "EMAIL_VERIFIED", sendEmail = true, sendPushNotification = false, sendSMS = false)
  val FORGOT_PASSWORD_OTP = new Notification(notificationType = "FORGOT_PASSWORD_OTP", sendEmail = true, sendPushNotification = false, sendSMS = false)
  val EMAIL_ADDRESS_UPDATED = new Notification(notificationType = "EMAIL_ADDRESS_UPDATED", sendEmail = false, sendPushNotification = false, sendSMS = false)
  val MOBILE_NUMBER_UPDATED = new Notification(notificationType = "MOBILE_NUMBER_UPDATED", sendEmail = false, sendPushNotification = false, sendSMS = false)

  //userReviewIdentification
  val USER_REVIEWED_IDENTIFICATION_DETAILS = new Notification(notificationType = "USER_REVIEWED_IDENTIFICATION_DETAILS", sendEmail = false, sendPushNotification = true, sendSMS = false)

  val IDENTIFICATION_UPDATE = new Notification(notificationType = "IDENTIFICATION_UPDATE", sendEmail = false, sendPushNotification = true, sendSMS = false)

  val CONTRACT_SIGNED = new Notification(notificationType = "CONTRACT_SIGNED", sendEmail = false, sendPushNotification = true, sendSMS = false)

  //docusign
  val DOCUSIGN_AUTHORIZATION_PENDING = new Notification(notificationType = "DOCUSIGN_AUTHORIZATION_PENDING", sendEmail = false, sendPushNotification = true, sendSMS = false)

  val SUCCESS = new Notification(notificationType = "SUCCESS", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val FAILURE = new Notification(notificationType = "FAILURE", sendEmail = false, sendPushNotification = true, sendSMS = false)


}
