package constants

import org.bitcoinj.crypto.ChildNumber
import play.api.Configuration

object Blockchain {
  val AccountPrefix: String = AppConfig.configuration.get[String]("blockchain.account.prefix")
  val ValidatorPrefix: String = AccountPrefix + "valoper"
  val ValidatorConsensusPublicPrefix: String = AccountPrefix + "valconspub"
  val AccountRegexString: String = AccountPrefix + RegularExpression.ADDRESS_SUFFIX.regex
  val ValidatorRegexString: String = ValidatorPrefix + RegularExpression.ADDRESS_SUFFIX.regex
  val IBCDenoms: Seq[AppConfig.IBCDenom] = AppConfig.configuration.get[Seq[Configuration]]("blockchain.ibcDenoms.ibcDenomList").map { ibcDenoms =>
    constants.AppConfig.IBCDenom(hash = ibcDenoms.get[String]("hash"), name = ibcDenoms.get[String]("name"))
  }
  val ChainID: String = AppConfig.configuration.get[String]("blockchain.chainID")
  val StakingDenom: String = AppConfig.configuration.get[String]("blockchain.stakingDenom")
  val CoinType = 118
  val DefaultHDPath: Seq[ChildNumber] = Seq(
    new ChildNumber(44, true),
    new ChildNumber(CoinType, true),
    new ChildNumber(0, true),
    new ChildNumber(0, false),
    new ChildNumber(0, false)
  )

  val RPCEndPoint: String = AppConfig.configuration.get[String]("blockchain.rpcURL")
  val RestEndPoint: String = AppConfig.configuration.get[String]("blockchain.restURL")
}