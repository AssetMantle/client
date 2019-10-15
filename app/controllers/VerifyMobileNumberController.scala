package controllers

import controllers.actions.WithLoginAction
import controllers.results.WithUsernameToken
import exceptions.BaseException
import javax.inject.{Inject, Singleton}
import models.master
import models.masterTransaction.SMSOTPs
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext

@Singleton
class VerifyMobileNumberController @Inject()(messagesControllerComponents: MessagesControllerComponents, masterAccounts: master.Accounts, smsOTPs: SMSOTPs, masterContacts: master.Contacts, withLoginAction: WithLoginAction, utilitiesNotification: utilities.Notification, withUsernameToken: WithUsernameToken)(implicit executionContext: ExecutionContext, configuration: Configuration) extends AbstractController(messagesControllerComponents) with I18nSupport {

  private implicit val module: String = constants.Module.CONTROLLERS_SMS

  private implicit val logger: Logger = Logger(this.getClass)

  def verifyMobileNumberForm: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      try {
        val otp = smsOTPs.Service.sendOTP(loginState.username)
        utilitiesNotification.send(accountID = loginState.username, notification = constants.Notification.VERIFY_PHONE, otp)
        withUsernameToken.Ok(views.html.component.master.verifyMobileNumber())
      }
      catch {
        case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
      }
  }

  def verifyMobileNumber: Action[AnyContent] = withLoginAction.authenticated { implicit loginState =>
    implicit request =>
      views.companion.master.VerifyMobileNumber.form.bindFromRequest().fold(
        formWithErrors => {
          BadRequest(views.html.component.master.verifyMobileNumber(formWithErrors))
        },
        verifyMobileNumberData => {
          try {
            smsOTPs.Service.verifyOTP(loginState.username, verifyMobileNumberData.otp)
            masterContacts.Service.verifyMobileNumber(loginState.username)
            val contact = masterContacts.Service.getContact(loginState.username).getOrElse(throw new BaseException(constants.Response.NO_SUCH_ELEMENT_EXCEPTION))
            if (contact.emailAddressVerified && contact.mobileNumberVerified) {
              masterAccounts.Service.updateStatusComplete(loginState.username)
            } else {
              masterAccounts.Service.updateStatusUnverifiedEmail(loginState.username)
            }
            withUsernameToken.Ok(views.html.index(successes = Seq(constants.Response.SUCCESS)))
          }
          catch {
            case baseException: BaseException => InternalServerError(views.html.index(failures = Seq(baseException.failure)))
          }
        }
      )
  }
}