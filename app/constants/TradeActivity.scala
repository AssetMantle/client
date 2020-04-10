package constants

class TradeActivity(activityType: String) {
  val title: String = Seq("TRADE_ACTIVITY", activityType, "TITLE").mkString(".")
  val message: String = Seq("TRADE_ACTIVITY", activityType, "MESSAGE").mkString(".")
}

object TradeActivity {
  val NEGOTIATION_STARTED = new TradeActivity("NEGOTIATION_STARTED")
}
