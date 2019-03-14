package controllers

import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

class IndexController @Inject()(messagesControllerComponents: MessagesControllerComponents)(implicit configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def index: Action[AnyContent] = Action { implicit request =>
    Console.println(request.lang)
    Ok(views.html.index())
  }
}
