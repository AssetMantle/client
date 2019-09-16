package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import models.masterTransaction.EmailOTPs
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}
import utilities.Email
import views.companion.master.VerifyEmailAddress

import scala.concurrent.ExecutionContext

@Singleton
class VerifyEmailAddressController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, emailOTPs: EmailOTPs, masterContacts: master.Contacts, withLoginAction: WithLoginAction, email: Email, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_EMAIL

  private implicit val logger: Logger = Logger(this.getClass)

  def verifyEmailAddressForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val otp = emailOTPs.Service.sendOTP(loginState.username)
        email.sendEmail(loginState.username, constants.Email.OTP, Seq(otp))
        withUsernameToken.Ok(views.html.component.master.verifyEmailAddress(VerifyEmailAddress.form))
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def verifyEmailAddress: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      VerifyEmailAddress.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.verifyEmailAddress(formWithErrors))
        },
        verifyEmailAddressData => {
          try {
            emailOTPs.Service.verifyOTP(loginState.username, verifyEmailAddressData.otp)
            masterContacts.Service.verifyEmailAddress(loginState.username)
            val contact = masterContacts.Service.getContact(loginState.username).getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION))
            if (contact.emailAddressVerified && contact.mobileNumberVerified) {
              masterAccounts.Service.updateStatusComplete(loginState.username)
            } else {
              masterAccounts.Service.updateStatusUnverifiedMobile(loginState.username)
            }
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.EMAIL_ADDRESS_VERIFIED)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}
