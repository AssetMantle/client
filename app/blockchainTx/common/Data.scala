package blockchainTx.common

import models.common.DataValue._
import models.common.Serializable
import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

case class Data(dataType: String, value: Data.Value) {
  def toData: Serializable.Data = dataType match {
    case constants.Blockchain.DataType.STRING_DATA => Serializable.Data(constants.Blockchain.DataType.STRING_DATA, StringDataValue(value.value))
    case constants.Blockchain.DataType.ID_DATA => Serializable.Data(constants.Blockchain.DataType.ID_DATA, IDDataValue(value.value))
    case constants.Blockchain.DataType.HEIGHT_DATA => Serializable.Data(constants.Blockchain.DataType.HEIGHT_DATA, HeightDataValue(value.value.toInt))
    case constants.Blockchain.DataType.DEC_DATA => Serializable.Data(constants.Blockchain.DataType.DEC_DATA, DecDataValue(BigDecimal(value.value)))
  }
}

object Data {

  private implicit val module: String = constants.Module.QUERIES_RESPONSE_DATA

  private implicit val logger: Logger = Logger(this.getClass)

  case class Value(value: String)

  implicit val valueReads: Reads[Value] = Json.reads[Value]
  implicit val valueWrites: OWrites[Value] = Json.writes[Value]

  case class StringValue(value: String)

  implicit val stringValueReads: Reads[StringValue] = Json.reads[StringValue]
  implicit val stringValueWrites: OWrites[StringValue] = Json.writes[StringValue]

  case class HeightValue(value: Height)

  implicit val heightValueReads: Reads[HeightValue] = Json.reads[HeightValue]
  implicit val heightValueWrites: OWrites[HeightValue] = Json.writes[HeightValue]

  case class IDValue(value: ID)

  implicit val idValueReads: Reads[IDValue] = Json.reads[IDValue]
  implicit val idValueWrites: OWrites[IDValue] = Json.writes[IDValue]

  case class DecValue(value: BigDecimal)

  implicit val decValueReads: Reads[DecValue] = Json.reads[DecValue]
  implicit val decValueWrites: OWrites[DecValue] = Json.writes[DecValue]

  def dataApply(dataType: String, value: JsObject): Data = dataType match {
    case constants.Blockchain.DataType.STRING_DATA => Data(constants.Blockchain.DataType.STRING_DATA, Value(utilities.JSON.convertJsonStringToObject[StringValue](value.toString).value))
    case constants.Blockchain.DataType.ID_DATA => Data(constants.Blockchain.DataType.ID_DATA, Value(utilities.JSON.convertJsonStringToObject[IDValue](value.toString).value.value.idString))
    case constants.Blockchain.DataType.HEIGHT_DATA => Data(constants.Blockchain.DataType.HEIGHT_DATA, Value(utilities.JSON.convertJsonStringToObject[HeightValue](value.toString).value.value.height))
    case constants.Blockchain.DataType.DEC_DATA => Data(constants.Blockchain.DataType.DEC_DATA, Value(utilities.JSON.convertJsonStringToObject[DecValue](value.toString).value.toString))
  }

  implicit val dataReads: Reads[Data] = (
    (JsPath \ "type").read[String] and
      (JsPath \ "value").read[JsObject]
    ) (dataApply _)

  implicit val dataWrites: Writes[Data] = (data: Data) => Json.obj(
    "type" -> data.dataType,
    "value" -> data.value.value
  )

}


