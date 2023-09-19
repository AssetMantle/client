package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}

object BroadcastTxSyncResponse {

  case class Result(code: Int, data: String, log: String, codespace: String, hash: String)

  implicit val ResultReads: Reads[Result] = Json.reads[Result]

  case class Response(jsonrpc: String, id: Int, result: Result)

  implicit val ResponseReads: Reads[Response] = Json.reads[Response]

  case class Error(code: Int, message: String, data: String)

  implicit val ErrorReads: Reads[Error] = Json.reads[Error]

  case class ErrorResponse(jsonrpc: String, id: Int, error: Error)

  implicit val ErrorResponseReads: Reads[ErrorResponse] = Json.reads[ErrorResponse]

}
