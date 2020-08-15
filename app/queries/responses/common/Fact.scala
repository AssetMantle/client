package queries.responses.common

import models.common.Serializable
import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsPath, Reads}

case class Fact(factType: String, value: queries.`abstract`.FactValue) {
  def toFact: Serializable.Fact = Serializable.Fact(factType, value.toFactValue)
}

object Fact {

  implicit val module: String = constants.Module.FACT_RESPONSES

  implicit val logger: Logger = Logger(this.getClass)

  def factApply(factType: String, value: JsObject): Fact = try {
    factType match {
      case constants.Blockchain.Fact.META_FACT => Fact(factType, utilities.JSON.convertJsonStringToObject[MetaFactValue](value.toString))
      case constants.Blockchain.Fact.FACT => Fact(factType, utilities.JSON.convertJsonStringToObject[NonMetaFactValue](value.toString))
    }
  }

  implicit val factReads: Reads[Fact] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "value").read[JsObject]
    ) (factApply _)

}
