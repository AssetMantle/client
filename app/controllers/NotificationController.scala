package controllers

import constants.Security
import controllers.actions.WithLoginAction
import exceptions.{BaseException, BlockChainException}
import javax.inject.Inject
import models.masterTransaction.Notifications
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import views.companion.master.NotificationBox
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.ExecutionContext

class NotificationController @Inject()(messagesControllerComponents: MessagesControllerComponents, notifications: Notifications, withLoginAction: WithLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport{

  private implicit val module: String = constants.Module.MASTER_ACCOUNT

  private val limit = configuration.get[Int]("notification.notificationsPerPage")

  def showNotificationsForm: Action[AnyContent] = withLoginAction { implicit request =>
    Ok(views.html.component.master.notificationBox(NotificationBox.form, notifications.Service.getNotifications(request.session.get(Security.USERNAME).get, 0, limit), 1, limit, notifications.Service.getNumberOfUnread(request.session.get(Security.USERNAME).get)))
      }

  def showNotifications: Action[AnyContent] = withLoginAction { implicit request =>
    NotificationBox.form.bindFromRequest().fold(
      formWithErrors => {
        print("kkk")
        BadRequest(views.html.component.master.notificationBox(formWithErrors, notifications.Service.getNotifications(request.session.get(Security.USERNAME).get, 0, limit), 1, limit,notifications.Service.getNumberOfUnread(request.session.get(Security.USERNAME).get)))
      },
      notificationBoxData => {
        try {
          if (notificationBoxData.notificationID!="nil") {
            notifications.Service.markAsRead(notificationBoxData.notificationID)
          }
          println(notificationBoxData.pageNumber)
          Ok(views.html.component.master.notificationBox(NotificationBox.form, notifications.Service.getNotifications(request.session.get(Security.USERNAME).get, (notificationBoxData.pageNumber - 1) * limit, limit), notificationBoxData.pageNumber, limit, notifications.Service.getNumberOfUnread(request.session.get(Security.USERNAME).get)))
        }
        catch {
          case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          case blockChainException: BlockChainException => Ok(views.html.index(failure = blockChainException.message))
        }
      })
  }
}
