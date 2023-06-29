package schema.id.base

import com.assetmantle.schema.ids.base.{AnyID, AnyOwnableID, CoinID => protoCoinID}
import schema.id._

case class CoinID(value: StringID) extends OwnableID {

  def getBytes: Array[Byte] = this.value.getBytes

  def getType: StringID = constants.CoinIDType

  def asString: String = this.value.asString

  def asProtoCoinID: protoCoinID = protoCoinID.newBuilder().setStringID(this.value.asProtoStringID).build()

  def toAnyOwnableID: AnyOwnableID = AnyOwnableID.newBuilder().setCoinID(this.asProtoCoinID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setCoinID(this.asProtoCoinID).build()

  def getProtoBytes: Array[Byte] = this.asProtoCoinID.toByteString.toByteArray

  def isCoinId: Boolean = false

}

object CoinID {
  def apply(id: protoCoinID): CoinID = CoinID(StringID(id.getStringID))

  def apply(id: AnyOwnableID): CoinID = CoinID(id.getCoinID)

  def apply(value: String): CoinID = CoinID(StringID(value))
}
