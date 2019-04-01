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

  private val accountSID = configuration.get[String]("twilio.accountSID")

  private val authToken = configuration.get[String]("twilio.authToken")

  private val from = new PhoneNumber(configuration.get[String]("twilio.fromNumber"))

  def sendSMS(accountID: String, messageType: String, passedData: Seq[String] = Seq(""))(implicit lang: Lang = Lang(accounts.Service.getLanguage(accountID))) {
    Twilio.init(accountSID, authToken)
    Message.creator(new PhoneNumber(contacts.Service.findMobileNumber(accountID)), from, messagesApi("SMSMessage" + "." + messageType, passedData(0))).create()
  }
}
