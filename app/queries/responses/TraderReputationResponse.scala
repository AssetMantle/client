package queries.responses

import play.api.libs.json.{Json, OWrites, Reads}
import transactions.Abstract.BaseResponse

object TraderReputationResponse {

  case class TransactionFeedbackResponse(sendAssetsPositiveTx: String, sendAssetsNegativeTx: String, sendFiatsPositiveTx: String, sendFiatsNegativeTx: String, ibcIssueAssetsPositiveTx: String, ibcIssueAssetsNegativeTx: String, ibcIssueFiatsPositiveTx: String, ibcIssueFiatsNegativeTx: String, buyerExecuteOrderPositiveTx: String, buyerExecuteOrderNegativeTx: String, sellerExecuteOrderPositiveTx: String, sellerExecuteOrderNegativeTx: String, changeBuyerBidPositiveTx: String, changeBuyerBidNegativeTx: String, changeSellerBidPositiveTx: String, changeSellerBidNegativeTx: String, confirmBuyerBidPositiveTx: String, confirmBuyerBidNegativeTx: String, confirmSellerBidPositiveTx: String, confirmSellerBidNegativeTx: String, negotiationPositiveTx: String, negotiationNegativeTx: String)

  implicit val transactionFeedbackResponseReads: Reads[TransactionFeedbackResponse] = Json.reads[TransactionFeedbackResponse]
  implicit val transactionFeedbackResponseWrites: OWrites[TransactionFeedbackResponse] = Json.writes[TransactionFeedbackResponse]

  case class TraderFeedbackHistory(buyerAddress: String, sellerAddress: String, pegHash: String, rating: String)

  implicit val traderFeedbackHistoryReads: Reads[TraderFeedbackHistory] = Json.reads[TraderFeedbackHistory]
  implicit val traderFeedbackHistoryWrites: OWrites[TraderFeedbackHistory] = Json.writes[TraderFeedbackHistory]

  case class Value(address: String, transactionFeedback: TransactionFeedbackResponse, traderFeedbackHistory: Option[Seq[TraderFeedbackHistory]])

  implicit val valueReads: Reads[Value] = Json.reads[Value]
  implicit val valueWrites: OWrites[Value] = Json.writes[Value]

  case class Response(value: Value) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
  implicit val responseWrites: OWrites[Response] = Json.writes[Response]
}
