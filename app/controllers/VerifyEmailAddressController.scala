package controllers

import controllers.actions.WithLoginAction
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master.Contacts
import models.masterTransaction.EmailOTPs
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.Email
import views.companion.master.VerifyEmailAddress

import scala.concurrent.ExecutionContext

@Singleton
class VerifyEmailAddressController @Inject()(messagesControllerComponents: MessagesControllerComponents, emailOTPs: EmailOTPs, contacts: Contacts, withLoginAction: WithLoginAction, email: Email)(implicit exec: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_VERIFY_EMAIL

  private implicit val logger: Logger = Logger(this.getClass)

  def verifyEmailAddressForm: Action[AnyContent] = withLoginAction.authenticated { username =>
    implicit request =>
      try {
        val otp = emailOTPs.Service.sendOTP(username)
        email.sendEmail(username, constants.Email.OTP, Seq(otp))
        Ok(views.html.component.master.verifyEmailAddress(VerifyEmailAddress.form))
      }
      catch {
        case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
      }
  }

  def verifyEmailAddress: Action[AnyContent] = withLoginAction.authenticated { username =>
    implicit request =>
      VerifyEmailAddress.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.verifyEmailAddress(formWithErrors))
        },
        verifyEmailAddressData => {
          try {
            if (!emailOTPs.Service.verifyOTP(username, verifyEmailAddressData.otp)) throw new BaseException(constants.Error.INVALID_OTP)
            if (contacts.Service.verifyEmailAddress(username) != 1) throw new BaseException(constants.Error.EMAIL_NOT_FOUND)
            Ok(views.html.index(success = "Email Updated"))
          }
          catch {
            case baseException: BaseException => Ok(views.html.index(failure = Messages(baseException.message)))
          }
        })
  }
}
