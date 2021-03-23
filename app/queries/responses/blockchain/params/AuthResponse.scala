package queries.responses.blockchain.params

import models.Abstract.Parameter
import models.common.Parameters.AuthParameter
import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object AuthResponse {

  case class Params(max_memo_characters: String, tx_sig_limit: String, tx_size_cost_per_byte: String, sig_verify_cost_ed25519: String, sig_verify_cost_secp256k1: String) {
    def toParameter: Parameter = AuthParameter(maxMemoCharacters = max_memo_characters, sigVerifyCostEd25519 = sig_verify_cost_ed25519, sigVerifyCostSecp256k1 = sig_verify_cost_secp256k1, txSigLimit = tx_sig_limit, txSizeCostPerByte = tx_size_cost_per_byte)
  }

  implicit val paramsReads: Reads[Params] = Json.reads[Params]

  case class Response(params: Params) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
