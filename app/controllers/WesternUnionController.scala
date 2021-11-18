package controllers


import constants.Form
import controllers.actions.WithLoginActionAsync
import exceptions.BaseException

import javax.inject.{Inject, Singleton}
import models.blockchain.Account
import models.{blockchain, blockchainTransaction, master, westernUnion}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}
import services.SFTPScheduler
import utilities.KeyStore

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class WesternUnionController @Inject()(
                                        blockchainAccounts: blockchain.Accounts,
                                        messagesControllerComponents: MessagesControllerComponents,
                                        masterEmails: master.Emails,
                                        westernUnionFiatRequests: westernUnion.FiatRequests,
                                        westernUnionRTCBs: westernUnion.RTCBs,
                                        withLoginActionAsync: WithLoginActionAsync,
                                        keyStore: KeyStore
                                      )(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private val rtcbSecretKey = keyStore.getPassphrase(constants.KeyStore.WESTERN_UNION_RTCB_SECRET_KEY)

  private val wuClientID = keyStore.getPassphrase(constants.KeyStore.WESTERN_UNION_CLIENT_ID)

  private val wuServiceID = keyStore.getPassphrase(constants.KeyStore.WESTERN_UNION_SERVICE_ID)

  private val wuURL = configuration.get[String]("westernUnion.url")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_WESTERN_UNION

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private implicit val otherApps: Seq[utilities.Configuration.OtherApp] = configuration.get[Seq[Configuration]]("webApp.otherApps").map { otherApp =>
    utilities.Configuration.OtherApp(url = otherApp.get[String]("url"), name = otherApp.get[String]("name"))
  }

  def westernUnionRTCB: Action[NodeSeq] = Action.async(parse.xml) {
    request =>

      val requestBody = views.companion.master.WesternUnionRTCB.fromXml(request.body)
      val hash = rtcbSecretKey + requestBody.id + requestBody.reference + requestBody.externalReference + requestBody.invoiceNumber +
        requestBody.buyerBusinessId + requestBody.buyerFirstName + requestBody.buyerLastName + requestBody.createdDate + requestBody.lastUpdatedDate +
        requestBody.status + requestBody.dealType + requestBody.paymentTypeId + requestBody.paidOutAmount

      (if (requestBody.requestSignature == utilities.String.sha256Sum(hash)) {
        val createRTCB = westernUnionRTCBs.Service.create(requestBody.id, requestBody.reference, requestBody.externalReference,
          requestBody.invoiceNumber, requestBody.buyerBusinessId, requestBody.buyerFirstName, requestBody.buyerLastName,
          utilities.Date.stringDateToTimeStamp(requestBody.createdDate), utilities.Date.stringDateToTimeStamp(requestBody.lastUpdatedDate),
          requestBody.status, requestBody.dealType, requestBody.paymentTypeId, requestBody.paidOutAmount.toInt, requestBody.requestSignature)

        def totalRTCBAmountReceived: Future[Int] = westernUnionRTCBs.Service.totalRTCBAmountByTransactionID(requestBody.externalReference)

        val fiatRequest = westernUnionFiatRequests.Service.tryGetByID(requestBody.externalReference)

        def updateIssueFiatRequestRTCBStatus(amountRequested: Int, totalRTCBAmount: Int): Future[Int] = westernUnionFiatRequests.Service.markRTCBReceived(requestBody.externalReference, amountRequested, totalRTCBAmount)

        def issueFiat = Future("ISSUE_FIAT_TRANSACTION")

        for {
          _ <- createRTCB
          fiatRequest <- fiatRequest
          totalRTCBAmountReceived <- totalRTCBAmountReceived
          _ <- updateIssueFiatRequestRTCBStatus(fiatRequest.transactionAmount, totalRTCBAmountReceived)
          _ <- issueFiat
        } yield utilities.XMLRestResponse.TRANSACTION_UPDATE_SUCCESSFUL.result
      } else Future(utilities.XMLRestResponse.INVALID_REQUEST_SIGNATURE.result)
        ).recover {
        case _: Exception => utilities.XMLRestResponse.VALIDATION_FAILURE.result
      }
  }

  def westernUnionPortalRedirect(): Action[AnyContent] = withLoginActionAsync { implicit loginState =>
    implicit request =>
      views.companion.master.IssueFiatRequest.form.bindFromRequest().fold(
        formWithErrors => {
          Future(BadRequest(views.html.component.master.issueFiatRequest(formWithErrors)))
        },
        issueFiatRequestData => {
          val emailAddress = masterEmails.Service.tryGetVerifiedEmailAddress(loginState.username)
          val account = blockchainAccounts.Service.tryGetWithAccountActor(loginState.address)

          def create(account: Account): Future[String] = westernUnionFiatRequests.Service.create(traderID = account.username, transactionAmount = issueFiatRequestData.transactionAmount)

          (for {
            emailAddress <- emailAddress
            account <- account
            transactionID <- create(account)
          } yield {
            val queryString = Map(Form.CLIENT_ID -> Seq(wuClientID), Form.CLIENT_REFERENCE -> Seq(transactionID),
              Form.WU_SFTP_BUYER_ID -> Seq(account.username), Form.WU_SFTP_BUYER_FIRST_NAME -> Seq(account.username), Form.WU_SFTP_BUYER_LAST_NAME -> Seq(account.username),
              Form.WU_SFTP_BUYER_ADDRESS -> Seq(account.address),
              Form.BUYER_CITY -> Seq(account.address), Form.BUYER_ZIP -> Seq(account.address),
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
