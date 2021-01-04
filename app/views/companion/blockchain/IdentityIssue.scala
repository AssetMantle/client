package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, seq}
import utilities.MicroNumber
import views.companion.common._

object IdentityIssue {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.FROM_ID.name -> constants.FormField.FROM_ID.field,
      constants.FormField.CLASSIFICATION_ID.name -> constants.FormField.CLASSIFICATION_ID.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
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
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.identityIssue)
  )

  case class Data(fromID: String, classificationID: String, to: String, immutableMetaProperties: Option[Seq[Option[Property.Data]]], addImmutableMetaField: Boolean, immutableProperties: Option[Seq[Option[Property.Data]]], addImmutableField: Boolean, mutableMetaProperties: Option[Seq[Option[Property.Data]]], addMutableMetaField: Boolean, mutableProperties: Option[Seq[Option[Property.Data]]], addMutableField: Boolean, gas: MicroNumber, password: Option[String])

}
