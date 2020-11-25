package views.companion.blockchain

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, seq}
import utilities.MicroNumber
import views.companion.common._

object MaintainerDeputize {

  val form: Form[Data] = Form(
    mapping(
      constants.FormField.FROM_ID.name -> constants.FormField.FROM_ID.field,
      constants.FormField.TO_ID.name -> constants.FormField.TO_ID.field,
      constants.FormField.CLASSIFICATION_ID.name -> constants.FormField.CLASSIFICATION_ID.field,
      constants.FormField.ADD_MAINTAINER.name -> constants.FormField.ADD_MAINTAINER.field,
      constants.FormField.MUTATE_MAINTAINER.name -> constants.FormField.MUTATE_MAINTAINER.field,
      constants.FormField.REMOVE_MAINTAINER.name -> constants.FormField.REMOVE_MAINTAINER.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
      constants.FormField.MAINTAINED_TRAITS.name -> optional(seq(optional(Property.subFormMapping))),
      constants.FormField.ADD_MAINTAINED_TRAITS.name -> constants.FormField.ADD_MAINTAINED_TRAITS.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(fromID: String, toID: String, classificationID: String, addMaintainer: Boolean, mutateMaintainer: Boolean, removeMaintainer: Boolean, gas: MicroNumber, password: String, maintainedTraits: Option[Seq[Option[Property.Data]]], addMaintainedTraits: Boolean)

}
