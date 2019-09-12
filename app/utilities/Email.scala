package utilities

import exceptions.BaseException
import javax.inject.Inject
import models.master.{Accounts, Contacts}
import play.api.Configuration
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.mailer._
import play.twirl.api.Html

import scala.concurrent.ExecutionContext

class Email @Inject()(mailerClient: MailerClient, contacts: Contacts, accounts: Accounts, messagesApi: MessagesApi)(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.UTILITIES_EMAIL

  private val fromAddress = configuration.get[String]("play.mailer.user")

  def sendEmail(subject: String, toAccountID: String, ccAccountIDs: Seq[String] = Seq.empty, bccAccountIDs: Seq[String] = Seq.empty, bodyHtml: Html, charset: Option[String] = None, replyTo: Seq[String] = Seq.empty, bounceAddress: Option[String] = None, attachments: Seq[Attachment] = Seq.empty, headers: Seq[(String, String)] = Seq.empty)(implicit lang: Lang = Lang(accounts.Service.getLanguage(toAccountID))) {
    try {
      val toEmailAddress = if(subject == constants.Email.VERIFY_EMAIL_OTP) contacts.Service.getUnverifiedEmailAddress(toAccountID) else contacts.Service.getVerifiedEmailAddress(toAccountID)
      mailerClient.send(Email(
        subject = subject,
        from = fromAddress,
        to = Seq(toEmailAddress),
        cc = contacts.Service.getVerifiedEmailAddresses(ccAccountIDs),
        bcc = contacts.Service.getVerifiedEmailAddresses(bccAccountIDs),
        bodyHtml = Option(bodyHtml.toString),
        charset = charset,
        replyTo = replyTo,
        bounceAddress = bounceAddress,
        attachments = attachments,
        headers = headers,
      ))
    }
    catch {
      case baseException: BaseException => throw new BaseException(baseException.failure)
    }
  }
}

