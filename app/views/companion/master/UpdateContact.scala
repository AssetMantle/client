package views.companion.master

object UpdateContact {

  import play.api.data.Form
  import play.api.data.Forms._

  val form = Form(
    mapping(
      "emailAddress" -> email,
      "mobileNumber" -> nonEmptyText(minLength = 10, maxLength = 10)
    )(Data.apply)(Data.unapply)
  )

  case class Data(emailAddress: String, mobileNumber: String)

}
