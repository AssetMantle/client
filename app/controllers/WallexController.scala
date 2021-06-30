package controllers

import controllers.actions._
import controllers.results.WithUsernameToken
import exceptions.BaseException
import models.common.Serializable.{BankAccount, BeneficiaryPayment, Company, ConversionDetails, UserProfile}
import models.master.{Negotiations, Trader}
import models.masterTransaction.{NegotiationFile, NegotiationFiles}
import models.wallex.{SimplePayments, _}
import models.{blockchain, blockchainTransaction, master, masterTransaction}
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import transactions.responses.WallexResponse.{CreateCollectionResponse, CreateDocumentResponse, CreatePaymentQuoteResponse, GetBalanceResponse, GetFundingResponse, GetUserResponse, PaymentFileUploadResponse, ScreeningResponse, UserUpdateAccountResponse, UserUpdateCompanyResponse, WalletToWalletTransferResponse}
import transactions.wallex._
import utilities.JSON.convertJsonStringToObject
import utilities.{KeyStore, MicroNumber}
import views.companion
import views.companion.wallex.{WalletTransfer, _}

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
                                   masterTraders: master.Traders,
                                   masterTransactionRedeemFiatRequests: masterTransaction.RedeemFiatRequests,
                                   messagesControllerComponents: MessagesControllerComponents,
                                   negotiations: Negotiations,
                                   negotiationFiles: NegotiationFiles,
                                   transactionsIssueFiat: transactions.IssueFiat,
                                   transactionsRedeemFiat: transactions.RedeemFiat,
                                   transaction: utilities.Transaction,
                                   transactionsWallexUserDetailsUpdate: UserUpdateAccount,
                                   transactionsWallexUpdateCompanyDetails: UserUpdateCompany,
                                   transactionsWallexWalletTransfer: CreateWalletTransfer,
                                   transactionsWallexCreateBeneficiary: CreateBeneficiaryAccount,
                                   transactionsWallexDeleteBeneficiary: DeleteBeneficiary,
                                   transactionsWallexCreateSimplePayment: CreateSimplePayment,
                                   transactionsWallexSimplePayments: SimplePayments,
                                   transactionsWallexUploadPaymentFile: UploadPaymentFile,
                                   transactionsWallexCreateCollectionAccount: CreateCollectionAccount,
                                   transactionsWallexGetCollectionAccount: GetCollectionAccount,
                                   transactionsWallexGetWalletBalance: GetWalletBalance,
                                   transactionsWallexUserSignUpRequest: UserSignUp,
                                   transactionsWallexGetUserRequest: GetUser,
                                   transactionsWallexCreateDocument: CreateDocument,
                                   transactionsWallexAuthToken: GenerateAuthToken,
                                   transactionsWallexCreatePaymentQuote: CreatePaymentQuote,
                                   transactionsWallexUserScreening: UserSubmitForScreening,
                                   transactionsWallexFundSimplePayment: FundSimplePayment,
                                   transactionsWallexGetFundingStatus: GetFundingStatus,
                                   wallexPaymentFiles: PaymentFiles,
                                   wallexOrganizationAccounts: OrganizationAccounts,
                                   wallexBeneficiaries: Beneficiaries,
                                   wallexCollectionAccounts: CollectionAccounts,
                                   wallexWalletTransfers: WalletTransfers,
                                   wallexWalletTransferRequest: WalletTransferRequests,
                                   wallexUserKYCs: UserKYCs,
                                   wallexFundingStatusDetails : FundingStatusDetails,
                                   withoutLoginActionAsync: WithoutLoginActionAsync,
                                   withUsernameToken: WithUsernameToken,
                                   withTraderLoginAction: WithTraderLoginAction,
                                   withoutLoginAction: WithoutLoginAction,
                                   withZoneLoginAction: WithZoneLoginAction,

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

          def getOrganizationWallexAccount(
                                                  organizationID: String
                                                ): Future[OrganizationAccount] =
            wallexOrganizationAccounts.Service.tryGet(organizationID)

          (for {
            organizationID <- organizationID
            organizationWallexAccountDetail <-
              getOrganizationWallexAccount(organizationID)
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

                val authToken = transactionsWallexAuthToken.Service.getToken()

                def userSignUpWallex(authToken: String) = {
                  transactionsWallexUserSignUpRequest.Service.post(
                    authToken,
                    transactionsWallexUserSignUpRequest.Request(
                      firstName = organizationWallexAccountData.firstName,
                      lastName = organizationWallexAccountData.lastName,
                      email = organizationWallexAccountData.email,
                      countryCode = organizationWallexAccountData.countryCode,
                      accountType = organizationWallexAccountData.accountType
                    )
                  )
                }

                def userGetWallexAccount(wallexID: String, authToken: String) =
                  transactionsWallexGetUserRequest.Service.get(wallexID, authToken)

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
                  wallexOrganizationAccounts.Service.insertOrUpdate(
                    organizationID = organizationID,
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    countryCode = countryCode,
                    accountType = accountType,
                    wallexID = wallexID,
                    accountID = accountID,
                    status = status,
                    traderID = traderID,
                    company = Option(Company(None,None,None,None,None,None,None,None,None,None)),
                    userProfile = Option(UserProfile(None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None,None))
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
                  result <- withUsernameToken.PartialContent(views.html.component.wallex.traderUpdateUserAccountDetails())
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
                  wallexOrganizationAccounts.Service.tryGet(organizationID)

                def getWallexDocument(
                                       documentType: String
                                     ): Future[models.wallex.UserKYC] = {
                  wallexUserKYCs.Service.tryGet(
                    loginState.username,
                    documentType
                  )
                }

                val authToken = transactionsWallexAuthToken.Service.getToken()

                def createDocument(
                                    authToken: String,
                                    wallexID: String,
                                    fileName: String,
                                    fileType: String
                                  ) =
                  transactionsWallexCreateDocument.Service.post(
                    authToken,
                    wallexID,
                    transactionsWallexCreateDocument.Request(
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

                  transactionsWallexCreateDocument.Service.put(authToken, uploadUrl, fileArray)
                }

                def insertDocumentDetails(
                                           traderID: String,
                                           documentResponse: CreateDocumentResponse
                                         ) = {
                  wallexUserKYCs.Service.updateUrlAndFileID(
                    id = traderID,
                    fileID = documentResponse.id,
                  )
                }

                def updateStatus(
                                  wallexID: String
                                ): Future[Int] =
                  wallexOrganizationAccounts.Service.updateStatus(
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
                    loginState.username,
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
                wallexOrganizationAccounts.Service.tryGet(organizationID)

              val authToken = transactionsWallexAuthToken.Service.getToken()

              def createPaymentQuote(authToken: String) =
                transactionsWallexCreatePaymentQuote.Service.post(
                  authToken,
                  transactionsWallexCreatePaymentQuote.Request(
                    sellCurrency = paymentQuoteData.sellCurrency,
                    buyCurrency = paymentQuoteData.buyCurrency,
                    amount = paymentQuoteData.amount.toDouble,
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
                transactionsWallexUploadPaymentFile.Service
                  .post(
                    authToken,
                    transactionsWallexUploadPaymentFile
                      .Request(fileName = negotiationFile.fileName)
                  )

              def uploadFileToWallex(
                  authToken: String,
                  uploadURL: String,
                  negotiationFile: NegotiationFile
              ) = {
                val fileArray = utilities.FileOperations.convertToByteArray(utilities.FileOperations
                    .fetchFile(fileResourceManager.getNegotiationFilePath(constants.File.Negotiation.INVOICE), negotiationFile.fileName))

                transactionsWallexUploadPaymentFile.Service.put(authToken, uploadURL, fileArray)
              }

              def insert(
                    organizationID: String,
                    wallexID: String,
                    fileID: String,
                    fileType: String,
                    quoteID: String
                ) =
                  wallexPaymentFiles.Service.insertOrUpdate(
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
                _ <- uploadFileToWallex(
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
                wallexOrganizationAccounts.Service.tryGet(organizationID)

              val file =
                wallexPaymentFiles.Service.tryGet(createPaymentData.quoteID)
              val authToken = transactionsWallexAuthToken.Service.getToken()

              def createSimplePayment(
                  authToken: String,
                  file: PaymentFile
              ) =
                transactionsWallexCreateSimplePayment.Service.post(
                  authToken,
                  transactionsWallexCreateSimplePayment.Request(
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
                transactionsWallexSimplePayments.Service.create(
                  simplePaymentID = simplePaymentID,
                  wallexID = wallexID,
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
                      entity = blockchainTransaction.RedeemFiat(from = loginState.address, to = toAddress, redeemAmount = MicroNumber(redeemAmount),
                        gas = createPaymentData.gas, ticketID = "", mode = transactionMode),
                      blockchainTransactionCreate = blockchainTransactionRedeemFiats.Service.create,
                      request = transactionsRedeemFiat.Request(transactionsRedeemFiat.BaseReq(from = loginState.address, gas = createPaymentData.gas), to = toAddress,
                        password = createPaymentData.password, redeemAmount = MicroNumber(redeemAmount), mode = transactionMode),
                      action = transactionsRedeemFiat.Service.post,
                      onSuccess = blockchainTransactionRedeemFiats.Utility.onSuccess,
                      onFailure = blockchainTransactionRedeemFiats.Utility.onFailure,
                      updateTransactionHash = blockchainTransactionRedeemFiats.Service.updateTransactionHash
                    )
                    for {
                      ticketID <- ticketID
                      _ <- createRedeemFiatRequests(trader.id, ticketID, MicroNumber(redeemAmount))
                      result <- withUsernameToken.Ok(views.html.trades(successes = Seq(constants.Response.FIAT_REDEEMED)))
                    } yield result
                  } else throw new BaseException(constants.Response.UNAUTHORIZED)
                } else Future(BadRequest(views.html.component.wallex.traderAcceptPaymentQuoteRequest(views.companion.wallex.
                  AcceptPaymentQuote.form.fill(createPaymentData).withGlobalError(constants.Response.INCORRECT_PASSWORD.message),
                  quoteID = createPaymentData.quoteID)))
              }

              def createRedeemFiatRequests(traderID: String, ticketID: String, redeemAmount: MicroNumber): Future[String] =
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
              ): Future[OrganizationAccount] =
                wallexOrganizationAccounts.Service.tryGet(organizationID)

              val authToken = transactionsWallexAuthToken.Service.getToken()

              def createBeneficiary(authToken: String) = {
                transactionsWallexCreateBeneficiary.Service.post(
                  authToken,
                  transactionsWallexCreateBeneficiary.Request(
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
                wallexBeneficiaries.Service.create(
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
                wallexBeneficiaries.Service
                  .getByBeneficiaryId(deleteBeneficiaryData.id)

              val authToken = transactionsWallexAuthToken.Service.getToken()

              def deleteBeneficiaryFromWallex(
                  authToken: String,
                  beneficiaryId: String
              ) = {
                transactionsWallexDeleteBeneficiary.Service
                  .delete(authToken, beneficiaryId = beneficiaryId)
              }

              def deleteBeneficiary(beneficiaryId: String): Future[Int] =
                wallexBeneficiaries.Service
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
          wallexOrganizationAccounts.Service.tryGet(organizationID)

        val authToken = transactionsWallexAuthToken.Service.getToken()

        val negotiation =
          negotiations.Service.tryGet(negotiationID)

        def walletBalanceResponse(
            authToken: String,
            userId: String
        ): Future[GetBalanceResponse] =
          transactionsWallexGetWalletBalance.Service
            .post(
              authToken,
              userId
            )

        def getBuyerWallexAccountId(
            buyerId: String
        ): Future[OrganizationAccount] = {

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
        ): Future[OrganizationAccount] = {
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
                  amount = wallexTransfer.amount.toString,
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

          def getOrganizationWallexAccount(organizationID: String): Future[OrganizationAccount] =
            wallexOrganizationAccounts.Service.tryGet(organizationID)

         def getResult(organizationWallexAccount : OrganizationAccount): Future[Result] = {

           if(organizationWallexAccount.company.get.incorporationDate != None){
            val company =  organizationWallexAccount.company.get
           Future(Ok(
             views.html.component.wallex.traderUpdateCompanyAccountDetails(
               UpdateCompanyAccount.form
                 .fill(
                   UpdateCompanyAccount
                     .Data(
                       countryOfIncorporation = company.countryOfIncorporation.get,
                       countryOfOperations = company.countryOfOperations.get,
                       businessType = company.businessType.get,
                       companyAddress = company.companyAddress.get,
                       postalCode = company.postalCode.get,
                       state = company.state.get,
                       city = company.city.get,
                       registrationNumber = company.registrationNumber.get,
                       incorporationDate = utilities.Date.parseStringToDate(company.incorporationDate.get)
                     )
                 )
             )
           ))
          } else {Future(Ok(views.html.component.wallex.traderUpdateCompanyAccountDetails()))}
       }

         (for {
           organizationID <- organizationID
           organizationWallexAccount <-
             getOrganizationWallexAccount(organizationID)
         result <- getResult(organizationWallexAccount)

         } yield result
         ).recover {
           case _: BaseException =>
             Ok(views.html.component.wallex.traderUpdateCompanyAccountDetails())
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
              ): Future[OrganizationAccount] =
                wallexOrganizationAccounts.Service.tryGet(organizationID)

              val authToken = transactionsWallexAuthToken.Service.getToken()

              def updateCompanyDetail(authToken: String, wallexID: String) = {
                transactionsWallexUpdateCompanyDetails.Service.post(
                  authToken,
                  transactionsWallexUpdateCompanyDetails.Request(
                    countryOfIncorporation =
                      updateCompany.countryOfIncorporation,
                    countryOfOperations = updateCompany.countryOfOperations,
                    businessType = updateCompany.businessType,
                    companyAddress = updateCompany.companyAddress,
                    postalCode = updateCompany.postalCode,
                    state = updateCompany.state,
                    city = updateCompany.city,
                    registrationNumber = updateCompany.registrationNumber,
                    incorporationDate = utilities.Date.formatDate(updateCompany.incorporationDate)
                  ),
                  userId = wallexID
                )
              }

              def insertOrUpdate(wallexAccount: OrganizationAccount,userUpdateCompanyResponse: UserUpdateCompanyResponse) =
              {
                wallexOrganizationAccounts.Service.insertOrUpdate(
                  wallexID = wallexAccount.wallexID,
                  organizationID = wallexAccount.organizationID,
                  accountID = wallexAccount.accountID,
                  email = wallexAccount.email,
                  firstName = wallexAccount.firstName,
                  lastName = wallexAccount.lastName,
                  status = wallexAccount.status,
                  countryCode = wallexAccount.countryCode,
                  accountType = wallexAccount.accountType,
                  traderID = wallexAccount.traderID,
                  company = Some(Company(name = Some(userUpdateCompanyResponse.companyName),
                    countryOfIncorporation = Some(userUpdateCompanyResponse.countryOfIncorporation),
                    countryOfOperations = Some(userUpdateCompanyResponse.countryOfOperations),
                    businessType = Some(userUpdateCompanyResponse.businessType),
                    companyAddress = Some(userUpdateCompanyResponse.companyAddress),
                    postalCode = Some(userUpdateCompanyResponse.postalCode),
                    state = Some(userUpdateCompanyResponse.state),
                    city = Some(userUpdateCompanyResponse.city),
                    registrationNumber = Some(userUpdateCompanyResponse.registrationNumber),
                    incorporationDate = Some(userUpdateCompanyResponse.incorporationDate))),
                  userProfile = wallexAccount.userProfile

                )
              }

              (for {
                organizationID <- organizationID
                wallexAccount <-
                  getOrganizationWallexAccount(organizationID)
                authToken <- authToken
                updateCompanyDetail <- updateCompanyDetail(authToken, wallexAccount.wallexID)
                _ <- insertOrUpdate(wallexAccount, updateCompanyDetail)
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
                wallexOrganizationAccounts.Service.tryGet(organizationID)

              val authToken = transactionsWallexAuthToken.Service.getToken()

              def createCollectionAccount(authToken: String) =
                transactionsWallexCreateCollectionAccount.Service.post(
                  authToken,
                  transactionsWallexCreateCollectionAccount.Request(
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
                  accountID: String
              ) = {
                wallexCollectionAccounts.Service.create(
                  id = collectionResponse.id,
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

              val authToken = transactionsWallexAuthToken.Service.getToken()
              def getCollectionsAccounts = wallexCollectionAccounts.Service.tryGetByAccountID(collectionAccount.accountID)

              def getCollectionAccount(authToken: String, accountID: String, collectionAccountID: String)
                  : Future[CreateCollectionResponse] =
                transactionsWallexGetCollectionAccount.Service.get(
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
        ): Future[OrganizationAccount] =
          wallexOrganizationAccounts.Service.tryGet(organizationID)

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
                wallexOrganizationAccounts.Service.tryGet(organizationID)

              val authToken = transactionsWallexAuthToken.Service.getToken()

              def wallexGetUser(wallexId: String, authToken: String) =
                transactionsWallexGetUserRequest.Service.get(wallexId, authToken)

              def updateStatus(
                  wallexGetUserResponse: GetUserResponse
              ): Future[Int] =
                wallexOrganizationAccounts.Service.updateStatus(
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

  def updateUserAccountForm(): Action[AnyContent] =
    withTraderLoginAction.authenticated {
      implicit loginState =>
        implicit request =>

          val organizationID =
            masterTraders.Service.getOrganizationIDByAccountID(
              loginState.username
            )

          def getOrganizationWallexAccount(organizationID: String): Future[OrganizationAccount] =
            wallexOrganizationAccounts.Service.tryGet(organizationID)

          def getResult(organizationWallexAccount : OrganizationAccount): Future[Result] = {
            if(organizationWallexAccount.userProfile.get.dateOfBirth != None){
            val userProfile = organizationWallexAccount.userProfile.get
                Future(Ok(
                  views.html.component.wallex.traderUpdateUserAccountDetails(
                    UpdateUserAccount.form
                      .fill(
                        UpdateUserAccount
                          .Data(
                            mobileCountryCode = userProfile.mobileNumber.get.split("-")(0),
                            mobileNumber = userProfile.mobileNumber.get.split("-")(1),
                            gender = userProfile.gender.get,
                            nationality = userProfile.nationality.get,
                            countryOfResidence = userProfile.countryOfResidence.get,
                            residentialAddress = userProfile.residentialAddress.get,
                            countryCode = userProfile.countryCode.get,
                            postalCode = userProfile.postalCode.get,
                            countryOfBirth = userProfile.countryOfBirth.get,
                            identificationType = userProfile.identificationType.get,
                            identificationNumber = userProfile.identificationNumber.get,
                            dateOfBirth = utilities.Date.parseStringToDate(userProfile.dateOfBirth.get),
                            issueDate = utilities.Date.parseStringToDate(userProfile.issueDate.get),
                            expiryDate = utilities.Date.parseStringToDate(userProfile.expiryDate.get),
                            employmentIndustry = userProfile.employmentIndustry.get,
                            employmentStatus = userProfile.employmentStatus.get,
                            employmentPosition = userProfile.employmentPosition.get
                          )
                      )
                  )
                ))
              }
              else Future(Ok(views.html.component.wallex.traderUpdateUserAccountDetails()))
          }

          (for {
            organizationID <- organizationID
            organizationWallexAccount <-
              getOrganizationWallexAccount(organizationID)
          result <- getResult(organizationWallexAccount)
          } yield result
        ).recover{
            case _: BaseException =>
              Ok(views.html.component.wallex.traderUpdateUserAccountDetails())
          }

    }

  def updateUserAccount(): Action[AnyContent] =
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

              def insertOrUpdate(updateUserResponse: UserUpdateAccountResponse,
                                 wallexAccount: OrganizationAccount) =
              {
                wallexOrganizationAccounts.Service.insertOrUpdate(
                  wallexID = wallexAccount.wallexID,
                  organizationID = wallexAccount.organizationID,
                  accountID = wallexAccount.accountID,
                  email = wallexAccount.email,
                  firstName = wallexAccount.firstName,
                  lastName = wallexAccount.lastName,
                  status = wallexAccount.status,
                  countryCode = wallexAccount.countryCode,
                  accountType = wallexAccount.accountType,
                  traderID = wallexAccount.traderID,
                  company = if(wallexAccount.company != null) wallexAccount.company else None,
                  userProfile = Option(UserProfile(
                    firstName = Option(updateUserResponse.firstName),
                    lastName = Option(updateUserResponse.lastName),
                    mobileNumber = Option(Seq(updateUserResponse.mobileCountryCode, updateUserResponse.mobileNumber).mkString("-")),
                    gender = Option(updateUserResponse.gender),
                    nationality = Option(updateUserResponse.nationality),
                    countryOfBirth = Option(updateUserResponse.countryOfBirth),
                    countryOfResidence = Option(updateUserResponse.countryOfResidence),
                    residentialAddress = Option(updateUserResponse.residentialAddress),
                    countryCode = Option(updateUserResponse.countryCode),
                    postalCode = Option(updateUserResponse.postalCode),
                    dateOfBirth = Option(updateUserResponse.dateOfBirth),
                    identificationType = Option(updateUserResponse.identificationType),
                    identificationNumber = Option(updateUserResponse.identificationNumber),
                    issueDate = Option(updateUserResponse.issueDate),
                    expiryDate = Option(updateUserResponse.expiryDate),
                    employmentIndustry = Option(updateUserResponse.employmentIndustry),
                    employmentStatus = Option(updateUserResponse.employmentStatus),
                    employmentPosition = Option(updateUserResponse.employmentPosition)
                  )
                )
                )
              }
              def getOrganizationWallexAccount(
                  organizationID: String
              ): Future[OrganizationAccount] =
                wallexOrganizationAccounts.Service.tryGet(organizationID)

              val authToken = transactionsWallexAuthToken.Service.getToken()

              def wallexUserUpdate(authToken: String, wallexID: String) = {
                transactionsWallexUserDetailsUpdate.Service.post(
                  authToken,
                  transactionsWallexUserDetailsUpdate.Request(
                    mobileCountryCode = userUpdateAccount.mobileCountryCode,
                    mobileNumber = userUpdateAccount.mobileNumber,
                    gender = userUpdateAccount.gender,
                    countryOfBirth = userUpdateAccount.countryOfBirth,
                    nationality = userUpdateAccount.nationality,
                    countryOfResidence = userUpdateAccount.countryOfResidence,
                    residentialAddress = userUpdateAccount.residentialAddress,
                    countryCode = userUpdateAccount.countryCode,
                    postalCode = userUpdateAccount.postalCode,
                    dateOfBirth = utilities.Date.formatDate(userUpdateAccount.dateOfBirth),
                    identificationType = userUpdateAccount.identificationType,
                    identificationNumber = userUpdateAccount.identificationNumber,
                    issueDate = utilities.Date.formatDate(userUpdateAccount.issueDate),
                    expiryDate = utilities.Date.formatDate(userUpdateAccount.expiryDate),
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
                _ <- insertOrUpdate(updateUserResponse, wallexAccount)
                result <- withUsernameToken.PartialContent(views.html.component.wallex.traderUpdateCompanyAccountDetails())
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
                    amount = transferRequest.amount.toDouble,
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
                  wallexOrganizationAccounts.Service.tryGetByAccountId(accountId)
                val zoneID =
                  masterZones.Service
                    .tryGetID(loginState.username)

                def getTraderID(traderID: String) =
                  masterTraders.Service.tryGetAccountId(traderID)

                val authToken = transactionsWallexAuthToken.Service.getToken()

                def uploadFileURLResponse(
                                          authToken: String,
                                          negotiationFile: NegotiationFile
                                        ): Future[PaymentFileUploadResponse] =
                  transactionsWallexUploadPaymentFile.Service
                    .post(
                      authToken,
                      transactionsWallexUploadPaymentFile
                        .Request(fileName = negotiationFile.fileName)
                    )

                def uploadFileToWallex(
                                        authToken: String,
                                        uploadURL: String,
                                        negotiationFile: NegotiationFile
                                      ) = {
                  val fileArray = utilities.FileOperations.convertToByteArray(utilities.FileOperations
                    .fetchFile(fileResourceManager.getWallexFilePath(constants.File.Negotiation.INVOICE), negotiationFile.fileName))
                  transactionsWallexUploadPaymentFile.Service
                    .put(authToken, uploadURL, fileArray)
                }

                def initiateWalletTransfer(
                                            authToken: String,
                                            fileId: String
                                          ) =
                  transactionsWallexWalletTransfer.Service.post(
                    authToken,
                    transactionsWallexWalletTransfer.Request(
                      onBehalfOf = wallexTransfer.onBehalfOf,
                      receiverAccountId = wallexTransfer.receiverAccountID,
                      amount = wallexTransfer.amount.toDouble,
                      currency = wallexTransfer.currency,
                      purposesOfTransfer = wallexTransfer.purposesOfTransfer,
                      reference = wallexTransfer.reference,
                      remarks = wallexTransfer.remarks,
                      supportingDocuments = Seq(fileId)
                    )
                  )

                def insert(
                            wallexTransferResponse: WalletToWalletTransferResponse,
                            wallexID: String
                          ) = {
                  wallexWalletTransfers.Service.insertOrUpdate(
                    id = wallexTransferResponse.id,
                    wallexID = wallexID,
                    senderAccountID = wallexTransferResponse.senderAccountId,
                    receiverAccountID = wallexTransferResponse.receiverAccountId,
                    amount = wallexTransferResponse.amount.toString,
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

                val authToken = transactionsWallexAuthToken.Service.getToken()

                def sendForScreening(wallexID: String, authToken: String) =
                  transactionsWallexUserScreening.Service
                    .post(
                      wallexID,
                      authToken,
                      transactionsWallexUserScreening.Request(userId = wallexID)
                    )

                def updateStatus(wallexID: String,
                                  screeningResponse: ScreeningResponse
                                ): Future[Int] =
                  wallexOrganizationAccounts.Service.updateStatus(
                    wallexID = wallexID,
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

      implicit val requestReads = transactionsWallexFundSimplePayment.requestReads

      val fundNotificationRequest = request.body.asJson.map { requestBody =>
        convertJsonStringToObject[transactionsWallexFundSimplePayment.Request](requestBody.toString())
      }.getOrElse(throw new BaseException(constants.Response.FAILURE))

      val authToken = transactionsWallexAuthToken.Service.getToken()

      def getFundingStatus(authToken: String,
                           fundingID: String
                                ) =
        transactionsWallexGetFundingStatus.Service.get(
          authToken,
          fundingID
        )

      def getWallexAccount(accountID: String) =
        wallexOrganizationAccounts.Service.tryGetByAccountId(accountID)

      def traderAccountID(traderID: String) = masterTraders.Service.tryGetAccountId(traderID)

      def traderAddress(traderAccountID: String) =
        blockchainAccounts.Service.tryGetAddress(traderAccountID)

      def getZoneID(traderID: String) = masterTraders.Service.tryGetZoneID(traderID)

      def zoneAccountID(zoneID: String) = masterZones.Service.tryGetAccountID(zoneID)

      def zoneAddress(zoneAccountID: String) =
        blockchainAccounts.Service.tryGetAddress(zoneAccountID)

      def insertOrUpdate(fundingResponse: GetFundingResponse) = wallexFundingStatusDetails.Service.insertOrUpdate(
        id = fundingResponse.id,balanceID = fundingResponse.balanceId, accountID = fundingResponse.accountId,
        amount = fundingResponse.amount.toString,reference = fundingResponse.reference, status = fundingResponse.status)

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

  def userNotification() = withoutLoginActionAsync {
    implicit request =>

      implicit val requestReads = transactionsWallexGetUserRequest.notificationReads

      val userNotificationRequest = request.body.asJson.map { requestBody =>
        convertJsonStringToObject[transactionsWallexGetUserRequest.UserNotification](requestBody.toString())
      }.getOrElse(throw new BaseException(constants.Response.FAILURE))

      val authToken = transactionsWallexAuthToken.Service.getToken()

      def userGetWallexAccount(authToken: String) =
        transactionsWallexGetUserRequest.Service.get(userNotificationRequest.resourceId, authToken)

      def updateStatus(wallexAccount: GetUserResponse) = wallexOrganizationAccounts.Service.updateStatus(
         wallexID = wallexAccount.id,
        status = wallexAccount.status
      )

      (for {
        authToken <- authToken
        userResponse <- userGetWallexAccount(authToken)
        _ <- updateStatus(userResponse)
      } yield Ok).recover {
        case baseException: BaseException => InternalServerError(baseException.failure.message)
      }
  }

}