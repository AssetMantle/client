package controllers

import javax.inject.Inject
import play.api.Configuration
import play.api.mvc._
import play.api.routing._

class JavaScriptRoutesController @Inject()(messagesControllerComponents: MessagesControllerComponents)(implicit configuration: Configuration) extends AbstractController(messagesControllerComponents) {
  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.SignUpController.checkUsernameAvailable,
        routes.javascript.ConfigurationController.queryConfigurationVariable,
        routes.javascript.NotificationController.showNotifications,
        routes.javascript.NotificationController.markNotificationAsRead,
        routes.javascript.NotificationController.changeNotificationPage
      )
    ).as("text/javascript")
  }
}
