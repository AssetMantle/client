package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.{BaseException, WSException}
import models.master. Negotiations
import models.masterTransaction.{NegotiationFile, NegotiationFiles}
import models.wallex.{SimplePayments, _}
import models.{blockchain, blockchainTransaction, master}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import transactions.responses.WallexResponse.{PaymentFileUploadResponse, ScreeningResponse, WalletToWalletXferResponse}
import transactions.wallex._
import utilities.{KeyStore, MicroNumber}
import views.companion
import views.companion.wallex.WallexWalletTransfer

import java.io.File
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WallexZoneController @Inject() (
                                       messagesControllerComponents: MessagesControllerComponents,
                                       withUsernameToken: WithUsernameToken,
                                       organizationWallexDetails: OrganizationWallexDetails,
                                       wallexAuthToken: WallexAuthToken,
                                       masterTraders: master.Traders,
                                       wallexUploadPaymentFile: WallexUploadPaymentFile,
                                       negotiationFiles: NegotiationFiles,
                                       fileResourceManager: utilities.FileResourceManager,
                                       wallexWalletTransfer: WallexWalletTransfer,
                                       walletTransferDetails: WalletTransfers,
                                       keyStore: KeyStore,
                                       transactionsIssueFiat: transactions.IssueFiat,
                                       transaction: utilities.Transaction,
                                       blockchainTransactionIssueFiats: blockchainTransaction.IssueFiats,
                                       negotiations: Negotiations,
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


  def zoneWalletTransferForm(negotiationId: String): Action[AnyContent] =
    withZoneLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        def transferRequests: Future[WalletTransferRequest] =
          walletTransferRequests.Service.tryGet(
            negotiationId = negotiationId
          )

        (for {
          transferRequest <- transferRequests
          result <- withUsernameToken.Ok(
            views.html.component.wallex.zoneWallexWalletTransfer(
              WallexWalletTransfer.form.fill(
                companion.wallex.WallexWalletTransfer.Data(
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
      implicit loginState =>
        implicit request =>
        companion.wallex.WallexWalletTransfer.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.wallex
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
                organizationWallexDetails.Service.tryGetByAccountId(accountId)
              val zoneID =
                masterZones.Service
                  .tryGetID(loginState.username)

              def getTraderID(traderID: String) =
                masterTraders.Service.tryGetAccountId(traderID)

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
                  organizationID: String,
                  zoneID: String,
                  wallexId: String
              ) = {
                walletTransferDetails.Service.insertOrUpdate(
                  id = wallexTransferResponse.id,
                  organizationID = organizationID,
                  zoneID = zoneID,
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
                zoneID <- zoneID
                zoneAccountID <- zoneAccountID(zoneID)
                zoneAddress <- zoneAddress(zoneAccountID)
                traderAccountId <- getTraderID(negotiationDetail.buyerTraderID)
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
                  wallexDetails.organizationID,
                  zoneID,
                  wallexDetails.wallexId
                )
                _ <- zoneAutomatedIssueFiat(
                  traderAddress,
                  zoneID,
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
                case wsException: WSException =>
                  InternalServerError(
                    views.html.index(
                      errorMessage = wsException.errorMessage,
                      failures = Seq(wsException.failure)
                    )
                  )
              }
            }
          )

    }

  def zoneViewWalletTransferRequestList(): Action[AnyContent] =
    withZoneLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        val zoneID = masterZones.Service.tryGetID(loginState.username)

        def pendingWalletTransferRequests(
            zoneID: String
        ): Future[Seq[WalletTransferRequest]] =
          walletTransferRequests.Service.tryGetPendingByZoneID(zoneID)

        (for {
          zoneID <- zoneID
          pendingWalletRequests <- pendingWalletTransferRequests(zoneID)
        } yield Ok(
          views.html.component.wallex
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
      implicit loginState =>
        implicit request =>
        val zoneID = masterZones.Service.tryGetID(loginState.username)
        def pendingScreeningRequests(
            zoneID: String
        ): Future[Seq[OrganizationWallexDetail]] =
          organizationWallexDetails.Service.tryGetPendingByZoneID(zoneID)

        (for {
          zoneID <- zoneID
          pendingKYCRequests <- pendingScreeningRequests(zoneID)
        } yield Ok(
          views.html.component.wallex
            .zoneViewPendingWallexKYCRequestList(pendingKYCRequests)
        )).recover {
          case baseException: BaseException =>
            InternalServerError(baseException.failure.message)
        }
    }

  def sendForScreeningForm(wallexID: String): Action[AnyContent] =
    withZoneLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
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
      implicit loginState =>
        implicit request =>
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
                organizationWallexDetails.Service.updateStatus(
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
                case wsException: WSException =>
                  InternalServerError(
                    views.html.index(errorMessage = wsException.errorMessage,failures = Seq(wsException.failure))
                  )
              }
            }
          )
    }

}
