package queries.responses.blockchain.params

import models.Abstract.Parameter
import models.common.Parameters
import play.api.libs.json.{Json, Reads}


object BankResponse {

  case class SendEnabled(denom: String, enabled: Boolean)

  implicit val sendEnabledReads: Reads[SendEnabled] = Json.reads[SendEnabled]

  case class Params(default_send_enabled: Boolean, send_enabled: Seq[SendEnabled]) {
    def toParameter: Parameter = Parameters.BankParameter(defaultSendEnabled = default_send_enabled, sendEnabled = send_enabled.map(x => Parameters.SendEnabled(denom = x.denom, enabled = x.enabled)))
  }

  implicit val paramsReads: Reads[Params] = Json.reads[Params]

  case class Response(params: Params)

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
