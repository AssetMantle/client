package queries.responses.common

import play.api.libs.json.{Json, Reads}

case class Event(`type`: String, attributes: Seq[Attribute]) {
  //Careful while using the following function, if the attributes are not be encoded throws base exception
  def decode: Event = Event(`type` = `type`, attributes = attributes.map(x => Attribute(key = utilities.Secrets.base64URLDecoder(x.key), value = x.value.fold[Option[String]](None)(y => Option(utilities.Secrets.base64URLDecoder(y))))))
}

object Event {
  implicit val eventReads: Reads[Event] = Json.reads[Event]
}
