package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, seq}
import utilities.MicroNumber
import views.companion.common._

object OrderMake {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.FROM_ID.name -> constants.FormField.FROM_ID.field,
      constants.FormField.CLASSIFICATION_ID.name -> constants.FormField.CLASSIFICATION_ID.field,
      constants.FormField.LABEL.name -> constants.FormField.LABEL.field,
      constants.FormField.MAKER_OWNABLE_ID.name -> constants.FormField.MAKER_OWNABLE_ID.field,
      constants.FormField.TAKER_OWNABLE_ID.name -> constants.FormField.TAKER_OWNABLE_ID.field,
      constants.FormField.EXPIRES_IN.name -> constants.FormField.EXPIRES_IN.field,
      constants.FormField.MAKER_OWNABLE_SPLIT.name -> constants.FormField.MAKER_OWNABLE_SPLIT.field,
      constants.FormField.IMMUTABLE_META_PROPERTIES.name -> optional(seq(optional(Property.subFormMapping))),
      constants.FormField.ADD_IMMUTABLE_META_FIELD.name -> constants.FormField.ADD_IMMUTABLE_META_FIELD.field,
      constants.FormField.IMMUTABLE_PROPERTIES.name -> optional(seq(optional(Property.subFormMapping))),
      constants.FormField.ADD_IMMUTABLE_FIELD.name -> constants.FormField.ADD_IMMUTABLE_FIELD.field,
      constants.FormField.MUTABLE_META_PROPERTIES.name -> optional(seq(optional(Property.subFormMapping))),
      constants.FormField.ADD_MUTABLE_META_FIELD.name -> constants.FormField.ADD_MUTABLE_META_FIELD.field,
      constants.FormField.MUTABLE_PROPERTIES.name -> optional(seq(optional(Property.subFormMapping))),
      constants.FormField.ADD_MUTABLE_FIELD.name -> constants.FormField.ADD_MUTABLE_FIELD.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> optional(constants.FormField.PASSWORD.field)
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.orderMake)
  )

  case class Data(fromID: String, classificationID: String, label: String, makerOwnableID: String, takerOwnableID: String, expiresIn: Int, makerOwnableSplit: BigDecimal, immutableMetaProperties: Option[Seq[Option[Property.Data]]], addImmutableMetaField: Boolean, immutableProperties: Option[Seq[Option[Property.Data]]], addImmutableField: Boolean, mutableMetaProperties: Option[Seq[Option[Property.Data]]], addMutableMetaField: Boolean, mutableProperties: Option[Seq[Option[Property.Data]]], addMutableField: Boolean, gas: MicroNumber, password: Option[String])

}
