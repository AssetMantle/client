package transactions.Response

import models.blockchain
import play.api.libs.json.{Json, Reads}

object AssetResponse {

  implicit val assetReads: Reads[blockchain.Asset] = Json.reads[blockchain.Asset]

  case class Response(value:  blockchain.Asset)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
