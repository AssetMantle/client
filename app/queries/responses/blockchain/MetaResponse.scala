package queries.responses.blockchain

import blockchain.common.ID
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object MetaResponse {

  case class Meta(id: ID, data: String)

  implicit val metaReads: Reads[Meta] = Json.reads[Meta]

  case class MetaValue(value: Meta)

  implicit val metaValueReads: Reads[MetaValue] = Json.reads[MetaValue]

  case class MetasValue(id: ID, list: Seq[MetaValue])

  implicit val metasValueReads: Reads[MetasValue] = Json.reads[MetasValue]

  case class Metas(value: MetasValue)

  implicit val metasReads: Reads[Metas] = Json.reads[Metas]

  case class Value(metas: Metas)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  case class Result(value: Value)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(height: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
