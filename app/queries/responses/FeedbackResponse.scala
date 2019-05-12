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

    def applyToBlockchainPositiveTraderHistory(): blockchain.PositiveTraderHistory = {
      blockchain.PositiveTraderHistory(address = value.address, buyerExecuteOrderPositiveTx = value.transcationFeedback.buyerExecuteOrderPositiveTx.toInt, changeBuyerBidPositiveTx = value.transcationFeedback.changeBuyerBidPositiveTx.toInt, changeSellerBidPositiveTx = value.transcationFeedback.changeSellerBidPositiveTx.toInt, confirmBuyerBidPositiveTx = value.transcationFeedback.confirmBuyerBidPositiveTx.toInt, confirmSellerBidPositiveTx = value.transcationFeedback.confirmSellerBidPositiveTx.toInt, ibcIssueAssetsPositiveTx = value.transcationFeedback.ibcIssueAssetsPositiveTx.toInt, ibcIssueFiatsPositiveTx = value.transcationFeedback.ibcIssueFiatsPositiveTx.toInt, negotiationPositiveTx = value.transcationFeedback.negotiationPositiveTx.toInt, sellerExecuteOrderPositiveTx = value.transcationFeedback.sellerExecuteOrderPositiveTx.toInt, sendAssetsPositiveTx = value.transcationFeedback.sendAssetsPositiveTx.toInt, sendFiatsPositiveTx = value.transcationFeedback.sendFiatsPositiveTx.toInt)
    }

    def applyToBlockchainNegativeTraderHistory(): blockchain.NegativeTraderHistory = {
      blockchain.NegativeTraderHistory(address = value.address, buyerExecuteOrderNegativeTx = value.transcationFeedback.buyerExecuteOrderNegativeTx.toInt, changeBuyerBidNegativeTx = value.transcationFeedback.changeBuyerBidNegativeTx.toInt, changeSellerBidNegativeTx = value.transcationFeedback.changeSellerBidNegativeTx.toInt, confirmBuyerBidNegativeTx = value.transcationFeedback.confirmBuyerBidNegativeTx.toInt, confirmSellerBidNegativeTx = value.transcationFeedback.confirmSellerBidNegativeTx.toInt, ibcIssueAssetsNegativeTx = value.transcationFeedback.ibcIssueAssetsNegativeTx.toInt, ibcIssueFiatsNegativeTx = value.transcationFeedback.ibcIssueFiatsNegativeTx.toInt, negotiationNegativeTx = value.transcationFeedback.negotiationNegativeTx.toInt, sellerExecuteOrderNegativeTx = value.transcationFeedback.sellerExecuteOrderNegativeTx.toInt, sendAssetsNegativeTx = value.transcationFeedback.sendAssetsNegativeTx.toInt, sendFiatsNegativeTx = value.transcationFeedback.sendFiatsNegativeTx.toInt)
    }
  }

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
