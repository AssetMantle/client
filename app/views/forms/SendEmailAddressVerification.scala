package views.forms

object SendEmailAddressVerification {

  import play.api.data.Form
  import play.api.data.Forms._

  val form = Form(
    mapping(
      "Username" -> nonEmptyText(minLength = 4, maxLength = 20),
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String)

}