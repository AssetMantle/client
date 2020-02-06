package controllers


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

      val requestBody = views.companion.master.WesternUnionRTCB.Request.fromXml(request.body)
      val hash = rtcbSecretKey + requestBody.id + requestBody.reference + requestBody.externalReference + requestBody.invoiceNumber +
        requestBody.buyerBusinessId + requestBody.buyerFirstName + requestBody.buyerLastName + requestBody.createdDate + requestBody.lastUpdatedDate +
        requestBody.status + requestBody.dealType + requestBody.paymentTypeId + requestBody.paidOutAmount

      (if (requestBody.requestSignature == utilities.String.sha256Hash(hash)) {
        val create = masterTransactionWURTCBRequests.Service.create((request.body \\ "request").mkString.replaceAll("[\\s\\n]+", ""))
        val updateIssueFiatRequestRTCBStatus = issueFiatRequests.Service.markRTCBReceived(requestBody.externalReference)
        for {
          _ <- create
          _ <- updateIssueFiatRequestRTCBStatus
        } yield constants.Response.TRANSACTION_UPDATE_SUCCESSFUL.result
      } else {
        Future {
          constants.Response.INVALID_REQUEST_SIGNATURE.result
        }
      }
        ).recover {
        case _: Exception => constants.Response.COMDEX_VALIDATION_FAILURE.result
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
            val queryString = Map("clientId" -> Seq(wuClientID), "clientReference" -> Seq(issueFiatRequestData.transactionID),
              "buyer.id" -> Seq(traderDetails.id), "buyer.firstName" -> Seq(traderDetails.name), "buyer.lastName" -> Seq(""),
              "buyer.address" -> Seq(organizationDetails.postalAddress.addressLine1, organizationDetails.postalAddress.addressLine2),
              "buyer.city" -> Seq(organizationDetails.postalAddress.city), "buyer.zip" -> Seq(organizationDetails.postalAddress.zipCode),
              "buyer.email" -> Seq(emailAddress), "service1.id" -> Seq(wuServiceID),
              "service1.amount" -> Seq(issueFiatRequestData.transactionAmount.toString))
            val fullURL = utilities.String.queryURLGenerator(wuURL, queryString)
            Status(302)(fullURL)
          }
            ).recover {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))

          }
        })
  }

}
