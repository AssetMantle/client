package queries.responses.bandChain

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object OracleScriptResponse {

  case class Result(owner: String, name: String, description: String, filename: String, schema: String, source_code_url: String)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(height: String, result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
