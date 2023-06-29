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

  def apply(protoBytes: Array[Byte]): ListableData = ListableData(AnyListableData.parseFrom(protoBytes))
}
