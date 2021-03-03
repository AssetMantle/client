package controllers

import controllers.actions.WithLoginActionAsync
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

@Singleton
class KycController @Inject() (
    withLoginActionAsync: WithLoginActionAsync,
    messagesControllerComponents: MessagesControllerComponents
)(implicit
    executionContext: ExecutionContext,
    configuration: Configuration
) extends AbstractController(messagesControllerComponents)
    with I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_NEGOTIATION

  def request(): Action[AnyContent] {}

  def requestForm(): Action[AnyContent] = {
    withLoginActionAsync { implicit loginState => implicit request =>
      Ok(views.html.component.master.kycRequest())

    }
  }
}
