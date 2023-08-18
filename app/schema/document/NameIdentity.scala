package schema.document

import schema.constants.Properties.{AuthenticationProperty, NameProperty}
import schema.data.base.{IDData, ListData}
import schema.id.base.{ClassificationID, IdentityID, StringID}
import schema.list.PropertyList
import schema.qualified.{Immutables, Mutables}

object NameIdentity {
  val DocumentImmutables: Immutables = Immutables(PropertyList(Seq(NameProperty)))
  val DocumentMutables: Mutables = Mutables(PropertyList(Seq(AuthenticationProperty)))
  val DocumentClassificationID: ClassificationID = schema.utilities.ID.getClassificationID(DocumentImmutables, DocumentMutables)

  def getNameIdentityImmutables(name: String): Immutables = Immutables(PropertyList(Seq(NameProperty.mutate(IDData(StringID(name))))))

  def getNameIdentityMutables(authentication: ListData): Mutables = Mutables(PropertyList(Seq(AuthenticationProperty.mutate(authentication))))

  def getNameIdentityDocument(name: String, authentication: ListData): Document = Document(classificationID = DocumentClassificationID, immutables = getNameIdentityImmutables(name), mutables = getNameIdentityMutables(authentication))

  def getNameIdentityID(name: String): IdentityID = schema.utilities.ID.getIdentityID(classificationID = DocumentClassificationID, immutables = getNameIdentityImmutables(name))

}
