package schema.document

import schema.constants.Properties.ModuleNameProperty
import schema.data.base.IDData
import schema.id.base.{ClassificationID, IdentityID, StringID}
import schema.list.PropertyList
import schema.qualified.{Immutables, Mutables}

object ModuleIdentity {
  private val ModuleIdentityImmutables: Immutables = Immutables(PropertyList(Seq(ModuleNameProperty)))

  val ModuleIdentityClassificationID: ClassificationID = schema.utilities.ID.getClassificationID(ModuleIdentityImmutables, Mutables(Seq()))

  def getModuleIdentityImmutables(moduleName: String): Immutables = Immutables(PropertyList(Seq(ModuleNameProperty.mutate(IDData(StringID(moduleName))))))

  def getModuleIdentityDocument(moduleName: String): Document = Document(classificationID = ModuleIdentityClassificationID, immutables = getModuleIdentityImmutables(moduleName), mutables = Mutables(Seq()))

  def getModuleIdentityID(moduleName: String): IdentityID = schema.utilities.ID.getIdentityID(classificationID = ModuleIdentityClassificationID, immutables = getModuleIdentityImmutables(moduleName))

}
