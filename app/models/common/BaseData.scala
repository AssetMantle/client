package models.common

import constants.Blockchain.DataType
import exceptions.BaseException
import models.Abstract.AnyDataImpl
import play.api.Logger
import play.api.libs.json._

object BaseData {

  private implicit val module: String = constants.Module.DATA

  private implicit val logger: Logger = Logger(this.getClass)

  private val DecDataTypePrecision: Int = 18

  case class StringData(value: String) extends AnyDataImpl {
    def dataType: DataType = constants.Blockchain.DataType.STRING_DATA

    def generateHash: String = getHash(this)

    def asString: String = value

    def asDec: BigDecimal = 0.0

    def asHeight: Int = 0

    def asID: String = ""

    def toFormattedString: String = value
  }

  implicit val stringDataReads: Reads[StringData] = Json.reads[StringData]

  implicit val stringDataWrites: OWrites[StringData] = Json.writes[StringData]

  case class DecData(value: BigDecimal) extends AnyDataImpl {
    def dataType: DataType = constants.Blockchain.DataType.DEC_DATA

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

  implicit val decDataReads: Reads[DecData] = Json.reads[DecData]

  implicit val decDataWrites: OWrites[DecData] = Json.writes[DecData]

  case class HeightData(value: Int) extends AnyDataImpl {
    def dataType: DataType = constants.Blockchain.DataType.HEIGHT_DATA

    def generateHash: String = getHash(this)

    def asString: String = value.toString

    def asDec: BigDecimal = 0.0

    def asHeight: Int = value

    def asID: String = ""

    def toFormattedString: String = utilities.NumericOperation.formatNumber(value)
  }

  implicit val heightDataReads: Reads[HeightData] = Json.reads[HeightData]

  implicit val heightDataWrites: OWrites[HeightData] = Json.writes[HeightData]

  case class IDData(value: String) extends AnyDataImpl {
    def dataType: DataType = constants.Blockchain.DataType.ID_DATA

    def generateHash: String = getHash(this)

    def asString: String = value

    def asDec: BigDecimal = 0.0

    def asHeight: Int = 0

    def asID: String = value

    def toFormattedString: String = value
  }

  implicit val idDataReads: Reads[IDData] = Json.reads[IDData]

  implicit val idDataWrites: OWrites[IDData] = Json.writes[IDData]

  implicit val dataValueWrites: Writes[AnyDataImpl] = {
    case stringData: StringData => Json.toJson(stringData)
    case decData: DecData => Json.toJson(decData)
    case idData: IDData => Json.toJson(idData)
    case heightData: HeightData => Json.toJson(heightData)
    case _ => throw new BaseException(constants.Response.DATA_TYPE_NOT_FOUND)
  }

  def getFactTypeFromDataType(dataType: String): String = dataType match {
    case constants.Blockchain.DataType.STRING_DATA.blockchainType => constants.Blockchain.FactType.STRING
    case constants.Blockchain.DataType.ID_DATA.blockchainType => constants.Blockchain.FactType.ID
    case constants.Blockchain.DataType.HEIGHT_DATA.blockchainType => constants.Blockchain.FactType.HEIGHT
    case constants.Blockchain.DataType.DEC_DATA.blockchainType => constants.Blockchain.FactType.DEC
    case _ => ""
  }

  def getDataTypeFromFactType(factType: String): DataType = factType match {
    case constants.Blockchain.FactType.STRING => constants.Blockchain.DataType.STRING_DATA
    case constants.Blockchain.FactType.ID => constants.Blockchain.DataType.ID_DATA
    case constants.Blockchain.FactType.HEIGHT => constants.Blockchain.DataType.HEIGHT_DATA
    case constants.Blockchain.FactType.DEC => constants.Blockchain.DataType.DEC_DATA
    case _ => constants.Blockchain.DataType("", "")
  }

  def getDataValue(dataType: String, dataValue: Option[String]): AnyDataImpl = try {
    dataType match {
      case constants.Blockchain.DataType.STRING_DATA.blockchainType => StringData(dataValue.getOrElse(""))
      case constants.Blockchain.DataType.ID_DATA.blockchainType => IDData(dataValue.getOrElse(""))
      case constants.Blockchain.DataType.HEIGHT_DATA.blockchainType => HeightData(dataValue.fold(constants.Blockchain.HeightDataDefaultValue)(_.toInt))
      case constants.Blockchain.DataType.DEC_DATA.blockchainType => DecData(dataValue.fold(constants.Blockchain.SmallestDec)(BigDecimal(_)))
      case _ => throw new BaseException(constants.Response.INVALID_DATA_TYPE)
    }
  } catch {
    case baseException: BaseException => throw baseException
    case exception: Exception => logger.error(exception.getLocalizedMessage)
      throw new BaseException(constants.Response.INVALID_DATA_VALUE)
  }

  def getHash(dataValue: AnyDataImpl): String = dataValue.dataType match {
    case constants.Blockchain.DataType.STRING_DATA => if (dataValue.asString == "") "" else utilities.Secrets.getBlockchainHash(dataValue.asString)
    case constants.Blockchain.DataType.ID_DATA => if (dataValue.asString == "") "" else utilities.Secrets.getBlockchainHash(dataValue.asString)
    case constants.Blockchain.DataType.HEIGHT_DATA => if (dataValue.asHeight == constants.Blockchain.HeightDataDefaultValue) "" else utilities.Secrets.getBlockchainHash(dataValue.asString)
    case constants.Blockchain.DataType.DEC_DATA => if (dataValue.asDec == constants.Blockchain.ZeroDec) "" else utilities.Secrets.getBlockchainHash(dataValue.asString)
    case _ => ""
  }
}
