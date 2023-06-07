package schema.utilities

import com.assetmantle.schema.ids.base.{HashID => protoHashID}
import com.google.protobuf.ByteString
import schema.data.Data
import schema.id.OwnableID
import schema.id.base._
import schema.property.Property
import schema.qualified.{Immutables, Mutables}
import schema.types.Split


object ID {

  def generateHashID(bytesList: Array[Byte]*): HashID = if (bytesList.exists(_.length != 0)) {
    HashID(protoHashID.newBuilder().setIDBytes(ByteString.copyFrom(
      utilities.Secrets.sha256Hash(bytesList.filter(_.length != 0).sortWith((x, y) => byteArraysCompare(x, y) < 0).flatten.toArray)
    )).build())
  } else HashID(value = Array[Byte]())

  def generateHashIDFromList(bytesList: Seq[Array[Byte]]): HashID = if (bytesList.exists(_.length != 0)) {
    HashID(protoHashID.newBuilder().setIDBytes(ByteString.copyFrom(
      utilities.Secrets.sha256Hash(bytesList.filter(_.length != 0).sortWith((x, y) => byteArraysCompare(x, y) < 0).flatten.toArray)
    )).build())
  } else HashID(value = Array[Byte]())

  def byteArraysCompare(a: Array[Byte], b: Array[Byte]): Int = {
    if (a sameElements b) return 0
    else if (a == null) return -1
    else if (b == null) return 1
    val length = Math.min(a.length, b.length)
    for (i <- 0 until length) {
      val first = if (a(i) < 0) a(i).toInt + 256 else a(i).toInt
      val second = if (b(i) < 0) b(i).toInt + 256 else b(i).toInt
      val result = first - second
      if (result != 0) {
        return result
      }
    }
    Integer.compare(a.length, b.length);
  }

  def getClassificationID(immutables: Immutables, mutables: Mutables): ClassificationID = {
    val immutablesHashID = generateHashIDFromList(immutables.propertyList.getProperties.map(_.getID.getBytes))
    val mutablesHashID = generateHashIDFromList(mutables.propertyList.getProperties.map(_.getID.getBytes))
    ClassificationID(generateHashID(immutablesHashID.getBytes, mutablesHashID.getBytes, immutables.generateHashID.getBytes))
  }

  def getAssetID(classificationID: ClassificationID, immutables: Immutables): AssetID = AssetID(generateHashID(classificationID.getBytes, immutables.generateHashID.getBytes))

  def getIdentityID(classificationID: ClassificationID, immutables: Immutables): IdentityID = IdentityID(generateHashID(classificationID.getBytes, immutables.generateHashID.getBytes))

  def getDataID(data: Data): DataID = DataID(typeID = data.getType, hashID = data.generateHashID)

  def getMaintainerID(classificationID: ClassificationID, immutables: Immutables): MaintainerID = MaintainerID(generateHashID(classificationID.getBytes, immutables.generateHashID.getBytes))

  def getOrderID(classificationID: ClassificationID, immutables: Immutables): OrderID = OrderID(generateHashID(classificationID.getBytes, immutables.generateHashID.getBytes))

  def getPropertyID(property: Property): PropertyID = PropertyID(keyID = property.getKey, typeID = property.getType)

  def getPropertyID(keyID: StringID, typeID: StringID): PropertyID = PropertyID(keyID = keyID, typeID = typeID)

  def getSplitID(split: Split): SplitID = SplitID(ownerID = split.ownerID, ownableID = split.ownableID)

  def getSplitID(ownerID: IdentityID, ownableID: OwnableID): SplitID = SplitID(ownerID = ownerID, ownableID = ownableID)


}
