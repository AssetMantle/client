package constants

import scala.util.matching.Regex

object RegularExpression {
  val ANY_STRING: Regex = """.*""".r
  val ALL_NUMBERS_ALL_LETTERS: Regex = """^[A-Za-z0-9]*$""".r
  val ALL_NUMBERS_ALL_CAPITAL_LETTERS = """^[A-Z0-9]*$""".r
  val PASSWORD: Regex = """^[A-Za-z0-9!@#$%^&* ._-]*$""".r
  val ACCOUNT_ID: Regex = """^[^\s]+[a-zA-Z0-9!@#$%^&* ._-]*$""".r
  val MOBILE_NUMBER: Regex = """^(\+\d{1,3}[- ]?)?\d{6,14}$""".r
  val PEG_HASH: Regex = """^[0-9]*$""".r
  val DOCUMENT_NUMBER = """^[^\s]+[A-Za-z0-9/ ,.-]*$""".r
  val NAME = """^[^\s]+[A-Za-z0-9/ ,.-]*$""".r
  val ADDRESS = """^[^\s]+[A-Za-z0-9/ ,.-]*$""".r
  val ALL_LETTERS: Regex = """^[a-zA-z]*$""".r
  val HASH: Regex = """^[a-fA-F0-9]*$""".r
  val EMAIL_ADDRESS: Regex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r
  val SWIFT_CODE: Regex = """^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$""".r
  val FIAT: Regex = """^((0{1}\.{1}((0{1}[1-9]{1})|([1-9]{1}([0-9]{1})?)))|([1-9]{1}[0-9]*(\.{1}[0-9]{1}([0-9]{1})?)?))$""".r
  val QUANTITY: Regex = """^((0{1}\.{1}((0{2}[1-9]{1})|(0{1}[1-9]{1}([0-9]{1})?)|([1-9]{1}([0-9]{1,2})?)))|([1-9]{1}[0-9]*(\.{1}[0-9]{1}([0-9]{1}([0-9]{1})?)?)?))$""".r
  val GAS: Regex = """^((0{1}\.{1}((0{1,5}[1-9]{1})|([1-9]{1}([0-9]{1,5})?)))|([1-9]{1}[0-9]*(\.{1}[0-9]{1}([0-9]{1,5})?)?))$""".r
}
