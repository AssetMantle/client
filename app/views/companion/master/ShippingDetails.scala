package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

object ShippingDetails {
  val form = Form(
    mapping(
      constants.FormField.REQUEST_ID.name -> constants.FormField.REQUEST_ID.field,
      constants.FormField.SHIPPING_PERIOD.name -> constants.FormField.SHIPPING_PERIOD.field,
      constants.FormField.PORT_OF_LOADING.name -> constants.FormField.PORT_OF_LOADING.field,
      constants.FormField.PORT_OF_DISCHARGE.name -> constants.FormField.PORT_OF_DISCHARGE.field,
    )(Data.apply)(Data.unapply)
  )

  case class Data(requestID: String, shippingPeriod: Int, portOfLoading: String, portOfDischarge: String)

}
