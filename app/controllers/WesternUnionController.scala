package controllers

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}
import models.masterTransaction
import scala.concurrent.ExecutionContext

@Singleton
class WesternUnionController @Inject()(messagesControllerComponents: MessagesControllerComponents, playBodyParsers: PlayBodyParsers, masterTransactionWURTCBRequests: masterTransaction.WURTCBRequests)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_WESTERN_UNION

  def westernUnionForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.westernUnion())
  }

  def westernUnion = Action(parse.xml) {
    request =>
      try {
        val reqBody = (request.body \\ "request")
        masterTransactionWURTCBRequests.Service.create(reqBody.mkString.replaceAll("[\\s\\n]+", ""))
        Ok(<response><code>200</code><status>Success</status><message>Transaction update successful.</message></response>).as("application/xml")
      }
      catch {
        case _: Exception => Forbidden(<response><code>403</code><status>FORBIDDEN</status><message>Comdex validation failure – invalid request signature</message></response>).as("application/xml")
      }
  }

}
