package controllers

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, MessagesControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class ConfigurationController @Inject()(messagesControllerComponents: MessagesControllerComponents)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def queryConfigurationVariable(query: String) = Action { implicit request =>
    Ok(configuration.get[String](query))
  }
}
