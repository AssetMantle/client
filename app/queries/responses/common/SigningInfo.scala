package queries.responses.common

import play.api.libs.json.{Json, Reads}
import utilities.Date.RFC3339

object SigningInfo {

  case class Result(address: String, start_height: String, index_offset: String, jailed_until: RFC3339, tombstoned: Boolean, missed_blocks_counter: String)

  implicit val resultReads: Reads[Result] = Json.reads[Result]

}
