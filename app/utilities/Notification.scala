package utilities

import com.twilio.Twilio
import com.twilio.`type`.PhoneNumber
import com.twilio.exception.{ApiConnectionException, ApiException}
import com.twilio.rest.api.v2010.account.Message
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{Json, OWrites}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Notification @Inject()(masterContacts: master.Contacts,
                             masterTransactionNotifications: masterTransaction.Notifications,
                             mailerClient: MailerClient,
                             masterTransactionPushNotificationTokens: masterTransaction.PushNotificationTokens,
                             wsClient: WSClient,
                             masterAccounts: master.Accounts,
                             messagesApi: MessagesApi
                            )
                            (implicit
                             executionContext: ExecutionContext,
                             configuration: Configuration
                            ) {

  private implicit val module: String = constants.Module.UTILITIES_NOTIFICATION

  private implicit val logger: Logger = Logger(this.getClass)

  private val emailFromAddress = configuration.get[String]("play.mailer.user")

  private val emailBounceAddress = configuration.get[String]("play.mailer.bounceAddress")

  private val emailReplyTo = configuration.get[String]("play.mailer.replyTo")

  private val emailCharset = configuration.get[String]("play.mailer.charset")

  private val smsAccountSID = configuration.get[String]("twilio.accountSID")

  private val smsAuthToken = configuration.get[String]("twilio.authToken")

  private val smsFromNumber = new PhoneNumber(configuration.get[String]("twilio.fromNumber"))

  private val pushNotificationURL = configuration.get[String]("pushNotification.url")

  private val pushNotificationAuthorizationKey = configuration.get[String]("pushNotification.authorizationKey")

  private case class Notification(title: String, body: String)

  private case class Data(to: String, notification: Notification)

  private implicit val notificationWrites: OWrites[Notification] = Json.writes[Notification]

  private implicit val dataWrites: OWrites[Data] = Json.writes[Data]

  private def sendSMS(accountID: String, sms: constants.Notification.SMS, messageParameters: String*)(implicit lang: Lang) = {
    try {
      Twilio.init(smsAccountSID, smsAuthToken)
      Message.creator(new PhoneNumber(masterContacts.Service.getMobileNumber(accountID)), smsFromNumber, messagesApi(sms.message, messageParameters: _*)).create()
    }
    catch {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.SMS_SEND_FAILED)
      case apiConnectionException: ApiConnectionException => logger.error(apiConnectionException.getMessage, apiConnectionException)
        throw new BaseException(constants.Response.SMS_SERVICE_CONNECTION_FAILURE)
    }
  }

  private def sendPushNotification(accountID: String, pushNotification: constants.Notification.PushNotification, messageParameters: String*)(implicit lang: Lang)=  {

    val title = messagesApi(pushNotification.title)
    val message = messagesApi(pushNotification.message, messageParameters: _*)
    val create=masterTransactionNotifications.Service.create(accountID, title, message)
    val createToken=masterTransactionNotifications.Service.create(accountID, title, message)
    def wsSend=wsClient.url(pushNotificationURL).withHttpHeaders(constants.Header.CONTENT_TYPE -> constants.Header.APPLICATION_JSON).withHttpHeaders(constants.Header.AUTHORIZATION -> pushNotificationAuthorizationKey).post(Json.toJson(Data(masterTransactionPushNotificationTokens.Service.getPushNotificationToken(accountID), Notification(title, message))))

    for{
      _<-create
      _<-createToken
      _<- wsSend
    }yield{}
  }

  private def sendEmail(toAccountID: String, email: constants.Notification.Email, messageParameters: String*)(implicit lang: Lang) = {
    try {
      val toEmailAddress = if (email == constants.Notification.VERIFY_EMAIL.email.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION))) masterContacts.Service.getUnverifiedEmailAddress(toAccountID) else masterContacts.Service.getVerifiedEmailAddress(toAccountID)
      mailerClient.send(Email(
        subject = messagesApi(email.subject),
        from = emailFromAddress,
        to = Seq(toEmailAddress),
        bodyHtml = Option(views.html.mail(messagesApi(email.message, messageParameters: _*)).toString),
        charset = Option(emailCharset),
        replyTo = Seq(emailReplyTo),
        bounceAddress = Option(emailBounceAddress),
      ))
    }
    catch {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }
  }

  def send(accountID: String, notification: constants.Notification, messagesParameters: String*)(implicit lang: Lang = Lang(masterAccounts.Service.getLanguage(accountID))): Unit = {
    try {
      if (notification.pushNotification.isDefined) sendPushNotification(accountID = accountID, pushNotification = notification.pushNotification.get, messageParameters = messagesParameters: _*)
      if (notification.email.isDefined) sendEmail(toAccountID = accountID, email = notification.email.get, messagesParameters: _*)
      if (notification.sms.isDefined) sendSMS(accountID = accountID, sms = notification.sms.get, messageParameters = messagesParameters: _*)
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }

  }

}
