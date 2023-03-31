package schema.id

import com.ids.AnyOwnableID
import org.slf4j.{Logger, LoggerFactory}
import schema.id.base.{AssetID, CoinID}

abstract class OwnableID extends ID {
  def toAnyOwnableID: AnyOwnableID

  def isCoinId: Boolean

}

object OwnableID {

  private implicit val module: String = constants.Module.SCHEMA_OWNABLE_ID

  private implicit val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(anyOwnableID: AnyOwnableID): OwnableID = anyOwnableID.getImplCase.getNumber match {
    case 1 => AssetID(anyOwnableID.getAssetID)
    case 2 => CoinID(anyOwnableID.getCoinID)
    case _ => throw new IllegalArgumentException("INVALID_OWNABLE_ID_TYPE")
  }

  def apply(protoBytes: Array[Byte]): OwnableID = OwnableID(AnyOwnableID.parseFrom(protoBytes))
}