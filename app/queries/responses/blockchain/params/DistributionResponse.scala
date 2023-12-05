package queries.responses.blockchain.params

import models.Abstract.Parameter
import models.common.Parameters.DistributionParameter
import play.api.libs.json.{Json, Reads}


object DistributionResponse {

  case class Params(community_tax: String, base_proposer_reward: String, bonus_proposer_reward: String, withdraw_addr_enabled: Boolean) {
    def toParameter: Parameter = DistributionParameter(communityTax = BigDecimal(community_tax), baseProposerReward = BigDecimal(base_proposer_reward), bonusProposerReward = BigDecimal(bonus_proposer_reward), withdrawAddrEnabled = withdraw_addr_enabled)
  }

  implicit val paramsReads: Reads[Params] = Json.reads[Params]

  case class Response(params: Params)

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
