package controllers

import javax.inject.{Inject, Singleton}
import models.masterTransaction
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}
import responses.XMLRequestHandler
import scala.concurrent.ExecutionContext
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

  def westernUnion: Action[NodeSeq] = Action(parse.xml){
    request =>
      try {
        val reqBody = request.body \\ "request"
        val id = (reqBody \\ "id").map(_.text).mkString.trim
        val reference = (reqBody \\ "reference").map(_.text).mkString.trim
        val externalReference = (reqBody \\ "externalReference").map(_.text).mkString.trim
        val invoiceNumber = (reqBody \\ "invoiceNumber").map(_.text).mkString.trim
        val buyerBusinessId = (reqBody \\ "buyerBusinessId").map(_.text).mkString.trim
        val buyerFirstName = (reqBody \\ "buyerFirstName").map(_.text).mkString.trim
        val buyerLastName = (reqBody \\ "buyerLastName").map(_.text).mkString.trim
        val createdDate = (reqBody \\ "createdDate").map(_.text).mkString.trim
        val lastUpdatedDate = (reqBody \\ "lastUpdatedDate").map(_.text).mkString.trim
        val status = (reqBody \\ "status").map(_.text).mkString.trim
        val dealType = (reqBody \\ "dealType").map(_.text).mkString.trim
        val paymentTypeId = (reqBody \\ "paymentTypeId").map(_.text).mkString.trim
        val paidOutAmount = (reqBody \\ "paidOutAmount").map(_.text).mkString.trim
        val requestSignature = (reqBody \\ "requestSignature").map(_.text).mkString.trim
        val hash = secretKey + id + reference + externalReference + invoiceNumber +
          buyerBusinessId + buyerFirstName + buyerLastName + createdDate + lastUpdatedDate +
          status + dealType + paymentTypeId + paidOutAmount
        logger.info(hash + "\n" + secretKey + "\n" + requestSignature + "\n" + utilities.String.sha256Hash(hash))
        if (requestSignature == utilities.String.sha256Hash(hash)) {
          masterTransactionWURTCBRequests.Service.create(reqBody.mkString.replaceAll("[\\s\\n]+", ""))
          Ok(<response>
            <code>200</code> <status>Success</status> <message>Transaction update successful.</message>
          </response>).as("application/xml")
        } else {
          Forbidden(<response>
            <code>403</code> <status>FORBIDDEN</status> <message>Comdex validation failure – invalid request signature</message>
          </response>).as("application/xml")
        }
      }
      catch {
        case _: Exception => InternalServerError(<response>
          <code>500</code> <status>INTERNAL_SERVER_ERROR</status> <message>Comdex validation failure</message>
        </response>).as("application/xml")
      }
  }

}
