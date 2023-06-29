package schema.data

import com.assetmantle.schema.data.base.AnyData
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

  def isListableData: Boolean
}

object Data {

  def apply(anyData: AnyData): Data = anyData.getImplCase.getNumber match {
    case 1 => AccAddressData(anyData.getAccAddressData)
    case 2 => BooleanData(anyData.getBooleanData)
    case 3 => DecData(anyData.getDecData)
    case 4 => HeightData(anyData.getHeightData)
    case 5 => IDData(anyData.getIDData)
    case 6 => ListData(anyData.getListData)
    case 7 => NumberData(anyData.getNumberData)
    case 8 => StringData(anyData.getStringData)
    case _ => throw new IllegalArgumentException("INVALID_DATA_IMPL_CASE_NUMBER: " + anyData.getImplCase.getNumber.toString)
  }

  def apply(protoBytes: Array[Byte]): Data = Data(AnyData.parseFrom(protoBytes))

}
