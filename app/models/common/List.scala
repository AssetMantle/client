package models.common

import play.api.libs.json.{Json, OWrites, Reads}

object List {

  case class IDList(IDList: Seq[AnyID])

  object IDList {
    implicit val reads: Reads[IDList] = Json.reads[IDList]

    implicit val writes: OWrites[IDList] = Json.writes[IDList]
  }

  case class PropertyList(propertyList: Seq[AnyProperty])

  object PropertyList {
    implicit val reads: Reads[PropertyList] = Json.reads[PropertyList]

    implicit val writes: OWrites[PropertyList] = Json.writes[PropertyList]
  }
}
