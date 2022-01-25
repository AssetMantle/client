package queries.responses.common

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}
import utilities.Date.RFC3339

case class Header(chain_id: String, height: Int, time: RFC3339, data_hash: String, evidence_hash: String, validators_hash: String, proposer_address: String)

object Header {

  def apply2(chain_id: String, height: String, time: String, data_hash: String, evidence_hash: String, validators_hash: String, proposer_address: String): Header = new Header(chain_id, height.toInt, RFC3339(time), data_hash, evidence_hash, validators_hash, proposer_address)

  implicit val headerReads: Reads[Header] = (
    (JsPath \ "chain_id").read[String] and
      (JsPath \ "height").read[String] and
      (JsPath \ "time").read[String] and
      (JsPath \ "data_hash").read[String] and
      (JsPath \ "evidence_hash").read[String] and
      (JsPath \ "validators_hash").read[String] and
      (JsPath \ "proposer_address").read[String]
    ) (apply2 _)
}