package queries.Abstract

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Reads}
import queries.responses.common.FeeGrant.allowanceApply

object FeeGrant {
  abstract class FeeAllowance {}

  implicit val feeAllowanceReads: Reads[FeeAllowance] = (
    (JsPath \ "@type").read[String] and
      JsPath.read[JsObject]
    ) (allowanceApply _)
}