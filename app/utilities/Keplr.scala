package utilities

import play.api.libs.json.{Json, Writes}

object Keplr {

  case class Coin(amount: String, denom: String)

  implicit val coinWrites: Writes[Coin] = Json.writes[Coin]

  case class Fee(amount: Seq[Coin] = Seq.empty, gas: String = "0")

  implicit val feeWrites: Writes[Fee] = Json.writes[Fee]

  case class MsgValue(data: String, signer: String)

  implicit val msgValueWrites: Writes[MsgValue] = Json.writes[MsgValue]

  case class Msg(`type`: String = "sign/MsgSignData", value: MsgValue)

  implicit val msgWrites: Writes[Msg] = Json.writes[Msg]

  case class ArbitraryData(account_number: String = "0", chain_id: String = "", fee: Fee = Fee(), memo: String = "", msgs: Seq[Msg], sequence: String = "0") {

    def getSHA256: Array[Byte] = utilities.Hash.sha256Hash(Json.toJson(this).toString)

  }

  def newArbitraryData(data: String, signer: String): ArbitraryData = ArbitraryData(msgs = Seq(Msg(value = MsgValue(data = utilities.Hash.base64Encoder(data), signer = signer))))

  implicit val arbitraryDataWrites: Writes[ArbitraryData] = Json.writes[ArbitraryData]


}
