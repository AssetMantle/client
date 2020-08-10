package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.common._
import transactions.Abstract.BaseResponse

object SplitResponse {

  case class SplitIDValue(ownerID: ID, ownableID: ID)

  implicit val splitIDValueReads: Reads[SplitIDValue] = Json.reads[SplitIDValue]

  case class SplitID(value: SplitIDValue)

  implicit val splitIDReads: Reads[SplitID] = Json.reads[SplitID]

  case class Split(id: SplitID, split: BigDecimal)

  implicit val splitReads: Reads[Split] = Json.reads[Split]

  case class SplitValue(value: Split)

  implicit val splitValueReads: Reads[SplitValue] = Json.reads[SplitValue]

  case class SplitsValue(id: ID, list: Seq[SplitValue])

  implicit val splitsValueReads: Reads[SplitsValue] = Json.reads[SplitsValue]

  case class Splits(value: SplitsValue)

  implicit val splitsReads: Reads[Splits] = Json.reads[Splits]

  case class Value(splits: Splits)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Result(value: Value)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(height: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}