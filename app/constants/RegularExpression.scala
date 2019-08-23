package constants

import scala.util.matching.Regex

object RegularExpression {

  val ALL_NUMBERS_ALL_LETTERS: Regex = """^[A-Za-z0-9]+$""".r
  val PASSWORD: Regex = """^[A-Za-z0-9]$""".r
  val ACCOUNT_ID: Regex = """^[a-zA-Z0-9]+$""".r
  val MOBILE_NUMBER: Regex = """^(\+\d{1,3}[- ]?)?\d{10}$""".r
  val PEG_HASH: Regex = """^[0-9]+$""".r
  val ALL_LETTERS: Regex = """^[a-zA-z]+$""".r
  val HASH: Regex = """^[a-fA-F0-9]+$""".r

}
