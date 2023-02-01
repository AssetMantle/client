package schema.data

import com.data.AnyData
import org.slf4j.{Logger, LoggerFactory}
import schema.data.base._
import schema.id.base.{DataID, HashID, StringID}

abstract class Data {

  def getType: StringID

  def getID: DataID

  def zeroValue: Data

  def generateHashID: HashID

  def toAnyData: AnyData

  def getBytes: Array[Byte]

  def getProtoBytes: Array[Byte]

  def viewString: String
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
    case 7 => ListData(anyData.getListData)
    case _ => throw new IllegalArgumentException("INVALID_DATA_TYPE")
  }

  def apply(dataType: String, protoBytes: Array[Byte]): Data = dataType match {
    case constants.DataTypeID.AccAddressDataTypeID.value => AccAddressData.fromProtoBytes(protoBytes)
    case constants.DataTypeID.BooleanDataTypeID.value => BooleanData(protoBytes)
    case constants.DataTypeID.HeightDataTypeID.value => HeightData(protoBytes)
    case constants.DataTypeID.IDDataTypeID.value => IDData(protoBytes)
    case constants.DataTypeID.StringDataTypeID.value => StringData(protoBytes)
    case constants.DataTypeID.ListDataTypeID.value => ListData(protoBytes)
    case _ => throw new IllegalArgumentException("INVALID_DATA_TYPE")
  }

}
