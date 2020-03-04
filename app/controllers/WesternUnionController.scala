package controllers


import constants.Form
import controllers.actions.WithTraderLoginAction
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import models.masterTransaction
import play.api.{Configuration, Logger}
import controllers.results.WithUsernameToken
import models.master.{Contacts, Organization, Organizations, Traders}
import models.masterTransaction.IssueFiatRequests
import play.api.libs.json
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class WesternUnionController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                                       masterTransactionWURTCBRequests: masterTransaction.WURTCBRequests,
                                       withTraderLoginAction: WithTraderLoginAction,
                                       withUsernameToken: WithUsernameToken,
                                       issueFiatRequests: IssueFiatRequests,
                                       organizations: Organizations,
                                       traders: Traders,
                                       contacts: Contacts)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {


  private val rtcbSecretKey = configuration.get[String]("westernUnion.rtcbSecretKey")

  private val wuClientID = configuration.get[String]("westernUnion.clientID")

  private val wuServiceID = configuration.get[String]("westernUnion.serviceID")

  private val wuURL = configuration.get[String]("westernUnion.url")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_WESTERN_UNION

  def westernUnionRTCB: Action[NodeSeq] = Action.async(parse.xml) {
    request =>

      val requestBody = views.companion.master.WesternUnionRTCB.fromXml(request.body)
      val hash = rtcbSecretKey + requestBody.id + requestBody.reference + requestBody.externalReference + requestBody.invoiceNumber +
        requestBody.buyerBusinessId + requestBody.buyerFirstName + requestBody.buyerLastName + requestBody.createdDate + requestBody.lastUpdatedDate +
        requestBody.status + requestBody.dealType + requestBody.paymentTypeId + requestBody.paidOutAmount

      (if (requestBody.requestSignature == utilities.String.sha256Sum(hash)) {
        val create = masterTransactionWURTCBRequests.Service.create(requestBody.id, Json.toJson(requestBody).toString())
        val updateIssueFiatRequestRTCBStatus = issueFiatRequests.Service.markRTCBReceived(requestBody.externalReference)
        for {
          _ <- create
          _ <- updateIssueFiatRequestRTCBStatus
        } yield utilities.XMLRestResponse.TRANSACTION_UPDATE_SUCCESSFUL.result
      } else {
        Future {
          utilities.XMLRestResponse.INVALID_REQUEST_SIGNATURE.result
        }
      }
        ).recover {
        case _: Exception => utilities.XMLRestResponse.COMDEX_VALIDATION_FAILURE.result
      }
  }

  def westernUnionPortalRedirect(): Action[AnyContent] = withTraderLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.IssueFiatRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.issueFiatRequest(formWithErrors)))
        },
        issueFiatRequestData => {
          val create = issueFiatRequests.Service.create(accountID = loginState.username, transactionID = issueFiatRequestData.transactionID, transactionAmount = issueFiatRequestData.transactionAmount)
          val traderDetails = traders.Service.getByAccountID(loginState.username)
          val emailAddress = contacts.Service.getVerifiedEmailAddress(loginState.username)

          def organizationDetails(organizationID: String): Future[Organization] = organizations.Service.get(organizationID)

          (for {
            _ <- create
            emailAddress <- emailAddress
            traderDetails <- traderDetails
            organizationDetails <- organizationDetails(traderDetails.organizationID)
          } yield {
            val queryString = Map(Form.CLIENT_ID -> Seq(wuClientID), Form.CLIENT_REFERENCE -> Seq(issueFiatRequestData.transactionID),
              Form.WU_SFTP_BUYER_ID -> Seq(traderDetails.id), Form.WU_SFTP_BUYER_FIRST_NAME -> Seq(traderDetails.name), Form.WU_SFTP_BUYER_LAST_NAME -> Seq(""),
              Form.WU_SFTP_BUYER_ADDRESS -> Seq(organizationDetails.postalAddress.addressLine1, organizationDetails.postalAddress.addressLine2),
              Form.BUYER_CITY -> Seq(organizationDetails.postalAddress.city), Form.BUYER_ZIP -> Seq(organizationDetails.postalAddress.zipCode),
              Form.BUYER_EMAIL -> Seq(emailAddress), Form.SERVICE_ID -> Seq(wuServiceID),
              Form.SERVICE_AMOUNT -> Seq(issueFiatRequestData.transactionAmount.toString))
            val fullURL = utilities.String.queryURLGenerator(wuURL, queryString)
            Status(302)(fullURL)
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))

          }
        })
  }

}
