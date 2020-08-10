package queries.responses

import play.api.libs.json.{Json, Reads}
import queries.responses.common.Header
import transactions.Abstract.BaseResponse

object BlockCommitResponse {

  case class Signature(validator_address: String)

  implicit val signatureReads: Reads[Signature] = Json.reads[Signature]

  case class Commit(height: String, signatures: Seq[Signature])

  implicit val commitReads: Reads[Commit] = Json.reads[Commit]

  case class SignedHeader(header: Header, commit: Commit)

  implicit val signedHeaderReads: Reads[SignedHeader] = Json.reads[SignedHeader]

  case class Result(signed_header: SignedHeader)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
