package schema.id

import com.ids.AnyID
import org.slf4j.{Logger, LoggerFactory}
import schema.id.base._

abstract class ID {

  def getBytes: Array[Byte]

  def asString: String

  def toAnyID: AnyID

  def getProtoBytes: Array[Byte]

}

object ID {

  private implicit val module: String = constants.Module.SCHEMA_ID

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(anyID: AnyID): ID = anyID.getImplCase.getNumber match {
    case 1 => AssetID(anyID.getAssetID)
    case 2 => ClassificationID(anyID.getClassificationID)
    case 3 => DataID(anyID.getDataID)
    case 4 => HashID(anyID.getHashID)
    case 5 => IdentityID(anyID.getIdentityID)
    case 6 => MaintainerID(anyID.getMaintainerID)
    case 7 => OrderID(anyID.getOrderID)
    case 8 => OwnableID(anyID.getAnyOwnableID)
    case 9 => PropertyID(anyID.getPropertyID)
    case 10 => SplitID(anyID.getSplitID)
    case 11 => StringID(anyID.getStringID)
    case _ => throw new IllegalArgumentException("UNKNOWN_ID_TYPE")
  }

  def apply(protoBytes: Array[Byte]): ID = ID(AnyID.parseFrom(protoBytes))
}
