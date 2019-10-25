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
class NotificationController @Inject()(messagesControllerComponents: MessagesControllerComponents, notifications: Notifications, withLoginAction: WithLoginAction, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_NOTIFICATION

  private val limit = configuration.get[Int]("notification.notificationsPerPage")

  def notificationPage(pageNumber: Int = 0): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val notification=notifications.Service.get(loginState.username, pageNumber * limit, limit)
      (for{
      notification<-notification
       }yield Ok(views.html.component.master.notifications(notification))
        ).recover{
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def unreadNotificationCount(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>

    val unreadNotificationCount=notifications.Service.getNumberOfUnread(loginState.username)
      (for{
      unreadNotificationCount<-unreadNotificationCount
    }yield  withUsernameToken.Ok(unreadNotificationCount.toString)
        ).recover{
        case _: BaseException => NoContent
      }
  }

  def markNotificationRead(notificationID: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>

    val markAsRead=notifications.Service.markAsRead(notificationID)
    val unreadNotificationCount=notifications.Service.getNumberOfUnread(loginState.username)
      (for{
      _<-markAsRead
      unreadNotificationCount<-unreadNotificationCount
    }yield withUsernameToken.Ok(unreadNotificationCount.toString)
        ).recover{
        case _: BaseException => NoContent
      }
  }
}
