package queries.Abstract

import utilities.MicroNumber

abstract class TendermintEvidence {
  val height: Int
  val timeStamp: String
  val validatorHexAddress: String
  val validatorPower: MicroNumber
  val totalVotingPower: MicroNumber
}
