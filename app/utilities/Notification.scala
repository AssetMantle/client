package utilities

import exceptions.BaseException
import javax.inject.Inject
import play.api.{Configuration, Logger}
import play.api.i18n.MessagesApi

import scala.concurrent.ExecutionContext

class Notification @Inject()(utilitiesPushNotification: utilities.PushNotification, utilitiesEmail: utilities.Email, utilitiesSMS: utilities.SMS, messagesApi: MessagesApi)(implicit executionContext: ExecutionContext, configuration: Configuration) {

  private implicit val module: String = constants.Module.UTILITIES_PUSH_NOTIFICATION

  private implicit val logger: Logger = Logger(this.getClass)

  def send(accountID: String, notification: constants.Notification.Notification, messagesParameters: String*): Unit = {
    try {
      if (notification.pushNotification.isDefined) utilitiesPushNotification.send(username = accountID, notification = notification.pushNotification.get, messageParameters = messagesParameters: _*)
      if (notification.email.isDefined) utilitiesEmail.send(toAccountID = accountID, email = notification.email.get, messagesParameters: _*)
      if (notification.sms.isDefined) utilitiesSMS.send(accountID = accountID, sms = notification.sms.get, messageParameters = messagesParameters: _*)
    } catch {
      case baseException: BaseException => logger.error(baseException.failure.message, baseException)
        throw baseException
    }

  }

}
