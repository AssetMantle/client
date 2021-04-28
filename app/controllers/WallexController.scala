package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.common.Serializable.{BankAccount, BeneficiaryPayment, ConversionDetails}
import models.master.{Negotiations, Trader}
import models.masterTransaction.NegotiationFile
import models.wallex.{SimplePayments, _}
import models.master
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import transactions.responses.WallexResponse.{CreateCollectionResponse, CreateDocumentResponse, CreatePaymentQuoteResponse, GetBalanceResponse, GetUserResponse, PaymentFileUploadResponse}
import transactions.wallex._
import views.companion
import views.companion.wallex.{AcceptPaymentQuote, AddOrUpdateOrganizationAccount, AddOrganizationBeneficiary, AddDocuments, CreateCollectionAccount, CreatePaymentQuote, DeleteBeneficiary, GetUserAccount, GetCollectionAccount, WalletTransfer}

import java.io.File
import java.text.SimpleDateFormat
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WallexController @Inject() (
                                   messagesControllerComponents: MessagesControllerComponents,
                                   withoutLoginActionAsync: WithoutLoginActionAsync,
                                   masterOrganizations: master.Organizations,
                                   withUsernameToken: WithUsernameToken,
                                   organizationWallexDetails: OrganizationWallexDetails,
                                   withTraderLoginAction: WithTraderLoginAction,
                                   masterTraders: master.Traders,
                                   withoutLoginAction: WithoutLoginAction,
                                   fileResourceManager: utilities.FileResourceManager,
                                   userDetailsUpdate: WallexUserDetailsUpdate,
                                   negotiations: Negotiations,
                                   paymentFiles: PaymentFiles,
                                   updateCompanyDetails: WallexUpdateCompanyDetails,
                                   wallexOrganizationBeneficiaries: OrganizationBeneficiaries,
                                   wallexCreateBeneficiary: WallexCreateBeneficiary,
                                   wallexDeleteBeneficiary: WallexDeleteBeneficiary,
                                   wallexCreateSimplePayment: WallexCreateSimplePayment,
                                   wallexSimplePayments: SimplePayments,
                                   wallexUploadPaymentFile: WallexUploadPaymentFile,
                                   wallexCreateCollectionAccount: WallexCreateCollectionAccount,
                                   wallexGetCollectionAccount: WallexGetCollectionAccount,
                                   wallexUserKYCDetails: UserKYCDetails,
                                   wallexCollectionAccounts: CollectionAccounts,
                                   wallexGetWalletBalance: WallexGetWalletBalance,
                                   wallexUserSignUpRequest: WallexUserSignUp,
                                   wallexGetUserRequest: WallexGetUser,
                                   wallexUserKYCs: UserKYCs,
                                   wallexCreateDocument: WallexCreateDocument,
                                   wallexAuthToken: WallexAuthToken,
                                   wallexCreatePaymentQuote: CreateWallexPaymentQuote,
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

  def createOrganizationAccountForm(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
          val organizationID =
            masterTraders.Service.getOrganizationIDByAccountID(
              loginState.username
            )

          def getOrganizationWallexAccountDetail(
                                                  organizationID: String
                                                ): Future[OrganizationWallexDetail] =
            organizationWallexDetails.Service.tryGet(organizationID)

          (for {
            organizationID <- organizationID
            organizationWallexAccountDetail <-
              getOrganizationWallexAccountDetail(organizationID)
            result <- withUsernameToken.Ok(
              views.html.component.wallex.addOrUpdateOrganizationWallexAccount(
                AddOrUpdateOrganizationAccount.form
                  .fill(
                    AddOrUpdateOrganizationAccount
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
                views.html.component.wallex.addOrUpdateOrganizationWallexAccount()
              )
          }

    }

  def createOrganizationAccount(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
          companion.wallex.AddOrUpdateOrganizationAccount.form
            .bindFromRequest()
            .fold(
              formWithErrors => {
                Future(
                  BadRequest(
                    views.html.component.wallex
                      .addOrUpdateOrganizationWallexAccount(formWithErrors)
                  )
                )
              },
              orgWallexAccountDetailData => {

                def getZoneID(organizationID: String) =
                  masterOrganizations.Service.tryGetZoneID(organizationID)

                val organizationID =
                  masterTraders.Service
                    .getOrganizationIDByAccountID(loginState.username)

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
                                    organizationID: String,
                                    email: String,
                                    firstName: String,
                                    lastName: String,
                                    countryCode: String,
                                    accountType: String,
                                    wallexId: String,
                                    accountId: String,
                                    status: String
                                  ): Future[Int] =
                  organizationWallexDetails.Service.insertOrUpdate(
                    zoneID = zoneID,
                    organizationID = organizationID,
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
                  organizationID <- organizationID
                  zoneID <- getZoneID(organizationID)
                  authToken <- authToken
                  wallexUserSignUp <- wallexUserSignUp(authToken)
                  wallexGetUser <-
                    wallexGetUser(wallexUserSignUp.userId, authToken)
                  _ <- insertOrUpdate(
                    zoneID,
                    organizationID,
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

  def documentForm(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
          Future(Ok(views.html.component.wallex.addOrUpdateWallexDocument()))

    }

  def document(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
          AddDocuments.form
            .bindFromRequest()
            .fold(
              formWithErrors => {
                Future(
                  BadRequest(
                    views.html.component.wallex
                      .addOrUpdateWallexDocument(formWithErrors)
                  )
                )
              },
              file => {
                val wallexKYC = wallexUserKYCs.Service
                  .get(loginState.username, file.documentType)
                for {
                  wallexKYC <- wallexKYC
                  result <- withUsernameToken.PartialContent(
                    views.html.component.wallex.uploadOrUpdateWallexDocument(
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
      implicit loginState =>
        implicit request =>
          Future(
            Ok(
              views.html.component.wallex.submitDocumentToWallex(
                views.companion.wallex.SubmitDocument.form.fill(
                  views.companion.wallex.SubmitDocument.Data(
                    documentType = documentType
                  )
                ),documentType = documentType
              )
            )
          )
    }

  def submitDocumentToWallex(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
          companion.wallex.SubmitDocument.form
            .bindFromRequest()
            .fold(
              formWithErrors => {
                Future(
                  BadRequest(
                    views.html.component.wallex
                      .submitDocumentToWallex(formWithErrors,
                        formWithErrors.data(constants.FormField.DOCUMENT_TYPE.name))
                  )
                )
              },
              wallexDocument => {

                val organizationID = masterTraders.Service
                  .getOrganizationIDByAccountID(loginState.username)

                def wallexDetails(organizationID: String) =
                  organizationWallexDetails.Service.tryGet(organizationID)

                def getWallexDocument(
                                       documentType: String
                                     ): Future[models.wallex.UserKYC] = {
                  wallexUserKYCs.Service.tryGet(
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
                                    userKYC: UserKYC
                                  ) = {
                  val path =
                    fileResourceManager
                      .getWallexFilePath(userKYC.documentType)
                  val fetchFile: File = utilities.FileOperations
                    .fetchFile(path, userKYC.fileName)
                  val fileArray =
                    utilities.FileOperations.convertToByteArray(fetchFile)
                  wallexCreateDocument.Service
                    .put(authToken, uploadUrl, fileArray)
                }

                def insertDocumentDetails(
                                           organizationID: String,
                                           wallexId: String,
                                           documentResponse: CreateDocumentResponse
                                         ) = {
                  wallexUserKYCDetails.Service.insertOrUpdate(
                    id = documentResponse.id,
                    organizationID = organizationID,
                    wallexId = wallexId,
                    url = documentResponse.uploadURL,
                    documentName = documentResponse.documentName,
                    documentType = documentResponse.documentType
                  )
                }

                def updateStatus(
                                  wallexID: String
                                ): Future[Int] =
                  organizationWallexDetails.Service.updateStatus(
                    wallexID = wallexID,
                    status =
                      constants.Status.SendWalletTransfer.ZONE_SEND_FOR_SCREENING
                  )

                (for {
                  organizationID <- organizationID
                  authToken <- authToken
                  wallexDetails <- wallexDetails(organizationID)
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
                    organizationID,
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
        val wallexKYC = wallexUserKYCs.Service
          .get(loginState.username, documentType)
        for {
          wallexKYC <- wallexKYC
          result <- withUsernameToken.Ok(
            views.html.component.wallex.uploadOrUpdateWallexDocument(
              wallexKYC,
              documentType
            )
          )
        } yield result
    }
  def initiatePaymentForm(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        Future(
          Ok(
            views.html.component.wallex
              .createWallexPaymentQuote()
          )
        )
    }

  def initiatePayment(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        CreatePaymentQuote.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.wallex
                    .createWallexPaymentQuote(
                      formWithErrors
                    )
                )
              )
            },
            paymentQuote => {

              val organizationID =
                masterTraders.Service
                  .getOrganizationIDByAccountID(loginState.username)

              def getWallexDetails(organizationID: String) =
                organizationWallexDetails.Service.tryGet(organizationID)

              val authToken = wallexAuthToken.Service.getToken()

              def createPaymentQuote(authToken: String) =
                wallexCreatePaymentQuote.Service.post(
                  authToken,
                  wallexCreatePaymentQuote.Request(
                    sellCurrency = paymentQuote.sellCurrency,
                    buyCurrency = paymentQuote.buyCurrency,
                    amount = paymentQuote.amount,
                    beneficiaryId = paymentQuote.beneficiaryId
                  )
                )

              def getQuoteResponse(postResponse: CreatePaymentQuoteResponse) = {

                for {

                  result <- withUsernameToken.PartialContent(
                    views.html.component.wallex
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
                    organizationID: String,
                    wallexId: String,
                    fileId: String,
                    fileType: String,
                    quoteId: String
                ) =
                  paymentFileDetails.Service.insertOrUpdate(
                    organizationID = organizationID,
                    negotiationId = paymentQuote.negotiationID,
                    quoteId = quoteId,
                    wallexId = wallexId,
                    fileId = fileId,
                    fileType = fileType
                  )*/

              (for {
                //negotiationFile <- negotiationFile
                organizationID <- organizationID
                authToken <- authToken
                /*fileUrlResponse <-
                    uploadFileURLRespone(authToken, negotiationFile)
                  uploadFile <- uploadFileToWallex(
                    authToken,
                    fileUrlResponse.data.uploadUrl,
                    negotiationFile
                  )*/
                wallexDetails <- getWallexDetails(organizationID)
                postResponse <- createPaymentQuote(authToken)
                result <- getQuoteResponse(postResponse)
                /* _ <- insert(
                    organizationID,
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

  def acceptQuoteForm(
      quoteId: String
  ): Action[AnyContent] = {
    withoutLoginActionAsync { implicit request =>
      Future(
        Ok(
          views.html.component.wallex
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
  def acceptQuote(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        AcceptPaymentQuote.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.wallex
                    .acceptWallexPaymentQuoteRequest(
                      formWithErrors,
                      formWithErrors.data(constants.FormField.QUOTE_ID.name)
                    )
                )
              )
            },
            createPaymentResponse => {

              val organizationID =
                masterTraders.Service
                  .getOrganizationIDByAccountID(loginState.username)
              val zoneID =
                masterTraders.Service
                  .tryGetZoneIDByAccountID(loginState.username)

              def getWallexDetails(organizationID: String) =
                organizationWallexDetails.Service.tryGet(organizationID)

              val file =
                paymentFiles.Service.tryGet(createPaymentResponse.quoteId)
              val authToken = wallexAuthToken.Service.getToken()

              def createSimplePayment(
                  authToken: String,
                  file: PaymentFile
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
                  zoneID: String,
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
                wallexSimplePayments.Service.create(
                  wallexId = wallexId,
                  zoneID = zoneID,
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
                organizationID <- organizationID
                zoneID <- zoneID
                wallexDetails <- getWallexDetails(organizationID)
                authToken <- authToken
                file <- file
                simplePaymentResponse <- createSimplePayment(authToken, file)
                simplePayment <- insertOrUpdate(
                  wallexId = wallexDetails.wallexId,
                  zoneID = zoneID,
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
                      aba = simplePaymentResponse.data.beneficiary.bankAccount.aba,
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
      implicit loginState =>
        implicit request =>
        Future(Ok(views.html.component.wallex.addOrgWallexBeneficiaryDetails()))
    }
  }

  def createBeneficiaries: Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        AddOrganizationBeneficiary.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.wallex
                    .addOrgWallexBeneficiaryDetails(formWithErrors)
                )
              )
            },
            beneficiary => {

              val organizationID =
                masterTraders.Service.getOrganizationIDByAccountID(
                  loginState.username
                )

              def getOrganizationWallexAccountDetail(
                  organizationID: String
              ): Future[OrganizationWallexDetail] =
                organizationWallexDetails.Service.tryGet(organizationID)

              val authToken = wallexAuthToken.Service.getToken()

              def createBeneficiary(authToken: String) = {
                wallexCreateBeneficiary.Service.post(
                  authToken,
                  wallexCreateBeneficiary.Request(
                    country = beneficiary.country,
                    address = beneficiary.address,
                    city = beneficiary.city,
                    postcode = beneficiary.postcode,
                    stateOrProvince = beneficiary.stateOrProvince,
                    nickname = beneficiary.nickName,
                    entityType = beneficiary.entityType,
                    companyName = beneficiary.companyName,
                    bankAccount = BankAccount(
                      bankName = beneficiary.bankData.bankName,
                      currency = beneficiary.bankData.currency,
                      country = beneficiary.bankData.country,
                      accountNumber = beneficiary.bankData.accountNumber,
                      bicSwift = beneficiary.bankData.bicSwift,
                      aba = beneficiary.bankData.aba,
                      address = beneficiary.bankData.address,
                      bankAccountHolderName =
                        beneficiary.bankData.accountHolderName
                    )
                  )
                )
              }

              def insertOrUpdate(
                  organizationID: String,
                  traderID: String,
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
                wallexOrganizationBeneficiaries.Service.create(
                  organizationID = organizationID,
                  traderID = traderID,
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
                    aba = beneficiaryResponse.data.bankAccount.aba,
                    currency = beneficiaryResponse.data.bankAccount.country,
                    bankAccountHolderName =
                      beneficiaryResponse.data.bankAccount.bankAccountHolderName
                  )
                )
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
        views.html.component.wallex.deleteBeneficiary(
          DeleteBeneficiary.form
            .fill(
              DeleteBeneficiary.Data(id = beneficiaryId)
            )
        )
      )
    }

  def deleteBeneficiary(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        companion.wallex.DeleteBeneficiary.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.wallex.deleteBeneficiary(formWithErrors)
                )
              )
            },
            deleteBeneficiary => {
              val beneficiary =
                wallexOrganizationBeneficiaries.Service
                  .getByTraderID(loginState.username)

              val authToken = wallexAuthToken.Service.getToken()

              def deleteBeneficiaryFromWallex(
                  authToken: String,
                  benefeciaryId: String
              ) = {
                wallexDeleteBeneficiary.Service
                  .delete(authToken, beneficiaryId = benefeciaryId)
              }

              def deleteBeneficiary(beneficiaryId: String): Future[Int] =
                wallexOrganizationBeneficiaries.Service
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
      implicit loginState =>
        implicit request =>
        def getWallexDetails(organizationID: String) =
          organizationWallexDetails.Service.tryGet(organizationID)

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
                views.html.component.wallex.wallexWalletTransfer(
                  WalletTransfer.form
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
              views.html.component.wallex.wallexWalletTransfer(
                WalletTransfer.form.fill(
                  companion.wallex.WalletTransfer.Data(
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
      implicit loginState =>
        implicit request =>
        companion.wallex.WalletTransfer.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.wallex
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
                  zoneID = trader.zoneID,
                  organizationID = trader.organizationID,
                  traderID = trader.id,
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

  def updateCompanyAccountForm(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        Future(Ok(views.html.component.wallex.updateDetailsWallexAccount()))

    }

  def updateCompanyAccount(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        views.companion.wallex.UpdateCompanyAccount.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.wallex
                    .updateDetailsWallexAccount(formWithErrors)
                )
              )
            },
            updateCompany => {

              val organizationID =
                masterTraders.Service.getOrganizationIDByAccountID(
                  loginState.username
                )

              def getOrganizationWallexAccountDetail(
                  organizationID: String
              ): Future[OrganizationWallexDetail] =
                organizationWallexDetails.Service.tryGet(organizationID)

              val authToken = wallexAuthToken.Service.getToken()

              def companyDetailsUpdate(authToken: String, wallexID: String) = {
                updateCompanyDetails.Service.post(
                  authToken,
                  updateCompanyDetails.Request(
                    countryOfIncorporation =
                      updateCompany.countryOfIncorporation,
                    countryOfOperations = updateCompany.countryOfOperations,
                    businessType = updateCompany.businessType,
                    companyAddress = updateCompany.companyAddress,
                    postalCode = updateCompany.postalCode,
                    state = updateCompany.state,
                    city = updateCompany.city,
                    registrationNumber = updateCompany.registrationNumber,
                    incorporationDate = {
                      new SimpleDateFormat(constants.External.Wallex.DATE_FORMAT)
                        .format(updateCompany.incorporationDate)
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
                  companyDetailsUpdate(authToken, wallexDetails.wallexId)
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
      implicit loginState =>
        implicit request =>
        (for {
          result <- withUsernameToken.Ok(
            views.html.component.wallex.createWallexCollectionAccount(
              CreateCollectionAccount.form
                .fill(
                  CreateCollectionAccount
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
              views.html.component.wallex.createWallexCollectionAccount()
            )
        }
    }

  def createCollectionAccounts(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        companion.wallex.CreateCollectionAccount.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.wallex
                    .createWallexCollectionAccount(formWithErrors)
                )
              )
            },
            collectionAccount => {
              val organizationID = masterTraders.Service
                .getOrganizationIDByAccountID(loginState.username)

              def wallexDetails(organizationID: String) =
                organizationWallexDetails.Service.tryGet(organizationID)

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
                wallexCollectionAccounts.Service.create(
                  id = collectionResponse.id,
                  wallexId = wallexId,
                  accountId = accountId
                )
              }
              (for {
                organizationID <- organizationID
                authToken <- authToken
                wallexDetails <- wallexDetails(organizationID)
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
        views.html.component.wallex.getWallexCollectionAccount(
          GetCollectionAccount.form.fill(
            GetCollectionAccount
              .Data(accountId = accountId)
          )
        )
      )
    }

  def getCollectionAccounts: Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        companion.wallex.GetCollectionAccount.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.wallex
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
                  views.html.component.wallex
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

  def getUserForm: Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        val organizationID =
          masterTraders.Service.getOrganizationIDByAccountID(
            loginState.username
          )

        def getOrganizationWallexAccountDetail(
            organizationID: String
        ): Future[OrganizationWallexDetail] =
          organizationWallexDetails.Service.tryGet(organizationID)

        (for {
          organizationID <- organizationID
          organizationWallexAccountDetail <-
            getOrganizationWallexAccountDetail(organizationID)
          result <- withUsernameToken.Ok(
            views.html.component.wallex.getUserWallexAccount(
              GetUserAccount.form
                .fill(
                  GetUserAccount
                    .Data(
                      userID = organizationWallexAccountDetail.wallexId
                    )
                )
            )
          )
        } yield result).recoverWith {
          case _: BaseException =>
            withUsernameToken.Ok(
              views.html.component.wallex.getUserWallexAccount()
            )
        }

    }

  def getUser: Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        companion.wallex.GetUserAccount.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.wallex
                    .getUserWallexAccount(formWithErrors)
                )
              )
            },
            organizationAccountData => {
              val organizationID =
                masterTraders.Service
                  .getOrganizationIDByAccountID(loginState.username)

              def wallexDetails(organizationID: String) =
                organizationWallexDetails.Service.tryGet(organizationID)

              val authToken = wallexAuthToken.Service.getToken()

              def wallexGetUser(wallexId: String, authToken: String) =
                wallexGetUserRequest.Service.get(wallexId, authToken)

              def updateStatus(
                  wallexGetUserResponse: GetUserResponse
              ): Future[Int] =
                organizationWallexDetails.Service.updateStatus(
                  wallexID = wallexGetUserResponse.id,
                  status = wallexGetUserResponse.status
                )

              (for {
                organizationID <- organizationID
                authToken <- authToken
                wallexDetail <- wallexDetails(organizationID)
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

  def updateAccountForm(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        Future(Ok(views.html.component.wallex.updateUserDetailsWallexAccount()))

    }

  def updateAccount(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
        views.companion.wallex.UpdateUserAccount.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future(
                BadRequest(
                  views.html.component.wallex
                    .updateUserDetailsWallexAccount(formWithErrors)
                )
              )
            },
            userUpdateAccount => {

              val organizationID =
                masterTraders.Service.getOrganizationIDByAccountID(
                  loginState.username
                )

              def getOrganizationWallexAccountDetail(
                  organizationID: String
              ): Future[OrganizationWallexDetail] =
                organizationWallexDetails.Service.tryGet(organizationID)

              val authToken = wallexAuthToken.Service.getToken()

              def wallexUserUpdate(authToken: String, wallexID: String) = {
                userDetailsUpdate.Service.post(
                  authToken,
                  userDetailsUpdate.Request(
                    mobileCountryCode = userUpdateAccount.mobileCountryCode,
                    mobileNumber = userUpdateAccount.mobileNumber,
                    gender = userUpdateAccount.gender,
                    countryOfBirth = userUpdateAccount.countryOfBirth,
                    nationality = userUpdateAccount.nationality,
                    countryOfResidence = userUpdateAccount.countryOfResidence,
                    residentialAddress = userUpdateAccount.residentialAddress,
                    countryCode = userUpdateAccount.countryCode,
                    postalCode = userUpdateAccount.postalCode,
                    dateOfBirth = {
                      new SimpleDateFormat(constants.External.Wallex.DATE_FORMAT)
                        .format(userUpdateAccount.dateOfBirth)
                    },
                    identificationType = userUpdateAccount.identificationType,
                    identificationNumber = userUpdateAccount.identificationNumber,
                    issueDate = {
                      new SimpleDateFormat(constants.External.Wallex.DATE_FORMAT)
                        .format(userUpdateAccount.issueDate)
                    },
                    expiryDate = {
                      new SimpleDateFormat(constants.External.Wallex.DATE_FORMAT)
                        .format(userUpdateAccount.expiryDate)
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
}
