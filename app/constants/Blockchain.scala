package constants

import utilities.MicroNumber

object Blockchain {
  val MnemonicShown = 3
  val NegotiationDefaultTime = 5000000
  val DefaultZoneFaucetTokenAmount = new MicroNumber(100000000.0)
  val DefaultOrganizationFaucetTokenAmount = new MicroNumber(100000.0)
  val DefaultTraderFaucetTokenAmount = new MicroNumber(100.0)
  val DefaultFaucetTokenAmount = new MicroNumber(1.0)
  val ZoneIssueFiatGasAmount = new MicroNumber(10.0)
  val ZoneIssueAssetGasAmount = new MicroNumber(10.0)
}