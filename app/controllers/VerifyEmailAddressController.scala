package controllers

import constants.Security
import controllers.actions.WithLoginAction
import javax.inject.Inject
import models.businesstxn.EmailOTPs
import models.master.Contacts
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import views.companion.master.VerifyEmailAddress

import scala.concurrent.ExecutionContext

class VerifyEmailAddressController @Inject()(messagesControllerComponents: MessagesControllerComponents, emailOTPs: EmailOTPs, contacts: Contacts, withLoginAction: WithLoginAction)(implicit exec: ExecutionContext) extends AbstractController(messagesControllerComponents) with I18nSupport {

  def verifyEmailAddressForm: Action[AnyContent] = withLoginAction { implicit request =>
    if (emailOTPs.Service.sendOTP(request.session.get(Security.USERNAME).get) == 1)
      Ok(views.html.verifyEmailAddress(VerifyEmailAddress.form))
    else
      Ok(views.html.index(failure = "Send Otp Failed!"))
  }

  def verifyEmailAddress: Action[AnyContent] = withLoginAction { implicit request =>
    VerifyEmailAddress.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.verifyEmailAddress(formWithErrors))
      },
      verifyEmailAddressData => {
        if (emailOTPs.Service.verifyOTP(request.session.get(Security.USERNAME).get, verifyEmailAddressData.otp))
          if (contacts.Service.verifyEmailAddress(request.session.get(Security.USERNAME).get) == 1)
            Ok(views.html.index(success = "EmailUpdated"))
        Ok(views.html.index(failure = "Failed"))
      })
  }
}
