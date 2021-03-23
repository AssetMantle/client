package queries.responses.common

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}

case class Event(`type`: String, attributes: Seq[Attribute]) {
  //Careful while using the following function, if the attributes are not be encoded throws base exception
  def decode: Event = Event(`type` = `type`, attributes = attributes.map(x => Attribute(key = utilities.Hash.base64URLDecoder(x.key), value = utilities.Hash.base64URLDecoder(x.value))))
}

object Event {
  implicit val eventReads: Reads[Event] = Json.reads[Event]
}
