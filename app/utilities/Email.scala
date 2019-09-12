package utilities

import exceptions.BaseException
import javax.inject.Inject
import models.master
import play.api.Configuration
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.mailer._
import play.twirl.api.Html

import scala.concurrent.ExecutionContext

class Email @Inject()(mailerClient: MailerClient, masterContacts: master.Contacts, masterAccounts: master.Accounts, messagesApi: MessagesApi)(implicit exec: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.UTILITIES_EMAIL

  private val fromAddress = configuration.get[String]("play.mailer.user")

  private val bounceAddress = configuration.get[String]("play.mailer.user")

  private val replyTo = configuration.get[String]("play.mailer.user")

  private val charset = "UTF-8"

//  val a = new SMTPConfiguration()
//
//  private def getFromAddress(accountID: String): String = {
//    masterAccounts.Service.getUserType(accountID) match {
//      case constants.User.ORGANIZATION => fromAddress
//      case constants.User.TRADER => fromAddress
//      case _ => fromAddress
//    }
//  }
//
//  private def getBounceAddress(accountID: String): String = {
//    masterAccounts.Service.getUserType(accountID) match {
//      case constants.User.ORGANIZATION => bounceAddress
//      case constants.User.TRADER => bounceAddress
//      case _ => bounceAddress
//    }
//  }
//
//  private def getReplyToAddress(accountID: String): String = {
//    masterAccounts.Service.getUserType(accountID) match {
//      case constants.User.ORGANIZATION => replyTo
//      case constants.User.TRADER => replyTo
//      case _ => replyTo
//    }
//  }

  def sendEmail(subject: String, toAccountID: String, ccAccountIDs: Seq[String] = Seq.empty, bccAccountIDs: Seq[String] = Seq.empty, bodyHtml: Html, attachments: Seq[Attachment] = Seq.empty, headers: Seq[(String, String)] = Seq.empty)(implicit lang: Lang = Lang(masterAccounts.Service.getLanguage(toAccountID))) {
    try {
      val toEmailAddress = if(subject == constants.Email.VERIFY_EMAIL_OTP) masterContacts.Service.getUnverifiedEmailAddress(toAccountID) else masterContacts.Service.getVerifiedEmailAddress(toAccountID)
      mailerClient.send(Email(
        subject = subject,
        from = fromAddress,
        to = Seq(toEmailAddress),
        cc = masterContacts.Service.getVerifiedEmailAddresses(ccAccountIDs),
        bcc = masterContacts.Service.getVerifiedEmailAddresses(bccAccountIDs),
        bodyHtml = Option(bodyHtml.toString),
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

