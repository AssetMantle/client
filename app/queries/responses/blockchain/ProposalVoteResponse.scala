package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}
import transactions.Abstract.BaseResponse

object ProposalVoteResponse {

  case class Vote(proposal_id: String, voter: String, option: String)

  implicit val voteReads: Reads[Vote] = Json.reads[Vote]

  case class Response(vote: Vote) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
