package queries.responses.common

import play.api.libs.json.{Json, Reads}

case class EventWrapper(events: Seq[Event], msg_index: Option[Int])

object EventWrapper {
  implicit val eventWrapperReads: Reads[EventWrapper] = Json.reads[EventWrapper]
}
