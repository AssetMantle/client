package controllers

import constants.Security
import controllers.actions.WithLoginAction
import exceptions.BaseException
import javax.inject.Inject
import models.masterTransaction.Notifications
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.ExecutionContext

class NotificationController @Inject()(messagesControllerComponents: MessagesControllerComponents, notifications: Notifications, withLoginAction: WithLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.MASTER_ACCOUNT

  private val limit = configuration.get[Int]("notification.notificationsPerPage")

  def showNotifications: Action[AnyContent] = withLoginAction { implicit request =>
    Ok(views.html.component.master.notificationWindow(notifications.Service.getNotifications(request.session.get(Security.USERNAME).get, 0, limit), 1, limit, notifications.Service.getNumberOfUnread(request.session.get(Security.USERNAME).get)))
  }

  def changeNotificationPage(pageNumber: Int): Action[AnyContent] = withLoginAction { implicit request =>
    Ok(views.html.component.master.notificationWindow(notifications.Service.getNotifications(request.session.get(Security.USERNAME).get, (pageNumber - 1) * limit, limit), pageNumber, limit, notifications.Service.getNumberOfUnread(request.session.get(Security.USERNAME).get)))
  }

  def markNotificationAsRead(notificationID: String): Action[AnyContent] = withLoginAction { implicit request =>
    try {
      notifications.Service.markAsRead(notificationID)
      Ok(notifications.Service.getNumberOfUnread(request.session.get(Security.USERNAME).get).toString)
    }
    catch {
      case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
    }
  }
}
