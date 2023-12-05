package queries.responses.blockchain

import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import queries.responses.common.Header


object BlockCommitResponse {

  implicit val module: String = constants.Module.BLOCK_COMMIT_RESPONSE

  implicit val logger: Logger = Logger(this.getClass)

  case class Signature(validator_address: String)

  implicit val signatureReads: Reads[Signature] = Json.reads[Signature]

  case class Commit(height: String, signatures: Seq[Option[Signature]])

  def optionalCommitApply(height: String, values: JsValue): Commit = {
    val signaturesList = try {
      values.as[Seq[JsValue]].map {
        case JsNull => None
        case v: JsObject => Some(utilities.JSON.convertJsonStringToObject[Signature](v.toString))
      }
    } catch {
      case exception: Exception => constants.Response.JSON_PARSE_EXCEPTION.throwBaseException(exception)
    }
    Commit(height, signaturesList)
  }

  implicit val commitReads: Reads[Commit] = (
    (JsPath \ "height").read[String] and
      (JsPath \ "signatures").read[JsValue]
    ) (optionalCommitApply _)


  case class SignedHeader(header: Header, commit: Commit)

  implicit val signedHeaderReads: Reads[SignedHeader] = Json.reads[SignedHeader]

  case class Result(signed_header: SignedHeader)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result)

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
