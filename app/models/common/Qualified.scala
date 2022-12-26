package models.common

import models.common.List.PropertyList
import play.api.libs.json.{Json, OWrites, Reads}

object Qualified {

  case class Immutables(propertyList: PropertyList)

  object Immutables {
    implicit val reads: Reads[Immutables] = Json.reads[Immutables]

    implicit val writes: OWrites[Immutables] = Json.writes[Immutables]
  }


  case class Mutables(propertyList: PropertyList)

  object Mutables {
    implicit val reads: Reads[Mutables] = Json.reads[Mutables]

    implicit val writes: OWrites[Mutables] = Json.writes[Mutables]
  }
}
