package queries.Abstract

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Reads}
import queries.responses.common.Authz.authorizationApply

object Authz {

  abstract class Authorization {}

  implicit val authorizationReads: Reads[Authorization] = (
    (JsPath \ "@type").read[String] and
      JsPath.read[JsObject]
    ) (authorizationApply _)

}
