package controllers

import javax.inject.Inject
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import views.forms.Login

class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents) extends MessagesAbstractController(messagesControllerComponents) {

  def index = Action { implicit request =>
    Ok(views.html.index(Login.form))
  }
}
