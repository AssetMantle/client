package queries.responses.blockchain.common

import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.ID._

object Classification {

  case class Key(classification_i_d: ClassificationID)

  implicit val keyReads: Reads[Key] = Json.reads[Key]

  case class Mappable(classification: Document)

  implicit val mappableReads: Reads[Mappable] = Json.reads[Mappable]

  case class Record(key: Key, mappable: Mappable)

  implicit val recordReads: Reads[Record] = Json.reads[Record]

  case class Module(records: Seq[Record], parameter_list: ParameterList)

  implicit val metaReads: Reads[Module] = Json.reads[Module]
}