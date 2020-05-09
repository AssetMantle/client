package views.companion.master

import play.api.data.Form
import play.api.data.Forms._

import java.util.Date

object PaymentTerms {
  val form = Form(
    mapping(
      constants.FormField.ID.name -> constants.FormField.ID.field,
      constants.FormField.ADVANCE_PERCENTAGE.name -> constants.FormField.ADVANCE_PERCENTAGE.field,
      constants.FormField.CREDIT.name -> optional(mapping(
        constants.FormField.TENURE.name -> optional(constants.FormField.TENURE.field),
        constants.FormField.TENTATIVE_DATE.name -> optional(constants.FormField.TENTATIVE_DATE.field),
        constants.FormField.REFERENCE.name -> optional(constants.FormField.REFERENCE.field),
      )(CreditData.apply)(CreditData.unapply)),
    )(Data.apply)(Data.unapply).verifying(constants.FormConstraint.paymentTermsConstraint)
  )

  case class CreditData(tenure: Option[Int] = None, tentativeDate: Option[Date] = None, reference: Option[String])

  case class Data(id: String, advancePercentage: Double, credit: Option[CreditData])

}
