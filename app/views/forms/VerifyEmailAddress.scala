package views.forms

object VerifyEmailAddress {

  import play.api.data.Form
  import play.api.data.Forms._

  val form = Form(
    mapping(
      "OTP" -> nonEmptyText(minLength = 6, maxLength = 6),
    )(Data.apply)(Data.unapply)
  )

  case class Data(otp: String)

}
