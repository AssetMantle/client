package schema.constants

import schema.constants.Properties._
import schema.data.base.StringData
import schema.id.base._
import schema.list._
import schema.property.base.MesaProperty
import schema.qualified.Immutables

object Document {

  val NubClassificationID: ClassificationID = schema.utilities.ID.getClassificationID(NubImmutables, NubMutables)
  val MaintainerClassificationID: ClassificationID = schema.utilities.ID.getClassificationID(MaintainerClassificationImmutables, MaintainerClassificationMutables)
  val OrderIdentityID: IdentityID = schema.utilities.ID.getIdentityID(NubClassificationID, Immutables(PropertyList(Seq(MesaProperty(id = NubProperty.id, dataID = StringData("orders").getDataID)))))
}
