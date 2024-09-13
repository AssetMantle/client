package constants

import constants.Notification.{Email, PushNotification, SMS}
import controllers.routes
import play.api.routing.JavaScriptReverseRoute

class Notification(val notificationType: String, sendEmail: Boolean, sendPushNotification: Boolean, sendSMS: Boolean, val route: Option[JavaScriptReverseRoute] = None) {

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


  val VALIDATOR_CREATED = new Notification(notificationType = "VALIDATOR_CREATED", sendEmail = false, sendPushNotification = false, sendSMS = false /*route = Option(routes.javascript.ComponentViewController.validator)*/)
  val VALIDATOR_EDITED = new Notification(notificationType = "VALIDATOR_EDITED", sendEmail = false, sendPushNotification = false, sendSMS = false /*route = Option(routes.javascript.ComponentViewController.validator)*/)
  val VALIDATOR_UNJAILED = new Notification(notificationType = "VALIDATOR_UNJAILED", sendEmail = false, sendPushNotification = false, sendSMS = false /*route = Option(routes.javascript.ComponentViewController.validator)*/)
  val VALIDATOR_MISSING_SIGNATURE_SLASHING = new Notification(notificationType = "VALIDATOR_MISSING_SIGNATURE_SLASHING", sendEmail = false, sendPushNotification = false, sendSMS = false /*route = Option(routes.javascript.ComponentViewController.validator)*/)
  val VALIDATOR_DOUBLE_SIGNING_SLASHING = new Notification(notificationType = "VALIDATOR_DOUBLE_SIGNING_SLASHING", sendEmail = false, sendPushNotification = false, sendSMS = false /*route = Option(routes.javascript.ComponentViewController.validator)*/)
  val VALIDATOR_MISSED_BLOCKS = new Notification(notificationType = "VALIDATOR_MISSED_BLOCKS", sendEmail = false, sendPushNotification = false, sendSMS = false /*route = Option(routes.javascript.ComponentViewController.validator)*/)


  val SUCCESS = new Notification(notificationType = "SUCCESS", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val FAILURE = new Notification(notificationType = "FAILURE", sendEmail = false, sendPushNotification = true, sendSMS = false)


}
