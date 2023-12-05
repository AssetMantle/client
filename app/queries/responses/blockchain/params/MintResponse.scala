package queries.responses.blockchain.params

import models.Abstract.Parameter
import models.common.Parameters.MintingParameter
import play.api.libs.json.{Json, Reads}


object MintResponse {

  case class Params(mint_denom: String, inflation_rate_change: String, inflation_max: String, inflation_min: String, goal_bonded: String, blocks_per_year: String) {
    def toParameter: Parameter = MintingParameter(mintDenom = mint_denom, inflationRateChange = BigDecimal(inflation_rate_change), inflationMax = BigDecimal(inflation_max), inflationMin = BigDecimal(inflation_min), goalBonded = BigDecimal(goal_bonded), blocksPerYear = blocks_per_year.toInt)
  }

  implicit val paramsReads: Reads[Params] = Json.reads[Params]

  case class Response(params: Params)

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
