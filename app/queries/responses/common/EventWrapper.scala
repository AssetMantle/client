package queries.responses.common

import play.api.libs.json.{Json, Reads}

case class EventWrapper(events: Seq[Event])

object EventWrapper {
  implicit val eventWrapperReads: Reads[EventWrapper] = Json.reads[EventWrapper]
}
