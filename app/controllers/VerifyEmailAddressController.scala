package controllers

import javax.inject.Inject
import models.businesstxn.EmailOTPs
import models.master.Contacts
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, MessagesControllerComponents}
import views.forms.{SendEmailAddressVerification, _}

import scala.concurrent.ExecutionContext

class VerifyEmailAddressController @Inject()(messagesControllerComponents: MessagesControllerComponents, emailOTPs: EmailOTPs, contacts: Contacts)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def sendEmailAddressVerification = Action { implicit request =>
    SendEmailAddressVerification.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.index(SignUp.form, Login.form, UpdateContact.form, VerifyEmailAddress.form, VerifyMobileNumber.form, formWithErrors, SendMobileNumberVerification.form))
      },
      sendEmailAddressVerificationData => {
        if (emailOTPs.Service.sendOTP(sendEmailAddressVerificationData.username) == 1) Ok(views.html.verifyEmailAddress(VerifyEmailAddress.form)) else Ok("Problem")
      })
  }

  def verifyEmailAddress = Action { implicit request =>
    VerifyEmailAddress.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.index(SignUp.form, Login.form, UpdateContact.form, formWithErrors, VerifyMobileNumber.form, SendEmailAddressVerification.form, SendMobileNumberVerification.form))
      },
      verifyEmailAddressData => {
        if (emailOTPs.Service.verifyOTP(verifyEmailAddressData.username, verifyEmailAddressData.otp))
          if (contacts.Service.verifyEmailAddress(verifyEmailAddressData.username) == 1)
            Ok("Verified")
          else
            Ok("Err Verifying")
        else Ok("Err Sending")
      })
  }
}
