package views.forms

object UpdateContact {

  import play.api.data.Form
  import play.api.data.Forms._

  val form = Form(
    mapping(
      "Username" -> nonEmptyText(minLength = 4, maxLength = 20),
      "EmailAddress" -> email,
      "MobileNumber" -> nonEmptyText(minLength = 10, maxLength = 10)
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String, emailAddress: String, mobileNumber: String)

}
