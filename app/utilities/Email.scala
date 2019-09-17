package utilities

import exceptions.BaseException
import javax.inject.Inject
import models.master
import play.api.Configuration
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.mailer._

import scala.concurrent.ExecutionContext

class Email @Inject()(mailerClient: MailerClient, masterContacts: master.Contacts, masterAccounts: master.Accounts, messagesApi: MessagesApi)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.UTILITIES_EMAIL

  private val fromAddress = configuration.get[String]("play.mailer.user")

  private val bounceAddress = configuration.get[String]("play.mailer.bounceAddress")

  private val replyTo = configuration.get[String]("play.mailer.replyTo")

  private val charset = configuration.get[String]("play.mailer.charset")

  def send(toAccountID: String, email: constants.Notification.Email, messageParameters: String*)(implicit lang: Lang = Lang(masterAccounts.Service.getLanguage(toAccountID))) {
    try {
      val toEmailAddress = if(email == constants.Notification.EMAIL_VERIFY_OTP) masterContacts.Service.getUnverifiedEmailAddress(toAccountID) else masterContacts.Service.getVerifiedEmailAddress(toAccountID)
      mailerClient.send(Email(
        subject = messagesApi(email.subject),
        from = fromAddress,
        to = Seq(toEmailAddress),
        bodyHtml = Option(views.html.mail(messagesApi(email.message, messageParameters: _*)).toString),
        charset = Option(charset),
        replyTo = Seq(replyTo),
        bounceAddress = Option(bounceAddress),
      ))
    }
    catch {
      case baseException: BaseException => throw new BaseException(baseException.failure)
    }
  }
}

