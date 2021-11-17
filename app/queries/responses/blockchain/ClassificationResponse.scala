package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import queries.responses.common.{ID, Immutables, Mutables}
import transactions.Abstract.BaseResponse

object ClassificationResponse {

  case class ClassificationIDValue(chainID: ID, maintainersID: ID, hashID: ID)

  implicit val classificationIDValueReads: Reads[ClassificationIDValue] = Json.reads[ClassificationIDValue]

  case class ClassificationID(value: ClassificationIDValue)

  implicit val classificationIDReads: Reads[ClassificationID] = Json.reads[ClassificationID]

  case class Classification(id: ClassificationID, immutableTraits: Immutables, mutableTraits: Mutables)

  implicit val classificationReads: Reads[Classification] = Json.reads[Classification]

  case class ClassificationValue(value: Classification)

  implicit val classificationValueReads: Reads[ClassificationValue] = Json.reads[ClassificationValue]

  case class ClassificationsValue(id: ID, list: Seq[ClassificationValue])

  implicit val classificationsValueReads: Reads[ClassificationsValue] = Json.reads[ClassificationsValue]

  case class Classifications(value: ClassificationsValue)

  implicit val classificationsReads: Reads[Classifications] = Json.reads[Classifications]

  case class Value(classifications: Classifications)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Result(value: Value)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(height: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
