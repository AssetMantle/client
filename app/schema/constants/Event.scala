package schema.constants

object Event {
  //transaction
  val Message = "message"
  //bank
  val Transfer = "transfer"
  //crisis
  val Invariant = "invariant"
  //distribution
  val SetWithdrawAddress = "set_withdraw_address"
  val Rewards = "rewards"
  val Commission = "commission"
  val WithdrawRewards = "withdraw_rewards"
  val WithdrawCommission = "withdraw_commission"
  val ProposerReward = "proposer_reward"
  //evidence
  val SubmitEvidence = "submit_evidence"
  //governance
  val SubmitProposal = "submit_proposal"
  val ProposalDeposit = "proposal_deposit"
  val ProposalVote = "proposal_vote"
  val InactiveProposal = "inactive_proposal"
  val ActiveProposal = "active_proposal"
  //mint
  val Mint = "mint"
  //slashing
  val Slash = "slash"
  val Liveness = "liveness"
  //staking
  val CompleteUnbonding = "complete_unbonding"
  val CompleteRedelegation = "complete_redelegation"
  val CreateValidator = "create_validator"
  val EditValidator = "edit_validator"
  val Delegate = "delegate"
  val Unbond = "unbond"
  val Redelegate = "redelegate"

  object Attribute {
    // transaction
    val Module = "module"
    //bank
    val Recipient = "recipient"
    val Sender = "sender"
    //crisis
    val Route = "route"
    //distribution
    val WithdrawAddress = "withdraw_address"
    val Amount = "amount"
    //evidence
    val EvidenceHash = "evidence_hash"
    //governance
    val ProposalResult = "proposal_result"
    val Option = "option"
    val ProposalID = "proposal_id"
    val VotingPeriodStart = "voting_period_start"
    val ProposalDropped = "proposal_dropped"
    val ProposalPassed = "proposal_passed"
    val ProposalRejected = "proposal_rejected"
    val ProposalFailed = "proposal_failed"
    val ProposalType = "proposal_type"
    //mint
    val BondedRatio = "bonded_ratio"
    val Inflation = "inflation"
    val AnnualProvisions = "annual_provisions"
    //slashing
    val Address = "address"
    val Height = "height"
    val Power = "power"
    val Reason = "reason"
    val Jailed = "jailed"
    val MissedBlocks = "missed_blocks"
    val DoubleSign = "double_sign"
    val MissingSignature = "missing_signature"
    //staking
    val Validator = "validator"
    val CommissionRate = "commission_rate"
    val MinSelfDelegation = "min_self_delegation"
    val SrcValidator = "source_validator"
    val DstValidator = "destination_validator"
    val Delegator = "delegator"
    val CompletionTime = "completion_time"
  }

}
