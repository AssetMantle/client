package views.forms

import play.api.data.Form
import play.api.data.Forms._

case class Login(username: String, password: String)

object Login {
  val form = Form(
    mapping(
      "Username" -> nonEmptyText(minLength = 4, maxLength = 20),
      "Password" -> nonEmptyText(minLength = 8, maxLength = 50)
    )(Login.apply)(Login.unapply)
  )
}
