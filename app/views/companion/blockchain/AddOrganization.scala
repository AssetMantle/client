package views.companion.blockchain

object AddOrganization {

  import play.api.data.Form
  import play.api.data.Forms._

  val form = Form(
    mapping(
      constants.FormField.FROM.name -> constants.FormField.FROM.field,
      constants.FormField.TO.name -> constants.FormField.TO.field,
      constants.FormField.ORGANIZATION_ID.name -> constants.FormField.ORGANIZATION_ID.field,
      constants.FormField.ZONE_ID.name -> constants.FormField.ZONE_ID.field,
      constants.FormField.NON_EMPTY_PASSWORD.name -> constants.FormField.NON_EMPTY_PASSWORD.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, organizationID: String, zoneID: String, password: String)

}