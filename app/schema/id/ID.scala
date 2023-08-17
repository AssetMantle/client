package schema.id

import com.assetmantle.schema.ids.base.AnyID
import schema.id.base._

abstract class ID {

  def getBytes: Array[Byte]

  def getType: StringID

  def asString: String

  def toAnyID: AnyID

  def getProtoBytes: Array[Byte]

  def compare(id: ID): Int

}

object ID {

  def apply(anyID: AnyID): ID = anyID.getImplCase.getNumber match {
    case 1 => AssetID(anyID.getAssetID)
    case 2 => ClassificationID(anyID.getClassificationID)
    case 3 => DataID(anyID.getDataID)
    case 4 => HashID(anyID.getHashID)
    case 5 => IdentityID(anyID.getIdentityID)
    case 6 => MaintainerID(anyID.getMaintainerID)
    case 7 => OrderID(anyID.getOrderID)
    case 8 => PropertyID(anyID.getPropertyID)
    case 9 => SplitID(anyID.getSplitID)
    case 10 => StringID(anyID.getStringID)
    case _ => throw new IllegalArgumentException("UNKNOWN_ID_IMPL_CASE_NUMBER: " + anyID.getImplCase.getNumber.toString)
  }

  def apply(protoBytes: Array[Byte]): ID = ID(AnyID.parseFrom(protoBytes))
}
