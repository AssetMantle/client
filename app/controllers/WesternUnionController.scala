package controllers

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class WesternUnionController @Inject()(messagesControllerComponents: MessagesControllerComponents, playBodyParsers: PlayBodyParsers)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_WESTERN_UNION

  def westernUnionForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.westernUnion())
  }

  def westernUnion = Action(parse.xml) {
    request =>
      try {
        (request.body headOption).map(_.text)
          .map { _ =>
            Ok(<message status="0000">SUCESS</message>).as("application/xml")
          }
          .getOrElse {
            InternalServerError(<message status="500">FAILURE</message>).as("application/xml")
          }
      }
      catch {
        case _: Exception => InternalServerError(<message status="500">FAILURE</message>).as("application/xml")
      }
  }
}
