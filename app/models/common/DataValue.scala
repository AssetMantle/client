package models.common

import exceptions.BaseException
import models.Abstract.DataValue
import play.api.Logger
import play.api.libs.json._

import scala.util.Try

object DataValue {

  private implicit val module: String = constants.Module.DATA

  private implicit val logger: Logger = Logger(this.getClass)

  private val DecDataTypePrecision: Int = 18

  case class StringDataValue(value: String) extends DataValue {
    val dataType: String = constants.Blockchain.DataType.STRING_DATA

    def generateHash: String = getHash(this)

    def asString: String = value

    def asDec: BigDecimal = 0.0

    def asHeight: Int = 0

    def asID: String = ""

    def toFormattedString: String = value
  }

  implicit val stringDataReads: Reads[StringDataValue] = Json.reads[StringDataValue]

  implicit val stringDataWrites: OWrites[StringDataValue] = Json.writes[StringDataValue]

  case class DecDataValue(value: BigDecimal) extends DataValue {
    val dataType: String = constants.Blockchain.DataType.DEC_DATA

    private def integerPart: String = value.bigDecimal.stripTrailingZeros.toPlainString.split("\\.")(0)

    private def decimalPart: String = {
      val integerDecimalSplit = value.bigDecimal.stripTrailingZeros.toPlainString.split("\\.")
      if (integerDecimalSplit.length == 1) "0" * DecDataTypePrecision else (integerDecimalSplit(1) + "0" * DecDataTypePrecision).take(DecDataTypePrecision)
    }

    def generateHash: String = getHash(this)

    def asString: String = Seq(integerPart, decimalPart).mkString(".")

    def asDec: BigDecimal = BigDecimal(asString)

    def asHeight: Int = 0

    def asID: String = ""

    def toFormattedString: String = utilities.NumericOperation.formatNumber(value)
  }

  implicit val decDataReads: Reads[DecDataValue] = Json.reads[DecDataValue]

  implicit val decDataWrites: OWrites[DecDataValue] = Json.writes[DecDataValue]

  case class HeightDataValue(value: Int) extends DataValue {
    val dataType: String = constants.Blockchain.DataType.HEIGHT_DATA

    def generateHash: String = getHash(this)

    def asString: String = value.toString

    def asDec: BigDecimal = 0.0

    def asHeight: Int = value

    def asID: String = ""

    def toFormattedString: String = utilities.NumericOperation.formatNumber(value)
  }

  implicit val heightDataReads: Reads[HeightDataValue] = Json.reads[HeightDataValue]

  implicit val heightDataWrites: OWrites[HeightDataValue] = Json.writes[HeightDataValue]

  case class IDDataValue(value: String) extends DataValue {
    val dataType: String = constants.Blockchain.DataType.ID_DATA

    def generateHash: String = getHash(this)

    def asString: String = value

    def asDec: BigDecimal = 0.0

    def asHeight: Int = 0

    def asID: String = value

    def toFormattedString: String = value
  }

  implicit val idDataReads: Reads[IDDataValue] = Json.reads[IDDataValue]

  implicit val idDataWrites: OWrites[IDDataValue] = Json.writes[IDDataValue]

  implicit val dataValueWrites: Writes[DataValue] = {
    case stringData: StringDataValue => Json.toJson(stringData)
    case decData: DecDataValue => Json.toJson(decData)
    case idData: IDDataValue => Json.toJson(idData)
    case heightData: HeightDataValue => Json.toJson(heightData)
    case _ => throw new BaseException(constants.Response.DATA_TYPE_NOT_FOUND)
  }

  def dataValueApply(dataType: String, value: JsObject): Serializable.Data = dataType match {
    case constants.Blockchain.DataType.STRING_DATA => Serializable.Data(dataType, utilities.JSON.convertJsonStringToObject[StringDataValue](value.toString))
    case constants.Blockchain.DataType.ID_DATA => Serializable.Data(dataType, utilities.JSON.convertJsonStringToObject[IDDataValue](value.toString))
    case constants.Blockchain.DataType.HEIGHT_DATA => Serializable.Data(dataType, utilities.JSON.convertJsonStringToObject[HeightDataValue](value.toString))
    case constants.Blockchain.DataType.DEC_DATA => Serializable.Data(dataType, utilities.JSON.convertJsonStringToObject[DecDataValue](value.toString))
  }

  def getFactTypeFromDataType(dataType: String): String = dataType match {
    case constants.Blockchain.DataType.STRING_DATA => constants.Blockchain.FactType.STRING
    case constants.Blockchain.DataType.ID_DATA => constants.Blockchain.FactType.ID
    case constants.Blockchain.DataType.HEIGHT_DATA => constants.Blockchain.FactType.HEIGHT
    case constants.Blockchain.DataType.DEC_DATA => constants.Blockchain.FactType.DEC
    case _ => ""
  }

  def getDataTypeFromFactType(factType: String): String = factType match {
    case constants.Blockchain.FactType.STRING => constants.Blockchain.DataType.STRING_DATA
    case constants.Blockchain.FactType.ID => constants.Blockchain.DataType.ID_DATA
    case constants.Blockchain.FactType.HEIGHT => constants.Blockchain.DataType.HEIGHT_DATA
    case constants.Blockchain.FactType.DEC => constants.Blockchain.DataType.DEC_DATA
    case _ => ""
  }

  def getDataValue(dataType: String, dataValue: Option[String]): DataValue = try {
    dataType match {
      case constants.Blockchain.DataType.STRING_DATA => StringDataValue(dataValue.getOrElse(""))
      case constants.Blockchain.DataType.ID_DATA => IDDataValue(dataValue.getOrElse(""))
      case constants.Blockchain.DataType.HEIGHT_DATA => HeightDataValue(dataValue.fold(constants.Blockchain.HeightDataDefaultValue)(_.toInt))
      case constants.Blockchain.DataType.DEC_DATA => DecDataValue(dataValue.fold(constants.Blockchain.SmallestDec)(BigDecimal(_)))
      case _ => throw new BaseException(constants.Response.INVALID_DATA_TYPE)
    }
  } catch {
    case baseException: BaseException => throw baseException
    case exception: Exception => logger.error(exception.getLocalizedMessage)
      throw new BaseException(constants.Response.INVALID_DATA_VALUE)
  }

  def verifyDataType(dataType: String, dataValue: Option[String]): Boolean = dataType match {
    case constants.Blockchain.DataType.STRING_DATA => true
    case constants.Blockchain.DataType.ID_DATA => true
    case constants.Blockchain.DataType.HEIGHT_DATA => Try(dataValue.fold(constants.Blockchain.HeightDataDefaultValue)(_.toInt)).isSuccess
    case constants.Blockchain.DataType.DEC_DATA => Try(dataValue.fold(constants.Blockchain.SmallestDec)(BigDecimal(_))).isSuccess
    case _ => false
  }

  def getData(dataType: String, dataValue: Option[String]): Serializable.Data = Serializable.Data(dataType = dataType, value = getDataValue(dataType = dataType, dataValue = dataValue))

  def getHash(dataValue: DataValue): String = dataValue.dataType match {
    case constants.Blockchain.DataType.STRING_DATA => if (dataValue.asString == "") "" else utilities.Hash.getHash(dataValue.asString)
    case constants.Blockchain.DataType.ID_DATA => if (dataValue.asString == "") "" else utilities.Hash.getHash(dataValue.asString)
    case constants.Blockchain.DataType.HEIGHT_DATA => if (dataValue.asHeight == constants.Blockchain.HeightDataDefaultValue) "" else utilities.Hash.getHash(dataValue.asString)
    case constants.Blockchain.DataType.DEC_DATA => if (dataValue.asDec == constants.Blockchain.ZeroDec) "" else utilities.Hash.getHash(dataValue.asString)
    case _ => ""
  }
}
