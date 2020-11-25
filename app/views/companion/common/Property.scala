package views.companion.common

import models.common.DataValue
import models.common.Serializable.{MetaFact, MetaProperty, NewFact, Property => SerializableProperty}
import play.api.data.Forms.{mapping, optional}
import play.api.data.Mapping

object Property {

  val subFormMapping: Mapping[Data] = mapping(
    constants.FormField.DATA_TYPE.name -> constants.FormField.DATA_TYPE.field,
    constants.FormField.DATA_NAME.name -> constants.FormField.DATA_NAME.field,
    constants.FormField.DATA_VALUE.name -> optional(constants.FormField.DATA_VALUE.field)
  )(Data.apply)(Data.unapply)

  case class Data(dataType: String, dataName: String, dataValue: Option[String]) {
    def toRequestString: String = s"${dataName}${constants.Blockchain.DataNameAndTypeSeparator}${DataValue.getFactTypeFromDataType(dataType)}${constants.Blockchain.DataTypeAndValueSeparator}${dataValue.getOrElse("")}"

    def toMetaProperty: MetaProperty = MetaProperty(id = dataName, metaFact = MetaFact(DataValue.getData(dataType = dataType, dataValue = dataValue)))

    def toProperty: SerializableProperty = SerializableProperty(id = dataName, fact = NewFact(factType = DataValue.getFactTypeFromDataType(dataType), dataValue = DataValue.getDataValue(dataType = dataType, dataValue = dataValue)))
  }

}
