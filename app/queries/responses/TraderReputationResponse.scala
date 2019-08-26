package queries.responses

import play.api.libs.json.{Json, Reads}
import transactions.responses.ResponseEntity

object TraderReputationResponse {

  case class TransactionFeedbackResponse(sendAssetsPositiveTx: String, sendAssetsNegativeTx: String, sendFiatsPositiveTx: String, sendFiatsNegativeTx: String, ibcIssueAssetsPositiveTx: String, ibcIssueAssetsNegativeTx: String, ibcIssueFiatsPositiveTx: String, ibcIssueFiatsNegativeTx: String, buyerExecuteOrderPositiveTx: String, buyerExecuteOrderNegativeTx: String, sellerExecuteOrderPositiveTx: String, sellerExecuteOrderNegativeTx: String, changeBuyerBidPositiveTx: String, changeBuyerBidNegativeTx: String, changeSellerBidPositiveTx: String, changeSellerBidNegativeTx: String, confirmBuyerBidPositiveTx: String, confirmBuyerBidNegativeTx: String, confirmSellerBidPositiveTx: String, confirmSellerBidNegativeTx: String, negotiationPositiveTx: String, negotiationNegativeTx: String)

  implicit val transactionFeedbackResponseReads: Reads[TransactionFeedbackResponse] = Json.reads[TransactionFeedbackResponse]

  case class TraderFeedbackHistory(buyerAddress: String, sellerAddress: String, pegHash: String, rating: String)

  implicit val traderFeedbackHistoryReads: Reads[TraderFeedbackHistory] = Json.reads[TraderFeedbackHistory]

  case class Value(address: String, transactionFeedback: TransactionFeedbackResponse, traderFeedbackHistory: Option[Seq[TraderFeedbackHistory]])

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value)  extends ResponseEntity

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
