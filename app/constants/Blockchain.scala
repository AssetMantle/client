package constants

import utilities.MicroNumber

object Blockchain {
  val MnemonicShown = 3
  val NegotiationDefaultTime = 5000000
  val DefaultZoneFaucetTokenAmount = new MicroNumber(100000000)
  val DefaultOrganizationFaucetTokenAmount = new MicroNumber(100000)
  val DefaultTraderFaucetTokenAmount = new MicroNumber(100)
  val DefaultFaucetTokenAmount = new MicroNumber(1)
  val ZoneIssueFiatGasAmount = new MicroNumber(10)
  val ZoneIssueAssetGasAmount = new MicroNumber(10)
}