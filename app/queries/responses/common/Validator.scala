package queries.responses.common

import models.blockchain.{Validator => BlockchainValidator}
import models.common.Serializable
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads}
import utilities.MicroNumber

object Validator {

  case class CommissionRates(rate: String, max_rate: String, max_change_rate: String) {
    def toCommissionRates: Serializable.CommissionRates = Serializable.CommissionRates(rate = BigDecimal(rate) * 100, maxRate = BigDecimal(max_rate) * 100, maxChangeRate = BigDecimal(max_change_rate) * 100)
  }

  implicit val commissionRatesReads: Reads[CommissionRates] = Json.reads[CommissionRates]

  case class Commission(commission_rates: CommissionRates, update_time: String) {
    def toCommission: Serializable.Commission = Serializable.Commission(commissionRates = commission_rates.toCommissionRates, updateTime = update_time)
  }

  implicit val commissionReads: Reads[Commission] = Json.reads[Commission]

  case class Description(moniker: Option[String], identity: Option[String], website: Option[String], security_contact: Option[String], details: Option[String]) {
    def toValidatorDescription: Serializable.ValidatorDescription = Serializable.ValidatorDescription(moniker = moniker, identity = identity, website = website, securityContact = security_contact, details = details)
  }

  implicit val descriptionReads: Reads[Description] = Json.reads[Description]

  case class Result(operator_address: String, consensus_pubkey: String, jailed: Option[Boolean], status: Int, tokens: MicroNumber, delegator_shares: BigDecimal, description: Description, unbonding_height: Option[Int], unbonding_time: Option[String], commission: Commission, min_self_delegation: String) {
    def toValidator: BlockchainValidator = BlockchainValidator(operatorAddress = operator_address,
      hexAddress = utilities.Bech32.convertConsensusPubKeyToHexAddress(consensus_pubkey),
      consensusPublicKey = consensus_pubkey,
      jailed = jailed.getOrElse(false),
      status = status,
      tokens = tokens,
      delegatorShares = delegator_shares,
      description = description.toValidatorDescription,
      unbondingHeight = unbonding_height,
      unbondingTime = unbonding_time,
      commission = commission.toCommission,
      minimumSelfDelegation = min_self_delegation)
  }

  def resultApply(operator_address: String, consensus_pubkey: String, jailed: Option[Boolean], status: Int, tokens: String, delegator_shares: BigDecimal, description: Description, unbonding_height: Option[String], unbonding_time: Option[String], commission: Commission, min_self_delegation: String): Result = Result(operator_address, consensus_pubkey, jailed, status, new MicroNumber(BigInt(tokens)), delegator_shares, description, unbonding_height.flatMap(x => Option(x.toInt)), unbonding_time, commission, min_self_delegation)

  implicit val resultReads: Reads[Result] = (
    (JsPath \ "operator_address").read[String] and
      (JsPath \ "consensus_pubkey").read[String] and
      (JsPath \ "jailed").readNullable[Boolean] and
      (JsPath \ "status").read[Int] and
      (JsPath \ "tokens").read[String] and
      (JsPath \ "delegator_shares").read[BigDecimal] and
      (JsPath \ "description").read[Description] and
      (JsPath \ "unbonding_height").readNullable[String] and
      (JsPath \ "unbonding_time").readNullable[String] and
      (JsPath \ "commission").read[Commission] and
      (JsPath \ "min_self_delegation").read[String]
    ) (resultApply _)

}
