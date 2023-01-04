package controllers

import controllers.actions.WithoutLoginActionAsync
import exceptions.BaseException
import models.masterTransaction
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class NotificationController @Inject()(
                                        messagesControllerComponents: MessagesControllerComponents,
                                        masterTransactionNotifications: masterTransaction.Notifications,
                                        withoutLoginActionAsync: WithoutLoginActionAsync
                                      )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_NOTIFICATION

  def recentActivityMessages(pageNumber: Int): Action[AnyContent] = withoutLoginActionAsync { implicit loginState =>
    implicit request =>
      val notifications = if (pageNumber < 1) throw new BaseException(constants.Response.INVALID_PAGE_NUMBER) else {
        loginState match {
          case Some(login) => masterTransactionNotifications.Service.getPublic(pageNumber = pageNumber)
          case None => masterTransactionNotifications.Service.getPublic(pageNumber)
        }
      }

      (for {
        notifications <- notifications
      } yield Ok(views.html.component.master.notification.recentActivityMessages(notifications = notifications))
        ).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

}
