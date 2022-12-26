package models.common

import exceptions.BaseException
import play.api.libs.json._
import utilities.MicroNumber

case class Coin(denom: String, amount: MicroNumber) {

  def isIBCDenom: Boolean = denom.startsWith("ibc/") && denom.length == 68

  def ibcDenomName: String = constants.Blockchain.IBCDenoms.find(_.hash == denom).fold(denom)(_.name)

  def normalizeDenom: String = if (denom.startsWith("u")) denom.substring(1).toUpperCase()
  else if (isIBCDenom && ibcDenomName.startsWith("u")) ibcDenomName.substring(1).toUpperCase()
  else denom.toUpperCase()

  def getAmountWithNormalizedDenom(formatted: Boolean = true): String = if (formatted) s"${utilities.NumericOperation.formatNumber(amount)} $normalizeDenom" else s"${amount.toString} $normalizeDenom"

  def getMicroAmountWithDenom: String = s"${utilities.NumericOperation.formatNumber(number = amount, normalize = false)} $denom"

  def isNegative: Boolean = amount < 0

  def isZero: Boolean = amount == MicroNumber.zero

  def add(coin: Coin): Coin = {
    if (coin.denom != denom) {
      throw new BaseException(constants.Response.ARITHMETIC_OPERATION_ON_DIFFERENT_COIN)
    }
    Coin(denom = denom, amount = amount + coin.amount)
  }

  def subtract(coin: Coin): Coin = {
    val result = add(coin.copy(amount = amount * -1))
    if (result.isNegative) {
      throw new BaseException(constants.Response.COIN_AMOUNT_NEGATIVE)
    }
    result
  }
}

object Coin {
  def coinApply(denom: String, amount: String): Coin = Coin(denom = denom, amount = MicroNumber(BigInt(amount)))

  implicit val coinReads: Reads[Coin] = (
    (JsPath \ "denom").read[String] and
      (JsPath \ "amount").read[String]
    ) (coinApply _)

  implicit val coinWrites: Writes[Coin] = (coin: Coin) => Json.obj(
    "denom" -> coin.denom,
    "amount" -> coin.amount.toMicroString
  )
}
