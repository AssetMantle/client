package models.common

import play.api.libs.json._
import utilities.Date.RFC3339

object Validator {

  case class Description(moniker: String, identity: String, website: String, securityContact: String, details: String)

  implicit val descriptionWrites: OWrites[Description] = Json.writes[Description]

  implicit val descriptionReads: Reads[Description] = Json.reads[Description]

  case class CommissionRates(rate: BigDecimal, maxRate: BigDecimal, maxChangeRate: BigDecimal)

  implicit val commissionRatesWrites: OWrites[CommissionRates] = Json.writes[CommissionRates]

  implicit val commissionRatesReads: Reads[CommissionRates] = Json.reads[CommissionRates]

  case class Commission(commissionRates: CommissionRates, updateTime: RFC3339)

  implicit val commissionWrites: OWrites[Commission] = Json.writes[Commission]

  implicit val commissionReads: Reads[Commission] = Json.reads[Commission]

}
