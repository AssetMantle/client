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
  val EMAIL_VERIFIED = new Notification(notificationType = "EMAIL_VERIFIED", sendEmail = false, sendPushNotification = false, sendSMS = true)
  val FORGOT_PASSWORD_OTP = new Notification(notificationType = "FORGOT_PASSWORD_OTP", sendEmail = true, sendPushNotification = false, sendSMS = false)
  val CONTACT_UPDATED = new Notification(notificationType = "CONTACT_UPDATED", sendEmail = true, sendPushNotification = false, sendSMS = false)

  //userReviewIdentification
  val USER_REVIEWED_IDENTIFICATION_DETAILS = new Notification(notificationType = "USER_REVIEWED_IDENTIFICATION_DETAILS", sendEmail = true, sendPushNotification = true, sendSMS = false)

  //inviteTrader
  val ORGANIZATION_TRADER_INVITATION = new Notification(notificationType = "ORGANIZATION_TRADER_INVITATION", sendEmail = false, sendPushNotification = true, sendSMS = false)

  //inviteZone
  val ZONE_INVITATION_SENT = new Notification(notificationType = "ZONE_INVITATION_SENT", sendEmail = false, sendPushNotification = true, sendSMS = false)

  //trader
  val ORGANIZATION_REJECTED_TRADER_REQUEST = new Notification(notificationType = "ORGANIZATION_REJECTED_TRADER_REQUEST", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val ZONE_REJECTED_TRADER_REQUEST = new Notification(notificationType = "ZONE_REJECTED_TRADER_REQUEST", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val TRADER_RELATION_REQUEST_SENT = new Notification(notificationType = "TRADER_RELATION_REQUEST_SENT", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val ORGANIZATION_TRADER_RELATION_REQUEST_SENT = new Notification(notificationType = "ORGANIZATION_TRADER_RELATION_REQUEST_SENT", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val ORGANIZATION_TRADER_RELATION_REQUEST_RECEIVED = new Notification(notificationType = "ORGANIZATION_TRADER_RELATION_REQUEST_RECEIVED", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val TRADER_RELATION_REQUEST_RECEIVED = new Notification(notificationType = "TRADER_RELATION_REQUEST_RECEIVED", sendEmail = true, sendPushNotification = true, sendSMS = false)

  //acceptOrRejectTraderRelation
  val TRADER_SENT_RELATION_REQUEST_ACCEPTED = new Notification(notificationType = "TRADER_SENT_RELATION_REQUEST_ACCEPTED", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val ORGANIZATION_TRADER_SENT_RELATION_REQUEST_ACCEPTED = new Notification(notificationType = "ORGANIZATION_TRADER_SENT_RELATION_REQUEST_ACCEPTED", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val ORGANIZATION_TRADER_RECEIVED_RELATION_REQUEST_ACCEPTED = new Notification(notificationType = "ORGANIZATION_TRADER_RECEIVED_RELATION_REQUEST_ACCEPTED", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val TRADER_RECEIVED_RELATION_REQUEST_ACCEPTED = new Notification(notificationType = "TRADER_RECEIVED_RELATION_REQUEST_ACCEPTED", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val TRADER_SENT_RELATION_REQUEST_REJECTED = new Notification(notificationType = "TRADER_SENT_RELATION_REQUEST_REJECTED", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val ORGANIZATION_TRADER_SENT_RELATION_REQUEST_REJECTED = new Notification(notificationType = "ORGANIZATION_TRADER_SENT_RELATION_REQUEST_REJECTED", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val ORGANIZATION_TRADER_RECEIVED_RELATION_REQUEST_REJECTED = new Notification(notificationType = "ORGANIZATION_TRADER_RECEIVED_RELATION_REQUEST_REJECTED", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val TRADER_RECEIVED_RELATION_REQUEST_REJECTED = new Notification(notificationType = "TRADER_RECEIVED_RELATION_REQUEST_REJECTED", sendEmail = true, sendPushNotification = true, sendSMS = false)

  //userReviewAddTraderRequest
  val USER_ADDED_OR_UPDATED_TRADER_REQUEST = new Notification(notificationType = "USER_ADDED_OR_UPDATED_TRADER_REQUEST", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val ORGANIZATION_USER_ADDED_OR_UPDATED_TRADER_REQUEST = new Notification(notificationType = "ORGANIZATION_USER_ADDED_OR_UPDATED_TRADER_REQUEST", sendEmail = true, sendPushNotification = true, sendSMS = false)

  val IDENTIFICATION_UPDATE = new Notification(notificationType = "IDENTIFICATION_UPDATE", sendEmail = false, sendPushNotification = true, sendSMS = false)

  //addOrganization
  val ADD_ORGANIZATION_REQUESTED = new Notification(notificationType = "ADD_ORGANIZATION_REQUESTED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val ORGANIZATION_REQUEST_ACCEPTED = new Notification(notificationType = "ORGANIZATION_REQUEST_ACCEPTED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val ORGANIZATION_REQUEST_REJECTED = new Notification(notificationType = "ORGANIZATION_REQUEST_REJECTED", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val ADD_ORGANIZATION_SUCCESSFUL = new Notification(notificationType = "ADD_ORGANIZATION_SUCCESSFUL", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val ADD_ORGANIZATION_FAILED = new Notification(notificationType = "ADD_ORGANIZATION_FAILED", sendEmail = true, sendPushNotification = true, sendSMS = false)

  val BLOCKCHAIN_TRANSACTION_ADD_TRADER_SUCCESSFUL = new Notification(notificationType = "BLOCKCHAIN_TRANSACTION_ADD_TRADER_SUCCESSFUL", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val TRADER_REGISTRATION_SUCCESSFUL = new Notification(notificationType = "TRADER_REGISTRATION_SUCCESSFUL", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val ORGANIZATION_TRADER_REGISTRATION_SUCCESSFUL = new Notification(notificationType = "ORGANIZATION_TRADER_REGISTRATION_SUCCESSFUL", sendEmail = true, sendPushNotification = true, sendSMS = false)
  val TRADER_REGISTRATION_FAILED = new Notification(notificationType = "D = ne", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val BLOCKCHAIN_TRANSACTION_ADD_TRADER_FAILED = new Notification(notificationType = "BLOCKCHAIN_TRANSACTION_ADD_TRADER_FAILED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val ORGANIZATION_TRADER_REGISTRATION_FAILED = new Notification(notificationType = "ORGANIZATION_TRADER_REGISTRATION_FAILED", sendEmail = true, sendPushNotification = true, sendSMS = false)

  //addZone
  val ADD_ZONE_REQUESTED = new Notification(notificationType = "ADD_ZONE_REQUESTED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val ADD_ZONE_CONFIRMED = new Notification(notificationType = "ADD_ZONE_CONFIRMED", sendEmail = false, sendPushNotification = true, sendSMS = false)

  //tradeRoom
  val SALES_QUOTE_CREATED = new Notification(notificationType = "SALES_QUOTE_CREATED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val COUNTER_PARTY_INVITED_FOR_SALES_QUOTE = new Notification(notificationType = "COUNTER_PARTY_INVITED_FOR_SALES_QUOTE", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val TRADE_ROOM_CREATED = new Notification(notificationType = "TRADE_ROOM_CREATED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val TERMS_UPDATED = new Notification(notificationType = "TERMS_UPDATED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val SALES_QUOTE_UPDATED = new Notification(notificationType = "SALES_QUOTE_UPDATED", sendEmail = false, sendPushNotification = true, sendSMS = false)

  val ASSET_ISSUED = new Notification(notificationType = "ASSET_ISSUED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val NEGOTIATION_REQUEST_SENT = new Notification(notificationType = "NEGOTIATION_REQUEST_SENT", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val ISSUE_ASSET_REQUEST_FAILED = new Notification(notificationType = "ISSUE_ASSET_REQUEST_FAILED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val NEGOTIATION_REQUEST_SENT_FAILED = new Notification(notificationType = "NEGOTIATION_REQUEST_SENT_FAILED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val NEGOTIATION_REQUEST_ACCEPTED_BLOCKCHAIN_TRANSACTION_PENDING = new Notification(notificationType = "NEGOTIATION_REQUEST_ACCEPTED_BLOCKCHAIN_TRANSACTION_PENDING", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val NEGOTIATION_REQUEST_REJECTED = new Notification(notificationType = "NEGOTIATION_REQUEST_REJECTED", sendEmail = false, sendPushNotification = true, sendSMS = false)

  val NEGOTIATION_UPDATED = new Notification(notificationType = "NEGOTIATION_UPDATED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val NEGOTIATION_UPDATE_FAILED = new Notification(notificationType = "NEGOTIATION_UPDATE_FAILED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val NEGOTIATION_ACCEPTED = new Notification(notificationType = "NEGOTIATION_ACCEPTED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val NEGOTIATION_ASSET_TERMS_UPDATED = new Notification(notificationType = "NEGOTIATION_ASSET_TERMS_UPDATED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val NEGOTIATION_PAYMENT_TERMS_UPDATED = new Notification(notificationType = "NEGOTIATION_PAYMENT_TERMS_UPDATED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED = new Notification(notificationType = "NEGOTIATION_DOCUMENT_CHECKLISTS_UPDATED", sendEmail = false, sendPushNotification = true, sendSMS = false)

  val OBL_DETAILS_ADDED = new Notification(notificationType = "OBL_DETAILS_ADDED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val INVOICE_DETAILS_ADDED = new Notification(notificationType = "INVOICE_DETAILS_ADDED", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val BUYER_CONFIRMED_ALL_NEGOTIATION_TERMS = new Notification(notificationType = "BUYER_CONFIRMED_ALL_NEGOTIATION_TERMS", sendEmail = false, sendPushNotification = true, sendSMS = false)

  val SUCCESS = new Notification(notificationType = "SUCCESS", sendEmail = false, sendPushNotification = true, sendSMS = false)
  val FAILURE = new Notification(notificationType = "FAILURE", sendEmail = false, sendPushNotification = true, sendSMS = false)


}
