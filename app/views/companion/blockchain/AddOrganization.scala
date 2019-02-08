package views.companion.blockchain

object AddOrganization {

  import play.api.data.Form
  import play.api.data.Forms._

  val form = Form(
    mapping(
      "from" -> nonEmptyText(),
      "to" -> nonEmptyText(),
      "organizationID" -> nonEmptyText()
    )(Data.apply)(Data.unapply)
  )

  case class Data(from: String, to: String, organizationID: String)

}