package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.common.Serializable.{BankAccount, BeneficiaryPayment, ConversionDetails, EmploymentDetails, ResidentialAddressDetails}
import models.master.{Negotiations, Trader}
import models.masterTransaction.{NegotiationFile, NegotiationFiles}
import models.wallex.{AccountProfileDetails, SimplePayments, _}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import transactions.responses.WallexResponse.{CreateCollectionResponse, CreateDocumentResponse, CreatePaymentQuoteResponse, GetBalanceResponse, GetFundingResponse, GetUserResponse, PaymentFileUploadResponse, ScreeningResponse, UpdateCompanyDetailsResponse, UpdateUserDetailsResponse, WalletToWalletTransferResponse}
import transactions.wallex._
import utilities.JSON.convertJsonStringToObject
import utilities.{KeyStore, MicroNumber}
import views.companion
import views.companion.wallex.{WalletTransfer, _}

import java.text.SimpleDateFormat
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WallexController @Inject() (
                                   blockchainTransactionRedeemFiats: blockchainTransaction.RedeemFiats,
                                   blockchainAccounts: blockchain.Accounts,
                                   blockchainTransactionIssueFiats: blockchainTransaction.IssueFiats,
                                   fileResourceManager: utilities.FileResourceManager,
                                   keyStore: KeyStore,
                                   masterAccounts: master.Accounts,
                                   masterZones: master.Zones,
                                   masterOrganizations: master.Organizations,
                                   masterTraders: master.Traders,
                                   masterTransactionRedeemFiatRequests: masterTransaction.RedeemFiatRequests,
                                   messagesControllerComponents: MessagesControllerComponents,
                                   negotiations: Negotiations,
                                   negotiationFiles: NegotiationFiles,
                                   paymentFiles: PaymentFiles,
                                   wallexOrganizationAccountDetails: OrganizationAccountDetails,
                                   transactionsIssueFiat: transactions.IssueFiat,
                                   transactionsRedeemFiat: transactions.RedeemFiat,
                                   transaction: utilities.Transaction,
                                   userDetailsUpdate: UserUpdateAccountDetails,
                                   updateCompanyDetails: UpdateCompanyDetails,
                                   withoutLoginActionAsync: WithoutLoginActionAsync,
                                   withUsernameToken: WithUsernameToken,
                                   withTraderLoginAction: WithTraderLoginAction,
                                   withoutLoginAction: WithoutLoginAction,
                                   withZoneLoginAction: WithZoneLoginAction,
                                   wallexWalletTransfer: CreateWalletTransfer,
                                   wallexOrganizationBeneficiaries: OrganizationBeneficiaries,
                                   wallexCreateBeneficiary: CreateBeneficiaryAccount,
                                   wallexDeleteBeneficiary: DeleteBeneficiary,
                                   wallexCreateSimplePayment: CreateSimplePayment,
                                   wallexSimplePayments: SimplePayments,
                                   wallexUploadPaymentFile: UploadPaymentFile,
                                   wallexCreateCollectionAccount: CreateCollectionAccount,
                                   wallexGetCollectionAccount: GetCollectionAccount,
                                   wallexUserKYCDetails: UserKYCDetails,
                                   wallexCollectionAccounts: CollectionAccounts,
                                   wallexGetWalletBalance: GetWalletBalance,
                                   wallexUserSignUpRequest: UserSignUp,
                                   wallexGetUserRequest: GetUser,
                                   wallexUserKYCs: UserKYCs,
                                   wallexCreateDocument: CreateDocument,
                                   wallexAuthToken: GenerateAuthToken,
                                   wallexCreatePaymentQuote: CreatePaymentQuote,
                                   wallexUserScreening: UserSubmitForScreening,
                                   wallexWalletTransferRequest: WalletTransferRequests,
                                   wallexWalletTransfers: WalletTransfers,
                                   wallexFundSimplePayment: FundSimplePayment,
                                   wallexGetFundingStatus: GetFundingStatus,
                                   wallexFundingStatusDetails : FundingStatusDetails,
                                   wallexAccountProfileDetails: AccountProfileDetails,
                                   wallexAccountCompanyDetails: AccountCompanyDetails

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
                                                ): Future[OrganizationAccountDetail] =
            wallexOrganizationAccountDetails.Service.tryGet(organizationID)

          (for {
            organizationID <- organizationID
            organizationWallexAccountDetail <-
              getOrganizationWallexAccountDetail(organizationID)
            result <- withUsernameToken.Ok(
              views.html.component.wallex.traderAddOrganizationAccount(
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
              Future(Ok(views.html.component.wallex.traderAddOrganizationAccount()))
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
                      .traderAddOrganizationAccount(formWithErrors)
                  )
                )
              },
              organizationWallexAccountData => {

                val getTraderID =
                  masterTraders.Service.tryGetID(loginState.username)

                val organizationID =
                  masterTraders.Service
                    .getOrganizationIDByAccountID(loginState.username)

                val authToken = wallexAuthToken.Service.getToken()

                def userSignUpWallex(authToken: String) = {
                  wallexUserSignUpRequest.Service.post(
                    authToken,
                    wallexUserSignUpRequest.Request(
                      firstName = organizationWallexAccountData.firstName,
                      lastName = organizationWallexAccountData.lastName,
                      email = organizationWallexAccountData.email,
                      countryCode = organizationWallexAccountData.countryCode,
                      accountType = organizationWallexAccountData.accountType
                    )
                  )
                }

                def userGetWallexAccount(wallexID: String, authToken: String) =
                  wallexGetUserRequest.Service.get(wallexID, authToken)

                def insertOrUpdate(
                                    organizationID: String,
                                    email: String,
                                    firstName: String,
                                    lastName: String,
                                    countryCode: String,
                                    accountType: String,
                                    wallexID: String,
                                    accountID: String,
                                    status: String,
                                    traderID: String
                                  ): Future[Int] =
                  wallexOrganizationAccountDetails.Service.insertOrUpdate(
                    organizationID = organizationID,
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    countryCode = countryCode,
                    accountType = accountType,
                    wallexID = wallexID,
                    accountID = accountID,
                    status = status,
                    traderID = traderID
                  )

                (for {
                  organizationID <- organizationID
                  traderID <- getTraderID
                  authToken <- authToken
                  wallexUserSignUp <- userSignUpWallex(authToken)
                  userWallexAccount <-
                    userGetWallexAccount(wallexUserSignUp.userId, authToken)
                  _ <- insertOrUpdate(
                    organizationID,
                    wallexUserSignUp.email,
                    wallexUserSignUp.firstName,
                    wallexUserSignUp.lastName,
                    wallexUserSignUp.countryCode,
                    wallexUserSignUp.accountType,
                    wallexUserSignUp.userId,
                    wallexUserSignUp.accountId,
                    userWallexAccount.status,
                    traderID
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

  def addDocumentForm(): Action[AnyContent] =
    withoutLoginAction {
        implicit request =>
          Ok(views.html.component.wallex.traderAddOrUpdateDocument())

    }

  def addDocument(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
          SubmitDocument.form
            .bindFromRequest()
            .fold(
              formWithErrors => {
                Future(
                  BadRequest(
                    views.html.component.wallex
                      .traderAddOrUpdateDocument(formWithErrors)
                  )
                )
              },
              addWallexDocumentData => {
                val wallexKYC = wallexUserKYCs.Service
                  .get(loginState.username, addWallexDocumentData.documentType)

                (for {
                  wallexKYC <- wallexKYC
                  result <- Future(PartialContent(
                    views.html.component.wallex.traderUploadOrUpdateDocument(
                      wallexKYC,
                      addWallexDocumentData.documentType
                    ))
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

  def submitDocumentToWallexForm(documentType: String): Action[AnyContent] =
    withoutLoginAction {
        implicit request =>
          Ok(views.html.component.wallex.traderSubmitDocument(documentType = documentType))
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
                      .traderSubmitDocument(formWithErrors,
                        formWithErrors.data(constants.FormField.DOCUMENT_TYPE.name))
                  )
                )
              },
              documentData => {

                val organizationID = masterTraders.Service
                  .getOrganizationIDByAccountID(loginState.username)

                def getWallexAccount(organizationID: String) =
                  wallexOrganizationAccountDetails.Service.tryGet(organizationID)

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
                                    wallexID: String,
                                    fileName: String,
                                    fileType: String
                                  ) =
                  wallexCreateDocument.Service.post(
                    authToken,
                    wallexID,
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
                  val fileArray = utilities.FileOperations.convertToByteArray(utilities.FileOperations
                                 .fetchFile(fileResourceManager.getWallexFilePath(userKYC.documentType), userKYC.fileName))

                  wallexCreateDocument.Service.put(authToken, uploadUrl, fileArray)
                }

                def insertDocumentDetails(
                                           organizationID: String,
                                           wallexID: String,
                                           documentResponse: CreateDocumentResponse
                                         ) = {
                  wallexUserKYCDetails.Service.insertOrUpdate(
                    id = documentResponse.id,
                    organizationID = organizationID,
                    wallexID = wallexID,
                    url = documentResponse.uploadURL,
                    documentName = documentResponse.documentName,
                    documentType = documentResponse.documentType
                  )
                }

                def updateStatus(
                                  wallexID: String
                                ): Future[Int] =
                  wallexOrganizationAccountDetails.Service.updateStatus(
                    wallexID = wallexID,
                    status =
                      constants.Status.SendWalletTransfer.ZONE_SEND_FOR_SCREENING
                  )

                (for {
                  organizationID <- organizationID
                  authToken <- authToken
                  wallexAccount <- getWallexAccount(organizationID)
                  wallexDocument <- getWallexDocument(documentData.documentType)
                  createDocument <- createDocument(
                    authToken,
                    wallexAccount.wallexID,
                    wallexDocument.fileName,
                    wallexDocument.documentType
                  )
                  _ <-
                    uploadDocument(authToken, createDocument.uploadURL, wallexDocument)
                  _ <- insertDocumentDetails(
                    organizationID,
                    wallexAccount.wallexID,
                    createDocument
                  )
                  _ <- updateStatus(wallexAccount.wallexID)
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
      implicit loginState =>
        implicit request =>
        val wallexKYC = wallexUserKYCs.Service
          .get(loginState.username, documentType)

        (for {
          wallexKYC <- wallexKYC
          result <- withUsernameToken.Ok(
            views.html.component.wallex.traderUploadOrUpdateDocument(
              wallexKYC,
              documentType
            )
          )
        } yield result).recover {
        case baseException: BaseException =>
          InternalServerError(
            views.html.index(failures = Seq(baseException.failure))
          )
      }
    }

  def initiatePaymentForm(negotiationID: String): Action[AnyContent] =
    withoutLoginAction {
        implicit request =>
        Ok(views.html.component.wallex.traderCreatePaymentQuote(negotiationID = negotiationID))
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
                    .traderCreatePaymentQuote(
                      formWithErrors,
                      formWithErrors.data(constants.FormField.NEGOTIATION_ID.name)
                    )
                )
              )
            },
            paymentQuoteData => {

              val negotiationID = paymentQuoteData.negotiationID
              val negotiationFile = negotiationFiles.Service.tryGet(negotiationID,constants.File.Negotiation.INVOICE)

              val organizationID = masterTraders.Service.getOrganizationIDByAccountID(loginState.username)

              def getWallexAccount(organizationID: String) =
                wallexOrganizationAccountDetails.Service.tryGet(organizationID)

              val authToken = wallexAuthToken.Service.getToken()

              def createPaymentQuote(authToken: String) =
                wallexCreatePaymentQuote.Service.post(
                  authToken,
                  wallexCreatePaymentQuote.Request(
                    sellCurrency = paymentQuoteData.sellCurrency,
                    buyCurrency = paymentQuoteData.buyCurrency,
                    amount = paymentQuoteData.amount,
                    beneficiaryId = paymentQuoteData.beneficiaryID
                  )
                )

              def getResult(postResponse: CreatePaymentQuoteResponse) = {

                 withUsernameToken.PartialContent(
                    views.html.component.wallex.traderAcceptPaymentQuoteResponse(postResponse))
              }

              def getUploadFileURL(
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
                val fileArray = utilities.FileOperations.convertToByteArray(utilities.FileOperations
                    .fetchFile(fileResourceManager.getNegotiationFilePath(constants.File.Negotiation.INVOICE), negotiationFile.fileName))

                wallexUploadPaymentFile.Service.put(authToken, uploadURL, fileArray)
              }

              def insert(
                    organizationID: String,
                    wallexID: String,
                    fileID: String,
                    fileType: String,
                    quoteID: String
                ) =
                  paymentFiles.Service.insertOrUpdate(
                    organizationID = organizationID,
                    negotiationID = paymentQuoteData.negotiationID,
                    quoteID = quoteID,
                    wallexID = wallexID,
                    fileID = fileID,
                    fileType = fileType
                  )

              (for {
                negotiationFile <- negotiationFile
                organizationID <- organizationID
                authToken <- authToken
                fileUrlResponse <-
                  getUploadFileURL(authToken, negotiationFile)
                uploadFile <- uploadFileToWallex(
                    authToken,
                    fileUrlResponse.data.uploadUrl,
                    negotiationFile
                  )
                wallexAccount <- getWallexAccount(organizationID)
                postResponse <- createPaymentQuote(authToken)
                result <- getResult(postResponse)
                _ <- insert(
                    organizationID,
                  wallexAccount.wallexID,
                    fileUrlResponse.data.fileId,
                    constants.File.Negotiation.INVOICE,
                    postResponse.data.quoteId
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

  def acceptQuoteForm(
      quoteID: String
  ): Action[AnyContent] = {
    withoutLoginAction {
      implicit request =>
        Ok(views.html.component.wallex.traderAcceptPaymentQuoteRequest(quoteID = quoteID))
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
                    .traderAcceptPaymentQuoteRequest(
                      formWithErrors,
                      formWithErrors.data(constants.FormField.WALLEX_QUOTE_ID.name)
                    )
                )
              )
            },
            createPaymentData => {

              val validateUsernamePassword = masterAccounts.Service.validateUsernamePassword(username = loginState.username, password = createPaymentData.password)

              val trader = masterTraders.Service.tryGetByAccountID(loginState.username)
              val organizationID = masterTraders.Service.getOrganizationIDByAccountID(loginState.username)
              val zoneID = masterTraders.Service.tryGetZoneIDByAccountID(loginState.username)
              def zoneAccountID(zoneID: String): Future[String] = masterZones.Service.tryGetAccountID(zoneID)
              def zoneAddress(zoneAccountID: String): Future[String] = blockchainAccounts.Service.tryGetAddress(zoneAccountID)

              def getWallexAccount(organizationID: String) =
                wallexOrganizationAccountDetails.Service.tryGet(organizationID)

              val file =
                paymentFiles.Service.tryGet(createPaymentData.quoteID)
              val authToken = wallexAuthToken.Service.getToken()

              def createSimplePayment(
                  authToken: String,
                  file: PaymentFile
              ) =
                wallexCreateSimplePayment.Service.post(
                  authToken,
                  wallexCreateSimplePayment.Request(
                    fundingSource = createPaymentData.fundingSource,
                    paymentReference = createPaymentData.paymentReference,
                    quoteId = createPaymentData.quoteID,
                    purposeOfTransfer = createPaymentData.purposeOfTransfer,
                    referenceId = file.negotiationID,
                    files = Seq(file.fileID)
                  )
                )

              def insertOrUpdate(
                  simplePaymentID: String,
                  wallexID: String,
                  organizationID: String,
                  status: String,
                  createdAt: String,
                  referenceID: String,
                  fundingSource: String,
                  purposeOfTransfer: String,
                  fundingReference: String,
                  fundingCutoffTime: String,
                  beneficiary: BeneficiaryPayment,
                  conversionDetails: ConversionDetails,
                  zoneApproved: Option[Boolean]
              ) =
                wallexSimplePayments.Service.create(
                  simplePaymentID = simplePaymentID,
                  wallexID = wallexID,
                  organizationID = organizationID,
                  status = status,
                  createdAt = createdAt,
                  referenceID = referenceID,
                  fundingSource = fundingSource,
                  purposeOfTransfer = purposeOfTransfer,
                  fundingReference = fundingReference,
                  fundingCutoffTime = fundingCutoffTime,
                  beneficiary = beneficiary,
                  conversionDetails = conversionDetails,
                  zoneApproved = zoneApproved
                )

              def sendTransactionAndGetResult(validateUsernamePassword: Boolean, toAddress: String, trader: Trader, redeemAmount: Double): Future[Result] = {
                if (validateUsernamePassword) {
                  if (loginState.acl.getOrElse(throw new BaseException(constants.Response.UNAUTHORIZED)).redeemFiat) {
                    val ticketID = transaction.process[blockchainTransaction.RedeemFiat, transactionsRedeemFiat.Request](
                      entity = blockchainTransaction.RedeemFiat(from = loginState.address, to = toAddress, redeemAmount = redeemAmount,
                        gas = createPaymentData.gas, ticketID = "", mode = transactionMode),
                      blockchainTransactionCreate = blockchainTransactionRedeemFiats.Service.create,
                      request = transactionsRedeemFiat.Request(transactionsRedeemFiat.BaseReq(from = loginState.address, gas = createPaymentData.gas), to = toAddress,
                        password = createPaymentData.password, redeemAmount = redeemAmount, mode = transactionMode),
                      action = transactionsRedeemFiat.Service.post,
                      onSuccess = blockchainTransactionRedeemFiats.Utility.onSuccess,
                      onFailure = blockchainTransactionRedeemFiats.Utility.onFailure,
                      updateTransactionHash = blockchainTransactionRedeemFiats.Service.updateTransactionHash
                    )
                    for {
                      ticketID <- ticketID
                      _ <- createRedeemFiatRequests(trader.id, ticketID, redeemAmount)
                      result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.FIAT_REDEEMED)))
                    } yield result
                  } else throw new BaseException(constants.Response.UNAUTHORIZED)
                } else Future(BadRequest(views.html.component.wallex.traderAcceptPaymentQuoteRequest(views.companion.wallex.
                  AcceptPaymentQuote.form.fill(createPaymentData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message),
                  quoteID = createPaymentData.quoteID)))
              }

              def createRedeemFiatRequests(traderID: String, ticketID: String, redeemAmount: Double): Future[String] =
                masterTransactionRedeemFiatRequests.Service.create(traderID, ticketID, redeemAmount)


              (for {
                validateUsernamePassword <- validateUsernamePassword
                trader <- trader
                organizationID <- organizationID
                zoneID <- zoneID
                zoneAccountID <- zoneAccountID(zoneID)
                zoneAddress <- zoneAddress(zoneAccountID)
                wallexAccount <- getWallexAccount(organizationID)
                authToken <- authToken
                file <- file
                simplePaymentResponse <- createSimplePayment(authToken, file)
                simplePayment <- insertOrUpdate(
                  simplePaymentID= simplePaymentResponse.data.simplePaymentId,
                  wallexID = wallexAccount.wallexID,
                  organizationID = organizationID,
                  status = simplePaymentResponse.data.status,
                  createdAt = simplePaymentResponse.data.createdAt,
                  referenceID = simplePaymentResponse.data.referenceId,
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

                _ <- sendTransactionAndGetResult(validateUsernamePassword,zoneAddress,trader,simplePaymentResponse.data.totalAmount)
                result <- withUsernameToken.Ok(
                  views.html.index(successes =
                    Seq(constants.Response.WALLEX_SIMPLE_PAYMENT_CREATED)
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
    withoutLoginAction {
        implicit request =>
        Ok(views.html.component.wallex.traderAddOrganizationBeneficiary())
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
                    .traderAddOrganizationBeneficiary(formWithErrors)
                )
              )
            },
            addOrganizationBeneficiaryData => {

              val organizationID =
                masterTraders.Service.getOrganizationIDByAccountID(
                  loginState.username
                )

              def getOrganizationWallexAccount(
                  organizationID: String
              ): Future[OrganizationAccountDetail] =
                wallexOrganizationAccountDetails.Service.tryGet(organizationID)

              val authToken = wallexAuthToken.Service.getToken()

              def createBeneficiary(authToken: String) = {
                wallexCreateBeneficiary.Service.post(
                  authToken,
                  wallexCreateBeneficiary.Request(
                    country = addOrganizationBeneficiaryData.country,
                    address = addOrganizationBeneficiaryData.address,
                    city = addOrganizationBeneficiaryData.city,
                    postcode = addOrganizationBeneficiaryData.postcode,
                    stateOrProvince = addOrganizationBeneficiaryData.stateOrProvince,
                    nickname = addOrganizationBeneficiaryData.nickName,
                    entityType = addOrganizationBeneficiaryData.entityType,
                    companyName = addOrganizationBeneficiaryData.companyName,
                    bankAccount = BankAccount(
                      bankName = addOrganizationBeneficiaryData.bankData.bankName,
                      currency = addOrganizationBeneficiaryData.bankData.currency,
                      country = addOrganizationBeneficiaryData.bankData.country,
                      accountNumber = addOrganizationBeneficiaryData.bankData.accountNumber,
                      bicSwift = addOrganizationBeneficiaryData.bankData.bicSwift,
                      aba = addOrganizationBeneficiaryData.bankData.aba,
                      address = addOrganizationBeneficiaryData.bankData.address,
                      bankAccountHolderName =
                        addOrganizationBeneficiaryData.bankData.accountHolderName
                    )
                  )
                )
              }

              def insertOrUpdate(
                  organizationID: String,
                  wallexID: String,
                  beneficiaryID: String,
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
                  wallexID = wallexID,
                  beneficiaryID = beneficiaryID,
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
                wallexAccount <-
                  getOrganizationWallexAccount(organizationID)
                authToken <- authToken
                beneficiaryResponse <- createBeneficiary(authToken)
                _ <- insertOrUpdate(
                  organizationID,
                  wallexAccount.wallexID,
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
    withoutLoginAction {
      implicit request =>
      Ok(views.html.component.wallex.traderDeleteBeneficiary(
          DeleteBeneficiary.form.fill(DeleteBeneficiary.Data(id = beneficiaryId))))
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
                  views.html.component.wallex.traderDeleteBeneficiary(formWithErrors)
                )
              )
            },
            deleteBeneficiaryData => {

              val beneficiary =
                wallexOrganizationBeneficiaries.Service
                  .getByBeneficiaryId(deleteBeneficiaryData.id)

              val authToken = wallexAuthToken.Service.getToken()

              def deleteBeneficiaryFromWallex(
                  authToken: String,
                  beneficiaryId: String
              ) = {
                wallexDeleteBeneficiary.Service
                  .delete(authToken, beneficiaryId = beneficiaryId)
              }

              def deleteBeneficiary(beneficiaryId: String): Future[Int] =
                wallexOrganizationBeneficiaries.Service
                  .delete(beneficiaryId)

              (for {
                beneficiary <- beneficiary
                authToken <- authToken
                deleteResponse <- deleteBeneficiaryFromWallex(
                  authToken,
                  beneficiary.beneficiaryID
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

        def getWallexAccount(organizationID: String) =
          wallexOrganizationAccountDetails.Service.tryGet(organizationID)

        val authToken = wallexAuthToken.Service.getToken()

        val negotiation =
          negotiations.Service.tryGet(negotiationID)

        def walletBalanceResponse(
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
        ): Future[OrganizationAccountDetail] = {

          val buyerOrganizationID =
            masterTraders.Service
              .tryGetOrganizationID(buyerId)

          for {
            buyerOrganizationID <- buyerOrganizationID
            buyerWallexAccount <- getWallexAccount(buyerOrganizationID)
          } yield buyerWallexAccount

        }

        def getSellerWallexAccountId(
            sellerId: String
        ): Future[OrganizationAccountDetail] = {
          val sellerOrganizationID =
            masterTraders.Service
              .tryGetOrganizationID(sellerId)

          for {
            sellerOrganizationID <- sellerOrganizationID
            sellerWallexAccount <- getWallexAccount(sellerOrganizationID)
          } yield sellerWallexAccount
        }

        def formResult(
            buyerAccountID: String,
            sellerAccountID: String,
            amount: Double,
            balance: Double
        ) = {
          if (balance > amount) {
            Future(
              Ok(
              views.html.component.wallex.traderWalletTransfer(
                companion.wallex.WalletTransfer.form.fill(
                  companion.wallex.WalletTransfer.Data(
                    onBehalfOf = buyerAccountID,
                    receiverAccountID = sellerAccountID,
                    amount = amount,
                    currency = "",
                    purposesOfTransfer = "",
                    reference = negotiationID,
                    remarks = "",
                    negotiationID = negotiationID
                  )
                )
              )
            )
          )

        }else {
            Future(BadRequest(
              views.html.component.wallex.traderWalletTransfer(
                companion.wallex.WalletTransfer.form
                  .withGlobalError(constants.Response.WALLEX_WALLET_LOW_BALANCE.message))))
          }}

        (for {
          negotiation <- negotiation
          authToken <- authToken
          buyerWallexAccountID <-
            getBuyerWallexAccountId(negotiation.buyerTraderID)
          sellerWallexAccountID <-
            getSellerWallexAccountId(negotiation.sellerTraderID)
          balance <- walletBalanceResponse(authToken, buyerWallexAccountID.wallexID)
          result <-
              formResult(
                buyerWallexAccountID.accountID,
                sellerWallexAccountID.accountID,
                negotiation.price.toDouble,
                balance.data.amount
              )

        } yield result).recoverWith {
          case _: BaseException =>
            Future(Ok(views.html.component.wallex.traderWalletTransfer()))
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
                    .traderWalletTransfer(formWithErrors)
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
                wallexWalletTransferRequest.Service.insertOrUpdate(
                  negotiationID = wallexTransfer.negotiationID,
                  organizationID = trader.organizationID,
                  traderID = trader.id,
                  onBehalfOf = wallexTransfer.onBehalfOf,
                  receiverAccountID = wallexTransfer.receiverAccountID,
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

          val organizationID =
            masterTraders.Service.getOrganizationIDByAccountID(
              loginState.username
            )

          def getOrganizationWallexAccountDetail(organizationID: String): Future[OrganizationAccountDetail] =
            wallexOrganizationAccountDetails.Service.tryGet(organizationID)

         def getCompanyAccountDetails(accountID: String): Future[AccountCompanyDetail] =
           wallexAccountCompanyDetails.Service.tryGet(accountID)

         (for {
           organizationID <- organizationID
           organizationWallexAccountDetail <-
             getOrganizationWallexAccountDetail(organizationID)
           companyAccountDetails <-
             getCompanyAccountDetails(organizationWallexAccountDetail.accountID)
           result <- withUsernameToken.Ok(
             views.html.component.wallex.traderUpdateCompanyAccountDetails(
               UpdateCompanyAccount.form
                 .fill(
                   UpdateCompanyAccount
                     .Data(
                       countryOfIncorporation = companyAccountDetails.countryOfIncorporation,
                       countryOfOperations = companyAccountDetails.countryOfOperations,
                       businessType = companyAccountDetails.businessType,
                       companyAddress = companyAccountDetails.companyAddress,
                       postalCode = companyAccountDetails.postalCode,
                       state = companyAccountDetails.state,
                       city = companyAccountDetails.city,
                       registrationNumber = companyAccountDetails.registrationNumber,
                       incorporationDate = new SimpleDateFormat(constants.External.Wallex.DATE_FORMAT)
                                          .parse(companyAccountDetails.incorporationDate)
                     )
                 )
             )
           )
         } yield result).recoverWith {
           case _: BaseException =>
             Future(Ok(views.html.component.wallex.traderUpdateCompanyAccountDetails()))
         }
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
                    .traderUpdateCompanyAccountDetails(formWithErrors)
                )
              )
            },
            updateCompany => {

              val organizationID =
                masterTraders.Service.getOrganizationIDByAccountID(
                  loginState.username
                )

              def getOrganizationWallexAccount(
                  organizationID: String
              ): Future[OrganizationAccountDetail] =
                wallexOrganizationAccountDetails.Service.tryGet(organizationID)

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

              def insertOrUpdate(updateCompanyDetailsResponse: UpdateCompanyDetailsResponse) =
              {
                wallexAccountCompanyDetails.Service.insertOrUpdate(
                  accountID = updateCompanyDetailsResponse.accountId,
                  companyName = updateCompanyDetailsResponse.companyName,
                  countryOfIncorporation = updateCompanyDetailsResponse.countryOfIncorporation,
                  countryOfOperations = updateCompanyDetailsResponse.countryOfOperations,
                  businessType = updateCompanyDetailsResponse.businessType,
                  companyAddress = updateCompanyDetailsResponse.companyAddress,
                  postalCode = updateCompanyDetailsResponse.postalCode,
                  state = updateCompanyDetailsResponse.state,
                  city = updateCompanyDetailsResponse.city,
                  registrationNumber = updateCompanyDetailsResponse.registrationNumber,
                  incorporationDate = updateCompanyDetailsResponse.incorporationDate

                )
              }

              (for {
                organizationID <- organizationID
                wallexAccount <-
                  getOrganizationWallexAccount(organizationID)
                authToken <- authToken
                companyResponse <- companyDetailsUpdate(authToken, wallexAccount.wallexID)
                _ <- insertOrUpdate(companyResponse)
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

  def createCollectionAccountsForm(accountId: String): Action[AnyContent] =
     withoutLoginAction { implicit request =>

          Ok(views.html.component.wallex.traderCreateCollectionAccount(
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
                    .traderCreateCollectionAccount(formWithErrors)
                )
              )
            },
            collectionAccount => {
              val organizationID = masterTraders.Service
                .getOrganizationIDByAccountID(loginState.username)

              def getWallexAccount(organizationID: String) =
                wallexOrganizationAccountDetails.Service.tryGet(organizationID)

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
                  wallexID: String,
                  accountID: String
              ) = {
                wallexCollectionAccounts.Service.create(
                  id = collectionResponse.id,
                  wallexID = wallexID,
                  accountID = accountID
                )
              }
              (for {
                organizationID <- organizationID
                authToken <- authToken
                wallexAccount <- getWallexAccount(organizationID)
                collectionResponse <- createCollectionAccount(authToken)
                _ <- insertOrUpdate(
                  collectionResponse,
                  wallexAccount.wallexID,
                  wallexAccount.accountID
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

  def getCollectionAccountsForm(accountID: String): Action[AnyContent] =
    withoutLoginAction {
      implicit request =>
       Ok(views.html.component.wallex.traderGetCollectionAccount(
          GetCollectionAccount.form.fill(GetCollectionAccount
              .Data(accountID = accountID))))
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
                    .traderGetCollectionAccount(
                      formWithErrors
                    )
                )
              )
            },
            collectionAccount => {

              val authToken = wallexAuthToken.Service.getToken()
              def getCollectionsAccounts = wallexCollectionAccounts.Service.tryGetByAccountID(collectionAccount.accountID)

              def getCollectionAccount(authToken: String, accountID: String, collectionAccountID: String)
                  : Future[CreateCollectionResponse] =
                wallexGetCollectionAccount.Service.get(
                  authToken,
                  accountID,
                  collectionAccountID
                )

              (for {
                authToken <- authToken
                collectionsAccount <- getCollectionsAccounts
                collectionResponse <-
                  getCollectionAccount(authToken, collectionAccount.accountID,collectionsAccount.id)
                result <- withUsernameToken.PartialContent(
                  views.html.component.wallex
                    .traderGetCollectionAccountResponse(
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

        def getOrganizationWallexAccount(
            organizationID: String
        ): Future[OrganizationAccountDetail] =
          wallexOrganizationAccountDetails.Service.tryGet(organizationID)

        (for {
          organizationID <- organizationID
          organizationWallexAccount <-
            getOrganizationWallexAccount(organizationID)
          result <- Future(Ok(
            views.html.component.wallex.userGetAccount(
              GetUserAccount.form
                .fill(GetUserAccount
                    .Data(userID = organizationWallexAccount.wallexID)))))
        } yield result).recoverWith {
          case _: BaseException =>
            Future(Ok(views.html.component.wallex.userGetAccount()))
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
                    .userGetAccount(formWithErrors)
                )
              )
            },
            organizationAccountData => {
              val organizationID =
                masterTraders.Service
                  .getOrganizationIDByAccountID(loginState.username)

              def getWallexAccount(organizationID: String) =
                wallexOrganizationAccountDetails.Service.tryGet(organizationID)

              val authToken = wallexAuthToken.Service.getToken()

              def wallexGetUser(wallexId: String, authToken: String) =
                wallexGetUserRequest.Service.get(wallexId, authToken)

              def updateStatus(
                  wallexGetUserResponse: GetUserResponse
              ): Future[Int] =
                wallexOrganizationAccountDetails.Service.updateStatus(
                  wallexID = wallexGetUserResponse.id,
                  status = wallexGetUserResponse.status
                )

              (for {
                organizationID <- organizationID
                authToken <- authToken
                wallexAccount <- getWallexAccount(organizationID)
                wallexGetUserResponse <-
                  wallexGetUser(wallexAccount.wallexID, authToken)
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

          val organizationID =
            masterTraders.Service.getOrganizationIDByAccountID(
              loginState.username
            )

          def getOrganizationWallexAccountDetail(organizationID: String): Future[OrganizationAccountDetail] =
            wallexOrganizationAccountDetails.Service.tryGet(organizationID)

          def getAccountProfileDetails(wallexID: String): Future[AccountProfileDetail] =
            wallexAccountProfileDetails.Service.tryGetByWallexID(wallexID)

          (for {
            organizationID <- organizationID
            organizationWallexAccountDetail <-
              getOrganizationWallexAccountDetail(organizationID)
            accountProfileDetails <-
              getAccountProfileDetails(organizationWallexAccountDetail.wallexID)
            result <- withUsernameToken.Ok(
              views.html.component.wallex.traderUpdateUserAccountDetails(
                UpdateUserAccount.form
                  .fill(
                    UpdateUserAccount
                      .Data(
                        mobileCountryCode = accountProfileDetails.mobileCountryCode,
                        mobileNumber = accountProfileDetails.mobileNumber,
                        gender = accountProfileDetails.gender,
                        nationality = accountProfileDetails.nationality,
                        countryOfResidence = accountProfileDetails.residentialAddressDetails.countryOfResidence,
                        residentialAddress = accountProfileDetails.residentialAddressDetails.residentialAddress,
                        countryCode = accountProfileDetails.residentialAddressDetails.countryCode,
                        postalCode = accountProfileDetails.residentialAddressDetails.postalCode,
                        countryOfBirth = accountProfileDetails.countryOfBirth,
                        dateOfBirth =  new SimpleDateFormat(constants.External.Wallex.DATE_FORMAT)
                                       .parse(accountProfileDetails.dateOfBirth),
                        identificationType = accountProfileDetails.identificationType,
                        identificationNumber = accountProfileDetails.identificationNumber,
                        issueDate = new SimpleDateFormat(constants.External.Wallex.DATE_FORMAT)
                          .parse(accountProfileDetails.issueDate),
                        expiryDate = new SimpleDateFormat(constants.External.Wallex.DATE_FORMAT)
                          .parse(accountProfileDetails.expiryDate),
                        employmentIndustry = accountProfileDetails.employmentDetails.employmentIndustry,
                        employmentStatus = accountProfileDetails.employmentDetails.employmentStatus,
                        employmentPosition = accountProfileDetails.employmentDetails.employmentPosition
                      )
                  )
              )
            )
          } yield result).recoverWith {
            case _: BaseException =>
              Future(Ok(views.html.component.wallex.traderUpdateUserAccountDetails()))
          }

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
                    .traderUpdateUserAccountDetails(formWithErrors)
                )
              )
            },
            userUpdateAccount => {

              val organizationID =
                masterTraders.Service.getOrganizationIDByAccountID(
                  loginState.username
                )

              def insertOrUpdate(updateUserResponse: UpdateUserDetailsResponse,
                                  wallexID: String) =
              {
                wallexAccountProfileDetails.Service.insertOrUpdate(
                  wallexID = wallexID,
                  firstName = updateUserResponse.firstName,
                  lastName = updateUserResponse.lastName,
                  mobileCountryCode = updateUserResponse.mobileCountryCode,
                  mobileNumber = updateUserResponse.mobileNumber,
                  gender = updateUserResponse.gender,
                  nationality = updateUserResponse.nationality,
                  residentialAddressDetails = ResidentialAddressDetails(
                    countryOfResidence = updateUserResponse.countryOfResidence,
                    residentialAddress = updateUserResponse.countryOfResidence,
                    countryCode = updateUserResponse.countryCode,
                    postalCode = updateUserResponse.postalCode),
                  countryOfBirth = updateUserResponse.countryOfBirth,
                  dateOfBirth = updateUserResponse.dateOfBirth,
                  identificationType = updateUserResponse.identificationType,
                  identificationNumber = updateUserResponse.identificationNumber,
                  issueDate = updateUserResponse.issueDate,
                  expiryDate = updateUserResponse.expiryDate,
                  employmentDetails = EmploymentDetails(
                    employmentIndustry = updateUserResponse.employmentIndustry,
                    employmentStatus = updateUserResponse.employmentStatus,
                    employmentPosition = updateUserResponse.employmentPosition)

                )
              }
              def getOrganizationWallexAccount(
                  organizationID: String
              ): Future[OrganizationAccountDetail] =
                wallexOrganizationAccountDetails.Service.tryGet(organizationID)

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
                    },
                    employmentIndustry =userUpdateAccount.employmentIndustry,
                    employmentStatus = userUpdateAccount.employmentStatus,
                    employmentPosition = userUpdateAccount.employmentPosition
                  ),
                  userId = wallexID
                )
              }

              (for {
                organizationID <- organizationID
                wallexAccount <-
                  getOrganizationWallexAccount(organizationID)
                authToken <- authToken
                updateUserResponse <- wallexUserUpdate(authToken, wallexAccount.wallexID)
                _ <- insertOrUpdate(updateUserResponse, wallexAccount.wallexID)
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

  def zoneWalletTransferForm(negotiationID: String): Action[AnyContent] =
    withZoneLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
          def transferRequests: Future[WalletTransferRequest] =
            wallexWalletTransferRequest.Service.tryGet(
              negotiationID = negotiationID
            )

          (for {
            transferRequest <- transferRequests
            result <- Future(Ok(
              views.html.component.wallex.zoneWalletTransfer(
                WalletTransfer.form.fill(
                  companion.wallex.WalletTransfer.Data(
                    onBehalfOf = transferRequest.onBehalfOf,
                    receiverAccountID = transferRequest.receiverAccountID,
                    amount = transferRequest.amount,
                    currency = transferRequest.currency,
                    purposesOfTransfer = transferRequest.purposeOfTransfer,
                    reference = transferRequest.negotiationID,
                    remarks = transferRequest.remarks,
                    negotiationID = transferRequest.negotiationID
                  )
                )
              )
            ))

          } yield result).recoverWith {
            case _: BaseException =>
              Future(Ok(views.html.component.wallex.zoneWalletTransfer()))
          }
    }

  def zoneCreateWalletTransfer(): Action[AnyContent] =
    withZoneLoginAction.authenticated {
      implicit loginState =>
        implicit request =>
          companion.wallex.WalletTransfer.form
            .bindFromRequest()
            .fold(
              formWithErrors => {
                Future(
                  BadRequest(
                    views.html.component.wallex
                      .zoneWalletTransfer(formWithErrors)
                  )
                )
              },
              wallexTransfer => {

                val negotiation =
                  negotiations.Service.tryGet(wallexTransfer.negotiationID)
                val negotiationFile = negotiationFiles.Service
                  .tryGet(
                    wallexTransfer.negotiationID,
                    constants.File.Negotiation.INVOICE
                  )

                def getWallexAccount(accountId: String) =
                  wallexOrganizationAccountDetails.Service.tryGetByAccountId(accountId)
                val zoneID =
                  masterZones.Service
                    .tryGetID(loginState.username)

                def getTraderID(traderID: String) =
                  masterTraders.Service.tryGetAccountId(traderID)

                val authToken = wallexAuthToken.Service.getToken()

                def uploadFileURLResponse(
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
                  val fileArray = utilities.FileOperations.convertToByteArray(utilities.FileOperations
                    .fetchFile(fileResourceManager.getWallexFilePath(constants.File.Negotiation.INVOICE), negotiationFile.fileName))
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
                      receiverAccountId = wallexTransfer.receiverAccountID,
                      amount = wallexTransfer.amount,
                      currency = wallexTransfer.currency,
                      purposesOfTransfer = wallexTransfer.purposesOfTransfer,
                      reference = wallexTransfer.reference,
                      remarks = wallexTransfer.remarks,
                      supportingDocuments = Seq(fileId)
                    )
                  )

                def insert(
                            wallexTransferResponse: WalletToWalletTransferResponse,
                            organizationID: String,
                            wallexID: String
                          ) = {
                  wallexWalletTransfers.Service.insertOrUpdate(
                    id = wallexTransferResponse.id,
                    organizationID = organizationID,
                    wallexID = wallexID,
                    senderAccountID = wallexTransferResponse.senderAccountId,
                    receiverAccountID = wallexTransferResponse.receiverAccountId,
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
                                  wallexTransferResponse: WalletToWalletTransferResponse
                                ) = {
                  val status =
                    if (
                      wallexTransferResponse.status == constants.Status.SendWalletTransfer.COMPLETED
                    ) {
                      constants.Status.SendWalletTransfer.SENT
                    } else { constants.Status.SendWalletTransfer.ZONE_APPROVED }

                  wallexWalletTransferRequest.Service
                    .updateZoneApprovalStatus(
                      negotiationID = wallexTransfer.negotiationID,
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
                                            wallexTransferResponse: WalletToWalletTransferResponse
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
                  wallexAccount <- getWallexAccount(wallexTransfer.onBehalfOf)
                  fileUrlResponse <-
                    uploadFileURLResponse(authToken, negotiationFile)
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
                    wallexAccount.organizationID,
                    wallexAccount.wallexID
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
                }
              }
            )

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



  def sendForScreeningForm(wallexID: String): Action[AnyContent] =
    withoutLoginAction {
        implicit request =>
          Ok(views.html.component.master.sendUserDetailsForScreening(
                views.companion.master.SendUserDetailsForScreening.form
                  .fill(views.companion.master.SendUserDetailsForScreening
                      .Data(userID = wallexID))))
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
              screeningUserData => {

                val authToken = wallexAuthToken.Service.getToken()

                def sendForScreening(wallexId: String, authToken: String) =
                  wallexUserScreening.Service
                    .post(
                      wallexId,
                      authToken,
                      wallexUserScreening.Request(userId = wallexId)
                    )

                def updateStatus(wallexId: String,
                                  screeningResponse: ScreeningResponse
                                ): Future[Int] =
                  wallexOrganizationAccountDetails.Service.updateStatus(
                    wallexID = wallexId,
                    status = screeningResponse.status
                  )

                (for {
                  authToken <- authToken
                  screeningResponse <-
                    sendForScreening(screeningUserData.userID, authToken)
                  _ <- updateStatus(screeningUserData.userID,screeningResponse)
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

  def fundingNotification() = withoutLoginActionAsync {
    implicit request =>

      implicit val requestReads = wallexFundSimplePayment.requestReads

      val fundNotificationRequest = request.body.asJson.map { requestBody =>
        convertJsonStringToObject[wallexFundSimplePayment.Request](requestBody.toString())
      }.getOrElse(throw new BaseException(constants.Response.FAILURE))

      val authToken = wallexAuthToken.Service.getToken()

      def getFundingStatus(authToken: String,
                           fundingID: String
                                ) =
        wallexGetFundingStatus.Service.get(
          authToken,
          fundingID
        )

      def getWallexAccount(accountID: String) =
        wallexOrganizationAccountDetails.Service.tryGetByAccountId(accountID)

      def traderAccountID(traderID: String) = masterTraders.Service.tryGetAccountId(traderID)

      def traderAddress(traderAccountID: String) =
        blockchainAccounts.Service.tryGetAddress(traderAccountID)

      def getZoneID(traderID: String) = masterTraders.Service.tryGetZoneID(traderID)

      def zoneAccountID(zoneID: String) = masterZones.Service.tryGetAccountID(zoneID)

      def zoneAddress(zoneAccountID: String) =
        blockchainAccounts.Service.tryGetAddress(zoneAccountID)

      def insertOrUpdate(fundingResponse: GetFundingResponse) = wallexFundingStatusDetails.Service.insertOrUpdate(
        id = fundingResponse.id,balanceID = fundingResponse.balanceId, accountID = fundingResponse.accountId,
        amount = fundingResponse.amount,reference = fundingResponse.reference, status = fundingResponse.status)

      def zoneAutomatedIssueFiat(
                                  traderAddress: String,
                                  zoneID: String,
                                  zoneAddress: String,
                                  fundingResponse: GetFundingResponse
                                ) =
          issueFiat(
            traderAddress = traderAddress,
            zoneID = zoneID,
            zoneWalletAddress = zoneAddress,
            wallexTransferReferenceID = fundingResponse.id,
            transactionAmount =
              new MicroNumber(fundingResponse.amount)
          )

      (for {
        authToken <- authToken
        fundingStatusResponse <- getFundingStatus(authToken,fundNotificationRequest.fundingId)
        wallexAccount <- getWallexAccount(fundingStatusResponse.accountId)
        traderAccountID <- traderAccountID(wallexAccount.traderID)
        zoneID <- getZoneID(wallexAccount.traderID)
        zoneAccountID <- zoneAccountID(zoneID)
        zoneAddress <- zoneAddress(zoneAccountID)
        traderAddress <- traderAddress(traderAccountID)
        _ <- insertOrUpdate(fundingStatusResponse)
        _ <- if (fundNotificationRequest.resource.equals(constants.External.Wallex.FundNotification.FUNDING)
                  && fundingStatusResponse.status.equals(constants.External.Wallex.FundNotification.COMPLETED))
                zoneAutomatedIssueFiat(traderAddress, zoneID, zoneAddress, fundingStatusResponse) else Future(None)
      } yield Ok).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

}