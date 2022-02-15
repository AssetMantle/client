package views.companion.common

import models.common.DataValue
import models.common.Serializable.{MetaFact, NewFact, Fact => SerializableFact, Data => SerializableData}
import play.api.data.Forms.mapping
import play.api.data.Mapping

object Fact {

  val subFormMapping: Mapping[Data] = mapping(
    constants.FormField.DATA_TYPE.name -> constants.FormField.DATA_TYPE.field,
    constants.FormField.DATA_VALUE.name -> constants.FormField.DATA_VALUE.field
  )(Data.apply)(Data.unapply)

  case class Data(dataType: String, dataValue: String) {
    def toRequestString: String = s"${DataValue.getFactTypeFromDataType(dataType)}${constants.Blockchain.DataTypeAndValueSeparator}${dataValue}"

    def toMetaFact: MetaFact = MetaFact(SerializableData(dataType = dataType, dataValue = Option(dataValue)))

    def toFact: SerializableFact = NewFact(factType = DataValue.getFactTypeFromDataType(dataType), dataValue = DataValue.getDataValue(dataType = dataType, dataValue = Option(dataValue)))
  }

}
