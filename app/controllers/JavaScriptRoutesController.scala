package controllers

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc._
import play.api.routing._

@Singleton
class JavaScriptRoutesController @Inject()(messagesControllerComponents: MessagesControllerComponents)(implicit configuration: Configuration) extends AbstractController(messagesControllerComponents) {
  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.SignUpController.checkUsernameAvailable,
        routes.javascript.ConfigurationController.queryConfigurationVariable,
        routes.javascript.NotificationController.notificationPage,
        routes.javascript.NotificationController.markNotificationAsRead
      )
    ).as("text/javascript")
  }
}
