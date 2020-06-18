package controllers

import controllers.actions.{WithoutLoginAction, WithoutLoginActionAsync}
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class ConfigurationController @Inject()(messagesControllerComponents: MessagesControllerComponents, withoutLoginAction: WithoutLoginAction)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  //  WARNING: This method can give any configuration value from conf file. Disabling it to prevent key store password leakage.

  //  def queryConfigurationVariable(query: String): Action[AnyContent] = withoutLoginAction { implicit request =>
  //    Ok(configuration.get[String](query))
  //  }
}
