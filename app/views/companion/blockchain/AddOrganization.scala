package views.companion.blockchain

object AddOrganization {

  import play.api.data.Form
  import play.api.data.Forms._

  val form = Form(
    mapping(
      constants.Form.FROM -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.TO -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Form.ORGANIZATION_ID -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.ZONE_ID -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Form.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, organizationID: String, zoneID: String, password: String)

}