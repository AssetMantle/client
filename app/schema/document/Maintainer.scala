package schema.document

import schema.constants.Properties._
import schema.data.base.IDData
import schema.id.base.{ClassificationID, IdentityID}
import schema.list.{IDList, PropertyList}
import schema.qualified.{Immutables, Mutables}

object Maintainer {
  val DocumentImmutables: Immutables = Immutables(PropertyList(Seq(IdentityIDProperty, MaintainedClassificationIDProperty)))
  val DocumentMutables: Mutables = Mutables(PropertyList(Seq(MaintainedPropertiesProperty, PermissionsProperty)))
  val DocumentClassificationID: ClassificationID = schema.utilities.ID.getClassificationID(DocumentImmutables, DocumentMutables)

  def getMaintainerImmutables(identityID: IdentityID, maintainedClassificationID: ClassificationID): Immutables = Immutables(PropertyList(Seq(IdentityIDProperty.mutate(IDData(identityID)), MaintainedClassificationIDProperty.mutate(IDData(maintainedClassificationID)))))

  def getMaintainerMutables(maintainedPropertyIDList: IDList, permissions: IDList): Mutables = Mutables(PropertyList(Seq(MaintainedPropertiesProperty.mutate(maintainedPropertyIDList.toListData), PermissionsProperty.mutate(permissions.toListData))))

  def getMaintainerDocument(identityID: IdentityID, maintainedPropertyIDList: IDList, maintainedClassificationID: ClassificationID, permissions: IDList): Document = Document(
    classificationID = DocumentClassificationID,
    immutables = getMaintainerImmutables(identityID, maintainedClassificationID),
    mutables = getMaintainerMutables(maintainedPropertyIDList = maintainedPropertyIDList, permissions = permissions))

}
