package schema.constants

import schema.constants.Properties._
import schema.data.base.StringData
import schema.id.base._
import schema.list._
import schema.property.base.MesaProperty
import schema.qualified.{Immutables, Mutables}

object ID {

  val Separator = "."

  val NumImmutables: Immutables = Immutables(PropertyList(Seq(NubProperty)))
  val NumMutables: Mutables = Mutables(PropertyList(Seq(AuthenticationProperty)))
  val NubClassificationID: ClassificationID = schema.utilities.ID.getClassificationID(NumImmutables, NumMutables)

  val MaintainerClassificationImmutables: Immutables = Immutables(PropertyList(Seq(IdentityIDProperty, MaintainedClassificationIDProperty)))
  val MaintainerClassificationMutables: Mutables = Mutables(PropertyList(Seq(MaintainedPropertiesProperty, PermissionsProperty)))
  val MaintainerClassificationID: ClassificationID = schema.utilities.ID.getClassificationID(MaintainerClassificationImmutables, MaintainerClassificationMutables)

  val OrderIdentityID: IdentityID = schema.utilities.ID.getIdentityID(NubClassificationID, Immutables(PropertyList(Seq(MesaProperty(id = NubProperty.id, dataID = StringData("orders").getDataID)))))


}
