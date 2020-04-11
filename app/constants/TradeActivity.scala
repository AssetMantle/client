package constants

class TradeActivity(activityType: String) {
  val title: String = Seq("TRADE_ACTIVITY", activityType, "TITLE").mkString(".")
  val message: String = Seq("TRADE_ACTIVITY", activityType, "MESSAGE").mkString(".")
}

object TradeActivity {
  val NEGOTIATION_STARTED = new TradeActivity("NEGOTIATION_STARTED")
  val DOCUMENT_LIST_UPDATED = new TradeActivity("DOCUMENT_LIST_UPDATED")
  val PAYMENT_TERMS_UPDATED = new TradeActivity("PAYMENT_TERMS_UPDATED")
  val ASSET_DETAILS_UPDATED = new TradeActivity("ASSET_DETAILS_UPDATED")
}
