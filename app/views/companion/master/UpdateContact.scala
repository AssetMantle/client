package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object UpdateContact {

  val form = Form(
    mapping(
      constants.Form.EMAIL -> email,
      constants.Form.MOBILE_NUMBER -> constants.FormField.PHONE.field,
      constants.Form.COUNTRY_CODE -> constants.FormField.COUNTRY_CODE.field
    )(Data.apply)(Data.unapply)
  )

  case class Data(emailAddress: String, mobileNumber: String, countryCode: String)

}
