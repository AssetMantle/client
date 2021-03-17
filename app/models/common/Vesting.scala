package models.common

import models.common.Serializable.Coin
import play.api.libs.json.{JsObject, JsPath, Json, OWrites, Reads, Writes}

object Vesting {

  case class VestingPeriod(length: String, amount: Seq[Coin])

  implicit val vestingPeriodReads: Reads[VestingPeriod] = Json.reads[VestingPeriod]

  implicit val vestingPeriodWrites: OWrites[VestingPeriod] = Json.writes[VestingPeriod]

  case class VestingParameters(originalVesting: Seq[Coin], delegatedFree: Seq[Coin], delegatedVesting: Seq[Coin], endTime: String, startTime: Option[String], vestingPeriods: Seq[VestingPeriod])

  implicit val vestingParametersReads: Reads[VestingParameters] = Json.reads[VestingParameters]

  implicit val vestingParametersWrites: OWrites[VestingParameters] = Json.writes[VestingParameters]

  //  case class BaseAccount(address: String, publicKey: Option[PublicKey], accountNumber: String, sequenceNumber: String) extends Account {
  //    val accountType: String = constants.Blockchain.Account.BASE
  //    val vestingParameters: Option[VestingParameters] = None
  //  }
  //
  //  implicit val baseAccountReads: Reads[BaseAccount] = Json.reads[BaseAccount]
  //
  //  implicit val baseAccountWrites: OWrites[BaseAccount] = Json.writes[BaseAccount]
  //
  //  case class ModuleAccount(address: String, publicKey: Option[PublicKey], accountNumber: String, sequenceNumber: String, name: String, permissions: Seq[String]) extends Account {
  //    val accountType: String = constants.Blockchain.Account.MODULE
  //    val vestingParameters: Option[VestingParameters] = None
  //  }
  //
  //  implicit val moduleAccountReads: Reads[ModuleAccount] = Json.reads[ModuleAccount]
  //
  //  implicit val moduleAccountWrites: OWrites[ModuleAccount] = Json.writes[ModuleAccount]
  //
  //  case class BaseVestingAccount(baseAccount: BaseAccount, originalVesting: Seq[Coin], delegatedFree: Seq[Coin], delegatedVesting: Seq[Coin], endTime: String)
  //
  //  implicit val baseVestingAccountReads: Reads[BaseVestingAccount] = Json.reads[BaseVestingAccount]
  //
  //  implicit val baseVestingAccountWrites: OWrites[BaseVestingAccount] = Json.writes[BaseVestingAccount]
  //
  //  case class DelayedVestingAccount(baseVestingAccount: BaseVestingAccount) extends Account {
  //    val accountType: String = constants.Blockchain.Account.DELAYED_VESTING
  //    val address: String = baseVestingAccount.baseAccount.address
  //    val publicKey: Option[PublicKey] = baseVestingAccount.baseAccount.publicKey
  //    val accountNumber: String = baseVestingAccount.baseAccount.accountNumber
  //    val sequenceNumber: String = baseVestingAccount.baseAccount.sequenceNumber
  //    val vestingParameters: Option[VestingParameters] = Option(VestingParameters(originalVesting = baseVestingAccount.originalVesting, delegatedFree = baseVestingAccount.delegatedFree, delegatedVesting = baseVestingAccount.delegatedVesting, endTime = baseVestingAccount.endTime, startTime = None, vestingPeriods = Seq()))
  //  }
  //
  //  implicit val delayedVestingAccountReads: Reads[DelayedVestingAccount] = Json.reads[DelayedVestingAccount]
  //
  //  implicit val delayedVestingAccountWrites: OWrites[DelayedVestingAccount] = Json.writes[DelayedVestingAccount]
  //
  //  case class ContinuousVestingAccount(baseVestingAccount: BaseVestingAccount, startTime: String) extends Account {
  //    val accountType: String = constants.Blockchain.Account.CONTINUOUS_VESTING
  //    val address: String = baseVestingAccount.baseAccount.address
  //    val publicKey: Option[PublicKey] = baseVestingAccount.baseAccount.publicKey
  //    val accountNumber: String = baseVestingAccount.baseAccount.accountNumber
  //    val sequenceNumber: String = baseVestingAccount.baseAccount.sequenceNumber
  //    val vestingParameters: Option[VestingParameters] = Option(VestingParameters(originalVesting = baseVestingAccount.originalVesting, delegatedFree = baseVestingAccount.delegatedFree, delegatedVesting = baseVestingAccount.delegatedVesting, endTime = baseVestingAccount.endTime, startTime = Option(startTime), vestingPeriods = Seq()))
  //  }
  //
  //  implicit val continuousVestingAccountReads: Reads[ContinuousVestingAccount] = Json.reads[ContinuousVestingAccount]
  //
  //  implicit val continuousVestingAccountWrites: OWrites[ContinuousVestingAccount] = Json.writes[ContinuousVestingAccount]
  //
  //  case class PeriodicVestingAccount(baseVestingAccount: BaseVestingAccount, startTime: String, vestingPeriods: Seq[VestingPeriod]) extends Account {
  //    val accountType: String = constants.Blockchain.Account.PERIODIC_VESTING
  //    val address: String = baseVestingAccount.baseAccount.address
  //    val publicKey: Option[PublicKey] = baseVestingAccount.baseAccount.publicKey
  //    val accountNumber: String = baseVestingAccount.baseAccount.accountNumber
  //    val sequenceNumber: String = baseVestingAccount.baseAccount.sequenceNumber
  //    val vestingParameters: Option[VestingParameters] = Option(VestingParameters(originalVesting = baseVestingAccount.originalVesting, delegatedFree = baseVestingAccount.delegatedFree, delegatedVesting = baseVestingAccount.delegatedVesting, endTime = baseVestingAccount.endTime, startTime = Option(startTime), vestingPeriods = vestingPeriods))
  //  }
  //
  //  implicit val periodicVestingAccountReads: Reads[PeriodicVestingAccount] = Json.reads[PeriodicVestingAccount]
  //
  //  implicit val periodicVestingAccountWrites: OWrites[PeriodicVestingAccount] = Json.writes[PeriodicVestingAccount]
  //
  //  def accountApply(accountType: String, value: JsObject): Account = accountType match {
  //    case constants.Blockchain.Account.BASE => utilities.JSON.convertJsonStringToObject[BaseAccount](value.toString)
  //    case constants.Blockchain.Account.MODULE => utilities.JSON.convertJsonStringToObject[ModuleAccount](value.toString)
  //    case constants.Blockchain.Account.DELAYED_VESTING => utilities.JSON.convertJsonStringToObject[DelayedVestingAccount](value.toString)
  //    case constants.Blockchain.Account.CONTINUOUS_VESTING => utilities.JSON.convertJsonStringToObject[ContinuousVestingAccount](value.toString)
  //    case constants.Blockchain.Account.PERIODIC_VESTING => utilities.JSON.convertJsonStringToObject[PeriodicVestingAccount](value.toString)
  //    case _ => throw new BaseException(constants.Response.ACCOUNT_TYPE_NOT_FOUND)
  //  }
  //
  //  implicit val accountReads: Reads[Account] = (
  //    (JsPath \ "accountType").read[String] and
  //      JsPath.read[JsObject]
  //    ) (accountApply _)
  //
  //  implicit val accountWrites: Writes[Account] = {
  //    case baseAccount: BaseAccount => Json.toJson(baseAccount)(Json.writes[BaseAccount])
  //    case moduleAccount: ModuleAccount => Json.toJson(moduleAccount)(Json.writes[ModuleAccount])
  //    case delayedVestingAccount: DelayedVestingAccount => Json.toJson(delayedVestingAccount)(Json.writes[DelayedVestingAccount])
  //    case continuousVestingAccount: ContinuousVestingAccount => Json.toJson(continuousVestingAccount)(Json.writes[ContinuousVestingAccount])
  //    case periodicVestingAccount: PeriodicVestingAccount => Json.toJson(periodicVestingAccount)(Json.writes[PeriodicVestingAccount])
  //    case _ => throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION)
  //  }

}
