package constants

object Blockchain {
  val MnemonicShown = 3
  val NegotiationDefaultTime = 5000000
  val DefaultFaucetTokenAmount = 1
  val IDSeparator = "."

  object Order {
    val MakerIDProperty = "MakerID"
    val TakerIDProperty = "TakerID"
    val MakerSplitProperty = "MakerSplit"
    val MakerSplitIDProperty = "MakerSplitID"
    val ExchangeRateProperty = "ExchangeRate"
    val TakerSplitIDProperty = "TakerSplitID"
    val HeightProperty = "Height"
  }

  object Events {

    object Slashing {
      val MissedBlocks = "missed_blocks"
      val DoubleSign = "double_sign"
      val MissingSignature = "missing_signature"

      def getSlashingReason(reason: String): String = reason match {
        case MissingSignature => constants.View.MISSING_SIGNATURE
        case DoubleSign => constants.View.DOUBLE_SIGNING
        case _ => constants.View.UNKNOWN
      }
    }

  }

}