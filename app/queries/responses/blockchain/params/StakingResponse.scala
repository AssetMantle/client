package queries.responses.blockchain.params

import models.Abstract.Parameter
import models.common.Parameters.StakingParameter
import play.api.libs.json.{Json, Reads}


object StakingResponse {

  case class Params(unbonding_time: String, max_validators: Int, max_entries: Int, historical_entries: Int, bond_denom: String) {
    def toParameter: Parameter = StakingParameter(unbondingTime = unbonding_time.split("s")(0).toLong, maxValidators = max_validators, maxEntries = max_entries, historicalEntries = historical_entries, bondDenom = bond_denom)
  }

  implicit val paramsReads: Reads[Params] = Json.reads[Params]

  case class Response(params: Params)

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
