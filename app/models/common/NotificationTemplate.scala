package models.common

import play.api.libs.json._

case class NotificationTemplate(template: String, parameters: Seq[String])

object NotificationTemplate {
  implicit val notificationTemplateReads: Reads[NotificationTemplate] = Json.reads[NotificationTemplate]

  implicit val notificationTemplateWrites: OWrites[NotificationTemplate] = Json.writes[NotificationTemplate]
}
