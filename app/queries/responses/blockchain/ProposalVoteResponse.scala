package queries.responses.blockchain

import play.api.libs.json.{Json, Reads}


object ProposalVoteResponse {

  case class Vote(proposal_id: String, voter: String, option: String)

  implicit val voteReads: Reads[Vote] = Json.reads[Vote]

  case class Response(vote: Vote)

  implicit val responseReads: Reads[Response] = Json.reads[Response]
}
