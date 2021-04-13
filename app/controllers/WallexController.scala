package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.common.Serializable.{
  BankAccount,
  BeneficiaryPayment,
  ConversionDetails
}
import models.master.{Negotiations, Trader}
import models.masterTransaction.{NegotiationFile, NegotiationFiles}
import models.wallex.{WallexSimplePaymentDetails, _}
import models.{blockchainTransaction, master}
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
  CreateDocumentResponse,
  CreatePaymentQuoteResponse,
  GetBalanceResponse,
  GetUserResponse,
  PaymentFileUploadResponse
}
import utilities.{KeyStore, WallexAuthToken}
import views.companion.master.AddOrgWallexBeneficiaryDetails.BankData

import java.io.File
import java.text.SimpleDateFormat
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WallexController @Inject() (
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
    userDetailsUpdate: WallexUserDetailsUpdate,
    keyStore: KeyStore,
    transactionsIssueFiat: transactions.IssueFiat,
    transaction: utilities.Transaction,
    blockchainTransactionIssueFiats: blockchainTransaction.IssueFiats,
    negotiations: Negotiations,
    wallexCreateCollectionAccount: WallexCreateCollectionAccount,
    wallexGetCollectionAccount: WallexGetCollectionAccount,
    wallexKYCDetails: WallexKYCDetails,
    collectionAccountDetails: WallexCollectionAccountDetails,
    wallexGetWalletBalance: WallexGetWalletBalance,
    walletTransferRequest: WalletTransferRequests
)(implicit
    executionContext: ExecutionContext,
    configuration: Configuration,
    wsClient: WSClient
) extends AbstractController(messagesControllerComponents)
    with I18nSupport {

  private implicit val module: String = constants.Module.WALLEX_CONTROLLER

  private implicit val logger: Logger = Logger(this.getClass)

  private val transactionMode =
    configuration.get[String]("blockchain.transaction.mode")

  def createOrganizationWallexAccountForm(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        val organizationID =
          masterTraders.Service.getOrganizationIDByAccountID(
            loginState.username
          )

        def getOrganizationWallexAccountDetail(
            organizationID: String
        ): Future[OrganizationWallexDetail] =
          orgWallexDetails.Service.tryGet(organizationID)

        (for {
          organizationID <- organizationID
          organizationWallexAccountDetail <-
            getOrganizationWallexAccountDetail(organizationID)
          result <- withUsernameToken.Ok(
            views.html.component.master.addOrUpdateOrganizationWallexAccount(
              views.companion.master.AddOrUpdateOrganizationWallexAccount.form
                .fill(
                  views.companion.master.AddOrUpdateOrganizationWallexAccount
                    .Data(
                      firstName = organizationWallexAccountDetail.firstName,
                      lastName = organizationWallexAccountDetail.lastName,
                      email = organizationWallexAccountDetail.email,
                      countryCode = organizationWallexAccountDetail.countryCode,
                      accountType = organizationWallexAccountDetail.accountType
                    )
                )
            )
          )
        } yield result).recoverWith {
          case _: BaseException =>
            withUsernameToken.Ok(
              views.html.component.master.addOrUpdateOrganizationWallexAccount()
            )
        }

    }

  def createOrganizationWallexAccount(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.AddOrUpdateOrganizationWallexAccount.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master
                    .addOrUpdateOrganizationWallexAccount(formWithErrors)
                )
              )
            },
            orgWallexAccountDetailData => {

              def getZoneID(orgID: String) =
                masterOrganizations.Service.tryGetZoneID(orgID)

              val orgId =
                masterTraders.Service
                  .getOrganizationIDByAccountID(loginState.username)

              def organization(orgId: String) =
                masterOrganizations.Service.tryGet(orgId)

              val authToken = wallexAuthToken.Service.getToken()

              def wallexUserSignUp(authToken: String) = {
                wallexUserSignUpRequest.Service.post(
                  authToken,
                  wallexUserSignUpRequest.Request(
                    firstName = orgWallexAccountDetailData.firstName,
                    lastName = orgWallexAccountDetailData.lastName,
                    email = orgWallexAccountDetailData.email,
                    countryCode = orgWallexAccountDetailData.countryCode,
                    accountType = orgWallexAccountDetailData.accountType
                  )
                )
              }

              def wallexGetUser(wallexId: String, authToken: String) =
                wallexGetUserRequest.Service.get(wallexId, authToken)

              def insertOrUpdate(
                  zoneID: String,
                  orgId: String,
                  email: String,
                  firstName: String,
                  lastName: String,
                  countryCode: String,
                  accountType: String,
                  wallexId: String,
                  accountId: String,
                  status: String
              ): Future[Int] =
                orgWallexDetails.Service.insertOrUpdate(
                  zoneID = zoneID,
                  orgId = orgId,
                  email = email,
                  firstName = firstName,
                  lastName = lastName,
                  countryCode = countryCode,
                  accountType = accountType,
                  wallexId = wallexId,
                  accountId = accountId,
                  status = status
                )

              (for {
                orgId <- orgId
                zoneID <- getZoneID(orgId)
                authToken <- authToken
                wallexUserSignUp <- wallexUserSignUp(authToken)
                wallexGetUser <-
                  wallexGetUser(wallexUserSignUp.userId, authToken)
                _ <- insertOrUpdate(
                  zoneID,
                  orgId,
                  wallexUserSignUp.email,
                  wallexUserSignUp.firstName,
                  wallexUserSignUp.lastName,
                  wallexUserSignUp.countryCode,
                  wallexUserSignUp.accountType,
                  wallexUserSignUp.userId,
                  wallexUserSignUp.accountId,
                  wallexGetUser.status
                )
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

  def wallexDocumentForm(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        Future(Ok(views.html.component.master.addOrUpdateWallexDocument()))

    }

  def wallexDocument(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.AddWallexDocuments.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master
                    .addOrUpdateWallexDocument(formWithErrors)
                )
              )
            },
            file => {
              val wallexKYC = wallexDocuments.Service
                .get(loginState.username, file.documentType)
              for {
                wallexKYC <- wallexKYC
                result <- withUsernameToken.PartialContent(
                  views.html.component.master.uploadOrUpdateWallexDocument(
                    wallexKYC,
                    file.documentType
                  )
                )
              } yield result
            }
          )
    }

  def submitDocumentToWallexForm(documentType: String): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        Future(
          Ok(
            views.html.component.master.submitDocumentToWallex(
              views.companion.master.SubmitDocumentToWallex.form.fill(
                views.companion.master.SubmitDocumentToWallex.Data(
                  documentType = documentType
                )
              )
            )
          )
        )

    }

  def submitDocumentToWallex(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.SubmitDocumentToWallex.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master
                    .submitDocumentToWallex(formWithErrors)
                )
              )
            },
            wallexDocument => {

              val orgId = masterTraders.Service
                .getOrganizationIDByAccountID(loginState.username)

              def wallexDetails(orgId: String) =
                orgWallexDetails.Service.tryGet(orgId)

              def getWallexDocument(
                  documentType: String
              ): Future[models.wallex.WallexDocument] = {
                wallexDocuments.Service.tryGet(
                  loginState.username,
                  documentType
                )
              }

              val authToken = wallexAuthToken.Service.getToken()

              def createDocument(
                  authToken: String,
                  wallexId: String,
                  fileName: String,
                  fileType: String
              ) =
                wallexCreateDocument.Service.post(
                  authToken,
                  wallexId,
                  wallexCreateDocument.Request(
                    documentType = fileType,
                    documentName = fileName
                  )
                )

              def uploadDocument(
                  authToken: String,
                  uploadUrl: String,
                  wallexDocument: WallexDocument
              ) = {
                val path =
                  fileResourceManager
                    .getWallexFilePath(wallexDocument.documentType)
                val fetchFile: File = utilities.FileOperations
                  .fetchFile(path, wallexDocument.fileName)
                val fileArray =
                  utilities.FileOperations.convertToByteArray(fetchFile)
                wallexCreateDocument.Service
                  .put(authToken, uploadUrl, fileArray)
              }

              def insertDocumentDetails(
                  orgId: String,
                  wallexId: String,
                  documentResponse: CreateDocumentResponse
              ) = {
                wallexKYCDetails.Service.insertOrUpdate(
                  id = documentResponse.id,
                  orgId = orgId,
                  wallexId = wallexId,
                  url = documentResponse.uploadURL,
                  documentName = documentResponse.documentName,
                  documentType = documentResponse.documentType
                )
              }

              def updateStatus(
                  wallexID: String
              ): Future[Int] =
                orgWallexDetails.Service.updateStatus(
                  wallexID = wallexID,
                  status =
                    constants.Status.SendWalletTransfer.ZONE_SEND_FOR_SCREENING
                )

              (for {
                orgId <- orgId
                authToken <- authToken
                wallexDetails <- wallexDetails(orgId)
                wallexDoc <- getWallexDocument(wallexDocument.documentType)
                createDocument <- createDocument(
                  authToken,
                  wallexDetails.wallexId,
                  wallexDoc.fileName,
                  wallexDoc.documentType
                )
                _ <-
                  uploadDocument(authToken, createDocument.uploadURL, wallexDoc)
                _ <- insertDocumentDetails(
                  orgId,
                  wallexDetails.wallexId,
                  createDocument
                )
                _ <- updateStatus(wallexDetails.wallexId)
                result <- withUsernameToken.Ok(
                  views.html.profile(successes =
                    Seq(constants.Response.WALLEX_DOCUMENT_SUBMITTED)
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

  def uploadOrUpdateWallexDocument(documentType: String): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        val wallexKYC = wallexDocuments.Service
          .get(loginState.username, documentType)
        for {
          wallexKYC <- wallexKYC
          result <- withUsernameToken.Ok(
            views.html.component.master.uploadOrUpdateWallexDocument(
              wallexKYC,
              documentType
            )
          )
        } yield result
    }
  def initiateWallexPaymentForm(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        Future(
          Ok(
            views.html.component.master
              .createWallexPaymentQuote()
          )
        )
    }

  def initiateWallexPayment(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.CreateWallexPaymentQuote.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master
                    .createWallexPaymentQuote(
                      formWithErrors
                    )
                )
              )
            },
            paymentQuote => {

              val orgId =
                masterTraders.Service
                  .getOrganizationIDByAccountID(loginState.username)

              def getWallexDetails(orgId: String) =
                orgWallexDetails.Service.tryGet(orgId)

              val authToken = wallexAuthToken.Service.getToken()

              def createPaymentQuote(authToken: String) =
                createWallexPaymentQuote.Service.post(
                  authToken,
                  createWallexPaymentQuote.Request(
                    sellCurrency = paymentQuote.sellCurrency,
                    buyCurrency = paymentQuote.buyCurrency,
                    amount = paymentQuote.amount,
                    beneficiaryId = paymentQuote.beneficiaryId
                  )
                )

              def getQuoteResponse(postResponse: CreatePaymentQuoteResponse) = {

                for {

                  result <- withUsernameToken.PartialContent(
                    views.html.component.master
                      .acceptWallexPaymentQuoteResponse(
                        postResponse
                      )
                  )
                } yield result

              }

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

              /*def insert(
                    orgId: String,
                    wallexId: String,
                    fileId: String,
                    fileType: String,
                    quoteId: String
                ) =
                  paymentFileDetails.Service.insertOrUpdate(
                    orgId = orgId,
                    negotiationId = paymentQuote.negotiationID,
                    quoteId = quoteId,
                    wallexId = wallexId,
                    fileId = fileId,
                    fileType = fileType
                  )*/

              (for {
                //negotiationFile <- negotiationFile
                orgId <- orgId
                authToken <- authToken
                /*fileUrlResponse <-
                    uploadFileURLRespone(authToken, negotiationFile)
                  uploadFile <- uploadFileToWallex(
                    authToken,
                    fileUrlResponse.data.uploadUrl,
                    negotiationFile
                  )*/
                wallexDetails <- getWallexDetails(orgId)
                postResponse <- createPaymentQuote(authToken)
                result <- getQuoteResponse(postResponse)
                /* _ <- insert(
                    orgId,
                    wallexDetails.wallexId,
                    fileUrlResponse.data.fileId,
                    "INVOICE",
                    postResponse.data.quoteId
                  )*/
              } yield result).recover {
                case baseException: BaseException =>
                  InternalServerError(
                    views.html.index(failures = Seq(baseException.failure))
                  )
              }
            }
          )
    }

  def acceptWallexQuoteForm(
      quoteId: String
  ): Action[AnyContent] = {
    withoutLoginActionAsync { implicit request =>
      Future(
        Ok(
          views.html.component.master
            .acceptWallexPaymentQuoteRequest(
              quoteId = quoteId
            )
        )
      )
    }
  }

  /*
    Creates simple payment
   */
  def acceptWallexQuote(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.AcceptWallexPaymentQuote.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master
                    .acceptWallexPaymentQuoteRequest(
                      formWithErrors,
                      formWithErrors.data(constants.FormField.QUOTE_ID.name)
                    )
                )
              )
            },
            createPaymentResponse => {

              val orgId =
                masterTraders.Service
                  .getOrganizationIDByAccountID(loginState.username)
              val zoneId =
                masterTraders.Service
                  .tryGetZoneIDByAccountID(loginState.username)

              def getWallexDetails(orgId: String) =
                orgWallexDetails.Service.tryGet(orgId)

              val file =
                paymentFileDetails.Service.tryGet(createPaymentResponse.quoteId)
              val authToken = wallexAuthToken.Service.getToken()

              def createSimplePayment(
                  authToken: String,
                  file: PaymentFileDetail
              ) =
                wallexCreateSimplePayment.Service.post(
                  authToken,
                  wallexCreateSimplePayment.Request(
                    fundingSource = createPaymentResponse.fundingSource,
                    paymentReference = createPaymentResponse.paymentReference,
                    quoteId = createPaymentResponse.quoteId,
                    purposeOfTransfer = createPaymentResponse.purposeOfTransfer,
                    referenceId = file.negotiationId,
                    files = Seq(file.fileId)
                  )
                )

              def insertOrUpdate(
                  wallexId: String,
                  zoneId: String,
                  simplePaymentId: String,
                  status: String,
                  createdAt: String,
                  referenceId: String,
                  fundingSource: String,
                  purposeOfTransfer: String,
                  fundingReference: String,
                  fundingCutoffTime: String,
                  beneficiary: BeneficiaryPayment,
                  conversionDetails: ConversionDetails,
                  zoneApproved: Option[Boolean]
              ) =
                wallexSimplePaymentDetails.Service.create(
                  wallexId = wallexId,
                  zoneId = zoneId,
                  simplePaymentId = simplePaymentId,
                  status = status,
                  createdAt = createdAt,
                  referenceId = referenceId,
                  fundingSource = fundingSource,
                  purposeOfTransfer = purposeOfTransfer,
                  fundingReference = fundingReference,
                  fundingCutoffTime = fundingCutoffTime,
                  beneficiary = beneficiary,
                  conversionDetails = conversionDetails,
                  zoneApproved = zoneApproved
                )

              (for {
                orgId <- orgId
                zoneId <- zoneId
                wallexDetails <- getWallexDetails(orgId)
                authToken <- authToken
                file <- file
                simplePaymentResponse <- createSimplePayment(authToken, file)
                simplePayment <- insertOrUpdate(
                  wallexId = wallexDetails.wallexId,
                  zoneId = zoneId,
                  simplePaymentId = simplePaymentResponse.data.simplePaymentId,
                  status = simplePaymentResponse.data.status,
                  createdAt = simplePaymentResponse.data.createdAt,
                  referenceId = simplePaymentResponse.data.referenceId,
                  fundingSource = simplePaymentResponse.data.fundingSource,
                  purposeOfTransfer =
                    simplePaymentResponse.data.purposeOfTransfer,
                  fundingReference =
                    simplePaymentResponse.data.fundingReference,
                  fundingCutoffTime =
                    simplePaymentResponse.data.fundingCutoffTime,
                  beneficiary = BeneficiaryPayment(
                    address = simplePaymentResponse.data.beneficiary.address,
                    city = simplePaymentResponse.data.beneficiary.city,
                    companyName =
                      simplePaymentResponse.data.beneficiary.companyName,
                    country = simplePaymentResponse.data.beneficiary.country,
                    beneficiaryId =
                      simplePaymentResponse.data.beneficiary.beneficiaryId,
                    nickname = simplePaymentResponse.data.beneficiary.nickname,
                    `type` = simplePaymentResponse.data.beneficiary.`type`,
                    bankAccount = BankAccount(
                      accountNumber =
                        simplePaymentResponse.data.beneficiary.bankAccount.accountNumber,
                      bankName =
                        simplePaymentResponse.data.beneficiary.bankAccount.bankName,
                      bicSwift =
                        simplePaymentResponse.data.beneficiary.bankAccount.bicSwift,
                      country =
                        simplePaymentResponse.data.beneficiary.bankAccount.country,
                      currency =
                        simplePaymentResponse.data.beneficiary.bankAccount.currency,
                      address =
                        simplePaymentResponse.data.beneficiary.bankAccount.address,
                      bankAccountHolderName =
                        simplePaymentResponse.data.beneficiary.bankAccount.bankAccountHolderName
                    )
                  ),
                  conversionDetails = ConversionDetails(
                    currencyPair = simplePaymentResponse.data.currencyPair,
                    buyCurrency = simplePaymentResponse.data.buyCurrency,
                    buyAmount = simplePaymentResponse.data.buyAmount,
                    sellCurrency = simplePaymentResponse.data.sellCurrency,
                    sellAmount = simplePaymentResponse.data.sellAmount,
                    fixedSide = simplePaymentResponse.data.fixedSide,
                    rate = simplePaymentResponse.data.rate,
                    totalFee = simplePaymentResponse.data.totalFee,
                    totalAmount = simplePaymentResponse.data.totalAmount
                  ),
                  zoneApproved = None
                )

                result <- withUsernameToken.Ok(
                  views.html.index(successes =
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

  def createBeneficiariesForm(): Action[AnyContent] = {
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        val organizationID =
          masterTraders.Service.getOrganizationIDByAccountID(
            loginState.username
          )

        val getOrganizationBeneficiaryDetail =
          orgWallexBeneficiaryDetails.Service.tryGet(loginState.username)

        (for {
          beneficiaryDetails <- getOrganizationBeneficiaryDetail
          result <- withUsernameToken.Ok(
            views.html.component.master.addOrgWallexBeneficiaryDetails(
              views.companion.master.AddOrgWallexBeneficiaryDetails.form
                .fill(
                  views.companion.master.AddOrgWallexBeneficiaryDetails
                    .Data(
                      country = beneficiaryDetails.country,
                      address = beneficiaryDetails.address,
                      city = beneficiaryDetails.city,
                      entityType = beneficiaryDetails.entityType,
                      companyName = beneficiaryDetails.companyName,
                      nickName = beneficiaryDetails.nickname,
                      bankData = BankData(
                        accountNumber =
                          beneficiaryDetails.bankAccount.accountNumber,
                        bankName = beneficiaryDetails.bankAccount.bankName,
                        bicSwift = beneficiaryDetails.bankAccount.bicSwift,
                        country = beneficiaryDetails.bankAccount.country,
                        currency = beneficiaryDetails.bankAccount.currency,
                        address = beneficiaryDetails.bankAccount.address,
                        accountHolderName =
                          beneficiaryDetails.bankAccount.bankAccountHolderName
                      )
                    )
                )
            )
          )
        } yield result).recoverWith {
          case _: BaseException =>
            withUsernameToken.Ok(
              views.html.component.master.addOrgWallexBeneficiaryDetails()
            )

        }
    }
  }

  def createBeneficiaries: Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.AddOrgWallexBeneficiaryDetails.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master
                    .addOrgWallexBeneficiaryDetails(formWithErrors)
                )
              )
            },
            beneficiaryDetail => {

              val organizationID =
                masterTraders.Service.getOrganizationIDByAccountID(
                  loginState.username
                )

              def getOrganizationWallexAccountDetail(
                  organizationID: String
              ): Future[OrganizationWallexDetail] =
                orgWallexDetails.Service.tryGet(organizationID)

              def beneficiaryDetails =
                orgWallexBeneficiaryDetails.Service
                  .getByTraderId(loginState.username)

              val authToken = wallexAuthToken.Service.getToken()

              def createBeneficiary(authToken: String) = {
                wallexCreateBeneficiary.Service.post(
                  authToken,
                  wallexCreateBeneficiary.Request(
                    country = beneficiaryDetail.country,
                    address = beneficiaryDetail.address,
                    city = beneficiaryDetail.city,
                    nickname = beneficiaryDetail.nickName,
                    entityType = beneficiaryDetail.entityType,
                    companyName = beneficiaryDetail.companyName,
                    bankAccount = BankAccount(
                      bankName = beneficiaryDetail.bankData.bankName,
                      currency = beneficiaryDetail.bankData.currency,
                      country = beneficiaryDetail.bankData.country,
                      accountNumber = beneficiaryDetail.bankData.accountNumber,
                      bicSwift = beneficiaryDetail.bankData.bicSwift,
                      address = beneficiaryDetail.bankData.address,
                      bankAccountHolderName =
                        beneficiaryDetail.bankData.accountHolderName
                    )
                  )
                )
              }

              def insertOrUpdate(
                  orgId: String,
                  traderId: String,
                  wallexId: String,
                  beneficiaryId: String,
                  address: String,
                  country: String,
                  city: String,
                  entityType: String,
                  companyName: String,
                  nickname: String,
                  accountType: String,
                  bankAccount: BankAccount
              ) =
                orgWallexBeneficiaryDetails.Service.create(
                  orgId = orgId,
                  traderId = traderId,
                  wallexId = wallexId,
                  beneficiaryId = beneficiaryId,
                  address = address,
                  country = country,
                  city = city,
                  entityType = entityType,
                  companyName = companyName,
                  nickname = nickname,
                  accountType = accountType,
                  bankAccount = bankAccount
                )

              (for {
                organizationID <- organizationID
                wallexDetails <-
                  getOrganizationWallexAccountDetail(organizationID)
                authToken <- authToken
                beneficiaryResponse <- createBeneficiary(authToken)
                _ <- insertOrUpdate(
                  organizationID,
                  loginState.username,
                  wallexDetails.wallexId,
                  beneficiaryResponse.data.beneficiaryId,
                  beneficiaryResponse.data.address,
                  beneficiaryResponse.data.country,
                  beneficiaryResponse.data.city,
                  beneficiaryResponse.data.entityType,
                  beneficiaryResponse.data.companyName,
                  beneficiaryResponse.data.nickname,
                  beneficiaryResponse.data.`type`,
                  BankAccount(
                    accountNumber =
                      beneficiaryResponse.data.bankAccount.accountNumber,
                    address = beneficiaryResponse.data.bankAccount.address,
                    bankName = beneficiaryResponse.data.bankAccount.bankName,
                    bicSwift = beneficiaryResponse.data.bankAccount.bicSwift,
                    country = beneficiaryResponse.data.bankAccount.country,
                    currency = beneficiaryResponse.data.bankAccount.country,
                    bankAccountHolderName =
                      beneficiaryResponse.data.bankAccount.bankAccountHolderName
                  )
                )
                beneficiary <- beneficiaryDetails
                result <- withUsernameToken.Ok(
                  views.html.profile(successes =
                    Seq(constants.Response.WALLEX_BENEFICIARY_DETAILS_SUBMITTED)
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

  def deleteBeneficiaryForm(beneficiaryId: String): Action[AnyContent] =
    withoutLoginAction { implicit request =>
      Ok(
        views.html.component.master.deleteBeneficiary(
          views.companion.master.DeleteBeneficiary.form
            .fill(
              views.companion.master.DeleteBeneficiary.Data(id = beneficiaryId)
            )
        )
      )
    }

  def deleteBeneficiary(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.DeleteBeneficiary.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master.deleteBeneficiary(formWithErrors)
                )
              )
            },
            deleteBeneficiary => {
              val beneficiary =
                orgWallexBeneficiaryDetails.Service
                  .getByTraderId(loginState.username)

              val authToken = wallexAuthToken.Service.getToken()

              def deleteBeneficiaryFromWallex(
                  authToken: String,
                  benefeciaryId: String
              ) = {
                wallexDeleteBeneficiary.Service
                  .delete(authToken, beneficiaryId = benefeciaryId)
              }

              def deleteBeneficiary(beneficiaryId: String): Future[Int] =
                orgWallexBeneficiaryDetails.Service
                  .delete(beneficiaryId)

              (for {
                beneficiary <- beneficiary
                authToken <- authToken
                deleteResponse <- deleteBeneficiaryFromWallex(
                  authToken,
                  beneficiary.beneficiaryId
                )
                _ <- deleteBeneficiary(deleteResponse.data.beneficiaryId)
                result <- withUsernameToken.Ok(
                  views.html
                    .profile(successes =
                      Seq(constants.Response.WALLEX_BENEFICIARY_DETAILS_DELETED)
                    )
                )
              } yield result).recover {
                case baseException: BaseException =>
                  InternalServerError(
                    views.html.profile(failures = Seq(baseException.failure))
                  )
              }
            }
          )
    }

  def walletTransferForm(negotiationID: String): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        def getWallexDetails(orgId: String) =
          orgWallexDetails.Service.tryGet(orgId)

        val authToken = wallexAuthToken.Service.getToken()

        val negotiation =
          negotiations.Service.tryGet(negotiationID)

        def walletBalanceRespone(
            authToken: String,
            userId: String
        ): Future[GetBalanceResponse] =
          wallexGetWalletBalance.Service
            .post(
              authToken,
              userId
            )

        def getBuyerWallexAccountId(
            buyerId: String
        ): Future[OrganizationWallexDetail] = {

          val buyerOrgId =
            masterTraders.Service
              .tryGetOrganizationID(buyerId)

          for {
            buyerOrgId <- buyerOrgId
            buyerWallex <- getWallexDetails(buyerOrgId)
          } yield buyerWallex

        }

        def getSellerWallexAccountId(
            sellerId: String
        ): Future[OrganizationWallexDetail] = {
          val sellerOrgId =
            masterTraders.Service
              .tryGetOrganizationID(sellerId)

          for {
            sellerOrgId <- sellerOrgId
            sellerWallex <- getWallexDetails(sellerOrgId)
          } yield sellerWallex
        }

        def validateBalance = {
          for {
            result <- Future(
              BadRequest(
                views.html.component.master.wallexWalletTransfer(
                  views.companion.master.WallexWalletTransfer.form
                    .withGlobalError(
                      constants.Response.WALLEX_WALLET_LOW_BALANCE.message
                    )
                )
              )
            )

          } yield result
        }
        def formResult(
            buyerAccId: String,
            sellerAccId: String,
            amount: Double
        ) = {
          for {
            result <- withUsernameToken.Ok(
              views.html.component.master.wallexWalletTransfer(
                views.companion.master.WallexWalletTransfer.form.fill(
                  views.companion.master.WallexWalletTransfer.Data(
                    onBehalfOf = buyerAccId,
                    receiverAccountId = sellerAccId,
                    amount = amount,
                    currency = "",
                    purposesOfTransfer = "",
                    reference = negotiationID,
                    remarks = "",
                    negotiationId = negotiationID
                  )
                )
              )
            )
          } yield result
        }

        (for {
          negotiation <- negotiation
          authToken <- authToken
          buyerWallexAcctId <-
            getBuyerWallexAccountId(negotiation.buyerTraderID)
          sellerWallexAcctId <-
            getSellerWallexAccountId(negotiation.sellerTraderID)
          balance <- walletBalanceRespone(authToken, buyerWallexAcctId.wallexId)
          result <-
            if (balance.data.amount > negotiation.price) {
              formResult(
                buyerWallexAcctId.accountId,
                sellerWallexAcctId.accountId,
                negotiation.price.toDouble
              )
            } else {
              validateBalance
            }

        } yield result).recoverWith {
          case _: BaseException =>
            withUsernameToken.Ok(
              views.html.index()
            )
        }
    }

  def walletTransfer(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.WallexWalletTransfer.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master
                    .wallexWalletTransfer(formWithErrors)
                )
              )
            },
            wallexTransfer => {

              val trader =
                masterTraders.Service
                  .tryGetByAccountID(loginState.username)

              def insert(
                  trader: Trader
              ) =
                walletTransferRequest.Service.insertOrUpdate(
                  negotiationId = wallexTransfer.negotiationId,
                  zoneId = trader.zoneID,
                  orgId = trader.organizationID,
                  traderId = trader.id,
                  onBehalfOf = wallexTransfer.onBehalfOf,
                  receiverAccountId = wallexTransfer.receiverAccountId,
                  amount = wallexTransfer.amount,
                  currency = wallexTransfer.currency,
                  purposeOfTransfer = wallexTransfer.purposesOfTransfer,
                  reference = wallexTransfer.reference,
                  remarks = wallexTransfer.remarks,
                  status = constants.Status.SendWalletTransfer.ZONE_APPROVAL
                )

              (for {
                trader <- trader
                _ <- insert(trader)
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

  def updateWallexAccountForm(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        Future(Ok(views.html.component.master.updateDetailsWallexAccount()))

    }

  def updateWallexAccount(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.UpdateDetailsWallexAccount.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master
                    .updateDetailsWallexAccount(formWithErrors)
                )
              )
            },
            updateDetails => {

              val organizationID =
                masterTraders.Service.getOrganizationIDByAccountID(
                  loginState.username
                )

              def getOrganizationWallexAccountDetail(
                  organizationID: String
              ): Future[OrganizationWallexDetail] =
                orgWallexDetails.Service.tryGet(organizationID)

              val authToken = wallexAuthToken.Service.getToken()

              def wallexUserUpdate(authToken: String, wallexID: String) = {
                userDetailsUpdate.Service.post(
                  authToken,
                  userDetailsUpdate.Request(
                    countryOfIncorporation =
                      updateDetails.countryOfIncorporation,
                    countryOfOperations = updateDetails.countryOfOperations,
                    businessType = updateDetails.businessType,
                    companyAddress = updateDetails.companyAddress,
                    postalCode = updateDetails.postalCode,
                    state = updateDetails.state,
                    city = updateDetails.city,
                    registrationNumber = updateDetails.registrationNumber,
                    incorporationDate = {
                      new SimpleDateFormat(constants.External.DATE_FORMAT)
                        .format(updateDetails.incorporationDate)
                    }
                  ),
                  userId = wallexID
                )
              }

              (for {
                organizationID <- organizationID
                wallexDetails <-
                  getOrganizationWallexAccountDetail(organizationID)
                authToken <- authToken
                wallexDetailsUpdate <-
                  wallexUserUpdate(authToken, wallexDetails.wallexId)
                result <- withUsernameToken.Ok(
                  views.html.index(successes =
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

  def createCollectionAccountsForm(accountId: String): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        (for {
          result <- withUsernameToken.Ok(
            views.html.component.master.createWallexCollectionAccount(
              views.companion.master.CreateWallexCollectionAccount.form
                .fill(
                  views.companion.master.CreateWallexCollectionAccount
                    .Data(
                      onBehalfOf = accountId,
                      name = "",
                      reference = "",
                      currency = "",
                      purpose = "",
                      description = ""
                    )
                )
            )
          )
        } yield result).recoverWith {
          case _: BaseException =>
            withUsernameToken.Ok(
              views.html.component.master.createWallexCollectionAccount()
            )
        }
    }

  def createCollectionAccounts(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.CreateWallexCollectionAccount.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master
                    .createWallexCollectionAccount(formWithErrors)
                )
              )
            },
            collectionAccount => {
              val orgId = masterTraders.Service
                .getOrganizationIDByAccountID(loginState.username)

              def wallexDetails(orgId: String) =
                orgWallexDetails.Service.tryGet(orgId)

              val authToken = wallexAuthToken.Service.getToken()

              def createCollectionAccount(authToken: String) =
                wallexCreateCollectionAccount.Service.post(
                  authToken,
                  wallexCreateCollectionAccount.Request(
                    onBehalfOf = collectionAccount.onBehalfOf,
                    name = collectionAccount.name,
                    reference = collectionAccount.reference,
                    currency = collectionAccount.currency,
                    purpose = collectionAccount.purpose,
                    description = collectionAccount.description
                  )
                )

              def insertOrUpdate(
                  collectionResponse: CreateCollectionResponse,
                  wallexId: String,
                  accountId: String
              ) = {
                collectionAccountDetails.Service.create(
                  id = collectionResponse.id,
                  wallexId = wallexId,
                  accountId = accountId
                )
              }
              (for {
                orgId <- orgId
                authToken <- authToken
                wallexDetails <- wallexDetails(orgId)
                collectionResponse <- createCollectionAccount(authToken)
                _ <- insertOrUpdate(
                  collectionResponse,
                  wallexDetails.wallexId,
                  wallexDetails.accountId
                )
                result <- withUsernameToken.Ok(
                  views.html.index(successes =
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

  def getCollectionAccountsForm(accountId: String): Action[AnyContent] =
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

  def getCollectionAccounts: Action[AnyContent] =
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

  def getWallexUserForm: Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        val organizationID =
          masterTraders.Service.getOrganizationIDByAccountID(
            loginState.username
          )

        def getOrganizationWallexAccountDetail(
            organizationID: String
        ): Future[OrganizationWallexDetail] =
          orgWallexDetails.Service.tryGet(organizationID)

        (for {
          organizationID <- organizationID
          organizationWallexAccountDetail <-
            getOrganizationWallexAccountDetail(organizationID)
          result <- withUsernameToken.Ok(
            views.html.component.master.getUserWallexAccount(
              views.companion.master.GetUserWallexAccount.form
                .fill(
                  views.companion.master.GetUserWallexAccount
                    .Data(
                      userID = organizationWallexAccountDetail.wallexId
                    )
                )
            )
          )
        } yield result).recoverWith {
          case _: BaseException =>
            withUsernameToken.Ok(
              views.html.component.master.getUserWallexAccount()
            )
        }

    }

  def getWallexUser: Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState => implicit request =>
        views.companion.master.GetUserWallexAccount.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.master
                    .getUserWallexAccount(formWithErrors)
                )
              )
            },
            orgWallexAccountDetailData => {
              val orgID =
                masterTraders.Service
                  .getOrganizationIDByAccountID(loginState.username)

              def wallexDetails(orgId: String) =
                orgWallexDetails.Service.tryGet(orgId)

              val authToken = wallexAuthToken.Service.getToken()

              def wallexGetUser(wallexId: String, authToken: String) =
                wallexGetUserRequest.Service.get(wallexId, authToken)

              def updateStatus(
                  wallexGetUserResponse: GetUserResponse
              ): Future[Int] =
                orgWallexDetails.Service.updateStatus(
                  wallexID = wallexGetUserResponse.id,
                  status = wallexGetUserResponse.status
                )

              (for {
                orgID <- orgID
                authToken <- authToken
                wallexDetail <- wallexDetails(orgID)
                wallexGetUserResponse <-
                  wallexGetUser(wallexDetail.wallexId, authToken)
                _ <- updateStatus(wallexGetUserResponse)
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
