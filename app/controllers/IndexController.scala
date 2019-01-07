package controllers

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import views.forms.Login

class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents) extends MessagesAbstractController(messagesControllerComponents) with I18nSupport {

  def index = Action { implicit request =>
    Ok(views.html.login(Login.form))
  }
}
