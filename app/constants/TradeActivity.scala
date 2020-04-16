package constants

class TradeActivity(val template: String)

object TradeActivity {
  val PREFIX = "TRADE_ACTIVITY"
  val TITLE_SUFFIX = "TITLE"
  val MESSAGE_SUFFIX = "MESSAGE"

  val NEGOTIATION_STARTED = new TradeActivity("NEGOTIATION_STARTED")
  val DOCUMENT_LIST_UPDATED = new TradeActivity("DOCUMENT_LIST_UPDATED")
  val PAYMENT_TERMS_UPDATED = new TradeActivity("PAYMENT_TERMS_UPDATED")
  val ASSET_DETAILS_UPDATED = new TradeActivity("ASSET_DETAILS_UPDATED")
}
