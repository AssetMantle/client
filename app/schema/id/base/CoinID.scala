package schema.id.base

import com.ids.{AnyID, AnyOwnableID, CoinID => protoCoinID}
import schema.id.{ID, OwnableID}

case class CoinID(value: StringID) extends OwnableID {

  def getBytes: Array[Byte] = this.value.getBytes

  def asString: String = this.value.asString

  def asProtoCoinID: protoCoinID = protoCoinID.newBuilder().setStringID(this.value.asProtoStringID).build()

  def toAnyOwnableID: AnyOwnableID = AnyOwnableID.newBuilder().setCoinID(this.asProtoCoinID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setOwnableID(this.toAnyOwnableID).build()

  def getProtoBytes: Array[Byte] = this.asProtoCoinID.toByteString.toByteArray

}

object CoinID {
  def apply(id: protoCoinID): CoinID = CoinID(StringID(id.getStringID))

  def apply(id: AnyOwnableID): CoinID = CoinID(id.getCoinID)
}
