package views.forms

object VerifyEmailAddress {

  import play.api.data.Form
  import play.api.data.Forms._

  val form = Form(
    mapping(
      "Username" -> nonEmptyText(minLength = 4, maxLength = 20),
      "OTP" -> nonEmptyText(minLength = 6, maxLength = 6),
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String, otp: String)

}
