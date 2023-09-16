package utilities

object Validator {

  def isHexAddress(query: String): Boolean = query.matches(constants.RegularExpression.VALIDATOR_HEX_ADDRESS.regex)

  def getSlashingReason(reason: String): String = reason match {
    case schema.constants.Event.Attribute.MissingSignature => constants.View.MISSING_SIGNATURE
    case schema.constants.Event.Attribute.DoubleSign => constants.View.DOUBLE_SIGNING
    case _ => constants.View.UNKNOWN
  }

}
