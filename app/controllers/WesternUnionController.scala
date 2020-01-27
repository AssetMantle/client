package controllers

import javax.inject.{Inject, Singleton}
import models.masterTransaction
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class WesternUnionController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterTransactionWURTCBRequests: masterTransaction.WURTCBRequests)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val secretKey = configuration.get[String]("westernUnion.secretKey")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_WESTERN_UNION

  def westernUnionForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.component.master.westernUnion())
  }

  def westernUnion: Action[NodeSeq] = Action.async(parse.xml) {
    request =>

      val requestBody = views.companion.master.WesternUnion.Request.fromXml(request.body)
      val hash = secretKey + requestBody.id.trim + requestBody.reference.trim + requestBody.externalReference.trim + requestBody.invoiceNumber.trim +
        requestBody.buyerBusinessId.trim + requestBody.buyerFirstName.trim + requestBody.buyerLastName.trim + requestBody.createdDate.trim + requestBody.lastUpdatedDate.trim +
        requestBody.status.trim + requestBody.dealType.trim + requestBody.paymentTypeId.trim + requestBody.paidOutAmount.trim

      (if (requestBody.requestSignature.trim == utilities.String.sha256Hash(hash)) {
        val create = masterTransactionWURTCBRequests.Service.create((request.body \\ "request").mkString.replaceAll("[\\s\\n]+", ""))
        for {
          _ <- create
        } yield Ok(<response>
          <code>200</code> <status>Success</status> <message>Transaction update successful.</message>
        </response>).as("application/xml")
      } else {
        Future {
          Forbidden(<response>
            <code>403</code> <status>FORBIDDEN</status> <message>Comdex validation failure – invalid request signature</message>
          </response>).as("application/xml")
        }
      }
        ).recover {
        case _: Exception => InternalServerError(<response>
          <code>500</code> <status>INTERNAL_SERVER_ERROR</status> <message>Comdex validation failure</message>
        </response>).as("application/xml")
      }
  }
}
