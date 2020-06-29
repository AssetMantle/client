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
import play.api.libs.mailer._
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Notification @Inject()(masterTransactionNotifications: masterTransaction.Notifications,
                             masterEmails: master.Emails,
                             masterMobiles: master.Mobiles,
                             mailerClient: MailerClient,
                             masterTransactionPushNotificationTokens: masterTransaction.PushNotificationTokens,
                             wsClient: WSClient,
                             masterAccounts: master.Accounts,
                             messagesApi: MessagesApi,
                             keyStore: KeyStore
                            )
                            (implicit
                             executionContext: ExecutionContext,
                             configuration: Configuration
                            ) {

  private implicit val module: String = constants.Module.UTILITIES_NOTIFICATION

  private implicit val logger: Logger = Logger(this.getClass)

  private val emailBounceAddress = configuration.get[String]("play.mailer.bounceAddress")

  private val emailReplyTo = configuration.get[String]("play.mailer.replyTo")

  private val emailCharset = configuration.get[String]("play.mailer.charset")

  private val smsAccountSID = keyStore.getPassphrase(constants.KeyStore.TWILIO_SMS_ACCOUNT_SID)

  private val smsFromNumber = new PhoneNumber(keyStore.getPassphrase(constants.KeyStore.TWILIO_SMS_FROM_NUMBER))

  private val smsAuthToken = keyStore.getPassphrase(constants.KeyStore.TWILIO_SMS_AUTH_TOKEN)

  Twilio.init(smsAccountSID, smsAuthToken)

  private val pushNotificationURL = configuration.get[String]("pushNotification.url")

  private val pushNotificationAuthorizationKey = keyStore.getPassphrase(constants.KeyStore.PUSH_NOTIFICATION_AUTHORIZATION_KEY)

  private case class Notification(title: String, body: String)

  private case class Data(to: String, notification: Notification)

  private implicit val notificationWrites: OWrites[Notification] = Json.writes[Notification]

  private implicit val dataWrites: OWrites[Data] = Json.writes[Data]

  private def sendSMS(mobileNumber: String, sms: constants.Notification.SMS, messageParameters: String*)(implicit lang: Lang): Future[Unit] = {

    val send = Future(Message.creator(new PhoneNumber(mobileNumber), smsFromNumber, messagesApi(sms.message, messageParameters: _*)).create())

    (for {
      _ <- send
    } yield ()
      ).recover {
      case baseException: BaseException => throw baseException
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.SMS_SEND_FAILED)
      case apiConnectionException: ApiConnectionException => logger.error(apiConnectionException.getMessage, apiConnectionException)
        throw new BaseException(constants.Response.SMS_SERVICE_CONNECTION_FAILURE)
    }
  }

  private def sendPushNotification(accountID: String, pushNotification: constants.Notification.PushNotification, messageParameters: String*)(implicit lang: Lang): Future[Unit] = {

    val title = Future(messagesApi(pushNotification.title))
    val message = Future(messagesApi(pushNotification.message, messageParameters: _*))
    val pushNotificationToken = masterTransactionPushNotificationTokens.Service.getPushNotificationToken(accountID)

    def post(title: String, message: String, pushNotificationToken: String) = wsClient.url(pushNotificationURL).withHttpHeaders(constants.Header.CONTENT_TYPE -> constants.Header.APPLICATION_JSON).withHttpHeaders(constants.Header.AUTHORIZATION -> ("key=" + pushNotificationAuthorizationKey)).post(Json.toJson(Data(pushNotificationToken, Notification(title, message))))

    (for {
      title <- title
      message <- message
      pushNotificationToken <- pushNotificationToken
      _ <- if (pushNotificationToken.isDefined) post(title, message, pushNotificationToken.get) else Future(None)
    } yield ()
      ).recover {
      case baseException: BaseException => logger.info(baseException.failure.message, baseException)
        throw baseException
    }
  }

  private def sendEmail(emailAddress: String, email: constants.Notification.Email, messageParameters: String*)(implicit lang: Lang) = {
    mailerClient.send(Email(
      subject = messagesApi(email.subject),
      from = messagesApi(constants.Notification.FROM_EMAIL_ADDRESS, emailReplyTo),
      to = Seq(emailAddress),
      bodyHtml = Option(views.html.mail(messagesApi(email.message, messageParameters: _*)).toString),
      charset = Option(emailCharset),
      replyTo = Seq(emailReplyTo),
      bounceAddress = Option(emailBounceAddress),
    ))
  }

  def sendEmailToEmailAddress(fromAccountID: String, emailAddress: String, email: constants.Notification.Email, messageParameters: String*): Future[String] = {
    val language = masterAccounts.Service.tryGetLanguage(fromAccountID)
    (for {
      language <- language
    } yield sendEmail(emailAddress = emailAddress, email = email, messageParameters = messageParameters: _*)(Lang(language))
      ).recover {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }

  }

  private def sendEmailByAccountID(accountID: String, email: constants.Notification.Email, messageParameters: String*)(implicit lang: Lang): Future[String] = {

    val emailAddress: Future[String] = masterEmails.Service.tryGetVerifiedEmailAddress(accountID)

    (for {
      emailAddress <- emailAddress
    } yield sendEmail(emailAddress = emailAddress, email = email, messageParameters = messageParameters: _*)
      ).recover {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }
  }

  def sendSMSToMobileNumber(fromAccountID: String, mobileNumber: String, sms: constants.Notification.SMS, messageParameters: String*): Future[Unit] = {
    val language = masterAccounts.Service.tryGetLanguage(fromAccountID)
    (for {
      language <- language
      _ <- sendSMS(mobileNumber = mobileNumber, sms = sms, messageParameters = messageParameters: _*)(Lang(language))
    } yield ()
      ).recover {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }
  }

  private def sendSMSByAccountID(accountID: String, sms: constants.Notification.SMS, messageParameters: String*)(implicit lang: Lang): Future[Unit] = {

    val mobileNumber: Future[String] = masterMobiles.Service.tryGetVerifiedMobileNumber(accountID)

    (for {
      mobileNumber <- mobileNumber
      _ <- sendSMS(mobileNumber = mobileNumber, sms = sms, messageParameters = messageParameters: _*)
    } yield ()).recover {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }
  }

  def send(accountID: String, notification: constants.Notification, messagesParameters: String*): Future[String] = {
    val language = masterAccounts.Service.tryGetLanguage(accountID)
    val notificationID = masterTransactionNotifications.Service.create(accountID, notification = notification, messagesParameters: _*)

    def pushNotification(implicit language: Lang): Future[Unit] = if (notification.pushNotification.isDefined) sendPushNotification(accountID = accountID, pushNotification = notification.pushNotification.get, messageParameters = messagesParameters: _*) else Future()

    def email(implicit language: Lang): Future[String] = if (notification.email.isDefined) sendEmailByAccountID(accountID = accountID, email = notification.email.get, messagesParameters: _*) else Future("")

    def sms(implicit language: Lang): Future[Unit] = if (notification.sms.isDefined) sendSMSByAccountID(accountID = accountID, sms = notification.sms.get, messageParameters = messagesParameters: _*) else Future()

    (for {
      language <- language
      notificationID <- notificationID
      _ <- pushNotification(Lang(language))
      _ <- email(Lang(language))
      _ <- sms(Lang(language))
    } yield notificationID).recover {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }
  }

}
