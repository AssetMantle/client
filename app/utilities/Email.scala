package utilities

import exceptions.BaseException
import javax.inject.Inject
import models.master
import play.api.Configuration
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.mailer._

import scala.concurrent.ExecutionContext

class Email @Inject()(mailerClient: MailerClient, masterContacts: master.Contacts, masterAccounts: master.Accounts, messagesApi: MessagesApi)(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.UTILITIES_EMAIL

  private val fromAddress = configuration.get[String]("play.mailer.user")

  private val bounceAddress = configuration.get[String]("play.mailer.user")

  private val replyTo = configuration.get[String]("play.mailer.user")

  private val charset = "UTF-8"

  def sendEmail(toAccountID: String, email: constants.Email.Email, messageParameters: Seq[String], ccAccountIDs: Seq[String] = Seq.empty, bccAccountIDs: Seq[String] = Seq.empty, attachments: Seq[Attachment] = Seq.empty, headers: Seq[(String, String)] = Seq.empty)(implicit lang: Lang = Lang(masterAccounts.Service.getLanguage(toAccountID))) {
    try {
      val toEmailAddress = if(email == constants.Email.VERIFY_EMAIL_OTP) masterContacts.Service.getUnverifiedEmailAddress(toAccountID) else masterContacts.Service.getVerifiedEmailAddress(toAccountID)
      mailerClient.send(Email(
        subject = messagesApi(email.subject),
        from = fromAddress,
        to = Seq(toEmailAddress),
        cc = masterContacts.Service.getVerifiedEmailAddresses(ccAccountIDs),
        bcc = masterContacts.Service.getVerifiedEmailAddresses(bccAccountIDs),
        bodyHtml = Option(views.html.mail(messagesApi(email.title), messagesApi(email.message(messageParameters))).toString),
        charset = Option(charset),
        replyTo = Seq(replyTo),
        bounceAddress = Option(bounceAddress),
        attachments = attachments,
        headers = headers,
      ))
    }
    catch {
      case baseException: BaseException => throw new BaseException(baseException.failure)
    }
  }
}

