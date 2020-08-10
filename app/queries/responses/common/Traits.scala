package queries.responses.common

import play.api.libs.json.{Json, Reads}

case class Traits(traitList: Seq[Trait])

object Traits {

  implicit val traitsReads: Reads[Traits] = Json.reads[Traits]

}
