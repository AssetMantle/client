package models.common

import play.api.libs.json._
import utilities.Date.RFC3339
import utilities.MicroNumber

object Staking {

  case class RedelegationEntry(creationHeight: Int, completionTime: RFC3339, initialBalance: MicroNumber, sharesDestination: BigDecimal) {
    def isMature(currentTime: RFC3339): Boolean = !this.completionTime.isAfter(currentTime)
  }

  implicit val redelegationEntryReads: Reads[RedelegationEntry] = Json.reads[RedelegationEntry]

  implicit val redelegationEntryWrites: OWrites[RedelegationEntry] = Json.writes[RedelegationEntry]

  case class UndelegationEntry(creationHeight: Int, completionTime: RFC3339, initialBalance: MicroNumber, balance: MicroNumber) {
    def isMature(currentTime: RFC3339): Boolean = !this.completionTime.isAfter(currentTime)
  }

  implicit val undelegationEntryReads: Reads[UndelegationEntry] = Json.reads[UndelegationEntry]

  implicit val undelegationEntryWrites: OWrites[UndelegationEntry] = Json.writes[UndelegationEntry]


}
