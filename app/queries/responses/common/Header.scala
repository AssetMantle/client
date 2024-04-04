package queries.responses.common

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}
import utilities.Date.RFC3339

case class Header(height: Int, time: RFC3339, proposer_address: String)

object Header {

  def apply2(height: String, time: RFC3339, proposer_address: String): Header = new Header(height.toInt, time, proposer_address)

  implicit val headerReads: Reads[Header] = (
    (JsPath \ "height").read[String] and
      (JsPath \ "time").read[RFC3339] and
      (JsPath \ "proposer_address").read[String]
    ) (apply2 _)
}