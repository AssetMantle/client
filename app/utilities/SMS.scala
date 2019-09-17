package utilities

import com.twilio.Twilio
import com.twilio.`type`.PhoneNumber
import com.twilio.exception.{ApiConnectionException, ApiException}
import com.twilio.rest.api.v2010.account.Message
import exceptions.BaseException
import javax.inject.Inject
import models.master.{Accounts, Contacts}
import play.api.{Configuration, Logger}
import play.api.i18n.{Lang, MessagesApi}

import scala.concurrent.ExecutionContext

class SMS @Inject()(contacts: Contacts, accounts: Accounts, messagesApi: MessagesApi)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.UTILITIES_SMS

  private implicit val logger: Logger = Logger(this.getClass)

  private val accountSID = configuration.get[String]("twilio.accountSID")

  private val authToken = configuration.get[String]("twilio.authToken")

  private val from = new PhoneNumber(configuration.get[String]("twilio.fromNumber"))

  def send(accountID: String, sms: constants.Notification.SMS, messageParameters: String*)(implicit lang: Lang = Lang(accounts.Service.getLanguage(accountID))) {
    try{
      Twilio.init(accountSID, authToken)
      Message.creator(new PhoneNumber(contacts.Service.getMobileNumber(accountID)), from, messagesApi(sms.message, messageParameters: _*)).create()
    }
    catch {
      case baseException: BaseException => throw baseException
      case apiException: ApiException => logger.error(apiException.getMessage, apiException)
        throw new BaseException(constants.Response.SMS_SEND_FAILED)
      case apiConnectionException: ApiConnectionException => logger.error(apiConnectionException.getMessage, apiConnectionException)
        throw new BaseException(constants.Response.SMS_SERVICE_CONNECTION_FAILURE)
    }
  }
}
