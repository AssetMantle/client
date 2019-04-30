package transactions.Response

import play.api.libs.json.{Json, Reads}

object OrderResponse {

  case class Value(id: String, fiatProofHash: Option[String], awbProofHash: Option[String], fiatPegWallet: Option[Seq[FiatResponse.Value]], assetPegWallet: Option[Seq[AssetResponse.Value]])

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Response(value: Value)
  
  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
