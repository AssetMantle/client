package schema.id

import com.assetmantle.schema.ids.base.AnyID
import org.slf4j.{Logger, LoggerFactory}
import schema.id.base._

abstract class ID {

  def getBytes: Array[Byte]

  def getType: StringID

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
    case 3 => CoinID(anyID.getCoinID)
    case 4 => DataID(anyID.getDataID)
    case 5 => HashID(anyID.getHashID)
    case 6 => IdentityID(anyID.getIdentityID)
    case 7 => MaintainerID(anyID.getMaintainerID)
    case 8 => OrderID(anyID.getOrderID)
    case 9 => OwnableID(anyID.getOwnableID)
    case 10 => PropertyID(anyID.getPropertyID)
    case 11 => SplitID(anyID.getSplitID)
    case 12 => StringID(anyID.getStringID)
    case _ => throw new IllegalArgumentException("UNKNOWN_ID_TYPE")
  }

  def apply(protoBytes: Array[Byte]): ID = ID(AnyID.parseFrom(protoBytes))
}
