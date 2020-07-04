package views.companion.master

import play.api.data.Form
import play.api.data.Forms._
import utilities.MicroLong

object AcceptOrganizationRequest {

  val form = Form(
    mapping(
      constants.FormField.ORGANIZATION_ID.name -> constants.FormField.ORGANIZATION_ID.field,
      constants.FormField.GAS.name -> constants.FormField.GAS.field,
      constants.FormField.PASSWORD.name -> constants.FormField.PASSWORD.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(organizationID: String, gas: MicroLong, password: String)

}
