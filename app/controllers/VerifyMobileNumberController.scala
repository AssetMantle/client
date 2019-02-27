package controllers

import constants.Security
import controllers.actions.WithLoginAction
import javax.inject.Inject
import models.masterTransaction.SMSOTPs
import models.master.Contacts
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.master.VerifyMobileNumber

import scala.concurrent.ExecutionContext

class VerifyMobileNumberController @Inject()(messagesControllerComponents: MessagesControllerComponents, smsOTPs: SMSOTPs, contacts: Contacts, withLoginAction: WithLoginAction)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def verifyMobileNumberForm: Action[AnyContent] = withLoginAction { implicit request =>
    if (smsOTPs.Service.sendOTP(request.session.get(Security.USERNAME).get) == 1)
      Ok(views.html.component.master.verifyMobileNumber(VerifyMobileNumber.form))
    else
      Ok(views.html.index(failure = "Send Otp Failed!"))
  }

  def verifyMobileNumber: Action[AnyContent] = withLoginAction { implicit request =>
    VerifyMobileNumber.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.component.master.verifyMobileNumber(formWithErrors))
      },
      verifyMobileNumberData => {
        if (smsOTPs.Service.verifyOTP(request.session.get(Security.USERNAME).get, verifyMobileNumberData.otp))
          if (contacts.Service.verifyMobileNumber(request.session.get(Security.USERNAME).get) == 1)
            Ok(views.html.index(success = "MobileUpdated"))
        Ok(views.html.index(failure = "Failed"))
      })
  }
}