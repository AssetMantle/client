package constants

import utilities.MicroNumber

object Blockchain {
  val MnemonicShown = 3
  val NegotiationDefaultTime = 5000000
  val DefaultZoneFaucetAmount = new MicroNumber(100000000)
  val DefaultOrganizationFaucetAmount = new MicroNumber(100000)
  val DefaultTraderFaucetAmount = new MicroNumber(100)
  val DefaultFaucetAmount = new MicroNumber(1)
  val ZoneIssueFiatGas = new MicroNumber(10)
  val ZoneIssueAssetGas = new MicroNumber(10)
}