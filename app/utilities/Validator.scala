package utilities

object Validator {

  def isHexAddress(query: String): Boolean = query.matches(constants.RegularExpression.VALIDATOR_HEX_ADDRESS.regex)

}
