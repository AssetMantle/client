package models.common

import exceptions.BaseException
import models.Abstract.DataValue
import play.api.Logger
import play.api.libs.json.{JsObject, Json, OWrites, Reads, Writes}

object DataValue {

  private implicit val module: String = constants.Module.DATA

  private implicit val logger: Logger = Logger(this.getClass)

  case class StringDataValue(value: String) extends DataValue {
    val dataType: String = constants.Blockchain.DataType.STRING_DATA

    def GenerateHash: String = utilities.Hash.getHash(value)

    def AsString: String = value

    def AsDec: BigDecimal = 0.0

    def AsHeight: Int = 0

    def AsID: String = ""
  }

  implicit val stringDataReads: Reads[StringDataValue] = Json.reads[StringDataValue]

  implicit val stringDataWrites: OWrites[StringDataValue] = Json.writes[StringDataValue]

  case class DecDataValue(value: BigDecimal) extends DataValue {
    val dataType: String = constants.Blockchain.DataType.DEC_DATA

    def GenerateHash: String = utilities.Hash.getHash(value.toString)

    def AsString: String = value.toString

    def AsDec: BigDecimal = value

    def AsHeight: Int = 0

    def AsID: String = ""
  }

  implicit val decDataReads: Reads[DecDataValue] = Json.reads[DecDataValue]

  implicit val decDataWrites: OWrites[DecDataValue] = Json.writes[DecDataValue]

  case class HeightDataValue(value: Int) extends DataValue {
    val dataType: String = constants.Blockchain.DataType.HEIGHT_DATA

    def GenerateHash: String = utilities.Hash.getHash(value.toString)

    def AsString: String = value.toString

    def AsDec: BigDecimal = 0.0

    def AsHeight: Int = value

    def AsID: String = ""
  }

  implicit val heightDataReads: Reads[HeightDataValue] = Json.reads[HeightDataValue]

  implicit val heightDataWrites: OWrites[HeightDataValue] = Json.writes[HeightDataValue]

  case class IDDataValue(value: String) extends DataValue {
    val dataType: String = constants.Blockchain.DataType.ID_DATA

    def GenerateHash: String = utilities.Hash.getHash(value)

    def AsString: String = value

    def AsDec: BigDecimal = 0.0

    def AsHeight: Int = 0

    def AsID: String = value
  }

  implicit val idDataReads: Reads[IDDataValue] = Json.reads[IDDataValue]

  implicit val idDataWrites: OWrites[IDDataValue] = Json.writes[IDDataValue]

  implicit val dataValueWrites: Writes[DataValue] = {
    case stringData: StringDataValue => Json.toJson(stringData)(Json.writes[StringDataValue])
    case decData: DecDataValue => Json.toJson(decData)(Json.writes[DecDataValue])
    case idData: IDDataValue => Json.toJson(idData)(Json.writes[IDDataValue])
    case heightData: HeightDataValue => Json.toJson(heightData)(Json.writes[HeightDataValue])
    case _ => throw new BaseException(constants.Response.DATA_TYPE_NOT_FOUND)
  }

  def dataValueApply(dataType: String, value: JsObject): Serializable.Data =  dataType match {
    case constants.Blockchain.DataType.STRING_DATA => Serializable.Data(dataType, utilities.JSON.convertJsonStringToObject[StringDataValue](value.toString))
    case constants.Blockchain.DataType.ID_DATA =>Serializable.Data(dataType, utilities.JSON.convertJsonStringToObject[IDDataValue](value.toString))
    case constants.Blockchain.DataType.HEIGHT_DATA =>Serializable.Data(dataType, utilities.JSON.convertJsonStringToObject[HeightDataValue](value.toString))
    case constants.Blockchain.DataType.DEC_DATA =>Serializable.Data(dataType, utilities.JSON.convertJsonStringToObject[DecDataValue](value.toString))
  }

}
