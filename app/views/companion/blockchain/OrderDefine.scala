package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, seq}
import utilities.MicroNumber
import views.companion.common._

object OrderDefine {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.FROM_ID.name -> constants.FormField.FROM_ID.field,
      constants.FormField.IMMUTABLE_META_TRAITS.name -> optional(seq(optional(Property.subFormMapping))),
      constants.FormField.ADD_IMMUTABLE_META_FIELD.name -> constants.FormField.ADD_IMMUTABLE_META_FIELD.field,
      constants.FormField.IMMUTABLE_TRAITS.name -> optional(seq(optional(Property.subFormMapping))),
      constants.FormField.ADD_IMMUTABLE_FIELD.name -> constants.FormField.ADD_IMMUTABLE_FIELD.field,
      constants.FormField.MUTABLE_META_TRAITS.name -> optional(seq(optional(Property.subFormMapping))),
      constants.FormField.ADD_MUTABLE_META_FIELD.name -> constants.FormField.ADD_MUTABLE_META_FIELD.field,
      constants.FormField.MUTABLE_TRAITS.name -> optional(seq(optional(Property.subFormMapping))),
      constants.FormField.ADD_MUTABLE_FIELD.name -> constants.FormField.ADD_MUTABLE_FIELD.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> optional(constants.FormField.PASSWORD.field)
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.orderDefine)
  )

  case class Data(fromID: String, immutableMetaTraits: Option[Seq[Option[Property.Data]]], addImmutableMetaField: Boolean, immutableTraits: Option[Seq[Option[Property.Data]]], addImmutableField: Boolean, mutableMetaTraits: Option[Seq[Option[Property.Data]]], addMutableMetaField: Boolean, mutableTraits: Option[Seq[Option[Property.Data]]], addMutableField: Boolean, gas: MicroNumber, password: Option[String])

}
