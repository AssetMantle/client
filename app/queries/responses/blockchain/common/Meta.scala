package queries.responses.blockchain.common

import play.api.libs.json.{Json, Reads}
import queries.responses.blockchain.common.Data._
import queries.responses.blockchain.common.ID._

object Meta {

  case class Key(data_i_d: DataID)

  implicit val keyReads: Reads[Key] = Json.reads[Key]

  case class Mappable(data: AnyData)

  implicit val mappableReads: Reads[Mappable] = Json.reads[Mappable]

  case class Record(key: Key, mappable: Mappable)

  implicit val recordReads: Reads[Record] = Json.reads[Record]

  case class Module(records: Seq[Record], parameter_list: ParameterList)

  implicit val metaReads: Reads[Module] = Json.reads[Module]

}
