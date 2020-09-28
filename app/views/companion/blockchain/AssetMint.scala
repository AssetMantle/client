package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, seq}
import utilities.MicroNumber
import views.companion.common._

object AssetMint {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.FROM_ID.name -> constants.FormField.FROM_ID.field,
      constants.FormField.TO_ID.name -> constants.FormField.TO_ID.field,
      constants.FormField.CLASSIFICATION_ID.name -> constants.FormField.CLASSIFICATION_ID.field,
      constants.FormField.IMMUTABLE_META_PROPERTIES.name -> optional(seq(optional(Property.subFormMapping))),
      constants.FormField.ADD_IMMUTABLE_META_FIELD.name -> constants.FormField.ADD_IMMUTABLE_META_FIELD.field,
      constants.FormField.IMMUTABLE_PROPERTIES.name -> optional(seq(optional(Property.subFormMapping))),
      constants.FormField.ADD_IMMUTABLE_FIELD.name -> constants.FormField.ADD_IMMUTABLE_FIELD.field,
      constants.FormField.MUTABLE_META_PROPERTIES.name -> optional(seq(optional(Property.subFormMapping))),
      constants.FormField.ADD_MUTABLE_META_FIELD.name -> constants.FormField.ADD_MUTABLE_META_FIELD.field,
      constants.FormField.MUTABLE_PROPERTIES.name -> optional(seq(optional(Property.subFormMapping))),
      constants.FormField.ADD_MUTABLE_FIELD.name -> constants.FormField.ADD_MUTABLE_FIELD.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, fromID: String, toID: String, classificationID: String, immutableMetaProperties: Option[Seq[Option[Property.Data]]], addImmutableMetaField: Boolean, immutableProperties: Option[Seq[Option[Property.Data]]], addImmutableField: Boolean, mutableMetaProperties: Option[Seq[Option[Property.Data]]], addMutableMetaField: Boolean, mutableProperties: Option[Seq[Option[Property.Data]]], addMutableField: Boolean, gas: MicroNumber)

}
