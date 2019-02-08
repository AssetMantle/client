package views.companion.master

object Login {

  import play.api.data.Form
  import play.api.data.Forms._

  val form = Form(
    mapping(
      "Username" -> nonEmptyText(minLength = 4, maxLength = 20),
      "Password" -> nonEmptyText(minLength = 8, maxLength = 50)
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String, password: String)

}
