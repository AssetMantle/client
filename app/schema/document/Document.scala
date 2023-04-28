package schema.document

import com.assetmantle.schema.documents.base.{Document => protoDocument}
import schema.id.base.{ClassificationID, HashID, PropertyID}
import schema.property.Property
import schema.qualified._

case class Document(classificationID: ClassificationID, immutables: Immutables, mutables: Mutables) {

  def generateHashID: HashID = schema.utilities.ID.generateHashID(this.classificationID.getBytes, this.immutables.generateHashID.getBytes)

  def getProperty(id: PropertyID): Option[Property] = {
    val immutable = this.immutables.getProperty(id)
    if (immutable.isEmpty) {
      val mutable = this.mutables.getProperty(id)
      if (mutable.nonEmpty) mutable else None
    } else immutable
  }

  def asProtoDocument: protoDocument = protoDocument.newBuilder()
    .setClassificationID(this.classificationID.asProtoClassificationID)
    .setImmutables(this.immutables.asProtoImmutables)
    .setMutables(this.mutables.asProtoMutables)
    .build()

  def getProtoBytes: Array[Byte] = this.asProtoDocument.toByteString.toByteArray

  def mutate(properties: Seq[Property]): Document = this.copy(mutables = this.mutables.mutate(properties))

  def getTotalBondWeight: Int = this.immutables.getTotalBondWeight + this.mutables.getTotalBondWeight

  def getTotalBondAmount(bondRate: Int): Int = this.getTotalBondWeight * bondRate
}

object Document {

  def apply(document: protoDocument): Document = Document(classificationID = ClassificationID(document.getClassificationID), immutables = Immutables(document.getImmutables), mutables = Mutables(document.getMutables))

  def apply(protoBytes: Array[Byte]): Document = Document(protoDocument.parseFrom(protoBytes))

}
