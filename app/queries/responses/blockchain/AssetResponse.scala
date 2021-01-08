package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.{Height, ID, Immutables, Mutables}
import transactions.Abstract.BaseResponse

object AssetResponse {

  case class AssetIDValue(chainID: ID, maintainersID: ID, classificationID: ID, hashID: ID)

  implicit val assetIDValueReads: Reads[AssetIDValue] = Json.reads[AssetIDValue]

  case class AssetID(value: AssetIDValue)

  implicit val assetIDReads: Reads[AssetID] = Json.reads[AssetID]

  case class Asset(id: AssetID, lock: Height, burn: Height, immutables: Immutables, mutables: Mutables)

  implicit val assetReads: Reads[Asset] = Json.reads[Asset]

  case class AssetValue(value: Asset)

  implicit val assetValueReads: Reads[AssetValue] = Json.reads[AssetValue]

  case class AssetsValue(id: ID, list: Seq[AssetValue])

  implicit val assetsValueReads: Reads[AssetsValue] = Json.reads[AssetsValue]

  case class Assets(value: AssetsValue)

  implicit val assetsReads: Reads[Assets] = Json.reads[Assets]

  case class Value(assets: Assets)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Result(value: Value)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(height: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
