package controllers.requests

import play.api.libs.json.{Json, OWrites, Reads}

class WallexNotification {

  private implicit val requestWrites: OWrites[Request] = Json.writes[Request]
  implicit val requestReads: Reads[Request] = Json.reads[Request]
  case class Request(resourceId: String,resource: String,status: String)

}
