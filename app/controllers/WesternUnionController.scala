package controllers

import java.util.Locale

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents, PlayBodyParsers}
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class WesternUnionController @Inject()(messagesControllerComponents: MessagesControllerComponents, playBodyParsers: PlayBodyParsers)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_WESTERN_UNION

  def westernUnionForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.westernUnion())
  }

  def westernUnion = Action(xmlOrAny) {
    request =>
      try {
        println("yo")
        request.body match {
          case xml: NodeSeq => new Status(200)(<message status="200">SUCCESS</message>).as("application/xml")
          case _ => InternalServerError(<message status="500">FAILURE</message>).as("application/xml")
        }
      }
      catch {
        case _: Exception => Ok(<message status="500">FAILURE</message>).as("application/xml")
      }
  }

  val xmlOrAny = parse.using {
    request =>
      request.contentType.map(_.toLowerCase(Locale.ENGLISH)) match {
        case Some("application/xml") | Some("text/xml") =>
          try {play.api.mvc.BodyParsers.parse.xml }catch {
            case _: Exception => play.api.mvc.BodyParsers.parse.error(Future.successful(InternalServerError(<message status="500">FAILURE</message>).as("application/xml")))
          }
        case _ => play.api.mvc.BodyParsers.parse.error(Future.successful(UnsupportedMediaType(<message status="415">FAILURE</message>).as("application/xml")))
      }
  }

}
