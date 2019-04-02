package views.companion.blockchain

object AddOrganization {

  import play.api.data.Form
  import play.api.data.Forms._

  val form = Form(
    mapping(
      constants.Forms.FROM -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.TO -> nonEmptyText(minLength = 1, maxLength = 45),
      constants.Forms.ORGANIZATION_ID -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.ZONE_ID -> nonEmptyText(minLength = 1, maxLength = 20),
      constants.Forms.PASSWORD -> nonEmptyText(minLength = 1, maxLength = 20)
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, organizationID: String, zoneID: String, password: String)

}