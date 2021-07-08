package controllers.requests

import play.api.libs.json.{Json, Reads}

object WallexNotification {

  case class Request(resourceId: String, resource: String, status: String)
  implicit val requestReads: Reads[Request] = Json.reads[Request]

}
