package schema.data

import com.assetmantle.schema.data.base.AnyListableData
import schema.data.base._

abstract class ListableData extends Data {

  def isListableData: Boolean = true

  def toAnyListableData: AnyListableData

}

object ListableData {

  def apply(anyListableData: AnyListableData): ListableData = anyListableData.getImplCase.getNumber match {
    case 1 => AccAddressData(anyListableData.getAccAddressData)
    case 2 => BooleanData(anyListableData.getBooleanData)
    case 3 => DecData(anyListableData.getDecData)
    case 4 => HeightData(anyListableData.getHeightData)
    case 5 => IDData(anyListableData.getIDData)
    case 6 => NumberData(anyListableData.getNumberData)
    case 7 => StringData(anyListableData.getStringData)
    case _ => throw new IllegalArgumentException("INVALID_LISTABLE_DATA_IMPL_CASE_NUMBER: " + anyListableData.getImplCase.getNumber.toString)
  }

  def apply(dataType: String, protoBytes: Array[Byte]): ListableData = dataType match {
    case schema.constants.Data.AccAddressDataTypeID.value => AccAddressData.fromProtoBytes(protoBytes)
    case schema.constants.Data.BooleanDataTypeID.value => BooleanData(protoBytes)
    case schema.constants.Data.DecDataTypeID.value => DecData(protoBytes)
    case schema.constants.Data.HeightDataTypeID.value => HeightData(protoBytes)
    case schema.constants.Data.IDDataTypeID.value => IDData(protoBytes)
    case schema.constants.Data.NumberDataTypeID.value => NumberData(protoBytes)
    case schema.constants.Data.StringDataTypeID.value => StringData(protoBytes)
    case _ => throw new IllegalArgumentException("INVALID_LISTABLE_DATA_TYPE: " + dataType)
  }

}
