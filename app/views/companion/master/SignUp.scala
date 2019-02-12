package views.companion.master

object SignUp {

  import play.api.data.Form
  import play.api.data.Forms._

  val form = Form(
    mapping(
      "username" -> nonEmptyText(minLength = 4, maxLength = 20),
      "password" -> nonEmptyText(minLength = 8, maxLength = 50)
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String, password: String)

}
