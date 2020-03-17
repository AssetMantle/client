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
import play.api.libs.ws.{WSClient, WSResponse}
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
    val twilioInit= Future{Twilio.init(smsAccountSID, smsAuthToken)}
    val mobileNumber=masterContacts.Service.getMobileNumber(accountID)
    (for{
      _<-twilioInit
      mobileNumber<-mobileNumber
    }yield Message.creator(new PhoneNumber(mobileNumber), smsFromNumber, messagesApi(sms.message, messageParameters: _*)).create()
      ).recover{
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.SMS_SEND_FAILED)
      case apiConnectionException: ApiConnectionException => logger.error(apiConnectionException.getMessage, apiConnectionException)
        throw new BaseException(constants.Response.SMS_SERVICE_CONNECTION_FAILURE)
    }
  }

  def send(accountID: String, notification: constants.Notification, messagesParameters: String*)(implicit lang: Lang = Lang(masterAccounts.Service.getLanguage(accountID))):Unit = {
    try {
      if (notification.pushNotification.isDefined) sendPushNotification(accountID = accountID, pushNotification = notification.pushNotification.get, messageParameters = messagesParameters: _*)
      if (notification.email.isDefined) sendEmail(toAccountID = accountID, email = notification.email.get, messagesParameters: _*)
      if (notification.sms.isDefined) sendSMS(accountID = accountID, sms = notification.sms.get, messageParameters = messagesParameters: _*)
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }
  }

  def sendTraderInvite(accountID:String,toEmail:String, messageParameters: String*)(implicit lang: Lang = Lang(masterAccounts.Service.getLanguage(accountID)))=sendTraderInviteEmail(toEmail,messageParameters = messageParameters: _*)

  def sendTraderInviteEmail(toEmail:String, messageParameters: String*)(implicit lang: Lang)={
    val email= constants.Notification.TRADER_INVITATION.email.get

    mailerClient.send(Email(
      subject = messagesApi(email.subject),
      from = emailFromAddress,
      to = Seq(toEmail),
      bodyHtml = Option(views.html.mail(messagesApi(email.message, messageParameters: _*)).toString),
      charset = Option(emailCharset),
      replyTo = Seq(emailReplyTo),
      bounceAddress = Option(emailBounceAddress),
    ))
  }

  private def sendPushNotification(accountID: String, pushNotification: constants.Notification.PushNotification, messageParameters: String*)(implicit lang: Lang) =  {

    val title=Future(messagesApi(pushNotification.title))
    val message=Future(messagesApi(pushNotification.message, messageParameters: _*))
    val pushNotificationToken=masterTransactionPushNotificationTokens.Service.getPushNotificationToken(accountID)
    def create(title:String,message:String): Future[String]=masterTransactionNotifications.Service.create(accountID, title, message)
    def post(title:String,message:String,pushNotificationToken:String)=wsClient.url(pushNotificationURL).withHttpHeaders(constants.Header.CONTENT_TYPE -> constants.Header.APPLICATION_JSON).withHttpHeaders(constants.Header.AUTHORIZATION -> pushNotificationAuthorizationKey).post(Json.toJson(Data(pushNotificationToken, Notification(title, message))))
    (for{
      title<-title
      message<-message
      _<-create(title,message)
      pushNotificationToken<-pushNotificationToken
      _<-post(title,message,pushNotificationToken)
    }yield{}).recover{
      case baseException: BaseException => logger.info(baseException.failure.message, baseException)
        throw baseException
    }
  }

  private def sendEmail(toAccountID: String, email: constants.Notification.Email, messageParameters: String*)(implicit lang: Lang): Future[String] = {

    val verifyEmail=Future(constants.Notification.VERIFY_EMAIL.email.getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)))

    def toEmailAddress(verifyEmail: constants.Notification.Email): Future[String]=if (email == verifyEmail) masterContacts.Service.getUnverifiedEmailAddress(toAccountID) else masterContacts.Service.getVerifiedEmailAddress(toAccountID)

    (for{
      verifyEmail<-verifyEmail
      toEmailAddress<-toEmailAddress(verifyEmail)
    }yield{
      mailerClient.send(Email(
        subject = messagesApi(email.subject),
        from = emailFromAddress,
        to = Seq(toEmailAddress),
        bodyHtml = Option(views.html.mail(messagesApi(email.message, messageParameters: _*)).toString),
        charset = Option(emailCharset),
        replyTo = Seq(emailReplyTo),
        bounceAddress = Option(emailBounceAddress),
      ))
    }).recover{
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }
  }
}
