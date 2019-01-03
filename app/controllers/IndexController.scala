package controllers

import javax.inject.Inject
import play.api.mvc.{AnyContent, MessagesAbstractController, MessagesControllerComponents, MessagesRequest}
import views.forms.Login

class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents) extends MessagesAbstractController(messagesControllerComponents) {

  def index = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(Login.form))
  }
}
