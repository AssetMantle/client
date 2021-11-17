package queries.responses.blockchain.params

import models.Abstract.Parameter
import models.common.Parameters.GovernanceParameter
import play.api.libs.json.{Json, Reads}
import queries.responses.common.Coin
import transactions.Abstract.BaseResponse

object GovResponse {

  case class VotingParams(voting_period: String)

  implicit val votingParamsReads: Reads[VotingParams] = Json.reads[VotingParams]

  case class DepositParams(max_deposit_period: String, min_deposit: Seq[Coin])

  implicit val depositParamsReads: Reads[DepositParams] = Json.reads[DepositParams]

  case class TallyParams(quorum: String, threshold: String, veto_threshold: String)

  implicit val tallyParamsReads: Reads[TallyParams] = Json.reads[TallyParams]

  case class VotingResponse(result: VotingParams) extends BaseResponse

  implicit val votingResponseReads: Reads[VotingResponse] = Json.reads[VotingResponse]

  case class DepositResponse(result: DepositParams) extends BaseResponse

  implicit val depositResponseReads: Reads[DepositResponse] = Json.reads[DepositResponse]

  case class TallyResponse(result: TallyParams) extends BaseResponse

  implicit val tallyResponseReads: Reads[TallyResponse] = Json.reads[TallyResponse]

  case class Response(voting_params: VotingParams, deposit_params: DepositParams, tally_params: TallyParams) extends BaseResponse {
    def toParameter: Parameter = GovernanceParameter(minDeposit = deposit_params.min_deposit.map(_.toCoin), maxDepositPeriod = deposit_params.max_deposit_period.split("s")(0).toLong, votingPeriod = voting_params.voting_period.split("s")(0).toLong, quorum = BigDecimal(tally_params.quorum), threshold = BigDecimal(tally_params.threshold), vetoThreshold = BigDecimal(tally_params.veto_threshold))
  }

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
