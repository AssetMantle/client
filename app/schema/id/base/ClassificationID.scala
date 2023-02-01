package schema.id.base

import com.ids.{AnyID, ClassificationID => protoClassificationID}
import schema.id.ID

case class ClassificationID(hashID: HashID) extends ID {

  def getBytes: Array[Byte] = this.hashID.getBytes

  def asString: String = commonUtilities.Secrets.base64URLEncoder(this.getBytes)

  def asProtoClassificationID: protoClassificationID = protoClassificationID.newBuilder().setHashID(this.hashID.asProtoHashID).build()

  def toAnyID: AnyID = AnyID.newBuilder().setClassificationID(this.asProtoClassificationID).build()

  def getProtoBytes: Array[Byte] = this.asProtoClassificationID.toByteString.toByteArray

}

object ClassificationID {
  def apply(anyID: protoClassificationID): ClassificationID = ClassificationID(HashID(anyID.getHashID))
}
