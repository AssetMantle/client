package blockchain.common

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class Event(eventType: String, attributes: Seq[Attribute]) {
  //Careful while using the following function, if the attributes are not be encoded throws base exception
  def decode: Event = Event(eventType = eventType, attributes = attributes.map(x => Attribute(key = utilities.Hash.base64URLDecoder(x.key), value = x.value.fold[Option[String]](None)(y => Option(utilities.Hash.base64URLDecoder(y))))))
}

object Event {
  implicit val eventReads: Reads[Event] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "attributes").read[Seq[Attribute]]
    ) (Event.apply _)
}
