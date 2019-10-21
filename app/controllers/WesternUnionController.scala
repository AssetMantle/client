package controllers

import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class WesternUnionController @Inject()(messagesControllerComponents: MessagesControllerComponents)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_WESTERN_UNION

  def westernUnionForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.westernUnion())
  }

  def sayHello = Action(parse.xml) { request =>
    (request.body \\ "name" headOption)
      .map(_.text)
      .map { _ =>
        Ok(<message status="OK">SUCCESS</message>).as("application/xml")
      }
      .getOrElse {
        BadRequest(<message status="KO"></message>).as("application/xml")
      }
  }

  def westernUnion: Action[AnyContent] = Action { implicit request =>
    views.companion.blockchain.ChangeBuyerBid.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.blockchain.changeBuyerBid(formWithErrors))
      },
      changeBuyerBidData => {
        try {
          Ok(views.html.index(successes = Seq(constants.Response.BUYER_BID_CHANGED)))
        }
        catch {
          case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
        }
      }
    )
  }
}
