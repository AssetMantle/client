package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{master, masterTransaction}
import models.masterTransaction.{Notification, Notifications}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationController @Inject()(
                                        messagesControllerComponents: MessagesControllerComponents,
                                        masterTransactionNotifications: masterTransaction.Notifications,
                                        withLoginAction: WithLoginAction,
                                      )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_NOTIFICATION

  def recentActivityMessages(pageNumber: Int): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>

      val notifications = if (pageNumber < 1) throw new BaseException(constants.Response.INVALID_PAGE_NUMBER) else masterTransactionNotifications.Service.get(accountID = loginState.username, pageNumber = pageNumber)

      (for {
        notifications <- notifications
      } yield Ok(views.html.component.master.recentActivityMessages(notifications = notifications))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

  def unreadNotificationCount(): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val unreadNotificationCount = masterTransactionNotifications.Service.getNumberOfUnread(loginState.username)
      (for {
        unreadNotificationCount <- unreadNotificationCount
      } yield Ok(unreadNotificationCount.toString)
        ).recover {
        case _: BaseException => NoContent
      }
  }

  def markNotificationRead(notificationID: String): Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      val markAsRead = masterTransactionNotifications.Service.markAsRead(notificationID)
      val unreadNotificationCount = masterTransactionNotifications.Service.getNumberOfUnread(loginState.username)
      (for {
        _ <- markAsRead
        unreadNotificationCount <- unreadNotificationCount
      } yield Ok(unreadNotificationCount.toString)
        ).recover {
        case _: BaseException => NoContent
      }
  }
}
