package controllers

import javax.inject.Inject
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import views.forms.Login

class LoginController @Inject()(messagesControllerComponents: MessagesControllerComponents) extends MessagesAbstractController(messagesControllerComponents) {

  def login = Action { implicit request =>
    Ok(views.html.index(Login.form))
  }
}
