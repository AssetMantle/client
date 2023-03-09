package schema.data

import com.data.AnyData
import org.slf4j.{Logger, LoggerFactory}
import schema.data.base._
import schema.id.base.{DataID, HashID, StringID}

abstract class Data {

  def getType: StringID

  def getDataID: DataID

  def zeroValue: Data

  def generateHashID: HashID

  def toAnyData: AnyData

  def getBytes: Array[Byte]

  def getProtoBytes: Array[Byte]

  def viewString: String

  def getBondWeight: Int
}

object Data {

  private implicit val module: String = constants.Module.SCHEMA_DATA

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(anyData: AnyData): Data = anyData.getImplCase.getNumber match {
    case 1 => AccAddressData(anyData.getAccAddressData)
    case 2 => BooleanData(anyData.getBooleanData)
    case 3 => DecData(anyData.getDecData)
    case 4 => HeightData(anyData.getHeightData)
    case 5 => IDData(anyData.getIDData)
    case 6 => StringData(anyData.getStringData)
    case 7 => NumberData(anyData.getNumberData)
    case 8 => ListData(anyData.getListData)
    case _ => throw new IllegalArgumentException("INVALID_DATA_TYPE")
  }

  def apply(dataType: String, protoBytes: Array[Byte]): Data = dataType match {
    case constants.Data.AccAddressDataTypeID.value => AccAddressData.fromProtoBytes(protoBytes)
    case constants.Data.BooleanDataTypeID.value => BooleanData(protoBytes)
    case constants.Data.HeightDataTypeID.value => HeightData(protoBytes)
    case constants.Data.IDDataTypeID.value => IDData(protoBytes)
    case constants.Data.StringDataTypeID.value => StringData(protoBytes)
    case constants.Data.NumberDataTypeID.value => NumberData(protoBytes)
    case constants.Data.ListDataTypeID.value => ListData(protoBytes)
    case _ => throw new IllegalArgumentException("INVALID_DATA_TYPE")
  }

}
