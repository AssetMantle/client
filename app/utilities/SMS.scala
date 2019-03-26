package utilities

import com.twilio.Twilio
import com.twilio.`type`.PhoneNumber
import com.twilio.rest.api.v2010.account.Message

import javax.inject.Inject
import models.master.{Accounts, Contacts}
import play.api.Configuration
import play.api.i18n.{Lang, MessagesApi}

import scala.concurrent.ExecutionContext

class SMS @Inject()(contacts: Contacts, accounts: Accounts, messagesApi: MessagesApi)(implicit exec: ExecutionContext, configuration: Configuration) {

  def sendSMS(id: String, messageType: String, passedData: Seq[String] = Seq(""))(implicit lang: Lang = Lang(accounts.Service.getLanguageById(id))) {

    val ACCOUNT_SID = configuration.get[String]("twilio.accountSID")
    val AUTH_TOKEN = configuration.get[String]("twilio.authToken")
    Twilio.init(ACCOUNT_SID, AUTH_TOKEN)
    val from = new PhoneNumber(configuration.get[String]("twilio.fromNumber"))
    val to = new PhoneNumber("+91"+contacts.Service.getMobileNumber(id))
    val body = messagesApi("SMSMessage" + "." + messageType, passedData(0))
    Message.creator(to, from, body).create()
  }
}
