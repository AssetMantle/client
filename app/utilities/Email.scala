package utilities

import javax.inject.Inject
import models.master.{Accounts, Contacts}
import play.api.Configuration
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.mailer._

import scala.concurrent.ExecutionContext

class Email @Inject()(mailerClient: MailerClient, contacts: Contacts, accounts: Accounts, messagesApi: MessagesApi)(implicit exec: ExecutionContext, configuration: Configuration) {

  def sendEmail(id: String, messageType: String, passedData: Seq[String] = Seq(""))(implicit lang: Lang = Lang(accounts.Service.getLanguageById(id))) {
    val email = Email(
      subject = messagesApi("EmailSubject" + "." + messageType),
      from = configuration.get[String]("play.mailer.user"),
      to = Seq(contacts.Service.getEmail(id)),
      attachments = Seq(),
      bodyText = Some(messagesApi("EmailMessage" + "." + messageType, passedData(0))),
    )
    mailerClient.send(email)
  }
}
