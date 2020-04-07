package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

import java.util.Date

object PaymentTerms {
  val form = Form(
    mapping(
      constants.FormField.ID.name -> constants.FormField.ID.field,
      constants.FormField.ADVANCE_PAYMENT.name -> constants.FormField.ADVANCE_PAYMENT.field,
      constants.FormField.ADVANCE_PERCENTAGE.name -> optional(constants.FormField.ADVANCE_PERCENTAGE.field),
      constants.FormField.CREDIT.name -> constants.FormField.CREDIT.field,
      constants.FormField.TENURE.name -> optional(constants.FormField.TENURE.field),
      constants.FormField.TENTATIVE_DATE.name -> optional(constants.FormField.TENTATIVE_DATE.field),
      constants.FormField.REFRENCE.name -> optional(constants.FormField.REFRENCE.field),
    )(Data.apply)(Data.unapply)
  )

  case class Data(id: String, advancePayment: Boolean, advancePercentage: Option[Double], credit: Boolean, tenure: Option[Int], tentativeDate: Option[Date], refrence: Option[String])

}
