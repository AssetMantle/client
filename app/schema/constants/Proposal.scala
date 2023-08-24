package schema.constants

object Proposal {
  val PARAMETER_CHANGE = "/cosmos.params.v1beta1.ParameterChangeProposal"
  val TEXT = "/cosmos.gov.v1beta1.TextProposal"
  val COMMUNITY_POOL_SPEND = "/cosmos.distribution.v1beta1.CommunityPoolSpendProposal"
  val SOFTWARE_UPGRADE = "/cosmos.upgrade.v1beta1.SoftwareUpgradeProposal"
  val CANCEL_SOFTWARE_UPGRADE = "/cosmos.upgrade.v1beta1.CancelSoftwareUpgradeProposal"

  object Status {
    val UNSPECIFIED = "PROPOSAL_STATUS_UNSPECIFIED"
    val DEPOSIT_PERIOD = "PROPOSAL_STATUS_DEPOSIT_PERIOD"
    val VOTING_PERIOD = "PROPOSAL_STATUS_VOTING_PERIOD"
    val PASSED = "PROPOSAL_STATUS_PASSED"
    val REJECTED = "PROPOSAL_STATUS_REJECTED"
    val FAILED = "PROPOSAL_STATUS_FAILED"
  }

}