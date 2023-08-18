package schema.document

import schema.constants.Properties.ModuleNameProperty
import schema.data.base.IDData
import schema.id.base.{ClassificationID, IdentityID, StringID}
import schema.list.PropertyList
import schema.qualified.{Immutables, Mutables}

object ModuleIdentity {
  val DocumentImmutables: Immutables = Immutables(PropertyList(Seq(ModuleNameProperty)))
  val DocumentMutables: Mutables = Mutables(Seq())
  val DocumentClassificationID: ClassificationID = schema.utilities.ID.getClassificationID(DocumentImmutables, DocumentMutables)

  def getModuleIdentityImmutables(moduleName: String): Immutables = Immutables(PropertyList(Seq(ModuleNameProperty.mutate(IDData(StringID(moduleName))))))

  def getModuleIdentityMutables: Mutables = DocumentMutables

  def getModuleIdentityDocument(moduleName: String): Document = Document(classificationID = DocumentClassificationID, immutables = getModuleIdentityImmutables(moduleName), mutables = DocumentMutables)

  def getModuleIdentityID(moduleName: String): IdentityID = schema.utilities.ID.getIdentityID(classificationID = DocumentClassificationID, immutables = getModuleIdentityImmutables(moduleName))

}
