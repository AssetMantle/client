package queries.responses.common

import models.common.DataValue._
import models.common.Serializable
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads}

case class Data(dataType: String, value: Data.Value) {
  def toData: Serializable.Data = dataType match {
    case constants.Blockchain.Data.STRING_DATA => Serializable.Data(constants.Blockchain.Data.STRING_DATA, StringDataValue(value.value))
    case constants.Blockchain.Data.ID_DATA => Serializable.Data(constants.Blockchain.Data.ID_DATA, IDDataValue(value.value))
    case constants.Blockchain.Data.HEIGHT_DATA => Serializable.Data(constants.Blockchain.Data.HEIGHT_DATA, HeightDataValue(value.value.toInt))
    case constants.Blockchain.Data.DEC_DATA => Serializable.Data(constants.Blockchain.Data.DEC_DATA, DecDataValue(BigDecimal(value.value)))
  }
}

object Data {

  case class Value(value: String)

  implicit val valueReads: Reads[Value] = Json.reads[Value]

  implicit val dataReads: Reads[Data] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "value").read[Value]
    ) (Data.apply _)

}


