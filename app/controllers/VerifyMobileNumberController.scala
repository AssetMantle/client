package controllers

import javax.inject.Inject
import models.businesstxn.SMSOTPs
import models.master.Contacts
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, MessagesControllerComponents}
import views.forms.{SendMobileNumberVerification, _}

import scala.concurrent.ExecutionContext

class VerifyMobileNumberController @Inject()(messagesControllerComponents: MessagesControllerComponents, smsOTPs: SMSOTPs, contacts: Contacts)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def sendMobileNumberVerification = Action { implicit request =>
    SendMobileNumberVerification.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.index(SignUp.form, Login.form, UpdateContact.form, VerifyEmailAddress.form, VerifyMobileNumber.form, SendEmailAddressVerification.form, formWithErrors))
      },
      sendMobileNumberVerificationData => {
        if (smsOTPs.Service.sendOTP(sendMobileNumberVerificationData.username) == 1) Ok(views.html.verifyMobileNumber(VerifyMobileNumber.form)) else Ok("Problem")
      })
  }

  def verifyMobileNumber = Action { implicit request =>
    VerifyMobileNumber.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.index(SignUp.form, Login.form, UpdateContact.form, VerifyEmailAddress.form, formWithErrors, SendEmailAddressVerification.form, SendMobileNumberVerification.form))
      },
      verifyMobileNumberData => {
        if (smsOTPs.Service.verifyOTP(verifyMobileNumberData.username, verifyMobileNumberData.otp))
          if (contacts.Service.verifyMobileNumber(verifyMobileNumberData.username) == 1)
            Ok("Verified")
          else
            Ok("Err Verifying")
        else Ok("Err Sending")
      })
  }
}