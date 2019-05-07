package queries.responses

import models.blockchain
import play.api.libs.json.{Json, Reads}

object FeedbackResponse {

  case class TranscationFeedback(buyerExecuteOrderNegativeTx: String, buyerExecuteOrderPositiveTx: String, changeBuyerBidNegativeTx: String, changeBuyerBidPositiveTx: String, changeSellerBidNegativeTx: String, changeSellerBidPositiveTx: String, confirmBuyerBidNegativeTx: String, confirmBuyerBidPositiveTx: String, confirmSellerBidNegativeTx: String, confirmSellerBidPositiveTx: String, ibcIssueAssetsNegativeTx: String, ibcIssueAssetsPositiveTx: String, ibcIssueFiatsNegativeTx: String, ibcIssueFiatsPositiveTx: String, negotiationNegativeTx: String, negotiationPositiveTx: String, sellerExecuteOrderNegativeTx: String, sellerExecuteOrderPositiveTx: String, sendAssetsNegativeTx: String, sendAssetsPositiveTx: String, sendFiatsNegativeTx: String, sendFiatsPositiveTx: String)

  implicit val transcationFeedbackReads: Reads[TranscationFeedback] = Json.reads[TranscationFeedback]

  case class TraderFeedbackHistory(buyerAddress: String, sellerAddress: String, pegHash: String, rating: String)

  implicit val traderFeedbackHistoryReads: Reads[TraderFeedbackHistory] = Json.reads[TraderFeedbackHistory]

  case class Value(address: String, transcationFeedback: TranscationFeedback, traderFeedbackHistory: Option[Seq[TraderFeedbackHistory]])

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value){
    def applyToBlockchainFeedback(): blockchain.Feedback = {
     if (value.traderFeedbackHistory.isDefined){
       blockchain.Feedback(value.address, value.traderFeedbackHistory.get.map{traderHistory: TraderFeedbackHistory => traderHistory.rating.toInt}.sum/value.traderFeedbackHistory.size, false)
     } else {
       blockchain.Feedback(value.address, 0, false)
     }
    }
  }

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
