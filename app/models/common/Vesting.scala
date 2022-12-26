package models.common

import play.api.libs.json._

object Vesting {

  case class VestingPeriod(length: String, amount: Seq[Coin])

  implicit val vestingPeriodReads: Reads[VestingPeriod] = Json.reads[VestingPeriod]

  implicit val vestingPeriodWrites: OWrites[VestingPeriod] = Json.writes[VestingPeriod]

  case class VestingParameters(originalVesting: Seq[Coin], delegatedFree: Seq[Coin], delegatedVesting: Seq[Coin], endTime: String, startTime: Option[String], vestingPeriods: Seq[VestingPeriod])

  implicit val vestingParametersReads: Reads[VestingParameters] = Json.reads[VestingParameters]

  implicit val vestingParametersWrites: OWrites[VestingParameters] = Json.writes[VestingParameters]


}
