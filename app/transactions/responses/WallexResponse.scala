package transactions.responses

import play.api.libs.json.{Json, OWrites, Reads}
import transactions.Abstract.BaseResponse

object WallexResponse {

  case class Response(
      userId: String,
      email: String,
      firstName: String,
      lastName: String,
      countryCode: String,
      accountType: String,
      accountId: String
  ) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val responseWrites: OWrites[Response] = Json.writes[Response]

  case class GetUserResponse(
      id: String,
      email: String,
      firstName: String,
      lastName: String,
      status: String
  ) extends BaseResponse

  implicit val userResponseReads: Reads[GetUserResponse] =
    Json.reads[GetUserResponse]
  implicit val userResponseWrites: OWrites[GetUserResponse] =
    Json.writes[GetUserResponse]

  case class CreateDocumentResponse(
      id: String,
      documentName: String,
      documentType: String,
      uploadURL: String
  ) extends BaseResponse

  implicit val userKYCResponseReads: Reads[CreateDocumentResponse] =
    Json.reads[CreateDocumentResponse]
  implicit val userKYCResponseWrites: OWrites[CreateDocumentResponse] =
    Json.writes[CreateDocumentResponse]

  case class CreateSimpleQuote(
      currencyPair: String,
      buyCurrency: String,
      sellCurrency: String,
      buyAmount: Double,
      sellAmount: Double,
      fixedSide: String,
      rate: Double,
      partnerRate: Double,
      partnerBuyAmount: Double,
      partnerSellAmount: Double,
      partnerPaymentFee: Option[Double],
      expiresAt: String,
      quoteId: String,
      conversionFee: Double,
      paymentFee: Double,
      totalFee: Double,
      totalAmount: Double,
      paymentChannel: String,
      bankCharges: Double,
      supportingDocumentsRequired: Boolean
  )
  implicit val createSimpleQuoteReads: Reads[CreateSimpleQuote] =
    Json.reads[CreateSimpleQuote]
  implicit val createSimpleQuoteWrites: OWrites[CreateSimpleQuote] =
    Json.writes[CreateSimpleQuote]

  case class CreatePaymentQuoteResponse(
      data: CreateSimpleQuote
  ) extends BaseResponse

  implicit val createPaymentQuoteResponseReads
      : Reads[CreatePaymentQuoteResponse] =
    Json.reads[CreatePaymentQuoteResponse]
  implicit val createPaymentQuoteResponseWrites
      : OWrites[CreatePaymentQuoteResponse] =
    Json.writes[CreatePaymentQuoteResponse]

  case class BankAccount(
      accountNumber: String,
      address: String,
      bankAccountHolderName: String,
      bankName: String,
      bicSwift: String,
      country: String,
      currency: String
  )

  implicit val bankAccountReads: Reads[BankAccount] =
    Json.reads[BankAccount]
  implicit val bankAccountWrites: OWrites[BankAccount] =
    Json.writes[BankAccount]

  case class BeneficiaryDetails(
      address: String,
      city: String,
      companyName: String,
      country: String,
      entityType: String,
      beneficiaryId: String,
      nickname: String,
      `type`: String,
      bankAccount: BankAccount
  )
  implicit val beneficiaryDetailsReads: Reads[BeneficiaryDetails] =
    Json.reads[BeneficiaryDetails]
  implicit val beneficiaryDetailsWrites: OWrites[BeneficiaryDetails] =
    Json.writes[BeneficiaryDetails]

  case class BeneficiaryResponse(
      data: BeneficiaryDetails
  ) extends BaseResponse

  implicit val beneficiaryResponseReads: Reads[BeneficiaryResponse] =
    Json.reads[BeneficiaryResponse]
  implicit val beneficiaryResponseWrites: OWrites[BeneficiaryResponse] =
    Json.writes[BeneficiaryResponse]

  case class DeleteBeneficiary(
      address: String,
      city: String,
      country: String,
      beneficiaryId: String,
      nickname: String,
      `type`: String
  )

  implicit val beneficiaryDataDeleteReads: Reads[DeleteBeneficiary] =
    Json.reads[DeleteBeneficiary]
  implicit val beneficiaryDataDeleteWrites: OWrites[DeleteBeneficiary] =
    Json.writes[DeleteBeneficiary]

  case class DeleteBeneficiaryResponse(
      data: DeleteBeneficiary
  ) extends BaseResponse

  implicit val deleteBeneficiaryResponseReads
      : Reads[DeleteBeneficiaryResponse] =
    Json.reads[DeleteBeneficiaryResponse]
  implicit val deleteBeneficiaryResponseWrites
      : OWrites[DeleteBeneficiaryResponse] =
    Json.writes[DeleteBeneficiaryResponse]

  case class WalletToWalletXferResponse(
      id: String,
      senderAccountId: String,
      receiverAccountId: String,
      amount: Double,
      currency: String,
      purposesOfTransfer: String,
      reference: String,
      remarks: String,
      `type`: String,
      status: String,
      createdAt: String
  ) extends BaseResponse

  implicit val walletToWalletXferResponseReads
      : Reads[WalletToWalletXferResponse] =
    Json.reads[WalletToWalletXferResponse]

  implicit val walletToWalletXferResponseWrites
      : OWrites[WalletToWalletXferResponse] =
    Json.writes[WalletToWalletXferResponse]

  case class BeneficiarySimplePayment(
      address: String,
      city: String,
      companyName: String,
      country: String,
      beneficiaryId: String,
      nickname: String,
      `type`: String,
      bankAccount: BankAccount
  )
  implicit val beneficiarySimpleReads: Reads[BeneficiarySimplePayment] =
    Json.reads[BeneficiarySimplePayment]
  implicit val beneficiarySimpleWrites: OWrites[BeneficiarySimplePayment] =
    Json.writes[BeneficiarySimplePayment]

  case class CreateSimplePaymentData(
      simplePaymentId: String,
      totalAmount: Double,
      status: String,
      buyAmount: Double,
      buyCurrency: String,
      sellAmount: Double,
      sellCurrency: String,
      currencyPair: String,
      fixedSide: String,
      conversionFee: Double,
      rate: Double,
      totalFee: Double,
      paymentFee: Double,
      createdAt: String,
      completedAt: Option[String],
      referenceId: String,
      fundingSource: String,
      purposeOfTransfer: String,
      fundingReference: String,
      fundingCutoffTime: String,
      beneficiary: BeneficiarySimplePayment
  )

  implicit val createSimplePaymentDataReads: Reads[CreateSimplePaymentData] =
    Json.reads[CreateSimplePaymentData]
  implicit val createSimplePaymentDataWrites: OWrites[CreateSimplePaymentData] =
    Json.writes[CreateSimplePaymentData]

  case class CreateSimplePaymentResponse(data: CreateSimplePaymentData)
      extends BaseResponse

  implicit val createSimplePaymentResponseReads
      : Reads[CreateSimplePaymentResponse] =
    Json.reads[CreateSimplePaymentResponse]
  implicit val createSimplePaymentResponseWrites
      : OWrites[CreateSimplePaymentResponse] =
    Json.writes[CreateSimplePaymentResponse]

  case class PaymentFileUploadDataResponse(
      uploadUrl: String,
      fileId: String
  )

  implicit
  val paymentFileUploadDataReads: Reads[PaymentFileUploadDataResponse] =
    Json.reads[PaymentFileUploadDataResponse]
  implicit val paymentFileUploadDataWrites
      : OWrites[PaymentFileUploadDataResponse] =
    Json.writes[PaymentFileUploadDataResponse]

  case class PaymentFileUploadResponse(
      data: PaymentFileUploadDataResponse
  ) extends BaseResponse

  implicit val paymentFileUploadReads: Reads[PaymentFileUploadResponse] =
    Json.reads[PaymentFileUploadResponse]
  implicit val paymentFileUploadWrites: OWrites[PaymentFileUploadResponse] =
    Json.writes[PaymentFileUploadResponse]

  case class UpdateDetailsResponse(
      accountId: String,
      companyName: String,
      countryOfIncorporation: String,
      countryOfOperations: String,
      businessType: String,
      companyAddress: String,
      postalCode: String,
      state: String,
      city: String,
      registrationNumber: String,
      incorporationDate: String
  ) extends BaseResponse

  implicit val updateDetailsResponseReads: Reads[UpdateDetailsResponse] =
    Json.reads[UpdateDetailsResponse]
  implicit val updateDetailsResponseWrites: OWrites[UpdateDetailsResponse] =
    Json.writes[UpdateDetailsResponse]

  case class CollectionBankDetails(
      bankName: String,
      currency: String,
      paymentType: String,
      accountNumber: String,
      accountHolderName: String,
      accountNumberType: String
  )
  implicit val collectionBankDetailsReads: Reads[CollectionBankDetails] =
    Json.reads[CollectionBankDetails]
  implicit val CollectionBankDetailsWrites: OWrites[CollectionBankDetails] =
    Json.writes[CollectionBankDetails]

  case class CreateCollectionResponse(
      id: String,
      name: String,
      currency: String,
      reference: String,
      purpose: String,
      createdAt: String,
      bankDetails: Seq[CollectionBankDetails]
  ) extends BaseResponse

  implicit val createCollectionResponseReads: Reads[CreateCollectionResponse] =
    Json.reads[CreateCollectionResponse]
  implicit val createCollectionResponseWrites
      : OWrites[CreateCollectionResponse] =
    Json.writes[CreateCollectionResponse]

  case class GetBalanceData(
      id: String,
      amount: Double,
      currency: String,
      currencyName: String
  )

  implicit val walletBalanceDataReads: Reads[GetBalanceData] =
    Json.reads[GetBalanceData]
  implicit val walletBalanceDataWrites: OWrites[GetBalanceData] =
    Json.writes[GetBalanceData]

  case class GetBalanceResponse(
      data: GetBalanceData
  ) extends BaseResponse

  implicit val walletBalanceReads: Reads[GetBalanceResponse] =
    Json.reads[GetBalanceResponse]
  implicit val walletBalanceWrites: OWrites[GetBalanceResponse] =
    Json.writes[GetBalanceResponse]
}
