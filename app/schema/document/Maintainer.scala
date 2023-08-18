package schema.document

import schema.constants.Properties._
import schema.data.base.IDData
import schema.id.base.{ClassificationID, IdentityID}
import schema.list.{IDList, PropertyList}
import schema.qualified.{Immutables, Mutables}

object Maintainer {
  private val MaintainerClassificationImmutables: Immutables = Immutables(PropertyList(Seq(IdentityIDProperty, MaintainedClassificationIDProperty)))
  private val MaintainerClassificationMutables: Mutables = Mutables(PropertyList(Seq(MaintainedPropertiesProperty, PermissionsProperty)))

  val MaintainerClassificationID: ClassificationID = schema.utilities.ID.getClassificationID(MaintainerClassificationImmutables, MaintainerClassificationMutables)

  def getMaintainerImmutables(identityID: IdentityID, maintainedClassificationID: ClassificationID): Immutables = Immutables(PropertyList(Seq(IdentityIDProperty.mutate(IDData(identityID)), MaintainedClassificationIDProperty.mutate(IDData(maintainedClassificationID)))))

  def getMaintainerMutables(maintainedPropertyIDList: IDList, permissions: IDList): Mutables = Mutables(PropertyList(Seq(MaintainedPropertiesProperty.mutate(maintainedPropertyIDList.toListData), PermissionsProperty.mutate(permissions.toListData))))

  def getMaintainerDocument(identityID: IdentityID, maintainedPropertyIDList: IDList, maintainedClassificationID: ClassificationID, permissions: IDList): Document = Document(
    classificationID = MaintainerClassificationID,
    immutables = getMaintainerImmutables(identityID, maintainedClassificationID),
    mutables = getMaintainerMutables(maintainedPropertyIDList = maintainedPropertyIDList, permissions = permissions))

}
