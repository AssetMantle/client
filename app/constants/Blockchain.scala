package constants

import utilities.MicroLong

object Blockchain {
  val MnemonicShown = 3
  val NegotiationDefaultTime = 5000000
  val DefaultZoneFaucetTokenAmount = new MicroLong(100000000.0)
  val DefaultOrganizationFaucetTokenAmount = new MicroLong(100000.0)
  val DefaultTraderFaucetTokenAmount = new MicroLong(100.0)
  val DefaultFaucetTokenAmount = new MicroLong(1.0)
  val ZoneIssueFiatGasAmount = new MicroLong(10.0)
  val ZoneIssueAssetGasAmount = new MicroLong(10.0)
}