package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.master.{AccountKYCs, Negotiations}
import models.masterTransaction.{NegotiationFile, NegotiationFiles}
import models.wallex.{WallexSimplePaymentDetails, _}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  MessagesControllerComponents
}
import play.api.{Configuration, Logger}
import transactions._
import transactions.responses.WallexResponse.{
  CreateCollectionResponse,
  PaymentFileUploadResponse,
  ScreeningResponse,
  WalletToWalletXferResponse
}
import utilities.{KeyStore, MicroNumber, WallexAuthToken}

import java.io.File
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WallexZoneController @Inject() (
    messagesControllerComponents: MessagesControllerComponents,
    withoutLoginActionAsync: WithoutLoginActionAsync,
    withUserLoginAction: WithUserLoginAction,
    withLoginAction: WithLoginAction,
    masterOrganizations: master.Organizations,
    withUsernameToken: WithUsernameToken,
    orgWallexDetails: OrganizationWallexDetails,
    withOrganizationLoginAction: WithOrganizationLoginAction,
    wallexUserSignUpRequest: WallexUserSignUp,
    wallexGetUserRequest: WallexGetUser,
    wallexDocuments: WallexDocuments,
    wallexCreateDocument: WallexCreateDocument,
    wallexAuthToken: WallexAuthToken,
    createWallexPaymentQuote: CreateWallexPaymentQuote,
    withTraderLoginAction: WithTraderLoginAction,
    masterTraders: master.Traders,
    orgWallexBeneficiaryDetails: OrgWallexBeneficiaryDetails,
    wallexCreateBeneficiary: WallexCreateBeneficiary,
    withoutLoginAction: WithoutLoginAction,
    wallexDeleteBeneficiary: WallexDeleteBeneficiary,
    wallexCreateSimplePayment: WallexCreateSimplePayment,
    wallexSimplePaymentDetails: WallexSimplePaymentDetails,
    wallexUploadPaymentFile: WallexUploadPaymentFile,
    negotiationFiles: NegotiationFiles,
    paymentFileDetails: PaymentFileDetails,
    fileResourceManager: utilities.FileResourceManager,
    wallexWalletTransfer: WallexWalletTransfer,
    walletTransferDetails: WallexWalletTransferDetails,
    accountKYCs: AccountKYCs,
    userDetailsUpdate: WallexUserDetailsUpdate,
    keyStore: KeyStore,
    transactionsIssueFiat: transactions.IssueFiat,
    transaction: utilities.Transaction,
    blockchainTransactionIssueFiats: blockchainTransaction.IssueFiats,
    negotiations: Negotiations,
    wallexCreateCollectionAccount: WallexCreateCollectionAccount,
    wallexGetCollectionAccount: WallexGetCollectionAccount,
    walletTransferRequests: WalletTransferRequests,
    withZoneLoginAction: WithZoneLoginAction,
    masterZones: master.Zones,
    blockchainAccounts: blockchain.Accounts,
    wallexUserScreening: WallexUserScreening
)(implicit
    executionContext: ExecutionContext,
    configuration: Configuration,
    wsClient: WSClient
) extends AbstractController(messagesControllerComponents)
    with I18nSupport {

  private implicit val module: String = constants.Module.WALLEX_ZONE_CONTROLLER

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode =
    configuration.get[String]("blockchain.transaction.mode")

  def sendForKycScreeningForm(accountId: String): Action[AnyContent] =
    withoutLoginAction { implicit request =>
      Ok(
        views.html.component.master.getWallexCollectionAccount(
          views.companion.master.GetWallexCollectionAccount.form.fill(
            views.companion.master.GetWallexCollectionAccount
              .Data(accountId = accountId)
          )
        )
      )
    }

  def sendForKycScreening(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.GetWallexCollectionAccount.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master
                    .getWallexCollectionAccount(
                      formWithErrors
                    )
                )
              )
            },
            collectionAccount => {

              val authToken = wallexAuthToken.Service.getToken()

              def getCollectionAccount(authToken: String, accountId: String)
                  : Future[CreateCollectionResponse] =
                wallexGetCollectionAccount.Service.get(
                  authToken,
                  accountId
                )

              (for {
                authToken <- authToken
                collectionResponse <-
                  getCollectionAccount(authToken, collectionAccount.accountId)
                result <- withUsernameToken.PartialContent(
                  views.html.component.master
                    .getWallexCollectionAccountResponse(
                      collectionResponse
                    )
                )
              } yield result).recover {
                case baseException: BaseException =>
                  InternalServerError(
                    views.html.index(failures = Seq(baseException.failure))
                  )
              }

            }
          )
    }

  def zoneWalletTransferForm(negotiationId: String): Action[AnyContent] =
    withZoneLoginAction.authenticated {
      implicit loginState => implicit request =>
        def transferRequests: Future[WalletTransferRequest] =
          walletTransferRequests.Service.tryGet(
            negotiationId = negotiationId
          )

        (for {
          transferRequest <- transferRequests
          result <- withUsernameToken.Ok(
            views.html.component.master.zoneWallexWalletTransfer(
              views.companion.master.WallexWalletTransfer.form.fill(
                views.companion.master.WallexWalletTransfer.Data(
                  onBehalfOf = transferRequest.onBehalfOf,
                  receiverAccountId = transferRequest.receiverAccountId,
                  amount = transferRequest.amount,
                  currency = transferRequest.currency,
                  purposesOfTransfer = transferRequest.purposeOfTransfer,
                  reference = transferRequest.negotiationId,
                  remarks = transferRequest.remarks,
                  negotiationId = transferRequest.negotiationId
                )
              )
            )
          )

        } yield result).recoverWith {
          case _: BaseException =>
            withUsernameToken.Ok(
              views.html.index()
            )
        }
    }

  def zoneCreateWalletTransfer(): Action[AnyContent] =
    withZoneLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.WallexWalletTransfer.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master
                    .zoneWallexWalletTransfer(formWithErrors)
                )
              )
            },
            wallexTransfer => {

              val negotiation =
                negotiations.Service.tryGet(wallexTransfer.negotiationId)
              val negotiationFile = negotiationFiles.Service
                .tryGet(
                  wallexTransfer.negotiationId,
                  constants.File.Negotiation.INVOICE
                )

              def getWallexDetails(accountId: String) =
                orgWallexDetails.Service.tryGetByAccountId(accountId)
              val zoneId =
                masterZones.Service
                  .tryGetID(loginState.username)

              def getTraderId(traderId: String) =
                masterTraders.Service.tryGetAccountId(traderId)

              val authToken = wallexAuthToken.Service.getToken()

              def uploadFileURLRespone(
                  authToken: String,
                  negotiationFile: NegotiationFile
              ): Future[PaymentFileUploadResponse] =
                wallexUploadPaymentFile.Service
                  .post(
                    authToken,
                    wallexUploadPaymentFile
                      .Request(fileName = negotiationFile.fileName)
                  )

              def uploadFileToWallex(
                  authToken: String,
                  uploadURL: String,
                  negotiationFile: NegotiationFile
              ) = {
                val path = fileResourceManager
                  .getNegotiationFilePath(constants.File.Negotiation.INVOICE)
                val fetchFile: File = utilities.FileOperations
                  .fetchFile(path, negotiationFile.fileName)
                val fileArray =
                  utilities.FileOperations.convertToByteArray(fetchFile)
                wallexUploadPaymentFile.Service
                  .put(authToken, uploadURL, fileArray)
              }

              def initiateWalletTransfer(
                  authToken: String,
                  fileId: String
              ) =
                wallexWalletTransfer.Service.post(
                  authToken,
                  wallexWalletTransfer.Request(
                    onBehalfOf = wallexTransfer.onBehalfOf,
                    receiverAccountId = wallexTransfer.receiverAccountId,
                    amount = wallexTransfer.amount,
                    currency = wallexTransfer.currency,
                    purposesOfTransfer = wallexTransfer.purposesOfTransfer,
                    reference = wallexTransfer.reference,
                    remarks = wallexTransfer.remarks,
                    supportingDocuments = Seq(fileId)
                  )
                )

              def insert(
                  wallexTransferResponse: WalletToWalletXferResponse,
                  orgId: String,
                  zoneId: String,
                  wallexId: String
              ) = {
                walletTransferDetails.Service.insertOrUpdate(
                  id = wallexTransferResponse.id,
                  orgId = orgId,
                  zoneId = zoneId,
                  wallexId = wallexId,
                  senderAccountId = wallexTransferResponse.senderAccountId,
                  receiverAccountId = wallexTransferResponse.receiverAccountId,
                  amount = wallexTransferResponse.amount,
                  currency = wallexTransferResponse.currency,
                  purposesOfTransfer =
                    wallexTransferResponse.purposesOfTransfer,
                  reference = wallexTransferResponse.reference,
                  remarks = wallexTransferResponse.remarks,
                  status = wallexTransferResponse.status,
                  createdAt = wallexTransferResponse.createdAt,
                  `type` = wallexTransferResponse.`type`
                )

              }
              def updateStatus(
                  wallexTransferRes: WalletToWalletXferResponse
              ) = {
                val status =
                  if (
                    wallexTransferRes.status == constants.Status.SendWalletTransfer.COMPLETED
                  ) {
                    constants.Status.SendWalletTransfer.SENT
                  } else { constants.Status.SendWalletTransfer.ZONE_APPROVED }

                walletTransferRequests.Service
                  .updateZoneApprovalStatus(
                    negotiationId = wallexTransfer.negotiationId,
                    status = status
                  )
              }
              def traderAddress(traderAccountID: String) =
                blockchainAccounts.Service.tryGetAddress(traderAccountID)

              def zoneAccountID(zoneID: String) =
                masterZones.Service.tryGetAccountID(zoneID)

              def zoneAddress(zoneAccountID: String) =
                blockchainAccounts.Service.tryGetAddress(zoneAccountID)

              def zoneAutomatedIssueFiat(
                  traderAddress: String,
                  zoneID: String,
                  zoneAddress: String,
                  wallexTransferResponse: WalletToWalletXferResponse
              ) =
                issueFiat(
                  traderAddress = traderAddress,
                  zoneID = zoneID,
                  zoneWalletAddress = zoneAddress,
                  wallexTransferReferenceID = wallexTransferResponse.id,
                  transactionAmount =
                    new MicroNumber(wallexTransferResponse.amount)
                )

              (for {
                negotiationFile <- negotiationFile
                negotiationDetail <- negotiation
                zoneId <- zoneId
                zoneAccountID <- zoneAccountID(zoneId)
                zoneAddress <- zoneAddress(zoneAccountID)
                traderAccountId <- getTraderId(negotiationDetail.buyerTraderID)
                traderAddress <- traderAddress(traderAccountId)
                authToken <- authToken
                wallexDetails <- getWallexDetails(wallexTransfer.onBehalfOf)
                fileUrlResponse <-
                  uploadFileURLRespone(authToken, negotiationFile)
                _ <- uploadFileToWallex(
                  authToken,
                  fileUrlResponse.data.uploadUrl,
                  negotiationFile
                )
                wallexTransferResponse <- initiateWalletTransfer(
                  authToken,
                  fileUrlResponse.data.fileId
                )
                _ <- updateStatus(wallexTransferResponse)
                _ <- insert(
                  wallexTransferResponse,
                  wallexDetails.orgId,
                  zoneId,
                  wallexDetails.wallexId
                )
                _ <- zoneAutomatedIssueFiat(
                  traderAddress,
                  zoneId,
                  zoneAddress,
                  wallexTransferResponse
                )

                result <- withUsernameToken.Ok(
                  views.html.index(successes =
                    Seq(constants.Response.WALLEX_TRANSFER_REQUEST_SENT)
                  )
                )

              } yield result).recover {
                case baseException: BaseException =>
                  InternalServerError(
                    views.html.index(failures = Seq(baseException.failure))
                  )
              }
            }
          )

    }

  def zoneViewWalletTransferRequestList(): Action[AnyContent] =
    withZoneLoginAction.authenticated {
      implicit loginState => implicit request =>
        val zoneID = masterZones.Service.tryGetID(loginState.username)

        def pendingWalletTransferRequests(
            zoneID: String
        ): Future[Seq[WalletTransferRequest]] =
          walletTransferRequests.Service.tryGetPendingByZoneId(zoneID)

        (for {
          zoneID <- zoneID
          pendingWalletRequests <- pendingWalletTransferRequests(zoneID)
        } yield Ok(
          views.html.component.master
            .zoneViewPendingWalletTransferRequestList(pendingWalletRequests)
        )).recover {
          case baseException: BaseException =>
            InternalServerError(baseException.failure.message)
        }
    }

  private def issueFiat(
      traderAddress: String,
      zoneID: String,
      zoneWalletAddress: String,
      wallexTransferReferenceID: String,
      transactionAmount: MicroNumber
  ): Future[String] = {
    val zonePassword = Future(keyStore.getPassphrase(zoneID))

    def sendTransaction(zonePassword: String) =
      transaction.process[
        blockchainTransaction.IssueFiat,
        transactionsIssueFiat.Request
      ](
        entity = blockchainTransaction.IssueFiat(
          from = zoneWalletAddress,
          to = traderAddress,
          transactionID = wallexTransferReferenceID,
          transactionAmount = transactionAmount,
          gas = constants.Blockchain.ZoneIssueFiatGas,
          ticketID = "",
          mode = transactionMode
        ),
        blockchainTransactionCreate =
          blockchainTransactionIssueFiats.Service.create,
        request = transactionsIssueFiat.Request(
          transactionsIssueFiat.BaseReq(
            from = zoneWalletAddress,
            gas = constants.Blockchain.ZoneIssueFiatGas
          ),
          to = traderAddress,
          password = zonePassword,
          transactionID = wallexTransferReferenceID,
          transactionAmount = transactionAmount,
          mode = transactionMode
        ),
        action = transactionsIssueFiat.Service.post,
        onSuccess = blockchainTransactionIssueFiats.Utility.onSuccess,
        onFailure = blockchainTransactionIssueFiats.Utility.onFailure,
        updateTransactionHash =
          blockchainTransactionIssueFiats.Service.updateTransactionHash
      )

    (for {
      zonePassword <- zonePassword
      ticketID <- sendTransaction(zonePassword)
    } yield ticketID).recover {
      case baseException: BaseException => throw baseException
    }

  }

  def zoneViewWallexKYCScreeningList(): Action[AnyContent] =
    withZoneLoginAction.authenticated {
      implicit loginState => implicit request =>
        val zoneID = masterZones.Service.tryGetID(loginState.username)
        def pendingScreeningRequests(
            zoneID: String
        ): Future[Seq[OrganizationWallexDetail]] =
          orgWallexDetails.Service.tryGetPendingByZoneID(zoneID)

        (for {
          zoneID <- zoneID
          pendingKYCRequests <- pendingScreeningRequests(zoneID)
        } yield Ok(
          views.html.component.master
            .zoneViewPendingWallexKYCRequestList(pendingKYCRequests)
        )).recover {
          case baseException: BaseException =>
            InternalServerError(baseException.failure.message)
        }
    }

  def sendForScreeningForm(wallexID: String): Action[AnyContent] =
    withZoneLoginAction.authenticated {
      implicit loginState => implicit request =>
        (for {
          result <- withUsernameToken.Ok(
            views.html.component.master.sendUserDetailsForScreening(
              views.companion.master.SendUserDetailsForScreening.form
                .fill(
                  views.companion.master.SendUserDetailsForScreening
                    .Data(
                      userID = wallexID
                    )
                )
            )
          )
        } yield result).recoverWith {
          case _: BaseException =>
            withUsernameToken.Ok(
              views.html.component.master.sendUserDetailsForScreening()
            )
        }

    }

  def sendForScreening: Action[AnyContent] =
    withZoneLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.SendUserDetailsForScreening.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master
                    .sendUserDetailsForScreening(formWithErrors)
                )
              )
            },
            orgWallexAccountDetailData => {

              val authToken = wallexAuthToken.Service.getToken()

              def sendForScreening(wallexId: String, authToken: String) =
                wallexUserScreening.Service
                  .post(
                    wallexId,
                    authToken,
                    wallexUserScreening.Request(userId = wallexId)
                  )

              def updateStatus(
                  screeningResponse: ScreeningResponse
              ): Future[Int] =
                orgWallexDetails.Service.updateStatus(
                  wallexID = screeningResponse.id,
                  status = screeningResponse.status
                )

              (for {
                authToken <- authToken
                screeningResponse <-
                  sendForScreening(orgWallexAccountDetailData.userID, authToken)
                _ <- updateStatus(screeningResponse)
                result <- withUsernameToken.Ok(
                  views.html.profile(successes =
                    Seq(constants.Response.WALLEX_ACCOUNT_DETAILS_UPDATED)
                  )
                )
              } yield result).recover {
                case baseException: BaseException =>
                  InternalServerError(
                    views.html.index(failures = Seq(baseException.failure))
                  )
              }
            }
          )
    }

}
