package queries.responses.blockchain

import exceptions.BaseException
import play.api.Logger
import play.api.libs.json.{JsObject, Json, Reads}
import queries.Abstract.TendermintEvidence
import queries.responses.common.Header
import transactions.Abstract.BaseResponse
import utilities.Blockchain.SlashingEvidence
import utilities.MicroNumber

object BlockResponse {

  private implicit val module: String = constants.Module.BLOCK_RESPONSE

  private implicit val logger: Logger = Logger(this.getClass)

  case class Vote(`type`: Int, height: String, round: Int, timestamp: String, validator_address: String, validator_index: Int, signature: String)

  implicit val voteReads: Reads[Vote] = Json.reads[Vote]

  case class DuplicateVoteEvidence(vote_a: Vote, vote_b: Vote, TotalVotingPower: String, ValidatorPower: String, Timestamp: String) extends TendermintEvidence {
    def getSlashingEvidences: Seq[SlashingEvidence] = Seq(SlashingEvidence(height = vote_a.height.toInt, time = Timestamp, validatorHexAddress = vote_a.validator_address, validatorPower = MicroNumber(ValidatorPower)))
  }

  implicit val duplicateVoteEvidenceReads: Reads[DuplicateVoteEvidence] = Json.reads[DuplicateVoteEvidence]

  case class ByzantineValidator(address: String, voting_power: String)

  implicit val byzantineValidatorReads: Reads[ByzantineValidator] = Json.reads[ByzantineValidator]

  case class LightClientAttackEvidence(CommonHeight: String, ByzantineValidators: Seq[ByzantineValidator], TotalVotingPower: String, Timestamp: String) extends TendermintEvidence {
    def getSlashingEvidences: Seq[SlashingEvidence] = this.ByzantineValidators.map(validator => SlashingEvidence(height = this.CommonHeight.toInt, time = this.Timestamp, validatorHexAddress = validator.address, validatorPower = MicroNumber(validator.voting_power)))
  }

  implicit val lightClientAttackEvidenceReads: Reads[LightClientAttackEvidence] = Json.reads[LightClientAttackEvidence]

  def tendermintEvidenceApply(evidenceType: String, value: JsObject): TendermintEvidence = evidenceType match {
    case constants.Blockchain.Tendermint.DuplicateVoteEvidence => utilities.JSON.convertJsonStringToObject[DuplicateVoteEvidence](value.toString)
    case constants.Blockchain.Tendermint.LightClientAttackEvidence => utilities.JSON.convertJsonStringToObject[LightClientAttackEvidence](value.toString)
    case _ => throw new BaseException(constants.Response.UNKNOWN_TENDERMINT_EVIDENCE_TYPE)
  }

  case class Evidence(evidence: Seq[TendermintEvidence])

  implicit val evidenceReads: Reads[Evidence] = Json.reads[Evidence]

  case class Block(header: Header, evidence: Evidence) // Add last_commit to get signature at previousBlock

  implicit val blockReads: Reads[Block] = Json.reads[Block]

  case class Result(block: Block)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

  case class Response(result: Result) extends BaseResponse

  implicit val responseReads: Reads[Response] = Json.reads[Response]

}
