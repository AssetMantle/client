package utilities

object Validator {

  def isHexAddress(query: String): Boolean = query.matches("[A-F0-9]{40}")

}
