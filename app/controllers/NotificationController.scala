package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.masterTransaction.Notifications
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class NotificationController @Inject()(messagesControllerComponents: MessagesControllerComponents, notifications: Notifications, withLoginAction: WithLoginAction, withUsernameToken: WithUsernameToken)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_NOTIFICATION

  private val limit = configuration.get[Int]("notification.notificationsPerPage")

  def notificationPage(pageNumber: Int = 0): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(views.html.component.master.notifications(notifications.Service.get(loginState.username, pageNumber * limit, limit)))
      }
      catch {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def unreadNotificationCount(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        withUsernameToken.Ok(notifications.Service.getNumberOfUnread(loginState.username).toString)
      }
      catch {
        case _: BaseException => NoContent
      }
  }

  def markNotificationRead(notificationID: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        notifications.Service.markAsRead(notificationID)
        withUsernameToken.Ok(notifications.Service.getNumberOfUnread(loginState.username).toString)
      }
      catch {
        case _: BaseException => NoContent
      }
  }
}
