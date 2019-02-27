package controllers

import javax.inject.Inject
import play.api.Configuration
import play.api.mvc._
import play.api.routing._

class JavaScriptRoutesController @Inject()(messagesControllerComponents: MessagesControllerComponents)(implicit configuration: Configuration) extends AbstractController(messagesControllerComponents) {
  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.SignUpController.checkUsernameAvailable
      )
    ).as("text/javascript")
  }
}
