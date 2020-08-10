package queries.responses.common

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class Event(eventType: String, attributes: Seq[Attribute])

object Event {
  implicit val eventReads: Reads[Event] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "attributes").read[Seq[Attribute]]
    ) (Event.apply _)
}
