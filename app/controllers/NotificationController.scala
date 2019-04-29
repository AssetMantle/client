package controllers

import controllers.actions.WithLoginAction
import exceptions.BaseException
import javax.inject.Inject
import models.masterTransaction.Notifications
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

class NotificationController @Inject()(messagesControllerComponents: MessagesControllerComponents, notifications: Notifications, withLoginAction: WithLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.MASTER_ACCOUNT

  private val limit = configuration.get[Int]("notification.notificationsPerPage")

  def showNotifications(pageNumber: Int): Action[AnyContent] = withLoginAction.authenticated { username =>
    implicit request =>
      try {
        Ok(views.html.component.master.notificationWindow(notifications.Service.getNotifications(username, (pageNumber - 1) * limit, limit), pageNumber, limit, notifications.Service.getNumberOfUnread(username)))
      }
      catch {
        case baseException: BaseException => Ok(baseException.message)
      }
  }

  def markNotificationAsRead(notificationID: String): Action[AnyContent] = withLoginAction.authenticated { username =>
    implicit request =>
      try {
        notifications.Service.markAsRead(notificationID)
        Ok(notifications.Service.getNumberOfUnread(username).toString)
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
      }
  }
}
