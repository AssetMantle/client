package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object SignUp {


  val form = Form(
    mapping(
      constants.Forms.USERNAME -> nonEmptyText(minLength = 4, maxLength = 20),
      constants.Forms.PASSWORD -> nonEmptyText(minLength = 8, maxLength = 50)
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String, password: String)

}
