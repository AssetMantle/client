package controllers


import constants.Form
import controllers.actions.{WithTraderLoginAction, WithZoneLoginAction}
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.{blockchain, blockchainTransaction, master, westernUnion}
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Configuration, Logger}
import services.SFTPScheduler

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

@Singleton
class WesternUnionController @Inject()(
                                        blockchainAccounts: blockchain.Accounts,
                                        messagesControllerComponents: MessagesControllerComponents,
                                        blockchainTransactionIssueFiats: blockchainTransaction.IssueFiats,
                                        sftpScheduler: SFTPScheduler,
                                        masterOrganizations: master.Organizations,
                                        masterTraders: master.Traders,
                                        masterZones: master.Zones,
                                        masterEmails: master.Emails,
                                        masterIdentifications: master.Identifications,
                                        masterAccounts: master.Accounts,
                                        masterFiats: master.Fiats,
                                        westernUnionFiatRequests: westernUnion.FiatRequests,
                                        westernUnionRTCBs: westernUnion.RTCBs,
                                        transactionsIssueFiat: transactions.IssueFiat,
                                        transaction: utilities.Transaction,
                                        withTraderLoginAction: WithTraderLoginAction,
                                        withZoneLoginAction: WithZoneLoginAction,
                                        withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {


  private val rtcbSecretKey = configuration.get[String]("westernUnion.rtcbSecretKey")

  private val wuClientID = configuration.get[String]("westernUnion.clientID")

  private val wuServiceID = configuration.get[String]("westernUnion.serviceID")

  private val wuURL = configuration.get[String]("westernUnion.url")

  private implicit val logger: Logger = Logger(this.getClass)

  private implicit val module: String = constants.Module.CONTROLLERS_WESTERN_UNION

  private val transactionMode = configuration.get[String]("blockchain.transaction.mode")

  private val zonePassword = configuration.get[String]("zone.password")

  private val zoneGas = configuration.get[Int]("zone.gas")


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

        def traderDetails(traderID: String) = masterTraders.Service.tryGet(traderID)

        def traderAddress(traderAccountID: String) = blockchainAccounts.Service.tryGetAddress(traderAccountID)

        def zoneAccountID(zoneID: String) = masterZones.Service.tryGetAccountID(zoneID)

        def zoneAddress(zoneAccountID: String) = blockchainAccounts.Service.tryGetAddress(zoneAccountID)

        def zoneAutomation(traderAddress: String, zoneAddress: String) = issueFiat(traderAddress, zoneAddress, requestBody.reference, requestBody.paidOutAmount.toInt)

        def createFiat(traderID: String) = masterFiats.Service.create(traderID, requestBody.reference, requestBody.paidOutAmount.toInt, 0)

        for {
          _ <- createRTCB
          fiatRequest <- fiatRequest
          totalRTCBAmountReceived <- totalRTCBAmountReceived
          _ <- updateIssueFiatRequestRTCBStatus(fiatRequest.transactionAmount, totalRTCBAmountReceived)
          traderDetails <- traderDetails(fiatRequest.traderID)
          traderAddress <- traderAddress(traderDetails.accountID)
          zoneAccountID <- zoneAccountID(traderDetails.zoneID)
          zoneAddress <- zoneAddress(zoneAccountID)
          _ <- createFiat(traderDetails.id)
          _ <- zoneAutomation(traderAddress, zoneAddress)
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
          val emailAddress = masterEmails.Service.tryGetVerifiedEmailAddress(loginState.username)
          val traderDetails = masterTraders.Service.tryGetByAccountID(loginState.username)

          def create(traderID: String): Future[String] = westernUnionFiatRequests.Service.create(traderID = traderID, transactionAmount = issueFiatRequestData.transactionAmount)


          def organizationDetails(organizationID: String): Future[master.Organization] = masterOrganizations.Service.tryGet(organizationID)

          (for {
            emailAddress <- emailAddress
            traderDetails <- traderDetails
            transactionID <- create(traderDetails.id)
            organizationDetails <- organizationDetails(traderDetails.organizationID)
          } yield {
            val queryString = Map(Form.CLIENT_ID -> Seq(wuClientID), Form.CLIENT_REFERENCE -> Seq(transactionID),
              Form.WU_SFTP_BUYER_ID -> Seq(traderDetails.id),
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

  def issueFiat(traderAddress: String, zoneWalletAddress: String, westernUnionReferenceID: String, transactionAmount: Int) = {

    val ticketID = transaction.process[blockchainTransaction.IssueFiat, transactionsIssueFiat.Request](
      entity = blockchainTransaction.IssueFiat(from = zoneWalletAddress, to = traderAddress, transactionID = westernUnionReferenceID, transactionAmount = transactionAmount, gas = zoneGas, ticketID = "", mode = transactionMode),
      blockchainTransactionCreate = blockchainTransactionIssueFiats.Service.create,
      request = transactionsIssueFiat.Request(transactionsIssueFiat.BaseReq(from = zoneWalletAddress, gas = zoneGas.toString), to = traderAddress, password = zonePassword, transactionID = westernUnionReferenceID, transactionAmount = transactionAmount.toString, mode = transactionMode),
      action = transactionsIssueFiat.Service.post,
      onSuccess = blockchainTransactionIssueFiats.Utility.onSuccess,
      onFailure = blockchainTransactionIssueFiats.Utility.onFailure,
      updateTransactionHash = blockchainTransactionIssueFiats.Service.updateTransactionHash
    )

    for {
      _ <- ticketID
    } yield Unit

  }

}
